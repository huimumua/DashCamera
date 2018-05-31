package com.askey.dvr.cdr7010.dashcam.domain;

import java.util.ArrayList;
import java.util.List;

public class EventList{
    private EventItem eventItem;
    private List eventInfoList;
    public EventList(){
        eventInfoList = new ArrayList();
    }
    public boolean add(int dialogType,int eventType,int priority,long beginTime,String message){
        EventItem eventItem = new EventItem();
        eventItem.dialogType = dialogType;
        eventItem.eventType =eventType;
        eventItem.priority = priority;
        eventItem.beginTime = beginTime;
        eventItem.message =message;
        return eventInfoList.add(eventItem);
    }
    public EventItem getEventItem(int eventType) {
        if (eventInfoList != null || eventInfoList.size() != 0) {
            for (int idx = 0; idx < eventInfoList.size(); idx++) {
                if(eventType == ((EventItem)eventInfoList.get(idx)).eventType ){
                    return (EventItem)eventInfoList.get(idx);
                }
            }
        }
        return null;
    }
    public boolean contains(int eventType) {
        EventItem eventItem = null;
        if (eventInfoList != null || eventInfoList.size() != 0) {
            for (int idx = 0; idx < eventInfoList.size(); idx++) {
                if (eventType == ((EventItem) eventInfoList.get(idx)).eventType) {
                    eventItem = (EventItem) eventInfoList.get(idx);
                }
            }
        }
        return eventItem == null ? false : true;
    }
    public void clear(){
        if (eventInfoList != null || eventInfoList.size() != 0) {
            eventInfoList.clear();
        }
    }
    public EventItem remove(int eventType){
        if (eventInfoList != null || eventInfoList.size() != 0) {
            for (int idx = 0; idx < eventInfoList.size(); idx++) {
                if (eventType == ((EventItem) eventInfoList.get(idx)).eventType) {
                    return (EventItem)eventInfoList.remove(idx);
                }
            }
        }
        return null;
    }
    public EventItem getNextEventItem(){
        long time =-1;
        int priority = Integer.MAX_VALUE;
        EventItem eventItem = null;
        if (eventInfoList != null || eventInfoList.size() != 0) {
            for (int idx = 0; idx < eventInfoList.size(); idx++) {
                if(((EventItem) eventInfoList.get(idx)).priority < priority ){
                    priority = ((EventItem) eventInfoList.get(idx)).priority;
                    time = ((EventItem) eventInfoList.get(idx)).beginTime;
                    eventItem = (EventItem) eventInfoList.get(idx);
                }else if(((EventItem) eventInfoList.get(idx)).priority == priority){
                    if(((EventItem) eventInfoList.get(idx)).beginTime > time){
                        time = ((EventItem) eventInfoList.get(idx)).beginTime;
                        eventItem = (EventItem) eventInfoList.get(idx);
                    }
                }
            }
        }
        return eventItem;
    }
}