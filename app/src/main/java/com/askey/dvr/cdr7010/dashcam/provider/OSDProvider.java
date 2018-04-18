package com.askey.dvr.cdr7010.dashcam.provider;

import com.askey.dvr.cdr7010.dashcam.logic.GlobalLogic;
import com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum;

public class OSDProvider{
    private static final String TAG = OSDProvider.class.getSimpleName();

    public UIElementStatusEnum.RecordingStatusType getRecordingStatus(){
        return GlobalLogic.getInstance().getRecordingStatus();
    }
    public UIElementStatusEnum.MICStatusType getMicStatus(){
        return GlobalLogic.getInstance().getMicStatus();
    }


}