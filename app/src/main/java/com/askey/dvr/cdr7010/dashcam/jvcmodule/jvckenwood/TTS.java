package com.askey.dvr.cdr7010.dashcam.jvcmodule.jvckenwood;

import android.content.Context;
import android.content.Intent;

import com.askey.dvr.cdr7010.dashcam.application.DashCamApplication;
import com.askey.dvr.cdr7010.dashcam.util.Logg;


public class TTS {
    private static final String TAG = "TTS";
    public static final String ACTION_VOICE_NOTIFICATION = "com.jvckenwood.tts.VOICE_NOTIFICATION";
    public static final String ACTION_SPEECH_STOP = "com.jvckenwood.tts.SPEECH_STOP";
    private final Context mAppContext;
    public static boolean isSpeaking = false;
    public static int speakingId = -1;

    public TTS() {
        mAppContext = DashCamApplication.getAppContext();
    }

    public boolean isSpeaking(){
        if (isSpeaking && speakingId != -1) {
            return true;
        }
        return false;
    }

    public void voiceNotification(int[] voiceId, final int requestId) {
        Intent intent = new Intent(ACTION_VOICE_NOTIFICATION);
        intent.putExtra("voiceId", voiceId);
        Logg.i(TAG, "voiceNotification=voiceId==" + voiceId[0]);
        intent.putExtra("requestId", requestId);
        Logg.i(TAG, "voiceNotification=requestId==" + requestId);
        sendOutBroadcast(intent);
        isSpeaking = true;
        speakingId = requestId;
    }

    public void speechStop(int requestId) {
        Intent intent = new Intent(ACTION_SPEECH_STOP);
        intent.putExtra("requestId", requestId);
        Logg.i(TAG, "speechStop=requestId==" + requestId);
        sendOutBroadcast(intent);
    }

    private void sendOutBroadcast(Intent intent) {
        mAppContext.sendBroadcastAsUser(intent, android.os.Process.myUserHandle());
    }
}
