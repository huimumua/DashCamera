package com.askey.dvr.cdr7010.dashcam.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.askey.dvr.cdr7010.dashcam.service.EventManager;

import java.util.Locale;

public class LanguageReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String language = Locale.getDefault().getLanguage();
        EventManager.getInstance().loadXML(language);


    }
}