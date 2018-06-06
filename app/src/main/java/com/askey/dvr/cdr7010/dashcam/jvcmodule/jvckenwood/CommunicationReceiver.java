package com.askey.dvr.cdr7010.dashcam.jvcmodule.jvckenwood;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.askey.dvr.cdr7010.dashcam.R;
import com.askey.dvr.cdr7010.dashcam.domain.EventInfo;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.local.CommunicationService;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.local.ManualUploadService;
import com.askey.dvr.cdr7010.dashcam.service.TTSManager;
import com.askey.dvr.cdr7010.dashcam.util.Logg;

import org.json.JSONException;
import org.json.JSONObject;

public class CommunicationReceiver extends BroadcastReceiver{
    private static final String LOG_TAG = "CommunicationReceiver";
    private static final String ACTION_VOIP_CALL = "com.jvckenwood.communication.VOIP_CALL";
    private static final String ACTION_MANUAL_UPLOAD_COMPLETE = "com.jvckenwood.communication.MANUAL_UPLOAD_COMPLETE";
    private static final String ACTION_WEATHER_ALERT_RESPONSE = "com.jvckenwood.communication.WEATHER_ALERT_RESPONSE";
    private static final String ACTION_TRIPID_VERSIONUP_RESPONSE = "com.jvckenwood.communication.TRIPID_VERSIONUP_RESPONSE";
    private static final String ACTION_TRIPID_LOG_UPLOAD_RESPONSE = "com.jvckenwood.communication.TRIPID_LOG_UPLOAD_RESPONSE";

    public static final int WEATHER_REQUEST_ID = R.id.weather_request_id;

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

        } else if (action.equals(ACTION_WEATHER_ALERT_RESPONSE)) {
            // 将气象预警获取结果通知给主APP
            String response = intent.getStringExtra("response");
            speakWeather(response);

        } else if (action.equals(ACTION_TRIPID_VERSIONUP_RESPONSE)) { //MainApp无需应对
            //通知VersionUp中取得TripID时的固件更新信息通知
            int fairmware = intent.getIntExtra("fairmware", -1);
            int voice = intent.getIntExtra("voice", -1);
            CommunicationService.tripIdVersionUpResponse(fairmware, voice);

        } else if (action.equals(ACTION_TRIPID_LOG_UPLOAD_RESPONSE)) {
            int logupload = intent.getIntExtra("logupload", -1);
            CommunicationService.tripIdLogUploadUpResponse(logupload);

        }

    }

    private boolean checkEventInfo(EventInfo eventInfo, int eventType){
        if(eventInfo == null){
            Logg.e(LOG_TAG, "checkEventInfo: can't find EventInfo, eventType=" + eventType);
            return false;
        }
        return true;
    }

    private void speakWeather(String response){
        try {
            Logg.d(LOG_TAG, "speakWeather: response=" + response);
            JSONObject jsonObject = new JSONObject(response);
            int status = jsonObject.getInt("status");
            if(status == 0){
                String code = jsonObject.getString("code");
                if(code != null && !code.equals("00")){
                    int codeInteger = Integer.parseInt(code,16);
                    Logg.d(LOG_TAG, "speakWeather: codeInteger=" + codeInteger);
                    TTSManager.getInstance().ttsEventStart(140,0,new int[]{codeInteger});
                }
            }
        } catch (JSONException e) {
            Logg.e(LOG_TAG, "speakWeather: " + e.getMessage());
        }
    }

}