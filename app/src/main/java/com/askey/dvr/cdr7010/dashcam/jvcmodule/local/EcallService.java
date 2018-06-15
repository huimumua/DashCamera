package com.askey.dvr.cdr7010.dashcam.jvcmodule.local;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.askey.dvr.cdr7010.dashcam.IEcall;
import com.askey.dvr.cdr7010.dashcam.IEcallCallback;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.jvckenwood.Communication;
import com.askey.dvr.cdr7010.dashcam.util.Logg;

public class EcallService extends Service {
    private static final String LOG_TAG = "EcallService";
    private static RemoteCallbackList<IEcallCallback> mEcallCallbackList;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if(mEcallCallbackList == null)
            mEcallCallbackList =  new RemoteCallbackList<>();

        return new MyBinder();
    }

    class MyBinder extends IEcall.Stub {

        @Override
        public void discEmergencyCall(int status) {
            Communication.getInstance().discEmergencyCall(status);
        }

        @Override
        public void voipInformationRequest(int requestID, int isUserCall) {
            Communication.getInstance().voipInformationRequest(requestID, isUserCall);
        }

        @Override
        public void registerCallback(IEcallCallback callback) {
            if(callback != null){
                mEcallCallbackList.register(callback);
            }else {
                Logg.w(LOG_TAG, "registerCallback: null callback.");
            }
        }

        @Override
        public void unregisterCallback(IEcallCallback callback) {
            if(callback != null){
                mEcallCallbackList.unregister(callback);
            }else {
                Logg.w(LOG_TAG, "unregisterCallback: null callback.");
            }
        }
    }

    public static void onVoipInformationResult(int requestID, int status, long impactId, long policyNo, int policyBranchNo,
                                               String authUserName, String displayName, String outboundProxy, String password,
                                               int port, String protocol, boolean isSendKeepAlive, String profileName, boolean isAutoRegistration){
        if(mEcallCallbackList == null)
            return;

        int num = mEcallCallbackList.beginBroadcast();
        try {
            for (int i = 0; i < num; i++) {
                mEcallCallbackList.getBroadcastItem(i).onVoipInformationResult(requestID, status, impactId, policyNo, policyBranchNo, authUserName, displayName,
                        outboundProxy, password, port, protocol, isSendKeepAlive, profileName, isAutoRegistration);
            }
        } catch (RemoteException e) {
            Logg.e(LOG_TAG, "onVoipInformationResult: " + e.getMessage());
        }
        mEcallCallbackList.finishBroadcast();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Logg.i(LOG_TAG, "onDestroy: ");
    }
}
