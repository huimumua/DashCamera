package com.askey.dvr.cdr7010.dashcam.business;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import com.askey.dvr.cdr7010.dashcam.util.Logg;

import java.util.HashMap;
import java.util.Locale;

public class CReaderSpeaker extends UtteranceProgressListener implements TextToSpeech.OnInitListener{
    private static final String TAG = CReaderSpeaker.class.getSimpleName();
    private TextToSpeech speech;
    private OnUtteranceProgressListener listener;

    public static class QueueModeConstant {
        public static final int QUEUE_FLUSH = 0;
        public static final int QUEUE_ADD = 1;

    }

    public CReaderSpeaker(Context context) {
        speech = new TextToSpeech(context, this);
        speech.setOnUtteranceProgressListener(this);
    }
    public void setOnUtteranceProgressListener(
            OnUtteranceProgressListener listener) {
        this.listener = listener;
    }
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int supported = -1;
            String language = Locale.getDefault().getLanguage();
            if(language.equals("en") || language.equals("us")){
                supported = speech.setLanguage(Locale.US);
            }
            if (language.equals("zh")) {
                supported = speech.setLanguage(Locale.TRADITIONAL_CHINESE);
            }
            if(supported == TextToSpeech.LANG_MISSING_DATA || supported == TextToSpeech.LANG_NOT_SUPPORTED){
                speech = null;
                Logg.w(TAG, "lang is not supported");
            }else {
                speech.setPitch(1.0f);
                speech.setSpeechRate(1.0f);
            }
        }
    }
    public void onTtsSpeak(String text,int queueMode,HashMap<String, String> params){
        if(speech != null){
            speech.speak(text,queueMode,params);
        }
    }
    @Override
    public void onStart(String utteranceId) {
        if (listener != null)
            listener.onStart();
    }


    @Override
    public void onDone(String utteranceId) {
        if (listener != null)
            listener.onDone();
    }


    @Override
    public void onError(String utteranceId) {
        if (listener != null)
            listener.onError();
    }
    public void onPause(){
        if(speech != null){
            if(!speech.isSpeaking()){
                return;
            }
        }
    }
    public void onResume(){
        if(speech != null){
            if(speech.isSpeaking()){
                return;
            }
        }
    }
    public void onStop() {
        if(speech != null) {
            speech.stop();
        }
    }
    public void onRelease(){
        if(speech != null) {
            speech.shutdown();
            speech = null;
        }
    }
    public boolean isSpeaking(){
        if(speech != null){
            if(speech.isSpeaking()){
                return true;
            }
        }
        return false;
    }
    public interface OnUtteranceProgressListener {
        void onStart();

        void onDone();

        void onError();
    }
}
