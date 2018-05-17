package com.askey.dvr.cdr7010.dashcam.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;

import com.askey.dvr.cdr7010.dashcam.util.Logg;
import com.askey.dvr.cdr7010.filemanagement.IFileManagerAidlInterface;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileManager {
    private static final String LOG_TAG = "FileManagement";

    private static FileManager INSTANCE;
    private IFileManagerAidlInterface mService;
    private static final SimpleDateFormat DATETIME_FORMAT =
            new SimpleDateFormat("yyMMddHHmmss", Locale.US);
    private Context mContext;

    private ServiceConnection mConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Logg.d(LOG_TAG, "onServiceConnected: ");
            mService = IFileManagerAidlInterface.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Logg.d(LOG_TAG, "onServiceDisconnected: ");
            mService = null;
        }
    };

    private FileManager(Context context) {
        mContext = context.getApplicationContext();
        Intent bindIntent = new Intent();
        bindIntent.setAction("com.askey.filemanagerservice.action");
        bindIntent.setPackage("com.askey.dvr.cdr7010.filemanagement");
        context.bindService(bindIntent, mConn, Context.BIND_AUTO_CREATE);
    }

    public static FileManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (FileManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new FileManager(context);
                }
            }
        }
        return INSTANCE;
    }

    public void release() {
        if (mConn != null) {
            mContext.unbindService(mConn);
            mConn = null;
        }
    }

    private String buildFilePath(long timeStamp, @NonNull String fileType, String ext) throws RemoteException {
        if (mService == null) {
            throw new RemoteException("No FileManagement service.");
        }

        String fileName;
        synchronized (DATETIME_FORMAT) {
            fileName = DATETIME_FORMAT.format(new Date(timeStamp)) + ext;
        }
        return mService.openSdcard(fileName, fileType);
    }

    public String getFilePathForNormal(long timeStamp) throws RemoteException {
        return buildFilePath(timeStamp, "NORMAL", ".mp4");
    }

    public String getFilePathForEvent(long timeStamp) throws RemoteException {
        return buildFilePath(timeStamp, "EVENT", ".mp4");
    }

    public String getFilePathForPicture(long timeStamp) throws RemoteException {
        return buildFilePath(timeStamp, "PICTURE", ".jpg");
    }

    public boolean isSdcardAvailable() throws RemoteException {
        if (mService == null) {
            throw new RemoteException("No FileManagement service.");
        }
        return mService.checkSdcardAvailable();
    }
}
