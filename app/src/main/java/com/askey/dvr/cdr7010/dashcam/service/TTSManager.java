package com.askey.dvr.cdr7010.dashcam.service;


import com.askey.dvr.cdr7010.dashcam.application.DashCamApplication;
import com.askey.dvr.cdr7010.dashcam.business.CReaderSpeaker;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.jvckenwood.TTS;
import com.askey.dvr.cdr7010.dashcam.util.Logg;

public class TTSManager{
    private static final String TAG = TTSManager.class.getSimpleName();
    private volatile static TTSManager instance;
    private TTS tts;
    private int lastPriority = Integer.MAX_VALUE;
    private int lastRequestId = -1;

    private TTSManager(){ }

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
        tts = new TTS();
        return;
    }
    public void ttsNormalStart(int requestId,int[] voiceId){
        if(instance != null && tts != null){
            tts.voiceNotification(voiceId,requestId);
        }
    }
    public void ttsEventStart(int requestId,int priority,int[] voiceId){
        Logg.d(TAG,"ttsEventStart requestId="+requestId+",priority="+priority+",voiceId="+voiceId[0]);
        if(ttsIsSpeaking()){
            if(priority <= lastPriority){
                if(instance != null && tts != null){
                    tts.speechStop(lastRequestId);
                    tts.voiceNotification(voiceId,requestId);
                }
                lastRequestId = requestId;
                lastPriority = priority;
            }
        }else{
            if(instance != null && tts != null){
                tts.voiceNotification(voiceId,requestId);
                lastPriority = priority;
                lastRequestId = requestId;
            }
        }

    }
    public void ttsStop(int requestId){
        if(instance != null && tts != null){
            tts.speechStop(requestId);
        }
    }
    private boolean ttsIsSpeaking(){
        if(instance != null && tts != null){
            return tts.isSpeaking();
        }
        return false;
    }

}