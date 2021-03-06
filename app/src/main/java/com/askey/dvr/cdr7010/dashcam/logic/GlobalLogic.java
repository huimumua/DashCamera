package com.askey.dvr.cdr7010.dashcam.logic;

import android.content.ContentResolver;
import android.provider.Settings;

import com.askey.dvr.cdr7010.dashcam.application.DashCamApplication;
import com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum;

import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.EventRecordingLimitStatusType.EVENT_RECORDING_UNREACHABLE_LIMIT_CONDITION;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.FOTAFileStatus.FOTA_FILE_NOT_EXIST;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.GPSStatusType.GPS_STRENGTH_NONE;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.LTEStatusType.LTE_NONE;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.MICStatusType.MIC_ON;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.ParkingRecordingLimitStatusType.PARKING_RECORDING_UNREACHABLE_LIMIT_CONDITION;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.RecordingStatusType.RECORDING_UNKNOWN;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_REMOVED;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SecondCameraStatusType.DISCONNECTED;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SimCardStatus.SIM_STATE_ABSENT;

public class GlobalLogic {
    private static GlobalLogic globalLogic;
    private boolean isInRecording;
    private String userInfo;
    private volatile boolean isStartSwitchUser = false;
    private boolean isECallAllow = true;
    private volatile boolean isFirstUserChange = true;
    private UIElementStatusEnum.RecordingStatusType recordingStatus = RECORDING_UNKNOWN;
    private UIElementStatusEnum.MICStatusType micStatusType = MIC_ON;
    private UIElementStatusEnum.LTEStatusType lteStatus = LTE_NONE;
    private UIElementStatusEnum.EventRecordingLimitStatusType eventRecordingLimitStatus = EVENT_RECORDING_UNREACHABLE_LIMIT_CONDITION;
    private UIElementStatusEnum.ParkingRecordingLimitStatusType parkingRecordingLimitStatus = PARKING_RECORDING_UNREACHABLE_LIMIT_CONDITION;
    private UIElementStatusEnum.SecondCameraStatusType secondCameraStatus = DISCONNECTED;
    private UIElementStatusEnum.GPSStatusType gpsStatus = GPS_STRENGTH_NONE;
    private UIElementStatusEnum.SDcardStatusType sdCardStatus = SDCARD_REMOVED;
    private UIElementStatusEnum.FOTAFileStatus fotaFileStatus = FOTA_FILE_NOT_EXIST;
    private UIElementStatusEnum.SimCardStatus simCardStatus = SIM_STATE_ABSENT;
    private UIElementStatusEnum.SDcardStatusType sdCardCurrentStatus = SDCARD_REMOVED;
    private ContentResolver contentResolver = DashCamApplication.getAppContext().getContentResolver();

    public static GlobalLogic getInstance() {
        if (globalLogic == null) {
            synchronized (GlobalLogic.class) {
                if (globalLogic == null) {
                    globalLogic = new GlobalLogic();
                }
            }
        }
        return globalLogic;
    }

    public void setRecordingStatus(UIElementStatusEnum.RecordingStatusType recordingStatus) {
        this.recordingStatus = recordingStatus;
    }

    public UIElementStatusEnum.RecordingStatusType getRecordingStatus() {
        return recordingStatus;
    }

    public void setMicStatus(UIElementStatusEnum.MICStatusType micStatusType) {
        this.micStatusType = micStatusType;
    }

    public UIElementStatusEnum.MICStatusType getMicStatus() {
        return micStatusType;
    }

    public void setLTEStatus(UIElementStatusEnum.LTEStatusType lteStatus) {
        this.lteStatus = lteStatus;
    }

    public UIElementStatusEnum.LTEStatusType getLTEStatus() {
        return lteStatus;
    }

    public void setEventRecordingLimitStatus(UIElementStatusEnum.EventRecordingLimitStatusType eventRecordingLimitStatus) {
        this.eventRecordingLimitStatus = eventRecordingLimitStatus;
    }

