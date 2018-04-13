package com.askey.dvr.cdr7010.dashcam.core.camera2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Surface;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class Camera2Controller {
    private static final String TAG = "Camera2Controller";
    private boolean mIsPreviewing;
    private boolean mIsRecordingVideo;
    private List<Surface> mSurfaceList;

    public enum CAMERA {MAIN, EXT}

    private Context mContext;
    private CameraManager mCameraManager;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mPreviewSession;
    private CaptureRequest.Builder mCaptureBuilder;

    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;

    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    @SuppressLint("MissingPermission")
    public Camera2Controller(Context context) {
        mContext = context.getApplicationContext();
        mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        mSurfaceList = new ArrayList<>();
    }

    public void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    public void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String getCameraId(CAMERA camera) {
        int lensFacing = (camera == CAMERA.EXT) ?
                CameraCharacteristics.LENS_FACING_FRONT :
                CameraCharacteristics.LENS_FACING_BACK;
        try {
            for (String cameraId : mCameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics
                        = mCameraManager.getCameraCharacteristics(cameraId);

                if (characteristics.get(CameraCharacteristics.LENS_FACING) == lensFacing) {
                    return cameraId;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private final CameraDevice.StateCallback mDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            Log.v(TAG, "onOpened: cameraDevice = " + cameraDevice);
            mCameraDevice = cameraDevice;
            //if (mIsPreviewing) {
            //    startPreviewInternal();
            //}
            mCameraOpenCloseLock.release();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            Log.v(TAG, "onDisconnected: cameraDevice = " + cameraDevice);
            mCameraDevice.close();
            mCameraDevice = null;
            mCameraOpenCloseLock.release();
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            Log.v(TAG, "onError: cameraDevice = " + cameraDevice + ", error = " + error);
            mCameraDevice.close();
            mCameraDevice = null;
            mCameraOpenCloseLock.release();
        }
    };

    public void open(CAMERA camera) {
        Log.v(TAG, "open()");
        try {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Log.d(TAG, "tryAcquire");
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            String cameraId = getCameraId(camera);
            Log.d(TAG, "camera id: " + cameraId);
            mCameraManager.openCamera(cameraId, mDeviceStateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.");
        }
    }

    public void closeCamera() {
        Log.d(TAG, "closeCamera");
        try {
            mCameraOpenCloseLock.acquire();
            closePreviewSession();
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.");
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    private void closePreviewSession() {
        if (mPreviewSession != null) {
            mPreviewSession.close();
            mPreviewSession = null;
        }
    }

    public void startPreview() {
        Log.d(TAG, "startPreview");
        startPreviewInternal();
        mIsPreviewing = true;
    }

    private boolean startPreviewInternal() {
        Log.v(TAG, "startPreviewInternal()");
        if (mIsPreviewing || mCameraDevice == null || mSurfaceList.size() == 0) {
            Log.e(TAG, "startPreviewInternal error");
            return false;
        }

        closePreviewSession();

        try {
            Log.v(TAG, "startPreviewInternal: createCaptureRequest");
            mCaptureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            for (Surface targetSurface: mSurfaceList) {
                mCaptureBuilder.addTarget(targetSurface);
            }
            mCameraDevice.createCaptureSession(mSurfaceList, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    Log.v(TAG, "onConfigured: cameraCaptureSession = " + session);
                    mPreviewSession = session;
                    updatePreview();
                    mIsPreviewing = true;
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void startRecordingVideo() {
        if (mIsRecordingVideo || null == mCameraDevice || mSurfaceList.size() == 0) {
            return;
        }

        try {
            closePreviewSession();
            mCaptureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            for (Surface targetSurface: mSurfaceList) {
                mCaptureBuilder.addTarget(targetSurface);
            }

            mCameraDevice.createCaptureSession(mSurfaceList, new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    mPreviewSession = cameraCaptureSession;
                    updatePreview();
                    mIsRecordingVideo = true;
                    // TODO:
                    // notify ui
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void stopRecordingVideo() {
        mIsRecordingVideo = false;
        startPreview();
    }

    public void addSurface(@NonNull Surface surface) {
        Log.d(TAG, "addSurface" + surface);
        mSurfaceList.add(surface);
    }

    private void updatePreview() {
        if (null == mCameraDevice) {
            return;
        }
        try {
            setUpCaptureRequestBuilder(mCaptureBuilder);
            HandlerThread thread = new HandlerThread("CameraPreview");
            thread.start();
            mPreviewSession.setRepeatingRequest(mCaptureBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setUpCaptureRequestBuilder(CaptureRequest.Builder builder) {
        builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
    }
}