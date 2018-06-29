package com.askey.dvr.cdr7010.dashcam.jvcmodule.local;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.askey.dvr.cdr7010.dashcam.ICommunication;
import com.askey.dvr.cdr7010.dashcam.ICommunicationCallback;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.jvckenwood.MainApp;
import com.askey.dvr.cdr7010.dashcam.util.Logg;

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
        public void settingsUpdateRequest(String setings) {
            MainApp.getInstance().settingsUpdateRequest(setings);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logg.i(LOG_TAG, "onDestroy: ");
    }

}
