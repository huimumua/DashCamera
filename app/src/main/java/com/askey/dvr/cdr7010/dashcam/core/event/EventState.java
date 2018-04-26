package com.askey.dvr.cdr7010.dashcam.core.event;

import android.os.Handler;

public class EventState {

    private Event mEvent;
    private boolean mInProcessing;
    private final Handler mHandler;
    private final Runnable mRunnable;

    public EventState() {
        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                processEnd();
            }
        };
    }

    public boolean eventInput(@Event.EventID int eventId, long time) {
        if (!mInProcessing) {
            mEvent = new Event(eventId, time);
        }

        return !mInProcessing;
    }

    public Event getEvent() {
        return mEvent;
    }

    public boolean isNeedProcess() {
        return (!mInProcessing && (mEvent != null) && (mEvent.getId() != Event.ID_NONE));
    }

    public void doProcess() {
        mInProcessing = true;
        mHandler.postDelayed(mRunnable, 5200);
    }

    private void processEnd() {
        mInProcessing = false;
    }
}
