package com.askey.dvr.cdr7010.dashcam.jvcmodule.jvckenwood;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;

import com.askey.dvr.cdr7010.dashcam.domain.EventInfo;
import com.askey.dvr.cdr7010.dashcam.logic.GlobalLogic;
import com.askey.dvr.cdr7010.dashcam.service.EventManager;
import com.askey.dvr.cdr7010.dashcam.service.FileManager;
import com.askey.dvr.cdr7010.dashcam.util.Logg;
import com.askey.dvr.cdr7010.dashcam.util.SDcardHelper;

import java.util.ArrayList;
import java.util.Arrays;

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
            int sdcardStatus = -1;
            try {
                sdcardStatus = FileManager.getInstance(context).checkSdcardAvailable();
            } catch (RemoteException e) {
                Logg.e(LOG_TAG, "sd card unavailable");
                sdcardStatus = 2;
            }

            if (!SDcardHelper.isSDCardAvailable(sdcardStatus)) {
                ArrayList<Integer> results = new ArrayList<>(Arrays.asList(100, 100, 100, 100, 100, 100, 100, 100));
                ArrayList<String> files = new ArrayList<>();
                JvcEventSending.recordResponse(eventNo, results, files);
            } else {
                Intent i = new  Intent("com.askey.dashcam.record.EVENT");
                i.putExtra("id", eventNo);
                LocalBroadcastManager.getInstance(context).sendBroadcast(i);
            }
        }
    }

    private boolean checkEventInfo(EventInfo eventInfo, int eventType){
        if(eventInfo == null){
            Logg.e(LOG_TAG, "checkEventInfo: can't find EventInfo, eventType=" + eventType);
            return false;
        }
        if(GlobalLogic.getInstance().isStartSwitchUser()){
            if(eventInfo.isSupportPopUp() || eventInfo.isSupportSpeech()){
                return false;
            }
        }
        return true;
    }

}