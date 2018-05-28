package com.askey.dvr.cdr7010.dashcam.jvckenwood;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.askey.dvr.cdr7010.dashcam.application.DashCamApplication;
import com.askey.dvr.cdr7010.dashcam.util.Logg;
import com.jvckenwood.communication.IEventSending;

import java.util.List;

/***
 * Company: Chengdu Skysoft Info&Tech Co., Ltd.
 * Copyright ©2014-2018 Chengdu Skysoft Info&Tech Co., Ltd.
 * Created by Mark on 2018/5/16.

 * @since:JDK1.6
 * @version:1.0
 * @see
 ***/
public class EventSending {
//    public static final String ACTION_xxx_REQUEST = "xxx";

    private static final String LOG_TAG = "EventSending";
    private static EventSending mEventSending;
    private IEventSending mEventSendingInterface;
    private final Context mAppContext;

    private EventSending() {
        mAppContext = DashCamApplication.getAppContext();
    }

    public static EventSending getInstance(){
        if(mEventSending == null)
            mEventSending = new EventSending();

        return mEventSending;
    }

    private ServiceConnection mEventSendingConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Logg.d(LOG_TAG, "onServiceConnected: EventSending");
            mEventSendingInterface = IEventSending.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Logg.d(LOG_TAG, "onServiceDisconnected: EventSending");
            mEventSendingInterface = null;
        }
    };

    public boolean bindJvcEventSendingService() {
//        Intent intent = new Intent();
//        intent.setAction("service.jkccomm.IMainApp");
//        intent.setPackage("com.jvckenwood.communication");
//        return mAppContext.bindService(intent, mEventSendingConn, Context.BIND_AUTO_CREATE);
        return true;
    }

    public void unBindJvcEventSendingService() {
//        mAppContext.unbindService(mEventSendingConn);
    }

    public void setEventData(int eventNo, long timeStamp, List<String> picturePath, List<String> moviePath){
        if(mEventSendingInterface == null)
            return;

        try {
            mEventSendingInterface.setEventData(eventNo, timeStamp, picturePath, moviePath);
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
