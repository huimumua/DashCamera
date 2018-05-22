package com.askey.dvr.cdr7010.dashcam.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.askey.dvr.cdr7010.dashcam.server.ITTSAidlInterface;
import com.askey.dvr.cdr7010.dashcam.util.Logg;

public class TTSSevice extends Service {
    private static final String LOG_TAG = TTSSevice.class.getSimpleName();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    class MyBinder extends ITTSAidlInterface.Stub{


        @Override
        public void ttsNormalStart(String message) throws RemoteException {
            TTSManager.getInstance().ttsNormalStart(message);
        }

        @Override
        public void ttsResume() throws RemoteException {
            TTSManager.getInstance().ttsResume();
        }

        @Override
        public void ttsPause() throws RemoteException {
            TTSManager.getInstance().ttsPause();
        }

        @Override
        public void ttsStop() throws RemoteException {
            TTSManager.getInstance().ttsStop();
        }
        @Override
        public void ttsRelease() throws RemoteException {
            TTSManager.getInstance().ttsRelease();
        }

        @Override
        public void changeLanguage(String language) throws RemoteException {
            TTSManager.getInstance().changeLanguage(language);
        }

        @Override
        public void ttsEventStart(String message, int eventType,int priority) throws RemoteException {
            TTSManager.getInstance().ttsEventStart(message,eventType,priority);
        }

        @Override
        public void setTtsStreamVolume() throws RemoteException {
            TTSManager.getInstance().setTtsStreamVolume();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logg.i(LOG_TAG, "onDestroy:");
    }


}
