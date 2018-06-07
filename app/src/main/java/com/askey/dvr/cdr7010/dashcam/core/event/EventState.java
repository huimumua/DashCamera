package com.askey.dvr.cdr7010.dashcam.core.event;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

public class EventState {

    private Event mEvent;
    private boolean mInProcessing;
    private final Handler mHandler;
    private final Runnable mRunnable;
    private Context mContext;

    public EventState(Context context) {
        mContext = context.getApplicationContext();
        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                processEnd();
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.askey.dashcam.debug.EVENT");
        mContext.registerReceiver(mDebugReceiver, filter);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMyReceiver,
                new IntentFilter("com.askey.dashcam.record.EVENT"));
    }

    public void release() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMyReceiver);
        mContext.unregisterReceiver(mDebugReceiver);
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
                int event = intent.getIntExtra("id", -1);
                eventInput(event, System.currentTimeMillis());
            }
        }
    };

    private BroadcastReceiver mMyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.askey.dashcam.record.EVENT".equals(intent.getAction())) {
                int event = intent.getIntExtra("id", -1);
                eventInput(event, System.currentTimeMillis());
            }
        }
    };
}
