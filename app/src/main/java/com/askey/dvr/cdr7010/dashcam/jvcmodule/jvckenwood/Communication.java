package com.askey.dvr.cdr7010.dashcam.jvcmodule.jvckenwood;

import android.content.Context;
import android.content.Intent;

import com.askey.dvr.cdr7010.dashcam.application.DashCamApplication;
import com.askey.dvr.cdr7010.dashcam.util.Logg;

/***
 * Company: Chengdu Skysoft Info&Tech Co., Ltd.
 * Copyright ©2014-2018 Chengdu Skysoft Info&Tech Co., Ltd.
 * Created by Mark on 2018/5/16.

 * @since:JDK1.6
 * @version:1.0
 * @see
 ***/
public class Communication {
    public static final String ACTION_MANUAL_UPLOAD_REQUEST = "com.jvckenwood.communication.MANUAL_UPLOAD_REQUEST";
    public static final String ACTION_CHANGE_USERID = "com.jvckenwood.communication.CHANGE_USERID";
    public static final String ACTION_ALERT_COMPLETE = "com.jvckenwood.communication.ALERT_COMPLETE";
    public static final String ACTION_CANCEL_EMERGENCY_CALL = "com.jvckenwood.communication.CANCEL_EMERGENCY_CALL";
    public static final String ACTION_DISC_EMERGENCY_CALL = "com.jvckenwood.communication.DISC_EMERGENCY_CALL";
    public static final String ACTION_WEATHER_ALERT_REQUEST = "com.jvckenwood.communication.WEATHER_ALERT_REQUEST";

    private static final String LOG_TAG = "Communication";
    private static Communication mCommunication;
    private final Context mAppContext;

    private Communication() {
        mAppContext = DashCamApplication.getAppContext();
    }

    public static Communication getInstance(){
        if(mCommunication == null)
            mCommunication = new Communication();

        return mCommunication;
    }

    /**
     *
     * @param camType:  0:メインカメラ;   1:2ndカメラ
     * @param filePath1:
     * @param startTime1:   撮影開始日時 YYYYMMDDHHMMSS
     * @param endTime1      撮影終了日時 YYYYMMDDHHMMSS
     * @param filePath2
     * @param startTime2:   撮影開始日時 YYYYMMDDHHMMSS
     * @param endTime2:     撮影終了日時 YYYYMMDDHHMMSS
     */
    public void manualUpload(int camType, String filePath1, String startTime1, String endTime1, String filePath2, String startTime2, String endTime2){
        Logg.d(LOG_TAG, "manualUpload: filePath1=" + filePath1 + ", filePath2=" + filePath2);
        Intent intent = new Intent(ACTION_MANUAL_UPLOAD_REQUEST);
        intent.putExtra("camType", camType);
        intent.putExtra("filePath1", filePath1);
        intent.putExtra("startTime1", startTime1);
        intent.putExtra("endTime1", endTime1);
        intent.putExtra("filePath2", filePath2);
        intent.putExtra("startTime2", startTime2);
        intent.putExtra("endTime2", endTime2);
        sendOutBroadcast(intent);
    }

    public void changeUserID(int userId){
        Intent intent = new Intent(ACTION_CHANGE_USERID);
        intent.putExtra("userId", userId);
        sendOutBroadcast(intent);
    }

    public void alertComplite(int eventType){
        Intent intent = new Intent(ACTION_ALERT_COMPLETE);
        intent.putExtra("eventType", eventType);
        sendOutBroadcast(intent);
    }

    public void cancelEmergencyCall(){
        Intent intent = new Intent(ACTION_CANCEL_EMERGENCY_CALL);
        sendOutBroadcast(intent);
    }

    /**
     *
     * @param status
     *          0:不明
     *          1:正常通話完了
     *          2:キャンセル(接続NG)
     *          3:キャンセル
     */
    public void discEmergencyCall(int status){
        Intent intent = new Intent(ACTION_DISC_EMERGENCY_CALL);
        intent.putExtra("status", status);
        sendOutBroadcast(intent);
    }

    /**
     * 从主APP向通信通知气象警报的获取每5分钟实施一次
     */
    public void weatherAlertRequest(){
        Intent intent = new Intent(ACTION_WEATHER_ALERT_REQUEST);
        sendOutBroadcast(intent);
    }

    private void sendOutBroadcast(Intent intent){
        mAppContext.sendBroadcast(intent);
    }


}
