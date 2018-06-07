package com.askey.dvr.cdr7010.dashcam.jvcmodule.jvckenwood;

import android.content.Intent;

import com.askey.dvr.cdr7010.dashcam.application.DashCamApplication;
import com.askey.dvr.cdr7010.dashcam.util.Logg;

import java.util.ArrayList;

/***
 * Company: Chengdu Skysoft Info&Tech Co., Ltd.
 * Copyright ©2014-2018 Chengdu Skysoft Info&Tech Co., Ltd.
 * Created by Mark on 2018/5/16.

 * @since:JDK1.6
 * @version:1.0
 * @see
 ***/
public class JvcEventSending {
    public static final String ACTION_EVENT_NOTIFY_ADAS = "com.jvckenwood.eventsending.EVENT_NOTIFY_ADAS";
    public static final String ACTION_EVENT_NOTIFY_EVENT_DETECT = "com.jvckenwood.eventsending.EVENT_NOTIFY_EVENT_DETECT";
    public static final String ACTION_EVENT_RECORD_RESPONSE = "com.jvckenwood.eventsending.EVENT_RECORD_RESPONSE";

    private static final String LOG_TAG = "JvcEventSending";

    public static void notifyAdas(int eventType, long timeStamp){
        Logg.d(LOG_TAG, "notifyAdas: eventType=" + eventType + ", timeStamp=" + timeStamp);
        Intent intent = new Intent(ACTION_EVENT_NOTIFY_ADAS);
        intent.putExtra("eventType", eventType);
        intent.putExtra("timeStamp", timeStamp);
        sendOutBroadcast(intent);
    }

    public static void notivyEventDetect(int eventType, long timeStamp){
        Logg.d(LOG_TAG, "notivyEventDetect: eventType=" + eventType + ", timeStamp=" + timeStamp);
        Intent intent = new Intent(ACTION_EVENT_NOTIFY_EVENT_DETECT);
        intent.putExtra("eventType", eventType);
        intent.putExtra("timeStamp", timeStamp);
        sendOutBroadcast(intent);
    }

    public static void recordResponse(int eventNo, ArrayList<Integer> result, ArrayList<String> recordPath){
        Logg.d(LOG_TAG, "recordResponse: eventNo=" + eventNo + " result=" + result + " recordPath=" + recordPath);
        Intent intent = new Intent(ACTION_EVENT_RECORD_RESPONSE);
        intent.putExtra("eventNo", eventNo);
        intent.putIntegerArrayListExtra("result", result);
        intent.putStringArrayListExtra("recordPath", recordPath);
        sendOutBroadcast(intent);
    }

    private static void sendOutBroadcast(Intent intent){
        DashCamApplication.getAppContext().sendBroadcastAsUser(intent, android.os.Process.myUserHandle());
    }
}
