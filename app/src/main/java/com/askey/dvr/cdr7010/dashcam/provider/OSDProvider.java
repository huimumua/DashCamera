package com.askey.dvr.cdr7010.dashcam.provider;

import android.os.Environment;

import com.askey.dvr.cdr7010.dashcam.logic.GlobalLogic;
import com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum;

public class OSDProvider{
    private static final String TAG = OSDProvider.class.getSimpleName();
    private UIElementStatusEnum.SDcardStatusType sDcardStatusType;

    public UIElementStatusEnum.RecordingStatusType getRecordingStatus(){
        return GlobalLogic.getInstance().getRecordingStatus();
    }
    public UIElementStatusEnum.MICStatusType getMicStatus(){
        return GlobalLogic.getInstance().getMicStatus();
    }
    public UIElementStatusEnum.LTEStatusType getLTEStatus(){
        return GlobalLogic.getInstance().getLTEStatus();
    }
    public UIElementStatusEnum.ParkingRecordingLimitStatusType getParkingRecordingLimitStatus(){
        return GlobalLogic.getInstance().getParkingRecordingLimitStatus();
    }
    public UIElementStatusEnum.EventRecordingLimitStatusType getEventRecordingLimitStatus(){
        return GlobalLogic.getInstance().getEventRecordingLimitStatus() ;
    }
    public UIElementStatusEnum.SDcardStatusType getSDcardStatusType(){
        String status = Environment.getExternalStorageState();
        if (status.equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            sDcardStatusType = UIElementStatusEnum.SDcardStatusType.MEDIA_MOUNTED;
        } else if (status.equalsIgnoreCase(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            sDcardStatusType = UIElementStatusEnum.SDcardStatusType.MEDIA_MOUNTED_READ_ONLY;
        } else if (status.equalsIgnoreCase(Environment.MEDIA_REMOVED)) {
            sDcardStatusType = UIElementStatusEnum.SDcardStatusType.MEDIA_REMOVED;
        } else if (status.equalsIgnoreCase(Environment.MEDIA_SHARED)) {
            sDcardStatusType = UIElementStatusEnum.SDcardStatusType.MEDIA_SHARED;
        } else if (status.equalsIgnoreCase(Environment.MEDIA_BAD_REMOVAL)) {
            sDcardStatusType = UIElementStatusEnum.SDcardStatusType.MEDIA_BAD_REMOVAL;
        } else if (status.equalsIgnoreCase(Environment.MEDIA_CHECKING)) {
            sDcardStatusType = UIElementStatusEnum.SDcardStatusType.MEDIA_CHECKING;
        } else if (status.equalsIgnoreCase(Environment.MEDIA_NOFS)) {
            sDcardStatusType = UIElementStatusEnum.SDcardStatusType.MEDIA_NOFS;
        } else if (status.equalsIgnoreCase(Environment.MEDIA_UNMOUNTABLE)) {
            sDcardStatusType = UIElementStatusEnum.SDcardStatusType.MEDIA_UNMOUNTABLE;
        } else if (status.equalsIgnoreCase(Environment.MEDIA_UNMOUNTED)) {
            sDcardStatusType = UIElementStatusEnum.SDcardStatusType.MEDIA_UNMOUNTED;
        } else {
            sDcardStatusType = UIElementStatusEnum.SDcardStatusType.MEDIA_UNKNOWN;
        }
        return sDcardStatusType;
    }
    public UIElementStatusEnum.SecondCameraStatusType getSecondCameraStatus(){
        return GlobalLogic.getInstance().getSecondCameraStatus();
    }


}