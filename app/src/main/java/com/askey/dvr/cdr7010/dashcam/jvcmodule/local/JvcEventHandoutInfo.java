package com.askey.dvr.cdr7010.dashcam.jvcmodule.local;

/***
 * Company: Chengdu Skysoft Info&Tech Co., Ltd.
 * Copyright ©2014-2018 Chengdu Skysoft Info&Tech Co., Ltd.
 * Created by Mark on 2018/6/19.

 * @since:JDK1.6
 * @version:1.0
 * @see
 ***/
public class JvcEventHandoutInfo {
    private int eventType;
    private int result;
    private int type;
    private int code;

    public JvcEventHandoutInfo(int eventType) {
        this.eventType = eventType;
    }

    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }


}
