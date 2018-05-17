package com.askey.dvr.cdr7010.dashcam.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.askey.dvr.cdr7010.dashcam.application.DashCamApplication;
import com.askey.dvr.cdr7010.dashcam.domain.EventInfo;
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
    private static final String LOG_TAG = "Communication";

    private static Communication mCommunication;
    private ICommunication mInterface;
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

    private Communication() { }

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

        }

        @Override
        public void reportDrivingReport() throws RemoteException {
            //id 52, 運転レポート Driving report, eventType define 101
            int eventType = 101;
            long timeStamp = System.currentTimeMillis();
            EventInfo eventInfo = EventManager.getInstance().getEventInfoByEventType(eventType);
            if(eventInfo == null){
                Logg.e(LOG_TAG, "reportDrivingReport: can't find EventInfo, eventType=" + eventType);
                return;
            }
            EventManager.getInstance().handOutEventInfo(eventInfo, timeStamp);
        }

        @Override
        public void reportManthlyDrivingReport() throws RemoteException {
            //id 53, 月間運転レポート Monthly driving report, eventType define 102
            int eventType = 102;
            long timeStamp = System.currentTimeMillis();
            EventInfo eventInfo = EventManager.getInstance().getEventInfoByEventType(eventType);
            if(eventInfo == null){
                Logg.e(LOG_TAG, "reportManthlyDrivingReport: can't find EventInfo, eventType=" + eventType);
                return;
            }
            EventManager.getInstance().handOutEventInfo(eventInfo, timeStamp);
        }

        @Override
        public void reportServerNotifocation() throws RemoteException {
            //id 51, お知らせ Notice, eventType define 100
            int eventType = 100;
            long timeStamp = System.currentTimeMillis();
            EventInfo eventInfo = EventManager.getInstance().getEventInfoByEventType(eventType);
            if(eventInfo == null){
                Logg.e(LOG_TAG, "reportServerNotifocation: can't find EventInfo, eventType=" + eventType);
                return;
            }
            EventManager.getInstance().handOutEventInfo(eventInfo, timeStamp);
        }

        @Override
        public void reportDrivingAdvice() throws RemoteException {
            //id 54, 運転前アドバイス Advice before driving, eventType define 103
            int eventType = 103;
            long timeStamp = System.currentTimeMillis();
            EventInfo eventInfo = EventManager.getInstance().getEventInfoByEventType(eventType);
            if(eventInfo == null){
                Logg.e(LOG_TAG, "reportDrivingAdvice: can't find EventInfo, eventType=" + eventType);
                return;
            }
            EventManager.getInstance().handOutEventInfo(eventInfo, timeStamp);
        }

        @Override
        public void reportRecorderSettings(int status, int volume, long settings) throws RemoteException {

        }

        @Override
        public void reportTxEventProgress(int progress, int total) throws RemoteException {

        }

        @Override
        public void reportRealTimeAlert(int alertID) throws RemoteException {

        }

        @Override
        public void reportTripDataResult(int status) throws RemoteException {

        }

        @Override
        public void reportEventDataResult(int status) throws RemoteException {

        }

        @Override
        public void reportVersionUpInformation() throws RemoteException {

        }

        @Override
        public void reportVersionUpData() throws RemoteException {

        }
    };


}
