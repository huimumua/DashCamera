package com.askey.dvr.cdr7010.dashcam.core;

import android.os.Handler;
import com.askey.dvr.cdr7010.dashcam.util.Logg;

import java.util.ArrayList;
import java.util.List;

public class StateMachine {
    private static final String TAG = "StateMachine";

    public static final int EVENT_OPEN = 1;
    public static final int EVENT_CLOSE = 2;
    public static final int EVENT_ERROR = 3;
    public static final int EVENT_OPEN_SUCCESS = 4;
    public static final int EVENT_CLOSE_SUCCESS = 5;
    public static final int EVENT_AUDIO_MUTE = 6;
    public static final int EVENT_AUDIO_DEMUTE = 7;

    private State currState;
    private State nextState;
    private Event NoneEvent = new Event(0, "");
    private Event pendingEvent = NoneEvent;
    private Handler handler = new Handler();

    private final DashCamControl mDashCamControl;

    private List<Transition> transitions = new ArrayList<>();

    public static class Event {
        public Event(int what, String reason) {
            this.what = what;
            this.reason = reason;
        }

        boolean equals(Event other) {
            return this.what == other.what;
        }

        int what;
        String reason;
    }

    private interface IState {
        void enter();
        void exit();
        void processEvent(Event event);
    }

    public class State implements IState {
        String name;

        protected State(String name) {
            this.name = name;
        }

        @Override
        public void enter() {
            Logg.d(TAG, name + " enter");
        }

        @Override
        public void exit() {
            Logg.d(TAG, name + " exit");
        }

        @Override
        public void processEvent(Event event) {
        }
    }

    public static class Transition {
        public static Transition create(State currState, int event, State destState) {
            return new Transition(currState, event, destState);
        }

        Transition(State currState, int event, State destState) {
            this.currState = currState;
            this.event = event;
            this.destState = destState;
        }

        State currState;
        State destState;
        int event;
    }

    public StateMachine(DashCamControl dashCam) {
        mDashCamControl = dashCam;
        State prepareOpenState = new State("prepareOpenState") {
            @Override
            public void enter() {
                super.enter();
                try {
                    mDashCamControl.onStartVideoRecord();
                } catch (Exception e) {
                    Logg.e(TAG, "startVideoRecord() fail with exception: " + e.getMessage());
                    dispatchEvent(new Event(EVENT_ERROR, e.getMessage()));
                }
            }

            @Override
            public void processEvent(Event event) {
                if (event.what == EVENT_CLOSE || event.what == EVENT_OPEN) {
                    pendingEvent = event;
                }
            }
        };

        State prepareCloseState = new State("prepareCloseState") {
            @Override
            public void enter() {
                super.enter();
                mDashCamControl.onStopVideoRecord();
            }

            @Override
            public void processEvent(Event event) {
                if (event.what == EVENT_CLOSE || event.what == EVENT_OPEN) {
                    pendingEvent = event;
                }
            }
        };

        State openState = new State("openState") {
            @Override
            public void enter() {
                super.enter();
                if (pendingEvent.what != 0) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            dispatchEvent(pendingEvent);
                            pendingEvent = NoneEvent;
                        }
                    });
                }
            }

            @Override
            public void processEvent(Event event) {
                if (event.what == EVENT_AUDIO_MUTE) {
                    mDashCamControl.onMuteAudio();
                } else if (event.what == EVENT_AUDIO_DEMUTE) {
                    mDashCamControl.onDemuteAudio();
                }
            }
        };

        State closeState = new State("closeState") {
            @Override
            public void enter() {
                super.enter();
                if (pendingEvent.what != 0) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            dispatchEvent(pendingEvent);
                            pendingEvent = NoneEvent;
                        }
                    });
                }
            }
        };

        addTransition(Transition.create(closeState, EVENT_OPEN, prepareOpenState));
        addTransition(Transition.create(prepareOpenState, EVENT_OPEN_SUCCESS, openState));
        addTransition(Transition.create(prepareOpenState, EVENT_ERROR, closeState));
        addTransition(Transition.create(openState, EVENT_CLOSE, prepareCloseState));
        addTransition(Transition.create(prepareCloseState, EVENT_CLOSE_SUCCESS, closeState));
        addTransition(Transition.create(prepareCloseState, EVENT_ERROR, openState));

        initialState(closeState);
    }

    public void initialState(State state) {
        currState = state;
        nextState = currState;
    }

    public void addTransition(Transition transition) {
        if (!transitions.contains(transition)) {
            transitions.add(transition);
        }
    }

    public synchronized void dispatchEvent(Event event) {
        boolean found = false;
        for (Transition trans : transitions) {
            if ((trans.currState == currState) && (trans.event == event.what)) {
                nextState = trans.destState;
                found = true;
            }
        }

        currState.processEvent(event);

        if (found && (currState != nextState)) {
            currState.exit();
            currState = nextState;
            nextState.enter();
        }
    }
}
