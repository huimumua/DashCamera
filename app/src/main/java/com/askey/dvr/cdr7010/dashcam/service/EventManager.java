package com.askey.dvr.cdr7010.dashcam.service;

import android.content.res.AssetManager;

import com.askey.dvr.cdr7010.dashcam.application.DashCamApplication;
import com.askey.dvr.cdr7010.dashcam.domain.EventInfo;
import com.askey.dvr.cdr7010.dashcam.parser.sax.GetEventInfoSAXParser;
import com.askey.dvr.cdr7010.dashcam.util.Logg;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EventManager {
    private static final String LOG_TAG = "EventManager";
    private static EventManager instance;
    private AssetManager assets;
    private GetEventInfoSAXParser eventInfoSAXParser;
    private InputStream inputStream;
    private ArrayList<EventInfo> eventInfoArrayList;
    private List<EventCallback> mPopUpEventCallbackList;
    private List<EventCallback> mIconEventCallbackList;
    private List<EventCallback> mLedEventCallbackList;
    private List<EventCallback> mTtsEventCallbackList;

    private EventManager() {
        assets = DashCamApplication.getAppContext().getAssets();
        mPopUpEventCallbackList = Collections.synchronizedList(new ArrayList<EventCallback>());
        mIconEventCallbackList = Collections.synchronizedList(new ArrayList<EventCallback>());
        mLedEventCallbackList = Collections.synchronizedList(new ArrayList<EventCallback>());
        mTtsEventCallbackList = Collections.synchronizedList(new ArrayList<EventCallback>());
    }

    public static EventManager getInstance() {
        if (instance == null) {
            synchronized (TTSManager.class) {
                if (instance == null) {
                    instance = new EventManager();
                }
            }
        }
        return instance;
    }

    public void loadXML(String language) {
        try {
            inputStream = assets.open("eventlist_en.xml");
        } catch (Exception e) {
            e.printStackTrace();
        }
        eventInfoSAXParser = new GetEventInfoSAXParser();
        eventInfoArrayList = eventInfoSAXParser.parser(inputStream);
        Logg.d(LOG_TAG, "eventInfoArrayList size=" + eventInfoArrayList.size());
    }

    public EventInfo getEventInfoById(int id) {
        if (eventInfoArrayList == null || eventInfoArrayList.size() == 0 || id <= 0) {
            return null;
        }
        return eventInfoArrayList.get(id - 1);
    }

    public EventInfo getEventInfoByEventType(int eventType) {
        if (eventInfoArrayList == null || eventInfoArrayList.size() == 0) {
            return null;
        }
        EventInfo eventInfo = null;
        for (int idx = 0; idx < eventInfoArrayList.size(); idx++) {
            if (eventInfoArrayList.get(idx).getEventType() == eventType) {
                eventInfo = eventInfoArrayList.get(idx);
            }
        }
        return eventInfo;
    }

    public void registPopUpEventCallback(EventCallback callback){
        mPopUpEventCallbackList.add(callback);
    }

    public void unRegistPopUpEventCallback(EventCallback callback){
        mPopUpEventCallbackList.remove(callback);
    }

    public void registIconEventCallback(EventCallback callback){
        mIconEventCallbackList.add(callback);
    }

    public void unRegistIconEventCallback(EventCallback callback){
        mIconEventCallbackList.remove(callback);
    }

    public void registLedEventCallback(EventCallback callback){
        mLedEventCallbackList.add(callback);
    }

    public void unRegistLedEventCallback(EventCallback callback){
        mLedEventCallbackList.remove(callback);
    }

    public void registTtsEventCallback(EventCallback callback){
        mTtsEventCallbackList.add(callback);
    }

    public void unRegistTtsEventCallback(EventCallback callback){
        mTtsEventCallbackList.remove(callback);
    }

    public void handOutEventInfo(int eventType){
        Logg.d(LOG_TAG, "handOutEventInfo: eventType=" + eventType);
        long timeStamp = System.currentTimeMillis();
        EventInfo eventInfo = EventManager.getInstance().getEventInfoByEventType(eventType);
        if(eventInfo == null){
            Logg.e(LOG_TAG, "handOutEventInfo: can't find EventInfo, eventType=" + eventType);
            return;
        }
        handOutEventInfo(eventInfo, timeStamp);
    }

    public void handOutEventInfo(EventInfo eventInfo, long timeStamp){
        Logg.d(LOG_TAG, "handOutEventInfo: id=" + eventInfo.getId() + ", eventType=" + eventInfo.getEventType() + ", eventName=" + eventInfo.getEventName());
        if(eventInfo.isSupportPopUp()){
            for(EventCallback eventCallback : mPopUpEventCallbackList)
                eventCallback.onEvent(eventInfo, timeStamp);
        }
        if(eventInfo.isSupportIcon()){
            for(EventCallback eventCallback : mIconEventCallbackList)
                eventCallback.onEvent(eventInfo, timeStamp);
        }
        if(eventInfo.isSupportLed()){
            for(EventCallback eventCallback : mLedEventCallbackList)
                eventCallback.onEvent(eventInfo, timeStamp);
        }
        if(eventInfo.isSupportSpeech()){
            for(EventCallback eventCallback : mTtsEventCallbackList)
                eventCallback.onEvent(eventInfo, timeStamp);
        }
    }
//
//    /**
//     * id为51、52、53、54由handOutCommunicationInfo发出
//     */
//    public void handOutCommunicationInfo(EventInfo eventInfo, int oos, String response){
//        Logg.d(LOG_TAG, "handOutCommunicationInfo: id=" + eventInfo.getId() + ", eventType=" + eventInfo.getEventType() + ", eventName=" + eventInfo.getEventName());
//        if(eventInfo.isSupportPopUp()){
//            for(EventCallback eventCallback : mPopUpEventCallbackList)
//                eventCallback.onCommunication(eventInfo, oos, response);
//        }
//        if(eventInfo.isSupportIcon()){
//            for(EventCallback eventCallback : mIconEventCallbackList)
//                eventCallback.onCommunication(eventInfo, oos, response);
//        }
//        if(eventInfo.isSupportLed()){
//            for(EventCallback eventCallback : mLedEventCallbackList)
//                eventCallback.onCommunication(eventInfo, oos, response);
//        }
//        if(eventInfo.isSupportSpeech()){
//            for(EventCallback eventCallback : mTtsEventCallbackList)
//                eventCallback.onCommunication(eventInfo, oos, response);
//        }
//    }

    public interface EventCallback{
        void onEvent(EventInfo eventInfo, long timeStamp);
//        void onCommunication(EventInfo eventInfo, int oos, String response);
    }

}