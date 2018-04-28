package com.askey.dvr.cdr7010.dashcam.core.event;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;

public class EventState {

    private Event mEvent;
    private boolean mInProcessing;
    private final Handler mHandler;
    private final Runnable mRunnable;

    public EventState(Context context) {
        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                processEnd();
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.askey.dashcam.debug.EVENT");
        context.registerReceiver(mDebugReceiver, filter);
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
        mEvent = null;
    }

    private BroadcastReceiver mDebugReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.askey.dashcam.debug.EVENT".equals(intent.getAction())) {
                int event = intent.getIntExtra("id", 0);
                eventInput(event, System.currentTimeMillis());
            }
        }
    };
}
