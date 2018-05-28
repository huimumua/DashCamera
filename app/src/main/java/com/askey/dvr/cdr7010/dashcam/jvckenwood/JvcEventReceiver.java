package com.askey.dvr.cdr7010.dashcam.jvckenwood;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.askey.dvr.cdr7010.dashcam.domain.EventInfo;
import com.askey.dvr.cdr7010.dashcam.service.EventManager;
import com.askey.dvr.cdr7010.dashcam.util.Logg;

public class JvcEventReceiver extends BroadcastReceiver{
    private static final String LOG_TAG = "JvcEventReceiver";
    private static final String ACTION_EVENT_DISPLAY_ALERT = "com.jvckenwood.eventsending.EVENT_DISPLAY_ALERT";
    private static final String ACTION_EVENT_RECORD_REQUEST = "com.jvckenwood.eventsending.EVENT_RECORD_REQUEST";
    private static final String EXTRA_EVENT_TYPE = "eventType";
    private static final String EXTRA_TIME_STAMP = "timeStamp";
    private static final String EXTRA_EVENT_NO = "eventNo";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Logg.i(LOG_TAG, "onReceive: action=" + action);
        if(action.equals(ACTION_EVENT_DISPLAY_ALERT)){
            int eventType = intent.getIntExtra(EXTRA_EVENT_TYPE, -1);
            long timeStamp = intent.getLongExtra(EXTRA_TIME_STAMP, -1);
            EventInfo eventInfo = EventManager.getInstance().getEventInfoByEventType(eventType);
            if(checkEventInfo(eventInfo, eventType)) EventManager.getInstance().handOutEventInfo(eventInfo, timeStamp);
        }else if(action.equals(ACTION_EVENT_RECORD_REQUEST)){
            int eventNo = intent.getIntExtra(EXTRA_EVENT_NO, -1);
            int eventType = intent.getIntExtra(EXTRA_EVENT_TYPE, -1);
            long timeStamp = intent.getLongExtra(EXTRA_TIME_STAMP, -1);

        }
    }

    private boolean checkEventInfo(EventInfo eventInfo, int eventType){
        if(eventInfo == null){
            Logg.e(LOG_TAG, "checkEventInfo: can't find EventInfo, eventType=" + eventType);
            return false;
        }
        return true;
    }

}