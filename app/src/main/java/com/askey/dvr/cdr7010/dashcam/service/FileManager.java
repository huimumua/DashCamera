package com.askey.dvr.cdr7010.dashcam.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;

import com.askey.dvr.cdr7010.dashcam.core.camera2.CameraHelper;
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
                    INSTANCE = new FileManager(context.getApplicationContext());
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

    private String buildFilePath(int cameraId, long timeStamp, @NonNull String fileType, String ext) throws RemoteException {
        if (mService == null) {
            throw new RemoteException("No FileManagement service.");
        }

        StringBuilder sb = new StringBuilder();
        synchronized (DATETIME_FORMAT) {
            sb.append(DATETIME_FORMAT.format(new Date(timeStamp)));
        }
        if (cameraId == CameraHelper.CAMERA_EXT) {
            sb.append("_2");
        }
        sb.append(ext);
        Logg.d(LOG_TAG, "buildFilePath,Path==" + sb.toString());
        final String path = mService.openSdcard(sb.toString(), fileType);
        if (path == null) {
            throw new RemoteException("null file path from FileManagement service.");
        }
        return path;
    }

    private String buildHashFilePath(String fileName, @NonNull String fileType) throws RemoteException {
        if (mService == null) {
            throw new RemoteException("No FileManagement service.");
        }
        fileName = String.format("%s.hash", fileName);
        final String path = mService.openSdcard(fileName, fileType);
        if (path == null) {
            throw new RemoteException("null file path from FileManagement service.");
        }
        return path;
    }

    private String buildPicFilePath(String fileName) throws RemoteException {
        if (mService == null) {
            throw new RemoteException("No FileManagement service.");
        }
        fileName = String.format("%s.jpg", fileName);
        final String path = mService.openSdcard(fileName, "PICTURE");
        if (path == null) {
            throw new RemoteException("null file path from FileManagement service.");
        }
        return path;
    }

    public String getFilePathForNormal(@CameraHelper.CameraName int cameraId, long timeStamp) throws RemoteException {
        return buildFilePath(cameraId, timeStamp, "NORMAL", ".mp4");
    }

    public String getFilePathForEvent(@CameraHelper.CameraName int cameraId, long timeStamp) throws RemoteException {
        return buildFilePath(cameraId, timeStamp, "EVENT", ".mp4");
    }

    public String getFilePathForPicture(String imageName) throws RemoteException {
        return buildPicFilePath(imageName);
    }

    public int checkSdcardAvailable() throws RemoteException {
        if (mService == null) {
            throw new RemoteException("No FileManagement service.");
        }
        return mService.checkSdcardAvailable();
    }

    public String getFilePathForNmeaNormal(@CameraHelper.CameraName int cameraId, long timeStamp) throws RemoteException {
        return buildFilePath(cameraId, timeStamp, "NMEA_NORMAL", ".nmea");
    }

    public String getFilePathForNmeaEvent(@CameraHelper.CameraName int cameraId, long timeStamp) throws RemoteException {
        return buildFilePath(cameraId, timeStamp, "NMEA_EVENT", ".nmea");
    }

    public String getFilePathForHashNormal(String fileName) throws RemoteException {
        return buildHashFilePath(fileName, "HASH_NORMAL");
    }

    public String getFilePathForHashEvent(String fileName) throws RemoteException {
        return buildHashFilePath(fileName, "HASH_EVENT");
    }
}
