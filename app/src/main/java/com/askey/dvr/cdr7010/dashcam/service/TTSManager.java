package com.askey.dvr.cdr7010.dashcam.service;


import com.askey.dvr.cdr7010.dashcam.application.DashCamApplication;
import com.askey.dvr.cdr7010.dashcam.business.CReaderSpeaker;
import com.askey.dvr.cdr7010.dashcam.domain.Event;
import com.askey.dvr.cdr7010.dashcam.domain.EventInfo;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.jvckenwood.TTS;
import com.askey.dvr.cdr7010.dashcam.util.Logg;

public class TTSManager{
    private static final String TAG = TTSManager.class.getSimpleName();
    private volatile static TTSManager instance;
    private TTS tts;
    private int lastPriority = Integer.MAX_VALUE;
    private int lastRequestId = -1;
    private int curRequestId = -1;

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
        Logg.d(TAG,"ttsEventStart requestId="+requestId+",lastRequestId="+lastRequestId+",priority="+priority+",voiceId="+voiceId[0]);
        if(ttsIsSpeaking()){
            boolean isSdCardAbnormalEvent = (Event.contains(Event.sdCardAbnormalEvent,requestId)
                    || Event.contains(Event.sdCardUnMountedEvent,requestId));
            if(priority <= lastPriority && ((lastRequestId !=requestId ) &&!isSdCardAbnormalEvent)
                    ||((priority <= lastPriority) && isSdCardAbnormalEvent) ){
                if(instance != null && tts != null){
                    curRequestId = requestId;
                    tts.speechStop(lastRequestId);
                    tts.voiceNotification(voiceId,requestId);
                }
                lastRequestId = requestId;
                lastPriority = priority;
            }
        }else{
            if(instance != null && tts != null){
                curRequestId = requestId;
                tts.voiceNotification(voiceId,requestId);
                lastPriority = priority;
                lastRequestId = requestId;
            }
        }

    }
    public void ttsDeleteFile(){
        ttsStartSound(Event.EVENT_DELETE_FILE);
    }
    public void ttsMenuCursorMove(){
        ttsStartSound(Event.EVENT_MENU_CURSOR_FOCUSED);
    }
    public void ttsMenuItemClick(){
        ttsStartSound(Event.EVENT_MENU_ITEM_CLICK);
    }
    public void ttsMenuItemBack(){
        ttsStartSound(Event.EVENT_MENU_ITEM_BACK);
    }
    private void ttsStartSound(int eventType){
        EventInfo eventInfo = EventManager.getInstance().getEventInfoByEventType(eventType);
        if(eventInfo != null) {
            int priority = eventInfo.getPriority();
            int requestId = eventInfo.getEventType();
            int[] voiceId = new int[1];
            voiceId[0] = Integer.parseInt(eventInfo.getVoiceGuidence().trim(), 16);
            if (ttsIsSpeaking()) {
                boolean isOtherSoundEvent = (Event.contains(Event.otherSoundEvent, lastRequestId));
                if (priority <= lastPriority || !isOtherSoundEvent){
                    if (instance != null && tts != null) {
                        curRequestId = requestId;
                        tts.speechStop(lastRequestId);
                        tts.voiceNotification(voiceId, requestId);
                    }
                    lastRequestId = requestId;
                    lastPriority = priority;
                }
            } else {
                if (instance != null && tts != null) {
                    curRequestId = requestId;
                    tts.voiceNotification(voiceId, requestId);
                    lastPriority = priority;
                    lastRequestId = requestId;
                }
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
   public boolean isResumeTtsByEventType(int eventType){
       Logg.d(TAG,"isResumeTtsByEventType curRequestId="+curRequestId+",ttsIsSpeaking="+ttsIsSpeaking()+"eventType="+eventType);
        if(eventType == curRequestId && ttsIsSpeaking()){
            return true;
        }
        return false;
   }
   public int getLastTtsEventType(){
       return  lastRequestId;
   }
}