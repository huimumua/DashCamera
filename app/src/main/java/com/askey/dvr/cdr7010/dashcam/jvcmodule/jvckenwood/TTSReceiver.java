package com.askey.dvr.cdr7010.dashcam.jvcmodule.jvckenwood;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.askey.dvr.cdr7010.dashcam.util.Logg;

public class TTSReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = "TTSReceiver";
    private static final String ACTION_VOICE_CANCEL_RESPONSE = "com.jvckenwood.tts.VOICE_CANCEL_RESPONSE";
    private static final String ACTION_VOICE_END_RESPONSE = "com.jvckenwood.tts.VOICE_END_RESPONSE";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Logg.i(LOG_TAG, "onReceive: action=" + action);
        if (action.equals(ACTION_VOICE_CANCEL_RESPONSE)) {
            int requestId = intent.getIntExtra("requestId", -1);
            Logg.i(LOG_TAG, "ACTION_VOICE_CANCEL_RESPONSE: requestId=" + requestId);
            int result = intent.getIntExtra("result", -1);
            Logg.i(LOG_TAG, "ACTION_VOICE_CANCEL_RESPONSE: result=" + result);
        } else if (action.equals(ACTION_VOICE_END_RESPONSE)) {
            int requestId = intent.getIntExtra("requestId", -1);
            Logg.i(LOG_TAG, "onReceive: requestId=" + requestId);
            int result = intent.getIntExtra("result", -1);
            Logg.i(LOG_TAG, "ACTION_VOICE_END_RESPONSE: result=" + result);
        }
    }
}
