package com.askey.dvr.cdr7010.dashcam.jvcmodule.local;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.askey.dvr.cdr7010.dashcam.util.AppUtils;
import com.askey.dvr.cdr7010.dashcam.util.Logg;
import com.askey.platform.AskeyIntent;

public class EcallButtonReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = "EcallButtonReceiver";
    public static final String ACTION_ECALL_BUTTON = AskeyIntent.ACTION_ECALL_PRESS;

    @Override
    public void onReceive(Context context, final Intent intent) {
        String action = intent.getAction();
        Logg.d(LOG_TAG, "onReceive: action=" + action);
        if (ACTION_ECALL_BUTTON.equals(action)) {
            boolean isAppTop = AppUtils.isAppTop(context);
            Logg.d(LOG_TAG, "onReceive: isAppTop=" + isAppTop);
//            if (isAppTop) {
                EcallUtils.startVoipActivity(1);
//            }
        }
    }
}