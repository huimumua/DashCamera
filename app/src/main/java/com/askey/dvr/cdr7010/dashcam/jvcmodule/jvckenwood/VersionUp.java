package com.askey.dvr.cdr7010.dashcam.jvcmodule.jvckenwood;

/***
 * Company: Chengdu Skysoft Info&Tech Co., Ltd.
 * Copyright ©2014-2018 Chengdu Skysoft Info&Tech Co., Ltd.
 * Created by Mark on 2018/5/16.

 * @since:JDK1.6
 * @version:1.0
 * @see
 ***/
public class VersionUp {
//    public static final String ACTION_xxx_REQUEST = "xxx";
//
//    private static final String LOG_TAG = "VersionUp";
//    private static VersionUp mVersionUp;
//    private IMainApp mVersionUpInterface;
//    private final Context mAppContext;
//
//    private VersionUp() {
//        mAppContext = DashCamApplication.getAppContext();
//    }
//
//    public static VersionUp getInstance(){
//        if(mVersionUp == null)
//            mVersionUp = new VersionUp();
//
//        return mVersionUp;
//    }
//
//    private ServiceConnection mVersionUpConn = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            Logg.d(LOG_TAG, "onServiceConnected: VersionUp");
//            mVersionUpInterface = IMainApp.Stub.asInterface(service);
//            registerCallback(mVersionUpCallback);
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//            Logg.d(LOG_TAG, "onServiceDisconnected: VersionUp");
//            mVersionUpInterface = null;
//        }
//    };
//
//    private IMainAppCallback.Stub mVersionUpCallback = new IMainAppCallback.Stub() {
//
//        @Override
//        public void onFWUpdateResponse(int result) {
//            Logg.d(LOG_TAG, "onFWUpdateResponse: result=" + result);
//
//        }
//    };
//
//    public boolean bindJvcVersionUpService() {
//        Intent intent = new Intent();
//        intent.setAction("service.jkccomm.IVersionUp");
//        intent.setPackage("com.jvckenwood.communication");
//        return mAppContext.bindService(intent, mVersionUpConn, Context.BIND_AUTO_CREATE);
//    }
//
//    public void unBindJvcVersionUpService() {
//        unregisterCallback(mVersionUpCallback);
//
//        mAppContext.unbindService(mVersionUpConn);
//    }
//
//    public void registerCallback(IVersionUpCallback callback){
//        Logg.d(LOG_TAG, "registerCallback: ");
//        if(mVersionUpInterface == null)
//            return;
//
//        try {
//            mVersionUpInterface.registerCallback(callback);
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void unregisterCallback(IVersionUpCallback callback){
//        Logg.d(LOG_TAG, "unregisterCallback: ");
//        if(mVersionUpInterface == null)
//            return;
//
//        try {
//            mVersionUpInterface.unregisterCallback(callback);
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void getVersionUpInformation(int fileType, String currentVersion){
//        Logg.d(LOG_TAG, "getVersionUpInformation: fileType=" + fileType + ", currentVersion=" + currentVersion);
//        if(mVersionUpInterface == null)
//            return;
//
//        try {
//            mVersionUpInterface.getVersionUpInformation(fileType, currentVersion);
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void getVersionUpData(int fileType, String version, int range){
//        Logg.d(LOG_TAG, "getVersionUpData: fileType=" + fileType + ", version=" + version + ", range=" + range);
//        if(mVersionUpInterface == null)
//            return;
//
//        try {
//            mVersionUpInterface.getVersionUpData(fileType, version, range);
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
//    }


}
