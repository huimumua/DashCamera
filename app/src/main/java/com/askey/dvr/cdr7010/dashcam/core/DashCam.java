package com.askey.dvr.cdr7010.dashcam.core;


import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.Surface;

import com.askey.dvr.cdr7010.dashcam.core.camera2.Camera2Controller;
import com.askey.dvr.cdr7010.dashcam.core.recorder.Recorder;

import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class DashCam {

    private static final String TAG = "DashCam";
    private Context mContext;
    private Camera2Controller mCameraController;
    private SurfaceTexture mSurfaceTexture;
    private Recorder mRecorder;
    private Semaphore mRecordLock = new Semaphore(1);

    public DashCam(Context context) {
        mContext = context.getApplicationContext();
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
        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
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
                mRecorder = new Recorder(mContext, new Recorder.InterruptedCallback() {
                    @Override
                    public void onInterrupted() {
                        Log.d(TAG, "recorder interrupted");
                        release();
                        mRecorder.release();
                        mRecorder = null;
                    }
                });
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

            if (mRecorder != null) {
                mRecorder.stopRecording();
                mRecorder = null;
                mCameraController.stopRecordingVideo();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mRecordLock.release();
        }
    }
}
