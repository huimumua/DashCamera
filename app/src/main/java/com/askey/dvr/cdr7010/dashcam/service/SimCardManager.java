package com.askey.dvr.cdr7010.dashcam.service;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.askey.dvr.cdr7010.dashcam.application.DashCamApplication;
import com.askey.dvr.cdr7010.dashcam.logic.GlobalLogic;
import com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum;
import com.askey.dvr.cdr7010.dashcam.util.Logg;

import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SimCardStatus.SIM_STATE_ABSENT;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SimCardStatus.SIM_STATE_ILLEGAL;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SimCardStatus.SIM_STATE_NETWORK_LOCKED;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SimCardStatus.SIM_STATE_PIN_REQUIRED;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SimCardStatus.SIM_STATE_PUK_REQUIRED;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SimCardStatus.SIM_STATE_READY;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SimCardStatus.SIM_STATE_UNKNOWN;

public class SimCardManager {
    private static  SimCardManager instance;
    private TelephonyManager telephoneMgr;
    public static final int SIM_STATE_NOT_READY = 6;

    private SimCardManager(){
        telephoneMgr = (TelephonyManager) DashCamApplication.getAppContext().getSystemService(Context.TELEPHONY_SERVICE);
    }
    public static SimCardManager getInstant(){
        if(instance == null)
            instance = new SimCardManager();
        return instance;
    }
    public int getSimState(){
        return telephoneMgr.getSimState();
    }
    public void setSimState(int simState){
        Logg.i("SimCardManager","simState="+simState);
        switch (simState) {
            case TelephonyManager.SIM_STATE_ABSENT:
                GlobalLogic.getInstance().setSimCardStatus(SIM_STATE_ABSENT);
                break;
            case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                GlobalLogic.getInstance().setSimCardStatus(SIM_STATE_NETWORK_LOCKED);
                break;
            case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                GlobalLogic.getInstance().setSimCardStatus(SIM_STATE_PIN_REQUIRED);
                break;
            case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                GlobalLogic.getInstance().setSimCardStatus(SIM_STATE_PUK_REQUIRED);
                break;
            case TelephonyManager.SIM_STATE_UNKNOWN:
                GlobalLogic.getInstance().setSimCardStatus(SIM_STATE_UNKNOWN);
                break;
            case TelephonyManager.SIM_STATE_READY:
                GlobalLogic.getInstance().setSimCardStatus(SIM_STATE_READY);
                break;
            case SIM_STATE_NOT_READY:
                GlobalLogic.getInstance().setSimCardStatus(UIElementStatusEnum.SimCardStatus.SIM_STATE_NOT_READY);
                break;
            default:
                GlobalLogic.getInstance().setSimCardStatus(SIM_STATE_ILLEGAL);
        }
        return;
    }

}
