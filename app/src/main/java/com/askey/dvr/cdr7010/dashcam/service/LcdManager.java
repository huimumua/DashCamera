package com.askey.dvr.cdr7010.dashcam.service;

import com.askey.platform.AskeyLcdThermalManager;

public class LcdManager{
    private volatile static LcdManager instance;
    private AskeyLcdThermalManager askeyLcdThermalManager;
    private LcdManager(){
        askeyLcdThermalManager = AskeyLcdThermalManager.getInstance();
    }
    public static LcdManager getInstance(){
        if (instance == null) {
            synchronized (LcdManager.class) {
                if (instance == null) {
                    instance = new LcdManager();
                }
            }
        }
        return instance;
    }
    public void setLcdLightStatus(boolean on){
        if(on) {
            this.askeyLcdThermalManager.setLcdLightOn();
        }else{
            this.askeyLcdThermalManager.setLcdLightOff();
        }
    }
    public void setLcdLightFlagStatus(boolean on){
        if(on){
            this.askeyLcdThermalManager.setLcdLightFlagOn();
        }else{
            this.askeyLcdThermalManager.setLcdLightFlagOff();
        }
    }

}