package com.askey.dvr.cdr7010.dashcam.provider;


import com.askey.dvr.cdr7010.dashcam.logic.GlobalLogic;
import com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum;

public class OSDProvider {

    public UIElementStatusEnum.RecordingStatusType getRecordingStatus() {
        return GlobalLogic.getInstance().getRecordingStatus();
    }

    public UIElementStatusEnum.MICStatusType getMicStatus() {
        return GlobalLogic.getInstance().getMicStatus();
    }

    public UIElementStatusEnum.LTEStatusType getLTEStatus() {
        return GlobalLogic.getInstance().getLTEStatus();
    }

    public UIElementStatusEnum.ParkingRecordingLimitStatusType getParkingRecordingLimitStatus() {
        return GlobalLogic.getInstance().getParkingRecordingLimitStatus();
    }

    public UIElementStatusEnum.EventRecordingLimitStatusType getEventRecordingLimitStatus() {
        return GlobalLogic.getInstance().getEventRecordingLimitStatus();
    }

    public UIElementStatusEnum.SecondCameraStatusType getSecondCameraStatus() {
        return GlobalLogic.getInstance().getSecondCameraStatus();
    }

    public UIElementStatusEnum.GPSStatusType getGpsStatus() {
        return GlobalLogic.getInstance().getGpsStatus();
    }

    public UIElementStatusEnum.SDcardStatusType getSDCardStatus() {
        return GlobalLogic.getInstance().getSDCardStatus();
    }

    public UIElementStatusEnum.FOTAFileStatus getFotaFileStatus() {
        return GlobalLogic.getInstance().getFotaFileStatus();
    }

    public UIElementStatusEnum.SimCardStatus getSimCardStatus() {
        return GlobalLogic.getInstance().getSimCardStatus();
    }

    public String getUserInfo() {
        return GlobalLogic.getInstance().getUserInfo();
    }
}