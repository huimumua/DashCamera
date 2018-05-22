package com.askey.dvr.cdr7010.dashcam.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.askey.dvr.cdr7010.dashcam.application.DashCamApplication;
import com.askey.dvr.cdr7010.dashcam.util.Logg;
import com.jvckenwood.communication.ICommunication;
import com.jvckenwood.communication.ICommunicationCallback;

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
    private ICommunication mInterface;
    private final Context appContext;
    private ServiceConnection mConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Logg.d(LOG_TAG, "onServiceConnected: ");
            mInterface = ICommunication.Stub.asInterface(service);
            try {
                mInterface.startInitialSetup(mStatusChangeCB);
            } catch (RemoteException e) {
                Logg.e(LOG_TAG, "onServiceConnected: " + e.getMessage());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Logg.d(LOG_TAG, "onServiceDisconnected: ");
            mInterface = null;
        }
    };

    private Communication() {
        appContext = DashCamApplication.getAppContext();
    }

    public static Communication getInstance(){
        if(mCommunication == null)
            mCommunication = new Communication();

        return mCommunication;
    }

    public boolean bindJvcCommunicationService() {
        Context appContext = DashCamApplication.getAppContext();
        Intent bindIntent = new Intent();
        bindIntent.setAction("service.jkccomm");
        bindIntent.setPackage("com.jvckenwood.communication");
        return appContext.bindService(bindIntent, mConn, Context.BIND_AUTO_CREATE);
    }

    public void unBindJvcCommunicationService() {
        Context appContext = DashCamApplication.getAppContext();
        appContext.unbindService(mConn);
    }

    private ICommunicationCallback mStatusChangeCB = new ICommunicationCallback.Stub() {
        @Override
        public void reportInsuranceTerm(int status, String start, String end, int flag) throws RemoteException {
            Logg.d(LOG_TAG, "reportInsuranceTerm: status=" + status + ", start=" + start + ", end=" + end + ", flag=" + flag);

        }

        @Override
        public void reportDrivingReport() throws RemoteException {
            //id 52, 運転レポート Driving report, eventType define 101
            Logg.d(LOG_TAG, "reportDrivingReport: ");
            int eventType = 101;
            EventManager.getInstance().handOutEventInfo(eventType);
        }

        @Override
        public void reportManthlyDrivingReport() throws RemoteException {
            //id 53, 月間運転レポート Monthly driving report, eventType define 102
            Logg.d(LOG_TAG, "reportManthlyDrivingReport: ");
            int eventType = 102;
            EventManager.getInstance().handOutEventInfo(eventType);
        }

        @Override
        public void reportServerNotifocation() throws RemoteException {
            //id 51, お知らせ Notice, eventType define 100
            Logg.d(LOG_TAG, "reportServerNotifocation: ");
            int eventType = 100;
            EventManager.getInstance().handOutEventInfo(eventType);
        }

        @Override
        public void reportDrivingAdvice() throws RemoteException {
            //id 54, 運転前アドバイス Advice before driving, eventType define 103
            Logg.d(LOG_TAG, "reportDrivingAdvice: ");
            int eventType = 103;
            EventManager.getInstance().handOutEventInfo(eventType);
        }

        @Override
        public void reportRecorderSettings(int status, int volume, long settings) throws RemoteException {
            Logg.d(LOG_TAG, "reportRecorderSettings: status=" + status + ", volume=" + volume + ", settings=" + settings);

        }

        @Override
        public void reportTxEventProgress(int progress, int total) throws RemoteException {
            Logg.d(LOG_TAG, "reportTxEventProgress: progress=" + progress + ", total=" + total);

        }

        @Override
        public void reportRealTimeAlert(int alertID) throws RemoteException {
            Logg.d(LOG_TAG, "reportRealTimeAlert: alertID=" + alertID);
            int eventType = -1;
            if(alertID == 1){
                eventType = 11;
            }else if(alertID == 2){
                eventType = 14;
            }else if(alertID == 3){
                eventType = 15;
            }else if(alertID == 4){
                eventType = 12;
            }else if(alertID == 5){
                eventType = 13;
            }else if(alertID == 20){
                eventType = 16;
            }else if(alertID == 21){
                eventType = 17;
            }else if(alertID == 22){
                eventType = 18;
            }else if(alertID == 23){
                eventType = 19;
            }else if(alertID == 24){
                eventType = 20;
            }else if(alertID == 25){
                eventType = 21;
            }else if(alertID == 26){
                eventType = 22;
            }

            EventManager.getInstance().handOutEventInfo(eventType);
        }

        @Override
        public void reportTripDataResult(int status) throws RemoteException {
            Logg.d(LOG_TAG, "reportTripDataResult: status=" + status);

        }

        @Override
        public void reportEventDataResult(int status) throws RemoteException {
            Logg.d(LOG_TAG, "reportEventDataResult: status=" + status);

        }

        @Override
        public void reportVersionUpInformation() throws RemoteException {
            Logg.d(LOG_TAG, "reportVersionUpInformation: ");

        }

        @Override
        public void reportVersionUpData() throws RemoteException {
            Logg.d(LOG_TAG, "reportVersionUpData: ");

        }
    };

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
        appContext.sendBroadcast(intent);
    }


}
