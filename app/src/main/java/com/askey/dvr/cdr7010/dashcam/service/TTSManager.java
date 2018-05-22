package com.askey.dvr.cdr7010.dashcam.service;


import com.askey.dvr.cdr7010.dashcam.application.DashCamApplication;
import com.askey.dvr.cdr7010.dashcam.business.CReaderSpeaker;

public class TTSManager{
    private static final String TAG = TTSManager.class.getSimpleName();
    private volatile static TTSManager instance;
    private CReaderSpeaker cReaderSpeaker;
    private int lastPriority = Integer.MAX_VALUE;


    private TTSManager(){

    }
    public static TTSManager getInstance() {
        if (instance == null) {
            synchronized (TTSManager.class) {
                if (instance == null) {
                    instance = new TTSManager();
                }
            }
        }
        return instance;
    }
    public void initTTS(){
        cReaderSpeaker = new CReaderSpeaker(DashCamApplication.getAppContext());
        cReaderSpeaker.setOnUtteranceProgressListener(new CReaderSpeaker.OnUtteranceProgressListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onDone() {

            }

            @Override
            public void onError() {
                ttsCancel();
                initTTS();
            }
        });
        return;
    }
    public void ttsNormalStart(String message){
        if(instance != null && cReaderSpeaker != null){
            cReaderSpeaker.onTtsSpeak(message,CReaderSpeaker.QueueModeConstant.QUEUE_FLUSH,null);
        }
    }
    public void ttsEventStart(String message,int eventType,int priority){
        if(ttsIsSpeaking()){
            if(priority <= lastPriority){
                ttsCancel();
                if(instance != null && cReaderSpeaker != null){
                    cReaderSpeaker.onTtsSpeak(message,CReaderSpeaker.QueueModeConstant.QUEUE_FLUSH,null);
                }
                lastPriority = priority;
            }
        }else{
            if(instance != null && cReaderSpeaker != null){
                cReaderSpeaker.onTtsSpeak(message,CReaderSpeaker.QueueModeConstant.QUEUE_FLUSH,null);
                lastPriority = priority;
            }
        }

    }
    public void ttsResume(){
        if(instance != null && cReaderSpeaker != null){
            cReaderSpeaker.onResume();
        }
    }
    public void ttsPause(){
        if(instance != null && cReaderSpeaker != null){
            cReaderSpeaker.onPause();
        }
    }
    public void ttsStop(){
        if(instance != null && cReaderSpeaker != null){
            cReaderSpeaker.onStop();
        }
    }
    public void ttsRelease(){
        if(instance != null && cReaderSpeaker != null){
            cReaderSpeaker.onRelease();
        }
    }
    private void ttsCancel(){
        if( cReaderSpeaker != null){
            cReaderSpeaker.onStop();
            cReaderSpeaker.onRelease();
        }
    }
    private boolean ttsIsSpeaking(){
        if(instance != null && cReaderSpeaker != null){
            return cReaderSpeaker.isSpeaking();
        }
        return false;
    }
    public void changeLanguage(String language) {
        ttsCancel();
        setTtsStreamVolume();
    }
    public void setTtsStreamVolume(){
        initTTS();
    }

}