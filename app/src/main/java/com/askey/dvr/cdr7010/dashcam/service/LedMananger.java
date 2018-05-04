package com.askey.dvr.cdr7010.dashcam.service;

import com.askey.dvr.cdr7010.dashcam.logic.GlobalLogic;
import com.askey.platform.AskeyLedManager;

public class LedMananger{
    private static LedMananger instance;
    private AskeyLedManager askeyLedManager;

    private LedMananger(){
        askeyLedManager = AskeyLedManager.getInstance();
    }
    public static LedMananger getInstance() {
        if(instance == null)
            instance = new LedMananger();
        return instance;
    }
    public void setLedRecStatus(boolean isNormal , boolean isInRecording){
        if(isInRecording && isNormal){
            this.askeyLedManager.setLedOn(AskeyLedManager.LIGHT_ID_UPPER, AskeyLedManager.COLOR_GREEN);
        }else if(!isInRecording && isNormal){
            this.askeyLedManager.setLedOff(AskeyLedManager.LIGHT_ID_UPPER);
        }else if(!isNormal){
            this.askeyLedManager.setFlashing(AskeyLedManager.LIGHT_ID_UPPER, AskeyLedManager.COLOR_RED, 1000, 1000);
        }
    }

    public void setLedMicStatus(boolean on) {
        if(on){
            this.askeyLedManager.setLedOn(AskeyLedManager.LIGHT_ID_BOTTOM, AskeyLedManager.COLOR_GREEN);
        }else {
            this.askeyLedManager.setLedOff(AskeyLedManager.LIGHT_ID_BOTTOM);
        }
    }
}