    public UIElementStatusEnum.EventRecordingLimitStatusType getEventRecordingLimitStatus() {
        return eventRecordingLimitStatus;
    }

    public void setParkingRecordingLimitStatus(UIElementStatusEnum.ParkingRecordingLimitStatusType parkingRecordingLimitStatus) {
        this.parkingRecordingLimitStatus = parkingRecordingLimitStatus;
    }

    public UIElementStatusEnum.ParkingRecordingLimitStatusType getParkingRecordingLimitStatus() {
        return parkingRecordingLimitStatus;
    }

    public void setSecondCameraStatus(UIElementStatusEnum.SecondCameraStatusType secondCameraStatus) {
        this.secondCameraStatus = secondCameraStatus;
    }

    public UIElementStatusEnum.SecondCameraStatusType getSecondCameraStatus() {
        return secondCameraStatus;
    }

    public void setGPSStatus(UIElementStatusEnum.GPSStatusType gpsStatus) {
        this.gpsStatus = gpsStatus;
    }

    public UIElementStatusEnum.GPSStatusType getGpsStatus() {
        return gpsStatus;
    }

    public void setSDCardStatus(UIElementStatusEnum.SDcardStatusType sdCardStatus) {
        this.sdCardStatus = sdCardStatus;
    }

    public UIElementStatusEnum.SDcardStatusType getSDCardStatus() {
        return sdCardStatus;
    }

    public void setSDCardCurrentStatus(UIElementStatusEnum.SDcardStatusType sdCardStatus) {
        this.sdCardCurrentStatus = sdCardStatus;
    }

    public UIElementStatusEnum.SDcardStatusType getSDCardCurrentStatus() {
        return sdCardCurrentStatus;
    }

    public void setFOTAFileStatus(UIElementStatusEnum.FOTAFileStatus fotaFileStatus) {
        this.fotaFileStatus = fotaFileStatus;
    }

    public UIElementStatusEnum.FOTAFileStatus getFotaFileStatus() {
        return fotaFileStatus;
    }

    public void setSimCardStatus(UIElementStatusEnum.SimCardStatus simCardStatus) {
        this.simCardStatus = simCardStatus;
    }

    public UIElementStatusEnum.SimCardStatus getSimCardStatus() {
        return simCardStatus;
    }

    public void setUserInfo(String userInfo) {
        this.userInfo = userInfo;
    }

    public String getUserInfo() {
        return userInfo;
    }

    public boolean putInt(String key, int value) {
        return Settings.Global.putInt(contentResolver, key, value);
    }

    public int getInt(String key) {
        int value;
        try {
            value = Settings.Global.getInt(contentResolver, key);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            value = -1;
        }
        return value;
    }

    public int getInt(String key, int def) {
        int value;
        try {
            value = Settings.Global.getInt(contentResolver, key, def);
        } catch (Exception e) {
            value = def;
        }
        return value;
    }

    public boolean putString(String key, String value) {
        return Settings.Global.putString(contentResolver, key, value);
    }

    public String getString(String key, String def) {
        String value;
        try {
            value = Settings.Global.getString(contentResolver, key);
        } catch (Exception e) {
            value = def;
        }
        return value;
    }

    public void setIsInRecording(boolean isInRecording) {
        this.isInRecording = isInRecording;
    }

    public boolean isInRecording() {
        return isInRecording;
    }

    public synchronized void setStartSwitchUser(boolean startSwitchUserFlag) {
        this.isStartSwitchUser = startSwitchUserFlag;
    }

    public synchronized boolean isStartSwitchUser() {
        return isStartSwitchUser;
    }

    public boolean isECallNotAllow() {
        return !isECallAllow;
    }

    public void setECallAllow(boolean ECallAllow) {
        isECallAllow = ECallAllow;
    }
    public void setFirstUserChange(boolean isFirstUserChange){
        this.isFirstUserChange = isFirstUserChange;
    }
    public boolean isFirstUserChange(){
        return isFirstUserChange;
    }
}