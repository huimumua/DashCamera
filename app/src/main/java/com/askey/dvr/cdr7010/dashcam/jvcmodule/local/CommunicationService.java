package com.askey.dvr.cdr7010.dashcam.jvcmodule.local;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.askey.dvr.cdr7010.dashcam.ICommunication;
import com.askey.dvr.cdr7010.dashcam.ICommunicationCallback;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.jvckenwood.Communication;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.jvckenwood.EventDetection;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.jvckenwood.EventSending;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.jvckenwood.MainApp;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.jvckenwood.VersionUp;
import com.askey.dvr.cdr7010.dashcam.util.Logg;

import java.util.List;

public class CommunicationService extends Service {
    private static final String LOG_TAG = "CommunicationService";
    private static RemoteCallbackList<ICommunicationCallback> mCommunicationCallbackList;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if(mCommunicationCallbackList == null)
            mCommunicationCallbackList =  new RemoteCallbackList<>();

        return new MyBinder();
    }

    class MyBinder extends ICommunication.Stub {

        @Override
        public void changeUserID(int userId) {
            Communication.getInstance().changeUserID(userId);
        }

        @Override
        public void alertComplite(int eventType) {
            Communication.getInstance().alertComplite(eventType);
        }

        @Override
        public void weatherAlertRequest() {
            Communication.getInstance().weatherAlertRequest();
        }

        @Override
        public void startInitialSetup() {
            MainApp.getInstance().startInitialSetup();
        }

        @Override
        public void endInitialSetup() {
            MainApp.getInstance().endInitialSetup();
        }

        @Override
        public void settingsUpdateRequest(String setings) {
            MainApp.getInstance().settingsUpdateRequest(setings);
        }

        @Override
        public void FWUpdateRequest() {
            MainApp.getInstance().FWUpdateRequest();
        }

        @Override
        public ParcelFileDescriptor get1HzDataPipe() {
            return EventDetection.getInstance().get1HzDataPipe();
        }

        @Override
        public void sendTripData(String filePath) {
            EventDetection.getInstance().sendTripData(filePath);
        }

        @Override
        public void EventSending_SetEventData(int eventNo, long timeStamp, List<String> picturePath, List<String> moviePath) {
            EventSending.getInstance().setEventData(eventNo, timeStamp, picturePath, moviePath);
        }

        @Override
        public void setEventData(int eventNo, long timeStamp, List<String> picturePath, List<String> moviePath) {
            Communication.getInstance().setEventData(eventNo, timeStamp, picturePath, moviePath);
        }

        @Override
        public void getVersionUpInformation(int fileType, String currentVersion) {
            VersionUp.getInstance().getVersionUpInformation(fileType, currentVersion);
        }

        @Override
        public void getVersionUpData(int fileType, String version, int range) {
            VersionUp.getInstance().getVersionUpData(fileType, version, range);
        }

        @Override
        public void registerCallback(ICommunicationCallback callback) {
            if(callback != null){
                mCommunicationCallbackList.register(callback);
            }else {
                Logg.w(LOG_TAG, "registerCallback: null callback.");
            }
        }

        @Override
        public void unregisterCallback(ICommunicationCallback callback) {
            if(callback != null){
                mCommunicationCallbackList.unregister(callback);
            }else {
                Logg.w(LOG_TAG, "unregisterCallback: null callback.");
            }
        }
    }

    public static void tripIdUpdateNotification(){
        if(mCommunicationCallbackList == null)
            return;

        int num = mCommunicationCallbackList.beginBroadcast();
        try {
            for (int i = 0; i < num; i++) {
                mCommunicationCallbackList.getBroadcastItem(i).tripIdUpdateNotification();
            }
        } catch (RemoteException e) {
            Logg.e(LOG_TAG, "tripIdUpdateNotification: " + e.getMessage());
        }
        mCommunicationCallbackList.finishBroadcast();

    }

    public static void weatherAlertResponse(String response){
        if(mCommunicationCallbackList == null)
            return;

        int num = mCommunicationCallbackList.beginBroadcast();
        try {
            for (int i = 0; i < num; i++) {
                mCommunicationCallbackList.getBroadcastItem(i).weatherAlertResponse(response);
            }
        } catch (RemoteException e) {
            Logg.e(LOG_TAG, "weatherAlertResponse: " + e.getMessage());
        }
        mCommunicationCallbackList.finishBroadcast();
    }

    public static void tripIdVersionUpResponse(int fairmware, int voice){
        if(mCommunicationCallbackList == null)
            return;

        int num = mCommunicationCallbackList.beginBroadcast();
        try {
            for (int i = 0; i < num; i++) {
                mCommunicationCallbackList.getBroadcastItem(i).tripIdVersionUpResponse(fairmware, voice);
            }
        } catch (RemoteException e) {
            Logg.e(LOG_TAG, "tripIdVersionUpResponse: " + e.getMessage());
        }
        mCommunicationCallbackList.finishBroadcast();
    }

    public static void tripIdLogUploadUpResponse(int logupload){
        if(mCommunicationCallbackList == null)
            return;

        int num = mCommunicationCallbackList.beginBroadcast();
        try {
            for (int i = 0; i < num; i++) {
                mCommunicationCallbackList.getBroadcastItem(i).tripIdLogUploadUpResponse(logupload);
            }
        } catch (RemoteException e) {
            Logg.e(LOG_TAG, "tripIdLogUploadUpResponse: " + e.getMessage());
        }
        mCommunicationCallbackList.finishBroadcast();
    }

    public static void reportInsuranceTerm(int oos, String response){
        if(mCommunicationCallbackList == null)
            return;

        int num = mCommunicationCallbackList.beginBroadcast();
        try {
            for (int i = 0; i < num; i++) {
                mCommunicationCallbackList.getBroadcastItem(i).reportInsuranceTerm(oos, response);
            }
        } catch (RemoteException e) {
            Logg.e(LOG_TAG, "reportInsuranceTerm: " + e.getMessage());
        }
        mCommunicationCallbackList.finishBroadcast();
    }

    public static void reportUserList(int oos, String response){
        if(mCommunicationCallbackList == null)
            return;

        int num = mCommunicationCallbackList.beginBroadcast();
        try {
            for (int i = 0; i < num; i++) {
                mCommunicationCallbackList.getBroadcastItem(i).reportUserList(oos, response);
            }
        } catch (RemoteException e) {
            Logg.e(LOG_TAG, "reportUserList: " + e.getMessage());
        }
        mCommunicationCallbackList.finishBroadcast();
    }

    public static void reportSystemSettings(int oos, String response){
        if(mCommunicationCallbackList == null)
            return;

        int num = mCommunicationCallbackList.beginBroadcast();
        try {
            for (int i = 0; i < num; i++) {
                mCommunicationCallbackList.getBroadcastItem(i).reportSystemSettings(oos, response);
            }
        } catch (RemoteException e) {
            Logg.e(LOG_TAG, "reportSystemSettings: " + e.getMessage());
        }
        mCommunicationCallbackList.finishBroadcast();
    }

    public static void reportUserSettings(int oos, String response){
        if(mCommunicationCallbackList == null)
            return;

        int num = mCommunicationCallbackList.beginBroadcast();
        try {
            for (int i = 0; i < num; i++) {
                mCommunicationCallbackList.getBroadcastItem(i).reportUserSettings(oos, response);
            }
        } catch (RemoteException e) {
            Logg.e(LOG_TAG, "reportUserSettings: " + e.getMessage());
        }
        mCommunicationCallbackList.finishBroadcast();
    }

    public static void reportSettingsUpdate(int oos, String response){
        if(mCommunicationCallbackList == null)
            return;

        int num = mCommunicationCallbackList.beginBroadcast();
        try {
            for (int i = 0; i < num; i++) {
                mCommunicationCallbackList.getBroadcastItem(i).reportSettingsUpdate(oos, response);
            }
        } catch (RemoteException e) {
            Logg.e(LOG_TAG, "reportSettingsUpdate: " + e.getMessage());
        }
        mCommunicationCallbackList.finishBroadcast();
    }

    public static void reportTxEventProgress(int eventNo,int progress,int total){
        if(mCommunicationCallbackList == null)
            return;

        int num = mCommunicationCallbackList.beginBroadcast();
        try {
            for (int i = 0; i < num; i++) {
                mCommunicationCallbackList.getBroadcastItem(i).reportTxEventProgress(eventNo, progress, total);
            }
        } catch (RemoteException e) {
            Logg.e(LOG_TAG, "reportTxEventProgress: " + e.getMessage());
        }
        mCommunicationCallbackList.finishBroadcast();
    }

    public static void onFWUpdateRequest(int result){
        if(mCommunicationCallbackList == null)
            return;

        int num = mCommunicationCallbackList.beginBroadcast();
        try {
            for (int i = 0; i < num; i++) {
                mCommunicationCallbackList.getBroadcastItem(i).onFWUpdateRequest(result);
            }
        } catch (RemoteException e) {
            Logg.e(LOG_TAG, "onFWUpdateRequest: " + e.getMessage());
        }
        mCommunicationCallbackList.finishBroadcast();
    }

    public static void reportVersionUpInformation(int oos,int fileType,String response){
        if(mCommunicationCallbackList == null)
            return;

        int num = mCommunicationCallbackList.beginBroadcast();
        try {
            for (int i = 0; i < num; i++) {
                mCommunicationCallbackList.getBroadcastItem(i).reportVersionUpInformation(oos, fileType, response);
            }
        } catch (RemoteException e) {
            Logg.e(LOG_TAG, "reportVersionUpInformation: " + e.getMessage());
        }
        mCommunicationCallbackList.finishBroadcast();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Logg.i(LOG_TAG, "onDestroy: ");
    }
}
