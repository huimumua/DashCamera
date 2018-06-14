package com.askey.dvr.cdr7010.dashcam.jvcmodule.local;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.askey.dvr.cdr7010.dashcam.util.AppUtils;
import com.askey.dvr.cdr7010.dashcam.util.Logg;

public class EcallButtonReceiver extends BroadcastReceiver{
    public static final String ACTION_ECALL_BUTTON = "action_ecall_button";

    private static final String LOG_TAG = "EcallButtonReceiver";

    @Override
    public void onReceive(Context context, final Intent intent){
        String action = intent.getAction();
        Logg.d(LOG_TAG, "onReceive: action=" + action);
        if(action.equals(ACTION_ECALL_BUTTON)){
            boolean isAppTop = AppUtils.isAppTop(context);
            Logg.d(LOG_TAG, "onReceive: isAppTop=" + isAppTop);
            if(isAppTop){
                int xxx_userID = 1000;
//                MainApp.getInstance().voipInfomationRequest(xxx_userID, 1);
            }
        }
    }


}