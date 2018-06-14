package com.askey.dvr.cdr7010.dashcam.jvcmodule.local;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.askey.dvr.cdr7010.dashcam.IEcall;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.jvckenwood.Communication;
import com.askey.dvr.cdr7010.dashcam.util.Logg;

public class EcallService extends Service {
    private static final String LOG_TAG = "EcallService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    class MyBinder extends IEcall.Stub {

        @Override
        public void discEmergencyCall(int status) throws RemoteException {
            Communication.getInstance().discEmergencyCall(status);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Logg.i(LOG_TAG, "onDestroy: ");
    }
}
