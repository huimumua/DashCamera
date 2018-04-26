package com.askey.dvr.cdr7010.dashcam.core;

import android.content.Context;
import android.util.Log;
import android.view.Surface;

import com.askey.dvr.cdr7010.dashcam.core.camera2.Camera2Controller;
import com.askey.dvr.cdr7010.dashcam.core.recorder.Recorder;
import com.askey.dvr.cdr7010.dashcam.util.Logg;

import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class DashCam {

    private static final String TAG = "DashCam";
    private Context mContext;
    private Camera2Controller mCameraController;
    private Recorder mRecorder;
    private Semaphore mRecordLock = new Semaphore(1);
    private StateCallback mStateCallback;

    public interface StateCallback {
        void onStarted();
        void onStoped();
        void onEventStateChanged(boolean on);
    }

    private Recorder.StateCallback mRecorderCallback = new Recorder.StateCallback() {
        @Override
        public void onStarted() {
            Logg.d(TAG, "RecorderStateCallback: onStarted");
            if (mStateCallback != null) {
                mStateCallback.onStarted();
            }
        }

        @Override
        public void onStoped() {
            Logg.d(TAG, "RecorderStateCallback: onStoped");
            if (mStateCallback != null) {
                mStateCallback.onStoped();
            }

        }

        @Override
        public void onInterrupted() {
            Logg.d(TAG, "RecorderStateCallback: onInterrupted");

            release();
            mRecorder.release();
            mRecorder = null;
        }

        @Override
        public void onEventStateChanged(boolean on) {
            Logg.d(TAG, "RecorderStateCallback: onEventStateChanged " + on);
            if (mStateCallback != null) {
                mStateCallback.onEventStateChanged(on);
            }

        }
    };

    public DashCam(Context context, StateCallback callback) {
        mContext = context.getApplicationContext();
        mStateCallback = callback;
        mCameraController = new Camera2Controller(context);
    }

    public void prepare() {
        mCameraController.startBackgroundThread();
        mCameraController.open(Camera2Controller.CAMERA.MAIN);
    }

    public void release() {
        if (mCameraController != null) {
            mCameraController.closeCamera();
        }
    }

    public void startPreview() {
        if (mCameraController != null) {
            mCameraController.startPreview();
        }
    }

    public void setPreviewSurface(Surface surface) {
        mCameraController.addSurface(surface);
    }

    public void startVideoRecord() {
        Log.d(TAG, "startVideoRecord");
        try {
            if (!mRecordLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock recorder start.");
            }

            if (mCameraController != null && mRecorder == null) {
                mRecorder = new Recorder(mContext, mRecorderCallback);
                mRecorder.prepare();
                mRecorder.startRecording();
                mCameraController.addSurface(mRecorder.getInputSurface());
                mCameraController.startRecordingVideo();
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        } finally {
            mRecordLock.release();
        }

    }

    public void stopVideoRecord() {
        Log.d(TAG, "stopVideoRecord");
        try {
            if (!mRecordLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock recorder stop.");
            }

            mCameraController.stopRecordingVideo();
            mCameraController.closeCamera();
            if (mRecorder != null) {
                mRecorder.stopRecording();
                mRecorder = null;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mRecordLock.release();
        }
    }
}
