package com.askey.dvr.cdr7010.dashcam.logic;

import android.content.Context;
import com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum;

import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.RecordingStatusType.RECORDING_UNKNOWN;

public class GlobalLogic{
    private static GlobalLogic globalLogic;
    private Context mContext;
    private UIElementStatusEnum.RecordingStatusType recordingStatus = RECORDING_UNKNOWN;


    public static GlobalLogic getInstance(){
        if(globalLogic == null){
            globalLogic = new GlobalLogic();
        }
        return globalLogic;
    }
    public void setRecordingStatus(UIElementStatusEnum.RecordingStatusType recordingStatus){
        this.recordingStatus = recordingStatus;
    }
    public UIElementStatusEnum.RecordingStatusType getRecordingStatus() {
        return recordingStatus;
    }

}