package com.askey.dvr.cdr7010.dashcam.jvcmodule.jvckenwood;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.askey.dvr.cdr7010.dashcam.application.DashCamApplication;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.local.JvcEventHandoutInfo;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.local.JvcStatusParams;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.local.LocalJvcStatusManager;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.local.ManualUploadService;
import com.askey.dvr.cdr7010.dashcam.util.EventUtil;
import com.askey.dvr.cdr7010.dashcam.util.Logg;
import com.jvckenwood.communication.IMainApp;
import com.jvckenwood.communication.IMainAppCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.EnumMap;

/***
 * Company: Chengdu Skysoft Info&Tech Co., Ltd.
 * Copyright ©2014-2018 Chengdu Skysoft Info&Tech Co., Ltd.
 * Created by Mark on 2018/5/16.

 * @since:JDK1.6
 * @version:1.0
 * @see
 ***/
public class MainApp {
//    public static final String ACTION_xxx_REQUEST = "xxx";

    private static final String LOG_TAG = "MainApp";
    private static MainApp mMainApp;
    private IMainApp mMainAppInterface;
    private final Context mAppContext;

    private MainApp() {
        mAppContext = DashCamApplication.getAppContext();
    }

    public static MainApp getInstance(){
        if(mMainApp == null)
            mMainApp = new MainApp();

        return mMainApp;
    }

