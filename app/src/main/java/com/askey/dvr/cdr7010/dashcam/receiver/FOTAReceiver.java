package com.askey.dvr.cdr7010.dashcam.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.askey.dvr.cdr7010.dashcam.domain.Event;
import com.askey.dvr.cdr7010.dashcam.domain.EventInfo;
import com.askey.dvr.cdr7010.dashcam.domain.MessageEvent;
import com.askey.dvr.cdr7010.dashcam.service.EventManager;
import com.askey.dvr.cdr7010.dashcam.service.TTSManager;
import com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum;
import com.askey.dvr.cdr7010.dashcam.util.EventUtil;
import com.askey.dvr.cdr7010.dashcam.util.Logg;

import static com.askey.dvr.cdr7010.dashcam.domain.Event.NOTICE_START;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.FOTAFileStatus.FOTA_FILE_EXIST;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.FOTAFileStatus.FOTA_FILE_NOT_EXIST;

public class FOTAReceiver extends BroadcastReceiver{
    private static final String TAG = FOTAReceiver.class.getSimpleName();
    private static final String ACTION_FOTA_STATUS = "action_fota_status";
    private static final int UPDOWNLOAD_SUCCESS =0;

    @Override
    public void onReceive(Context context, Intent intent) {
        Logg.i(TAG, "onReceive: " + intent);
        String action = intent.getAction();
        if(action.equals(ACTION_FOTA_STATUS)){
            int status = intent.getIntExtra("status",-1);
            if(status == UPDOWNLOAD_SUCCESS){
                EventUtil.sendEvent(new MessageEvent<UIElementStatusEnum.FOTAFileStatus>(Event.EventCode.EVENT_FOTA_UPDATE, FOTA_FILE_EXIST));
                EventManager.getInstance().handOutEventInfo(Event.EVENT_DOWNLOAD_RESULT);
            }
        }
    }
}