package com.askey.dvr.cdr7010.dashcam.core.camera2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.media.ImageReader;
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
    private static final String TAG_BASE = Camera2Controller.class.getSimpleName();
    private String TAG = TAG_BASE;
    private final CameraControllerListener mListener;
    private final Handler mListenerHandler;
    private boolean mIsRecordingVideo;
    private ImageReader mImageReader;
    private SurfaceTexture mSurfaceTexture;

    public enum State {
        STOPPED, CAPTURING
    }

    private State mState;
    public State getState() {
        return mState;
    }

    private Context mContext;
    private CameraManager mCameraManager;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCaptureSession;
    private CaptureRequest.Builder mCaptureBuilder;

    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;

    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    @SuppressLint("MissingPermission")
    public Camera2Controller(Context context, CameraControllerListener listener, Handler handler) {
        mContext = context.getApplicationContext();
        mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        mImageReader = null;
        mListener = listener;
        mListenerHandler = handler;
        mState = State.STOPPED;
    }

    public void setImageReader(@NonNull ImageReader imageReader) {
        mImageReader = imageReader;
    }

    public void setSurface(@NonNull SurfaceTexture surfaceTexture) {
        mSurfaceTexture = surfaceTexture;
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

    private String getCameraId(@CameraHelper.CameraName int cameraName) {
        try {
            for (String cameraId : mCameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraId);
                Integer fc = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (fc != null && fc == cameraName) {
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
            mCameraOpenCloseLock.release();
            mListenerHandler.post(mListener::onCameraOpened);
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            Log.v(TAG, "onDisconnected: cameraDevice = " + cameraDevice);
            mCameraDevice.close();
            mCameraDevice = null;
            mCameraOpenCloseLock.release();
            mListenerHandler.post(mListener::onCameraClosed);
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            Log.v(TAG, "onError: cameraDevice = " + cameraDevice + ", error = " + error);
            mCameraDevice.close();
            mCameraDevice = null;
            mCameraOpenCloseLock.release();
            mListenerHandler.post(() -> mListener.onCameraError(error));
        }
    };

    public void open(@CameraHelper.CameraName int camera) throws Exception {
        TAG = TAG_BASE + "-" + camera;
        Log.v(TAG, "open: facing = " + camera);
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            throw new RuntimeException("Camera permission fail.");
        }
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            String cameraId = getCameraId(camera);
            Log.d(TAG, "camera id: " + cameraId);
            mCameraManager.openCamera(cameraId, mDeviceStateCallback, mBackgroundHandler);
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.");
        }
    }

    public void closeCamera() throws CameraAccessException {
        Log.d(TAG, "closeCamera");
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera closing.");
            }
            if (mCaptureSession != null) {
                mCaptureSession.stopRepeating();
                mCaptureSession.close();
                mCaptureSession = null;
                mState = State.STOPPED;
                mListenerHandler.post(mListener::onCaptureStopped);
            }
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

    public void startRecordingVideo() throws CameraAccessException {
        if (mIsRecordingVideo) {
            return;
        }

        if (null == mCameraDevice) {
            throw new RuntimeException("null CameraDevice.");
        }

        Surface surface = null;
        if (mSurfaceTexture != null) {
            surface = new Surface(mSurfaceTexture);
        }
        mCaptureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
        List<Surface> listSurface = new ArrayList<>();
        if (surface != null) {
            mCaptureBuilder.addTarget(surface);
            listSurface.add(surface);
        }
        if (mImageReader != null) {
            mCaptureBuilder.addTarget(mImageReader.getSurface());
            listSurface.add(mImageReader.getSurface());
        }
        mCameraDevice.createCaptureSession(listSurface,
                new CameraCaptureSession.StateCallback() {

            @Override
            public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                mCaptureSession = cameraCaptureSession;
                updatePreview();
                mIsRecordingVideo = true;
                // TODO:
                // notify ui
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

            }
        }, mBackgroundHandler);
    }

    public void stopRecordingVideo() {
        mIsRecordingVideo = false;
        //closePreviewSession();
    }

    private void updatePreview() {
        if (null == mCameraDevice) {
            return;
        }
        try {
            setUpCaptureRequestBuilder(mCaptureBuilder);
            HandlerThread thread = new HandlerThread("CameraPreview");
            thread.start();
            mCaptureSession.setRepeatingRequest(mCaptureBuilder.build(), null, mBackgroundHandler);
            mState = State.CAPTURING;
            mListenerHandler.post(mListener::onCaptureStarted);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setUpCaptureRequestBuilder(CaptureRequest.Builder builder) {
        if (mImageReader != null) {
            builder.addTarget(mImageReader.getSurface());
        }
        builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        builder.set(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_DISABLED);
    }
}
