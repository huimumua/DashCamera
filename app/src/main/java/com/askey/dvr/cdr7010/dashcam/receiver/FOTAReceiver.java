package com.askey.dvr.cdr7010.dashcam.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.askey.dvr.cdr7010.dashcam.domain.Event;
import com.askey.dvr.cdr7010.dashcam.domain.MessageEvent;
import com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum;
import com.askey.dvr.cdr7010.dashcam.util.EventUtil;
import com.askey.dvr.cdr7010.dashcam.util.Logg;

import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.FOTAFileStatus.FOTA_FILE_EXIST;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.FOTAFileStatus.FOTA_FILE_NOT_EXIST;

public class FOTAReceiver extends BroadcastReceiver{
    private static final String TAG = FOTAReceiver.class.getSimpleName();
    private static final String ACTION_FOTA_STATUS = "action_fota_status";
    public static final String CMD_SHOW_FOTA_FILE_EXIST ="show_fota_file_exist";
    public static final String CMD_SHOW_FOTA_FILE_NOT_EXIST = "show_fota_file_not_exist";

    @Override
    public void onReceive(Context context, Intent intent) {
        Logg.i(TAG, "onReceive: " + intent);
        String action = intent.getAction();
        String data = intent.getStringExtra("data");
        if(action.equals(ACTION_FOTA_STATUS)){
            if(CMD_SHOW_FOTA_FILE_EXIST.equals(data)){
                EventUtil.sendEvent(new MessageEvent<UIElementStatusEnum.FOTAFileStatus>(Event.EventCode.EVENT_FOTA_UPDATE, FOTA_FILE_EXIST));
            }else if(CMD_SHOW_FOTA_FILE_NOT_EXIST.equals(data)){
                EventUtil.sendEvent(new MessageEvent<UIElementStatusEnum.FOTAFileStatus>(Event.EventCode.EVENT_FOTA_UPDATE, FOTA_FILE_NOT_EXIST));
            }
        }
    }
}