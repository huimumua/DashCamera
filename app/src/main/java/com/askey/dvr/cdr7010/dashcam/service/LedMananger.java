package com.askey.dvr.cdr7010.dashcam.service;

import com.askey.dvr.cdr7010.dashcam.logic.GlobalLogic;
import com.askey.platform.AskeyLedManager;

public class LedMananger{
    private static LedMananger instance;
    private AskeyLedManager askeyLedManager;
    private int lastPriority = Integer.MAX_VALUE;
    private boolean isLedOff = true;

    private LedMananger(){
        askeyLedManager = AskeyLedManager.getInstance();
    }
    public static LedMananger getInstance() {
        if(instance == null)
            instance = new LedMananger();
        return instance;
    }
    public void setLedRecStatus(boolean isNormal , boolean isInRecording ,int priority){
        if ((priority <= lastPriority && !isLedOff) || isLedOff) {
            if(isInRecording && isNormal){
                isLedOff = false;
                this.askeyLedManager.setLedOn(AskeyLedManager.LIGHT_ID_REC, AskeyLedManager.COLOR_BLUE);
            }else if(!isInRecording && isNormal){
                isLedOff = true;
                this.askeyLedManager.setLedOff(AskeyLedManager.LIGHT_ID_REC);
            }else if(!isNormal){
                isLedOff = false;
                this.askeyLedManager.setFlashing(AskeyLedManager.LIGHT_ID_REC, AskeyLedManager.COLOR_RED, 1000, 1000);
            }
            lastPriority = priority;
        }
    }

    public void setLedMicStatus(boolean on) {
        if(on){
            this.askeyLedManager.setLedOn(AskeyLedManager.LIGHT_ID_MIC, AskeyLedManager.COLOR_GREEN);
        }else {
            this.askeyLedManager.setLedOff(AskeyLedManager.LIGHT_ID_MIC);
        }
    }
}