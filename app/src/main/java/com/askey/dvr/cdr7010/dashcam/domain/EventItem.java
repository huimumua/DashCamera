package com.askey.dvr.cdr7010.dashcam.domain;
public class EventItem{
    public int dialogType = -1;
    public int priority = Integer.MAX_VALUE;
    public int eventType = -1;
    public long beginTime =0;
    public String message;

    @Override
    public String toString(){
        return "EventItem[ dialogType="+dialogType+",priority="+priority+",eventType="+ eventType +",beginTime="+beginTime
                +",message="+message +"  ]";
    }
}