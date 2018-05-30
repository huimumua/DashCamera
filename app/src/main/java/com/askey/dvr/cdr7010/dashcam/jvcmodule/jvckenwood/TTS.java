package com.askey.dvr.cdr7010.dashcam.jvcmodule.jvckenwood;

import android.content.Context;
import android.content.Intent;

import com.askey.dvr.cdr7010.dashcam.application.DashCamApplication;
import com.askey.dvr.cdr7010.dashcam.util.Logg;

public class TTS {
    private static final String TAG = "TTS";
    public static final String ACTION_VOICE_NOTIFICATION = "com.jvckenwood.tts.VOICE_NOTIFICATION";
    public static final String ACTION_SPEECH_STOP = "com.jvckenwood.tts.SPEECH_STOP";
    private static TTS mTTS;
    private final Context mAppContext;

    private TTS() {
        mAppContext = DashCamApplication.getAppContext();
    }

    public static TTS getInstance() {
        if (mTTS == null)
            mTTS = new TTS();
        return mTTS;
    }

    public void voiceNotification(int[] voiceId, int requestId) {
        Intent intent = new Intent(ACTION_VOICE_NOTIFICATION);
        intent.putExtra("voiceId", voiceId);
        Logg.i(TAG,"voiceNotification=voiceId=="+voiceId[0]);
        intent.putExtra("requestId", requestId);
        Logg.i(TAG,"voiceNotification=requestId=="+requestId);
        sendOutBroadcast(intent);
    }

    public void speechStop(int requestId) {
        Intent intent = new Intent(ACTION_SPEECH_STOP);
        intent.putExtra("requestId", requestId);
        Logg.i(TAG,"speechStop=requestId=="+requestId);
        sendOutBroadcast(intent);
    }

    private void sendOutBroadcast(Intent intent) {
        mAppContext.sendBroadcast(intent);
    }

}
