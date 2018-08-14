package com.askey.dvr.cdr7010.dashcam.jvcmodule.jvckenwood;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.askey.dvr.cdr7010.dashcam.domain.Event;
import com.askey.dvr.cdr7010.dashcam.domain.EventInfo;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.local.EcallService;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.local.EcallUtils;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.local.JvcLogUploadTask;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.local.ManualUploadService;
import com.askey.dvr.cdr7010.dashcam.service.EventManager;
import com.askey.dvr.cdr7010.dashcam.util.Logg;

import org.json.JSONException;
import org.json.JSONObject;

public class CommunicationReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = "CommunicationReceiver";
    private static final String ACTION_VOIP_CALL = "com.jvckenwood.communication.VOIP_CALL";
    private static final String ACTION_VOIP_INFORMATION_RESULT = "com.jvckenwood.communication.VOIP_INFORMATION_RESULT";
    private static final String ACTION_MANUAL_UPLOAD_COMPLETE = "com.jvckenwood.communication.MANUAL_UPLOAD_COMPLETE";
    private static final String ACTION_WEATHER_ALERT_RESPONSE = "com.jvckenwood.communication.WEATHER_ALERT_RESPONSE";
    private static final String ACTION_TRIPID_LOG_UPLOAD_RESPONSE = "com.jvckenwood.communication.TRIPID_LOG_UPLOAD_RESPONSE";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Logg.i(LOG_TAG, "onReceive: action=" + action);
        if (ACTION_VOIP_CALL.equals(action)) {
            /*
             * 1:事故発生画面表示指示
             * 2:VoIP指示
             */
            int order = intent.getIntExtra("order", -1);
            EcallUtils.startVoipActivity(context, order);
        } else if (ACTION_VOIP_INFORMATION_RESULT.equals(action)) {
            int requestID = intent.getIntExtra("requestID", -1);
            int status = intent.getIntExtra("status", -1);
            long impactId = intent.getLongExtra("impactId", -1);
            String policyNo = intent.getStringExtra("policyNo");
            int policyBranchNo = intent.getIntExtra("policyBranchNo", -1);
            String authUserName = intent.getStringExtra("authUserName");
            String displayName = intent.getStringExtra("displayName");
            String outboundProxy = intent.getStringExtra("outboundProxy");
            String password = intent.getStringExtra("password");
            int port = intent.getIntExtra("port", -1);
            String protocol = intent.getStringExtra("protocol");
            boolean isSendKeepAlive = intent.getBooleanExtra("isSendKeepAlive", false);
            String profileName = intent.getStringExtra("profileName");
            boolean isAutoRegistration = intent.getBooleanExtra("isAutoRegistration", false);

            EcallService.onVoipInformationResult(requestID, status, impactId, policyNo, policyBranchNo, authUserName, displayName,
                    outboundProxy, password, port, protocol, isSendKeepAlive, profileName, isAutoRegistration);
        } else if (ACTION_MANUAL_UPLOAD_COMPLETE.equals(action)) {
            int result = intent.getIntExtra("result", -2);
            String response = intent.getStringExtra("response");
            ManualUploadService.manualUploadComplete(result, response);
        } else if (ACTION_WEATHER_ALERT_RESPONSE.equals(action)) {
            // 将气象预警获取结果通知给主APP
            String response = intent.getStringExtra("response");
            speakWeather(response);
        } else if (ACTION_TRIPID_LOG_UPLOAD_RESPONSE.equals(action)) {
            int logupload = intent.getIntExtra("logupload", -1); // 0:通知なし、1:通知あり
            if (logupload == 1) {
                new JvcLogUploadTask().execute();
            }
        }
    }

    private boolean checkEventInfo(EventInfo eventInfo, int eventType) {
        if (eventInfo == null) {
            Logg.e(LOG_TAG, "checkEventInfo: can't find EventInfo, eventType=" + eventType);
            return false;
        }
        return true;
    }

    private void speakWeather(String response) {
        try {
            Logg.d(LOG_TAG, "speakWeather: response=" + response);
            JSONObject jsonObject = new JSONObject(response);
            int status = jsonObject.getInt("status");
            if (status == 0) {
                String code = jsonObject.getString("code");
                if (code != null && !code.equals("00")) {
                    int codeInteger = Integer.parseInt(code, 16);
                    Logg.d(LOG_TAG, "speakWeather: codeInteger=" + codeInteger);
                    if (Event.contains(Event.weatherWarning, codeInteger)) {
                        EventManager.getInstance().getEventInfoByEventType(Event.WEATHER_ALERT).setVoiceGuidence(code);
                        EventManager.getInstance().handOutEventInfo(Event.WEATHER_ALERT);
                    } else if (Event.contains(Event.specialWeatherWarning, codeInteger)) {
                        EventManager.getInstance().getEventInfoByEventType(Event.WEATHER_ALERT_SPECIAL).setVoiceGuidence(code);
                        EventManager.getInstance().handOutEventInfo(Event.WEATHER_ALERT_SPECIAL);
                    }
                }
            }
        } catch (JSONException e) {
            Logg.e(LOG_TAG, "speakWeather: " + e.getMessage());
        }
    }
}