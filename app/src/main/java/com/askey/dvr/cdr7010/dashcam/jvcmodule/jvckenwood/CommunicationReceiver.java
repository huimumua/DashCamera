package com.askey.dvr.cdr7010.dashcam.jvcmodule.jvckenwood;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.askey.dvr.cdr7010.dashcam.domain.EventInfo;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.local.ManualUploadService;
import com.askey.dvr.cdr7010.dashcam.util.Logg;

public class CommunicationReceiver extends BroadcastReceiver{
    private static final String LOG_TAG = "CommunicationReceiver";
    private static final String ACTION_VOIP_CALL = "com.jvckenwood.communication.VOIP_CALL";
    private static final String ACTION_MANUAL_UPLOAD_COMPLETE = "com.jvckenwood.communication.MANUAL_UPLOAD_COMPLETE";
    private static final String ACTION_TRIPID_UPDATE_NOTIFICATION = "com.jvckenwood.communication.TRIPID_UPDATE_NOTIFICATION";
    private static final String ACTION_WEATHER_ALERT_RESPONSE = "com.jvckenwood.communication.WEATHER_ALERT_RESPONSE";
    private static final String ACTION_TRIPID_VERSIONUP_RESPONSE = "com.jvckenwood.communication.TRIPID_VERSIONUP_RESPONSE";
    private static final String ACTION_TRIPID_LOG_UPLOAD_RESPONSE = "com.jvckenwood.communication.TRIPID_LOG_UPLOAD_RESPONSE";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Logg.i(LOG_TAG, "onReceive: action=" + action);
        if (action.equals(ACTION_VOIP_CALL)) {
            int xxx_userID = 1000;
            MainApp.getInstance().voipInfomationRequest(xxx_userID, 2);

        } else if (action.equals(ACTION_MANUAL_UPLOAD_COMPLETE)) {
            int userCancel = intent.getIntExtra("userCancel", -1);
            String response = intent.getStringExtra("response");
            ManualUploadService.manualUploadComplete(userCancel, response);

        } else if (action.equals(ACTION_TRIPID_UPDATE_NOTIFICATION)) {
            //通过用户变更等取得TripID的时候通知事件检测

        } else if (action.equals(ACTION_WEATHER_ALERT_RESPONSE)) {
            // 将气象预警获取结果通知给主APP
            String response = intent.getStringExtra("response");

        } else if (action.equals(ACTION_TRIPID_VERSIONUP_RESPONSE)) {
            //通知VersionUp中取得TripID时的固件更新信息通知
            int fairmware = intent.getIntExtra("fairmware", -1);
            int voice = intent.getIntExtra("voice", -1);

        } else if (action.equals(ACTION_TRIPID_LOG_UPLOAD_RESPONSE)) {
            int logupload = intent.getIntExtra("logupload", -1);


        }

    }

    private boolean checkEventInfo(EventInfo eventInfo, int eventType){
        if(eventInfo == null){
            Logg.e(LOG_TAG, "checkEventInfo: can't find EventInfo, eventType=" + eventType);
            return false;
        }
        return true;
    }

}