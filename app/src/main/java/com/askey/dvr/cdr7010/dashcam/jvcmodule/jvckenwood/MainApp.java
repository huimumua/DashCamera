package com.askey.dvr.cdr7010.dashcam.jvcmodule.jvckenwood;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.askey.dvr.cdr7010.dashcam.application.DashCamApplication;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.local.EcallUtils;
import com.askey.dvr.cdr7010.dashcam.service.EventManager;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.local.ManualUploadService;
import com.askey.dvr.cdr7010.dashcam.util.Logg;
import com.jvckenwood.communication.IMainApp;
import com.jvckenwood.communication.IMainAppCallback;

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
        public void reportInsuranceTerm(int oos, String response) throws RemoteException {
            Logg.d(LOG_TAG, "reportInsuranceTerm: oos=" + oos + ", response=" + response);

        }

        @Override
        public void reportUserList(int oos, String response) throws RemoteException {
            Logg.d(LOG_TAG, "reportUserList: oos=" + oos + ", response=" + response);

        }

        @Override
        public void reportSystemSettings(int oos, String response) throws RemoteException {
            Logg.d(LOG_TAG, "reportSystemSettings: oos=" + oos + ", response=" + response);

        }

        @Override
        public void reportUserSettings(int oos, String response) throws RemoteException {
            Logg.d(LOG_TAG, "reportUserSettings: oos=" + oos + ", response=" + response);

        }

        @Override
        public void reportSettingsUpdate(int oos, String response) throws RemoteException {
            Logg.d(LOG_TAG, "reportSettingsUpdate: oos=" + oos + ", response=" + response);

        }

        @Override
        public void reportDrivingReport(int oos, String response) throws RemoteException {
            Logg.d(LOG_TAG, "reportDrivingReport: oos=" + oos + ", response=" + response);
            //id 52, 運転レポート Driving report, eventType define 101
            int eventType = 101;
            EventManager.getInstance().handOutEventInfo(eventType);
        }

        @Override
        public void reportManthlyDrivingReport(int oos, String response) throws RemoteException {
            Logg.d(LOG_TAG, "reportManthlyDrivingReport: oos=" + oos + ", response=" + response);
            //id 53, 月間運転レポート Monthly driving report, eventType define 102
            int eventType = 102;
            EventManager.getInstance().handOutEventInfo(eventType);
        }

        @Override
        public void reportServerNotifocation(int oos, String response) throws RemoteException {
            Logg.d(LOG_TAG, "reportServerNotifocation: oos=" + oos + ", response=" + response);
            //id 51, お知らせ Notice, eventType define 100
            int eventType = 100;
            EventManager.getInstance().handOutEventInfo(eventType);
        }

        @Override
        public void reportDrivingAdvice(int oos, String response) throws RemoteException {
            Logg.d(LOG_TAG, "reportDrivingAdvice: oos=" + oos + ", response=" + response);
            //id 54, 運転前アドバイス Advice before driving, eventType define 103
            int eventType = 103;
            EventManager.getInstance().handOutEventInfo(eventType);
        }

        @Override
        public void reportTxEventProgress(int eventNo, int progress, int total) throws RemoteException {
            Logg.d(LOG_TAG, "reportTxEventProgress: progress=" + progress + ", total=" + total);

        }

        @Override
        public void reportTxManualProgress(int progress1, int total1, int progress2, int total2) throws RemoteException {
            Logg.d(LOG_TAG, "reportTxManualProgress: progress1=" + progress1 + ", total1=" + total1 + ", progress2=" + progress2 + ", total2=" + total2);
            ManualUploadService.reportTxManualProgress(progress1 ,total1, progress2, total2);
        }

        @Override
        public void voipInfomationResponse(int oos, String response) throws RemoteException {
            Logg.d(LOG_TAG, "voipInfomationResponse: oos=" + oos + ", response=" + response);
            EcallUtils.startVoipActivity(oos, response);
        }

        @Override
        public void onFWUpdateRequest(int result) throws RemoteException {
            Logg.d(LOG_TAG, "onFWUpdateRequest: result=" + result);

        }
    };

    public boolean bindJvcMainAppService() {
        Intent intent = new Intent();
        intent.setAction("service.jkccomm.IMainApp");
        intent.setPackage("com.jvckenwood.communication");
        return mAppContext.bindService(intent, mMainAppConn, Context.BIND_AUTO_CREATE);
    }

    public void unBindJvcMainAppService() {
        unregisterCallback(mMainAppCallbackCallback);
        mAppContext.unbindService(mMainAppConn);
    }

    public void startInitialSetup(){
        if(!checkConnection())
            return;

        try {
            mMainAppInterface.startInitialSetup();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void endInitialSetup(){
        if(!checkConnection())
            return;

        try {
            mMainAppInterface.endInitialSetup();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void settingsUpdateRequest(String setings){
        if(!checkConnection())
            return;

        try {
            mMainAppInterface.settingsUpdateRequest(setings);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void manualUploadCancel(int cancel){
        if(!checkConnection())
            return;

        try {
            mMainAppInterface.manualUploadCancel(cancel);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param userId
     * @param isUserCall;  1.ユーザからの要求発信 / 2.コールセンターからの要求発信(リアルタイム)
     */
    public void voipInfomationRequest(int userId,int isUserCall){
        if(!checkConnection())
            return;

        try {
            mMainAppInterface.voipInfomationRequest(userId, isUserCall);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void FWUpdateRequest(){
        if(!checkConnection())
            return;

        try {
            mMainAppInterface.FWUpdateRequest();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void registerCallback(IMainAppCallback callback){
        if(!checkConnection())
            return;

        try {
            mMainAppInterface.registerCallback(callback);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void unregisterCallback(IMainAppCallback callback){
        if(!checkConnection())
            return;

        try {
            mMainAppInterface.unregisterCallback(callback);
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