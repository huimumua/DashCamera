package com.askey.dvr.cdr7010.dashcam.jvcmodule.jvckenwood;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.askey.dvr.cdr7010.dashcam.domain.Event;
import com.askey.dvr.cdr7010.dashcam.service.DialogManager;
import com.askey.dvr.cdr7010.dashcam.util.Logg;

public class TTSReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = "TTSReceiver";
    private static final String ACTION_VOICE_CANCEL_RESPONSE = "com.jvckenwood.tts.VOICE_CANCEL_RESPONSE";
    private static final String ACTION_VOICE_END_RESPONSE = "com.jvckenwood.tts.VOICE_END_RESPONSE";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        int requestId = -1;
        int result = -1;
        Logg.i(LOG_TAG, "onReceive: action=" + action);
        if (action.equals(ACTION_VOICE_CANCEL_RESPONSE)) {
            requestId = intent.getIntExtra("requestId", -1);
            Logg.i(LOG_TAG, "ACTION_VOICE_CANCEL_RESPONSE: requestId=" + requestId);
            result = intent.getIntExtra("result", -1);
            Logg.i(LOG_TAG, "ACTION_VOICE_CANCEL_RESPONSE: result=" + result);
            if (TTS.isSpeaking && requestId == TTS.speakingId) {
                TTS.isSpeaking = false;
                TTS.speakingId = -1;
            }
            if (result == 0) {//0 : 正常完了
                Logg.i(LOG_TAG, "==0 : 正常完了===isSpeaking = false");
            } else if (result == 1) {//1 : パラメータエラー
                Logg.i(LOG_TAG, "=1 : パラメータエラー==");
            } else if (result == 2) {//2 : キャンセル失敗
                Logg.i(LOG_TAG, "==2 : キャンセル失敗=");
            }
        } else if (action.equals(ACTION_VOICE_END_RESPONSE)) {
            requestId = intent.getIntExtra("requestId", -1);
            Logg.i(LOG_TAG, "onReceive: requestId=" + requestId);
            result = intent.getIntExtra("result", -1);
            Logg.i(LOG_TAG, "ACTION_VOICE_END_RESPONSE: result=" + result);
            if (TTS.isSpeaking && requestId == TTS.speakingId) {
                TTS.isSpeaking = false;
                TTS.speakingId = -1;
            }
            if (result == 0) {// 0 : 再生正常完了
                Logg.i(LOG_TAG, "== 0 : 再生正常完了==isSpeaking = false");
            } else if (result == 1) {//1 : パラメータエラー
                Logg.i(LOG_TAG, "==1 : パラメータエラー=");
            } else if (result == 2) {//2 : 音声ファイルなし
                Logg.i(LOG_TAG, "==2 : 音声ファイルなし==");
            } else if (result == 3) {//3 : キャンセル終了
                Logg.i(LOG_TAG, "==3 : キャンセル終了==");
            } else if (result == 99) {//99 : その他再生失敗
                Logg.i(LOG_TAG, "==99 : その他再生失敗==");
            }
        }
        if(Event.contains(Event.noticeEvent,requestId) || Event.contains(Event.simCardErroeEvent,requestId)){
            DialogManager.getIntance().setSpeechCompleted(true);
        }
        if(Event.contains(Event.detectEvent,requestId)){
            Communication.getInstance().alertComplite(requestId);
        }
    }
}
