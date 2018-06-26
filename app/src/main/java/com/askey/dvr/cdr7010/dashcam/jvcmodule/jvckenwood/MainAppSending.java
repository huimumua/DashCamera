package com.askey.dvr.cdr7010.dashcam.jvcmodule.jvckenwood;

import android.content.Context;
import android.content.Intent;

import com.askey.dvr.cdr7010.dashcam.application.DashCamApplication;
import com.askey.dvr.cdr7010.dashcam.util.Logg;

/***
 * Company: Chengdu Skysoft Info&Tech Co., Ltd.
 * Copyright ©2014-2018 Chengdu Skysoft Info&Tech Co., Ltd.
 * Created by Mark on 2018/5/16.

 * @since:JDK1.6
 * @version:1.0
 * @see
 ***/
public class MainAppSending {
    public static final String ACTION_STARTUP_NOTIFY = "android.intent.action.STARTUP_NOTIFY";
    public static final String ACTION_MENU_TRANSITION = "android.intent.action.MENU_TRANSITION";

    private static final String LOG_TAG = "MainAppSending";

    public static void startupNotify(int startUp, int rtcInfo){
        Logg.d(LOG_TAG, "startupNotify: startUp=" + startUp + ", rtcInfo=" + rtcInfo);
        Intent intent = new Intent(ACTION_STARTUP_NOTIFY);
        intent.putExtra("startUp", startUp);
        intent.putExtra("rtcInfo", rtcInfo);
        sendOutBroadcast(intent);
    }

    public static void menuTransition(int status){
        Logg.d(LOG_TAG, "menuTransition: status=" + status);
        Intent intent = new Intent(ACTION_MENU_TRANSITION);
        intent.putExtra("status", status);
        sendOutBroadcast(intent);
    }

    private static void sendOutBroadcast(Intent intent){
        Context appContext = DashCamApplication.getAppContext();
        appContext.sendBroadcastAsUser(intent, android.os.Process.myUserHandle());
    }


}