    private ServiceConnection mMainAppConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Logg.d(LOG_TAG, "onServiceConnected: MainApp");
            mMainAppInterface = IMainApp.Stub.asInterface(service);
            registerCallback(mMainAppCallbackCallback);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Logg.d(LOG_TAG, "onServiceDisconnected: MainApp");
            mMainAppInterface = null;
        }
    };

    private IMainAppCallback.Stub mMainAppCallbackCallback = new IMainAppCallback.Stub() {
        @Override
        public void reportInsuranceTerm(int oos, String response) {
            Logg.d(LOG_TAG, "reportInsuranceTerm: oos=" + oos + ", response=" + response);

            EnumMap<JvcStatusParams.JvcStatusParam, Object> enumMap = new EnumMap<>(JvcStatusParams.JvcStatusParam.class);
            enumMap.put(JvcStatusParams.JvcStatusParam.OOS, oos);
            enumMap.put(JvcStatusParams.JvcStatusParam.RESPONSE, response);
            LocalJvcStatusManager.setInsuranceTerm(enumMap);
        }

        @Override
        public void reportUserList(int oos, int defaultUser, int selectUser, int userList, String response) {
            Logg.d(LOG_TAG, "reportUserList: oos=" + oos + ", response=" + response);

        }

        @Override
        public void reportSystemSettings(int oos, String response) {
            Logg.d(LOG_TAG, "reportSystemSettings: oos=" + oos + ", response=" + response);


        }

        @Override
        public void reportUserSettings(int oos, String response) {
            Logg.d(LOG_TAG, "reportUserSettings: oos=" + oos + ", response=" + response);


        }

        @Override
        public void reportSettingsUpdate(int oos, String response) {
            Logg.d(LOG_TAG, "reportSettingsUpdate: oos=" + oos + ", response=" + response);


        }

        @Override
        public void reportDrivingReport(int oos, String response) {
            Logg.d(LOG_TAG, "reportDrivingReport: oos=" + oos + ", response=" + response);
            //id 52, 運転レポート Driving report, eventType define 101
            int eventType = 101;
            JvcEventHandoutInfo info = new JvcEventHandoutInfo(eventType);
            info.setOos(oos);
            if(oos == 0){
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int status = jsonObject.getInt("status");
                    if(status == 0){
                        int result = jsonObject.getInt("result");
                        info.setResult(result);
                    }
                } catch (JSONException e) {
                    Logg.e(LOG_TAG, "reportDrivingReport: error: " + e.getMessage());
                }
            }
            EventUtil.sendEvent(info);
        }

        @Override
        public void reportManthlyDrivingReport(int oos, String response) {
            Logg.d(LOG_TAG, "reportManthlyDrivingReport: oos=" + oos + ", response=" + response);
            //id 53, 月間運転レポート Monthly driving report, eventType define 102
            int eventType = 102;
            JvcEventHandoutInfo info = new JvcEventHandoutInfo(eventType);
            info.setOos(oos);
            if(oos == 0){
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int status = jsonObject.getInt("status");
                    if(status == 0){
                        int result = jsonObject.getInt("result");
                        info.setResult(result);
                    }
                } catch (JSONException e) {
                    Logg.e(LOG_TAG, "reportManthlyDrivingReport: error: " + e.getMessage());
                }
            }
            EventUtil.sendEvent(info);
        }

        @Override
        public void reportServerNotifocation(int oos, String response) {
            Logg.d(LOG_TAG, "reportServerNotifocation: oos=" + oos + ", response=" + response);
            //id 51, お知らせ Notice, eventType define 100
            int eventType = 100;
            JvcEventHandoutInfo info = new JvcEventHandoutInfo(eventType);
            info.setOos(oos);
            if(oos == 0){
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int status = jsonObject.getInt("status");
                    if(status == 0){
                        int type = jsonObject.getInt("type");
                        info.setType(type);
                    }
                } catch (JSONException e) {
                    Logg.e(LOG_TAG, "reportServerNotifocation: error: " + e.getMessage());
                }
            }
            EventUtil.sendEvent(info);
        }

        @Override
        public void reportDrivingAdvice(int oos, String response) {
            Logg.d(LOG_TAG, "reportDrivingAdvice: oos=" + oos + ", response=" + response);
            //id 54, 運転前アドバイス Advice before driving, eventType define 103
            int eventType = 103;
            JvcEventHandoutInfo info = new JvcEventHandoutInfo(eventType);
            info.setOos(oos);
            if(oos == 0){
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int status = jsonObject.getInt("status");
                    if(status == 0){
                        int code = jsonObject.getInt("code");
                        info.setCode(code);
                    }
                } catch (JSONException e) {
                    Logg.e(LOG_TAG, "reportDrivingAdvice: error: " + e.getMessage());
                }
            }
            EventUtil.sendEvent(info);
        }

        @Override
        public void reportTxEventProgress(int eventNo, int mainPicture, int mainMovie, int secondPicture, int secondMovie) {
            /**
             * MainApp无需应对
             */
            Logg.d(LOG_TAG, "reportTxEventProgress: eventNo=" + eventNo);
        }

        @Override
        public void reportTxManualProgress(int progress1, int total1, int progress2, int total2) {
            Logg.d(LOG_TAG, "reportTxManualProgress: progress1=" + progress1 + ", total1=" + total1 + ", progress2=" + progress2 + ", total2=" + total2);
            ManualUploadService.reportTxManualProgress(progress1 ,total1, progress2, total2);
        }

        @Override
        public void logUploadResult(int oos, String result) {
            Logg.d(LOG_TAG, "logUploadResult: oos=" + oos + ", result=" + result);


        }
    };

    public boolean bindJvcMainAppService() {
        Logg.d(LOG_TAG, "bindJvcMainAppService: ");
        Intent intent = new Intent();
//        intent.setAction("service.jkccomm.IMainApp");
        intent.setAction("com.jvckenwood.communication.CommunicationService");
        intent.setPackage("com.jvckenwood.communication");
        return mAppContext.bindService(intent, mMainAppConn, Context.BIND_AUTO_CREATE);
    }

    public void unBindJvcMainAppService() {
        Logg.d(LOG_TAG, "unBindJvcMainAppService: ");
        unregisterCallback(mMainAppCallbackCallback);
        mAppContext.unbindService(mMainAppConn);
    }

    /**
     * **********************************************
     * ********************** API ****************************
     * **********************************************
     */
    public void registerCallback(IMainAppCallback callback){
        Logg.d(LOG_TAG, "registerCallback: ");
        if(!checkConnection())
            return;

        try {
            mMainAppInterface.registerCallback(callback);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void unregisterCallback(IMainAppCallback callback){
        Logg.d(LOG_TAG, "unregisterCallback: ");
        if(!checkConnection())
            return;

        try {
            mMainAppInterface.unregisterCallback(callback);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void settingsUpdateRequest(String setings){
        Logg.d(LOG_TAG, "settingsUpdateRequest: setings=" + setings);
        if(!checkConnection())
            return;

        try {
            mMainAppInterface.settingsUpdateRequest(setings);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param cancel
     *      0:中断
     *      1:再開
     *      2:キャンセル
     */
    public void manualUploadCancel(int cancel){
        Logg.d(LOG_TAG, "manualUploadCancel: cancel=" + cancel);
        if(!checkConnection())
            return;

        try {
            mMainAppInterface.manualUploadCancel(cancel);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void logUpload(String zipPath){
        Logg.d(LOG_TAG, "logUpload: zipPath=" + zipPath);
        if(!checkConnection())
            return;

        try {
            mMainAppInterface.logUpload(zipPath);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    private boolean checkConnection(){
        if(mMainAppInterface == null){
            Logg.e(LOG_TAG, "checkConnection: service not connected.");
            return false;
        }

        return true;
    }

}
