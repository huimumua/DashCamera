package com.askey.dvr.cdr7010.dashcam.service;

import android.content.res.AssetManager;

import com.askey.dvr.cdr7010.dashcam.application.DashCamApplication;
import com.askey.dvr.cdr7010.dashcam.domain.EventInfo;
import com.askey.dvr.cdr7010.dashcam.parser.sax.GetEventInfoSAXParser;
import com.askey.dvr.cdr7010.dashcam.util.Logg;

import java.io.InputStream;
import java.util.ArrayList;

public class EventManager{
private static EventManager instance;
private AssetManager assets;
private GetEventInfoSAXParser eventInfoSAXParser;
private InputStream inputStream;
private ArrayList<EventInfo> eventInfoArrayList;
private EventManager(){
    assets = DashCamApplication.getAppContext().getAssets();
}
public static EventManager getInstance(){
    if (instance == null) {
        synchronized (TTSManager.class) {
            if (instance == null) {
                instance = new EventManager();
            }
        }
    }
    return instance;
}
public void loadXML(String language){
    try {
        inputStream = assets.open("eventlist_en.xml");
    }catch (Exception e){
        e.printStackTrace();
    }
    eventInfoSAXParser = new GetEventInfoSAXParser();
    eventInfoArrayList =  eventInfoSAXParser.parser(inputStream);
    Logg.d("EventManager","eventInfoArrayList size="+eventInfoArrayList.size()+",last="+eventInfoArrayList.get(57));
    Logg.d("EventManager","eventInfoArrayList="+eventInfoArrayList+",eventInfoArrayList size="+eventInfoArrayList.size());
}
}