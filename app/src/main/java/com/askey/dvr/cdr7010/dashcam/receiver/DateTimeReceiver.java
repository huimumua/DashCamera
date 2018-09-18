package com.askey.dvr.cdr7010.dashcam.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.askey.dvr.cdr7010.dashcam.jvcmodule.jvckenwood.MainApp;

public class DateTimeReceiver extends BroadcastReceiver {
    private String LOG_TAG = DateTimeReceiver.class.getSimpleName();
    private static final String ACTION_DATE_CHANGED = Intent.ACTION_DATE_CHANGED;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (ACTION_DATE_CHANGED.equals(action)) {
            Log.d(LOG_TAG, "~~~_DATE_CHANGED! sendStatUpNotify~~~");
            MainApp.sendStatUpNotify();
        }

    }
}
