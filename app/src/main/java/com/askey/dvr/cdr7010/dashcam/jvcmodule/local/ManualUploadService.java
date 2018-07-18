package com.askey.dvr.cdr7010.dashcam.jvcmodule.local;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.askey.dvr.cdr7010.dashcam.IManualUpload;
import com.askey.dvr.cdr7010.dashcam.IManualUploadCallback;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.jvckenwood.Communication;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.jvckenwood.MainApp;
import com.askey.dvr.cdr7010.dashcam.util.Logg;

public class ManualUploadService extends Service {
    private static final String LOG_TAG = "ManualUploadService";
    private static RemoteCallbackList<IManualUploadCallback> mManualUploadCallbackList;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if(mManualUploadCallbackList == null)
            mManualUploadCallbackList =  new RemoteCallbackList<>();

        return new MyBinder();
    }

    class MyBinder extends IManualUpload.Stub {
        @Override
        public void manualUpload(int camType, String filePath1, String startTime1, String endTime1, String filePath2, String startTime2, String endTime2) throws RemoteException {
            Communication.getInstance().manualUpload(camType, filePath1, startTime1, endTime1, filePath2, startTime2, endTime2);
        }

        @Override
        public void manualUploadCancel() throws RemoteException {
            MainApp.getInstance().manualUploadCancel();
        }

        @Override
        public void registerCallback(IManualUploadCallback callback) throws RemoteException {
            if(callback != null){
                mManualUploadCallbackList.register(callback);
            }else {
                Logg.w(LOG_TAG, "registerCallback: null callback.");
            }
        }

        @Override
        public void unregisterCallback(IManualUploadCallback callback) throws RemoteException {
            if(callback != null){
                mManualUploadCallbackList.unregister(callback);
            }else {
                Logg.w(LOG_TAG, "unregisterCallback: null callback.");
            }
        }
    }

    public static void reportTxManualProgress(int progress1, int total1, int progress2, int total2){
        if(mManualUploadCallbackList == null)
            return;

        int num = mManualUploadCallbackList.beginBroadcast();
        try {
            for (int i = 0; i < num; i++) {
                mManualUploadCallbackList.getBroadcastItem(i).reportTxManualProgress(progress1, total1, progress2, total2);
            }
        } catch (RemoteException e) {
            Logg.e(LOG_TAG, "reportTxManualProgress: " + e.getMessage());
        }
        mManualUploadCallbackList.finishBroadcast();
    }

    public static void manualUploadComplete(int result, String response){
        if(mManualUploadCallbackList == null)
            return;

        int num = mManualUploadCallbackList.beginBroadcast();
        try {
            for (int i = 0; i < num; i++) {
                mManualUploadCallbackList.getBroadcastItem(i).manualUploadComplete(result, response);
            }
        } catch (RemoteException e) {
            Logg.e(LOG_TAG, "manualUploadComplete: " + e.getMessage());
        }
        mManualUploadCallbackList.finishBroadcast();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logg.i(LOG_TAG, "onDestroy: ");
    }
}
