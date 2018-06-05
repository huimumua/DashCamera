package com.askey.dvr.cdr7010.dashcam.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import com.askey.dvr.cdr7010.dashcam.ITTSAidlInterface;

import com.askey.dvr.cdr7010.dashcam.util.Logg;

public class TTSSevice extends Service {
    private static final String LOG_TAG = TTSSevice.class.getSimpleName();

    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    class MyBinder extends ITTSAidlInterface.Stub{
        @Override
        public void ttsStop(int requestId) throws RemoteException {
            TTSManager.getInstance().ttsStop(requestId);
        }
        @Override
        public void ttsEventStart(int requestId,int priority,int voiceId) throws RemoteException {
            TTSManager.getInstance().ttsEventStart(requestId,priority,new int[]{voiceId});
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logg.i(LOG_TAG, "onDestroy:");
    }
}
