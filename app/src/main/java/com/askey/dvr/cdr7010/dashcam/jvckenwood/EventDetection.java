package com.askey.dvr.cdr7010.dashcam.jvckenwood;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.askey.dvr.cdr7010.dashcam.application.DashCamApplication;
import com.askey.dvr.cdr7010.dashcam.service.EventManager;
import com.askey.dvr.cdr7010.dashcam.util.Logg;
import com.jvckenwood.communication.IEventDetection;
import com.jvckenwood.communication.IEventDetectionCallback;

/***
 * Company: Chengdu Skysoft Info&Tech Co., Ltd.
 * Copyright ©2014-2018 Chengdu Skysoft Info&Tech Co., Ltd.
 * Created by Mark on 2018/5/16.

 * @since:JDK1.6
 * @version:1.0
 * @see
 ***/
public class EventDetection {
//    public static final String ACTION_xxx_REQUEST = "xxx";

    private static final String LOG_TAG = "EventDetection";
    private static EventDetection mEventDetection;
    private IEventDetection mEventDetectionInterface;
    private final Context mAppContext;

    private EventDetection() {
        mAppContext = DashCamApplication.getAppContext();
    }

    public static EventDetection getInstance(){
        if(mEventDetection == null)
            mEventDetection = new EventDetection();

        return mEventDetection;
    }

    private ServiceConnection mEventDetectionConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Logg.d(LOG_TAG, "onServiceConnected: EventDetection");
            mEventDetectionInterface = IEventDetection.Stub.asInterface(service);
            registerCallback(mEventDetectionCallback);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Logg.d(LOG_TAG, "onServiceDisconnected: EventDetection");
            mEventDetectionInterface = null;
        }
    };

    private IEventDetectionCallback mEventDetectionCallback = new IEventDetectionCallback() {
        @Override
        public void reportRealTimeAlert(int alertID) throws RemoteException {
            Logg.d(LOG_TAG, "reportRealTimeAlert: alertID=" + alertID);
            int eventType = -1;
            if(alertID == 1){
                eventType = 11;
            }else if(alertID == 2){
                eventType = 14;
            }else if(alertID == 3){
                eventType = 15;
            }else if(alertID == 4){
                eventType = 12;
            }else if(alertID == 5){
                eventType = 13;
            }else if(alertID == 20){
                eventType = 16;
            }else if(alertID == 21){
                eventType = 17;
            }else if(alertID == 22){
                eventType = 18;
            }else if(alertID == 23){
                eventType = 19;
            }else if(alertID == 24){
                eventType = 20;
            }else if(alertID == 25){
                eventType = 21;
            }else if(alertID == 26){
                eventType = 22;
            }

            EventManager.getInstance().handOutEventInfo(eventType);
        }

        @Override
        public void requestLocationData(int oos, String response) throws RemoteException {

        }

        @Override
        public IBinder asBinder() {
            return null;
        }
    };


    public boolean bindJvcEventDetectionService() {
        Intent intent = new Intent();
        intent.setAction("service.jkccomm.IEventDetection");
        intent.setPackage("com.jvckenwood.communication");
        return mAppContext.bindService(intent, mEventDetectionConn, Context.BIND_AUTO_CREATE);
    }

    public void unBindJvcEventDetectionService() {
        unregisterCallback(mEventDetectionCallback);

        mAppContext.unbindService(mEventDetectionConn);
    }

    public void registerCallback(IEventDetectionCallback callback){
        if(mEventDetectionInterface == null)
            return;

        try {
            mEventDetectionInterface.registerCallback(callback);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void unregisterCallback(IEventDetectionCallback callback){
        if(mEventDetectionInterface == null)
            return;

        try {
            mEventDetectionInterface.unregisterCallback(callback);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }





//    public void xxxRequest(){
//        Intent intent = new Intent(ACTION_xxx_REQUEST);
//        sendOutBroadcast(intent);
//    }
//
//    private void sendOutBroadcast(Intent intent){
//        mAppContext.sendBroadcast(intent);
//    }

}
