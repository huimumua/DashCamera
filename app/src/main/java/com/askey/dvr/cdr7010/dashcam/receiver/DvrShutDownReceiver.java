package com.askey.dvr.cdr7010.dashcam.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.askey.dvr.cdr7010.dashcam.service.DialogManager;
import com.askey.dvr.cdr7010.dashcam.util.Logg;
import com.askey.platform.AskeyIntent;

/***
 * Company: Chengdu Skysoft Info&Tech Co., Ltd.
 * Copyright ©2014-2018 Chengdu Skysoft Info&Tech Co., Ltd.
 * Created by Mark on 2018/5/4.

 * @since:JDK1.6
 * @version:1.0
 * @see
 ***/
public class DvrShutDownReceiver extends BroadcastReceiver {
    private final static String  LOG_TAG = "DvrShutDownReceiver";
    private static boolean mShutDown;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Logg.d(LOG_TAG, "onReceive: action=" + action);
        if(action.equals(AskeyIntent.ACTION_DVR_SHUTDOWN)){
            setShutDown(true);
            DialogManager.getIntance().setPowerOff(true);
        }else {

        }
    }

    public static boolean isShutDown() {
        return mShutDown;
    }

    public static void setShutDown(boolean isShutDown) {
        DvrShutDownReceiver.mShutDown = isShutDown;
    }
}