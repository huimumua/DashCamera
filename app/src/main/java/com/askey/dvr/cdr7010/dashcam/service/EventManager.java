package com.askey.dvr.cdr7010.dashcam.service;

import android.content.ContentResolver;
import android.content.res.AssetManager;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;

import com.askey.dvr.cdr7010.dashcam.application.DashCamApplication;
import com.askey.dvr.cdr7010.dashcam.domain.EventInfo;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.local.DrivingSupportAlertSettingOnOffCheck;
import com.askey.dvr.cdr7010.dashcam.logic.GlobalLogic;
import com.askey.dvr.cdr7010.dashcam.parser.sax.GetEventInfoSAXParser;
import com.askey.dvr.cdr7010.dashcam.util.Logg;
import com.askey.platform.AskeySettings;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EventManager {
    private static final String LOG_TAG = "EventManager";
    private volatile static EventManager instance;
    private AssetManager assets;
    private GetEventInfoSAXParser eventInfoSAXParser;
    private InputStream inputStream;
    private ArrayList<EventInfo> eventInfoArrayList;
    private List<EventCallback> mPopUpEventCallbackList;
    private List<EventCallback> mIconEventCallbackList;
    private List<EventCallback> mLedEventCallbackList;
    private List<EventCallback> mTtsEventCallbackList;

    private ContentObserver mMicPhoneSettingsObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            if (getMicPhoneEnable()) {
                int eventType = 106;
                EventManager.getInstance().handOutEventInfo(eventType);
            } else {
                int eventType = 107;
                EventManager.getInstance().handOutEventInfo(eventType);
            }
            super.onChange(selfChange);
        }
    };

    private boolean getMicPhoneEnable() {
        ContentResolver contentResolver = DashCamApplication.getAppContext().getContentResolver();
        int on = 1;
        try {
            on = Settings.Global.getInt(contentResolver, AskeySettings.Global.RECSET_VOICE_RECORD);
        } catch (Settings.SettingNotFoundException e) {
            Logg.e(LOG_TAG, "SettingNotFoundException MIC");
            Settings.Global.putInt(contentResolver, AskeySettings.Global.RECSET_VOICE_RECORD, 1);
        }
        return (on != 0);
    }

    private EventManager() {
        assets = DashCamApplication.getAppContext().getAssets();
        mPopUpEventCallbackList = Collections.synchronizedList(new ArrayList<EventCallback>());
        mIconEventCallbackList = Collections.synchronizedList(new ArrayList<EventCallback>());
        mLedEventCallbackList = Collections.synchronizedList(new ArrayList<EventCallback>());
        mTtsEventCallbackList = Collections.synchronizedList(new ArrayList<EventCallback>());

        ContentResolver contentResolver = DashCamApplication.getAppContext().getContentResolver();
        contentResolver.registerContentObserver(
                Settings.Global.getUriFor(AskeySettings.Global.RECSET_VOICE_RECORD),
                false,
                mMicPhoneSettingsObserver);
    }

    public static EventManager getInstance() {
        if (instance == null) {
            synchronized (EventManager.class) {
                if (instance == null) {
                    instance = new EventManager();
                }
            }
        }
        return instance;
    }

    public void loadXML(String language) {
        try {
            if (language.equals("en") || language.equals("us")) {
                inputStream = assets.open("eventlist_en.xml");
            } else {
                inputStream = assets.open("eventlist_jp.xml");
            }
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

    public void registPopUpEventCallback(EventCallback callback) {
        mPopUpEventCallbackList.add(callback);
    }

    public void unRegistPopUpEventCallback(EventCallback callback) {
        mPopUpEventCallbackList.remove(callback);
    }

    public void registIconEventCallback(EventCallback callback) {
        mIconEventCallbackList.add(callback);
    }

    public void unRegistIconEventCallback(EventCallback callback) {
        mIconEventCallbackList.remove(callback);
    }

    public void registLedEventCallback(EventCallback callback) {
        mLedEventCallbackList.add(callback);
    }

    public void unRegistLedEventCallback(EventCallback callback) {
        mLedEventCallbackList.remove(callback);
    }

    public void registTtsEventCallback(EventCallback callback) {
        mTtsEventCallbackList.add(callback);
    }

    public void unRegistTtsEventCallback(EventCallback callback) {
        mTtsEventCallbackList.remove(callback);
    }

    public void handOutEventInfo(int eventType) {
        long timeStamp = System.currentTimeMillis();
        EventInfo eventInfo = EventManager.getInstance().getEventInfoByEventType(eventType);
        if(checkEventInfo(eventInfo,eventType,timeStamp)){
            handOutEventInfo(eventInfo, timeStamp);
        }
    }

    public boolean checkEventInfo(EventInfo eventInfo, int eventType ,long timeStamp){
        if (eventInfo == null) {
            Logg.e(LOG_TAG, "handOutEventInfo: can't find EventInfo, eventType=" + eventType);
            return false;
        }
        if (GlobalLogic.getInstance().isStartSwitchUser() || GlobalLogic.getInstance().isECallNotAllow()) {
            if (eventInfo.isSupportLed()) {
                for (EventCallback eventCallback : mLedEventCallbackList)
                    eventCallback.onEvent(eventInfo, timeStamp);
            }
            return false;
        }
        if (!DrivingSupportAlertSettingOnOffCheck.checkDrivingSupportAlertSettingOnOff(eventType)) {
            Logg.d(LOG_TAG, "checkDrivingSupportAlertSettingOnOff, eventType=" + eventType + " off");
            return false;
        }
        return true;
    }

    public void handOutEventInfo(EventInfo eventInfo, long timeStamp) {
        Logg.d(LOG_TAG, "handOutEventInfo: id=" + eventInfo.getId() + ", eventType=" + eventInfo.getEventType() + ", eventName=" + eventInfo.getEventName());
        if (eventInfo.isSupportPopUp()) {
            for (EventCallback eventCallback : mPopUpEventCallbackList)
                eventCallback.onEvent(eventInfo, timeStamp);
        }
        if (eventInfo.isSupportIcon()) {
            for (EventCallback eventCallback : mIconEventCallbackList)
                eventCallback.onEvent(eventInfo, timeStamp);
        }
        if (eventInfo.isSupportLed()) {
            for (EventCallback eventCallback : mLedEventCallbackList)
                eventCallback.onEvent(eventInfo, timeStamp);
        }
        if (eventInfo.isSupportSpeech()) {
            for (EventCallback eventCallback : mTtsEventCallbackList)
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

    public interface EventCallback {
        void onEvent(EventInfo eventInfo, long timeStamp);
//        void onCommunication(EventInfo eventInfo, int oos, String response);
    }

}