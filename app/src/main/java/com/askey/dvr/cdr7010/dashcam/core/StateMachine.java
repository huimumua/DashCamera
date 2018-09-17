package com.askey.dvr.cdr7010.dashcam.core;

import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.askey.dvr.cdr7010.dashcam.application.DashCamApplication;
import com.askey.dvr.cdr7010.dashcam.util.Logg;
import com.askey.platform.AskeyIntent;

import java.util.ArrayList;
import java.util.List;

public class StateMachine {
    private static final String TAG_BASE = StateMachine.class.getSimpleName();
    private final String TAG;

    public final State STATE_CLOSE;
    public final State STATE_OPEN;
    private final State STATE_PREPARE_CLOSE;
    private final State STATE_PREPARE_OPEN;

    private static final boolean DEBUG = true;

    public enum EEvent {
        NONE, OPEN, CLOSE, ERROR, OPEN_SUCCESS, CLOSE_SUCCESS, MUTE, UNMUTE
    }

    private State currState;
    private State nextState;
    private Event NoneEvent = new Event(EEvent.NONE, "");
    private Event pendingEvent = NoneEvent;
    private Handler handler = new Handler();

    private final DashCamControl mDashCamControl;

    private List<Transition> transitions = new ArrayList<>();

    public static class Event {
        public Event(EEvent what, String reason) {
            this.what = what;
            this.reason = reason;
        }

        boolean equals(Event other) {
            return this.what == other.what;
        }

        @Override
        public String toString() {
            return "Event{" +
                    "what=" + what +
                    ", reason='" + reason + '\'' +
                    '}';
        }

        EEvent what;
        String reason;
    }

    private interface IState {
        String getName();
        void enter();
        void exit();
        void processEvent(Event event);
    }

    public class State implements IState {
        private final String name;

        State(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
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
            Logg.d(TAG, name + " processEvent: " + event);
        }

        @Override
        public String toString() {
            return "State{" +
                    "name='" + name + '\'' +
                    '}';
        }
    }

    public static class Transition {
        public static Transition create(State currState, EEvent event, State destState) {
            return new Transition(currState, event, destState);
        }

        Transition(State currState, EEvent event, State destState) {
            this.currState = currState;
            this.event = event;
            this.destState = destState;
        }

        State currState;
        State destState;
        EEvent event;
    }

    private  void  sendErrorBroadCast(){
        Intent intent =new Intent(AskeyIntent.JKC_EVENTSENDING_EVENT_NOTIFY_EVENT_DETECT);
        intent.putExtra("eventType",8);
        intent.putExtra("timeStamp",System.currentTimeMillis());
        DashCamApplication.getAppContext().sendBroadcast(intent);
    }

    public StateMachine(DashCamControl dashCam, int cameraID) {
        TAG = TAG_BASE + "-" + cameraID;
        mDashCamControl = dashCam;
        STATE_PREPARE_OPEN = new State("PREPARE_OPEN") {
            @Override
            public void enter() {
                super.enter();
                try {
                    mDashCamControl.onOpenCamera();
                } catch (Exception e) {
                    sendErrorBroadCast();
                    Logg.e(TAG, "onOpenCamera() fail with exception: " + e.getMessage());
                    dispatchEvent(new Event(EEvent.ERROR, e.getMessage()));
                }
            }

            @Override
            public void processEvent(Event event) {
                if (event.what == EEvent.CLOSE || event.what == EEvent.OPEN) {
                    pendingEvent = event;
                }
            }
        };

        STATE_PREPARE_CLOSE = new State("PREPARE_CLOSE") {
            @Override
            public void enter() {
                super.enter();
                mDashCamControl.onStopVideoRecord();
            }

            @Override
            public void processEvent(Event event) {
                if (event.what == EEvent.CLOSE || event.what == EEvent.OPEN) {
                    pendingEvent = event;
                }
            }
        };

        STATE_OPEN = new State("OPEN") {
            @Override
            public void enter() {
                super.enter();
                mDashCamControl.onStartVideoRecord();
                if (pendingEvent.what != EEvent.NONE) {
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
                if (event.what == EEvent.MUTE) {
                    mDashCamControl.onMuteAudio();
                } else if (event.what == EEvent.UNMUTE) {
                    mDashCamControl.onDemuteAudio();
                }
            }
        };

        STATE_CLOSE = new State("CLOSE") {
            @Override
            public void enter() {
                super.enter();
                mDashCamControl.onCameraClosed();
                if (pendingEvent.what != EEvent.NONE) {
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

        addTransition(Transition.create(STATE_CLOSE, EEvent.OPEN, STATE_PREPARE_OPEN));
        addTransition(Transition.create(STATE_PREPARE_OPEN, EEvent.OPEN_SUCCESS, STATE_OPEN));
        addTransition(Transition.create(STATE_PREPARE_OPEN, EEvent.ERROR, STATE_PREPARE_CLOSE));
        addTransition(Transition.create(STATE_OPEN, EEvent.CLOSE, STATE_PREPARE_CLOSE));
        addTransition(Transition.create(STATE_OPEN, EEvent.OPEN, STATE_PREPARE_CLOSE));
        addTransition(Transition.create(STATE_OPEN, EEvent.ERROR, STATE_PREPARE_CLOSE));
        addTransition(Transition.create(STATE_PREPARE_CLOSE, EEvent.CLOSE_SUCCESS, STATE_CLOSE));
        addTransition(Transition.create(STATE_PREPARE_CLOSE, EEvent.ERROR, STATE_CLOSE));

        initialState(STATE_CLOSE);
    }

    public State getCurrentState() {
        return currState;
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

    public synchronized void dispatchEventDelayed(Event event, long delayMillis) {
        if (DEBUG) Log.v(TAG, "dispatchEvent: currState=" + currState + ", event=" + event
                + ", delayMillis=" + delayMillis);
        if (delayMillis == 0) {
            dispatchEvent(event);
        } else {
            handler.postDelayed(new DispatchRunnable(event), delayMillis);
        }
    }

    public synchronized void dispatchEvent(Event event) {
        if (DEBUG) Log.v(TAG, "dispatchEvent: currState=" + currState + ", event=" + event);
        boolean found = false;
        for (Transition trans : transitions) {
            if ((trans.currState == currState) && (trans.event == event.what)) {
                nextState = trans.destState;
                if (DEBUG) Log.v(TAG, "dispatchEvent: nextState=" + nextState);
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

    private class DispatchRunnable implements Runnable {
        private Event mEvent;
        public DispatchRunnable(Event event) {
            mEvent = event;
        }

        @Override
        public void run() {
            dispatchEvent(mEvent);
        }
    }
}
