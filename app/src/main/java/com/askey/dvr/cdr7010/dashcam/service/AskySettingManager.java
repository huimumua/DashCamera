package com.askey.dvr.cdr7010.dashcam.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.askey.dvr.cdr7010.dashcam.util.Logg;
import com.askey.dvr.cdr7010.filemanagement.IAskeySettingsAidlInterface;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class AskySettingManager {

    private static final String LOG_TAG = AskySettingManager.class.getSimpleName();

    private static AskySettingManager INSTANCE;
    private IAskeySettingsAidlInterface mService;
    private static final SimpleDateFormat DATETIME_FORMAT =
            new SimpleDateFormat("yyMMddHHmmss", Locale.US);
    private Context mContext;

    private ServiceConnection mConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Logg.d(LOG_TAG, "onServiceConnected: ");
            mService = IAskeySettingsAidlInterface.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Logg.d(LOG_TAG, "onServiceDisconnected: ");
            mService = null;
        }
    };

    private AskySettingManager(Context context) {
        mContext = context.getApplicationContext();
        Intent bindIntent = new Intent();
        bindIntent.setAction("com.askey.askeysettingservice.action");
        bindIntent.setPackage("com.askey.dvr.cdr7010.filemanagement");
        context.bindService(bindIntent, mConn, Context.BIND_AUTO_CREATE);
    }

    public static AskySettingManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AskySettingManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AskySettingManager(context);
                }
            }
        }
        return INSTANCE;
    }

    public void initAskySetting(String userId) throws RemoteException {
        Log.d("abby","    userId==="+userId);
        if (mService == null){
            throw  new RemoteException("No FileManagement service.");
        }else {
            mService.init(userId);
        }
    }
}
