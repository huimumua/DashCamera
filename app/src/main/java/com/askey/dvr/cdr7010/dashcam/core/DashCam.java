package com.askey.dvr.cdr7010.dashcam.core;

import android.content.Context;
import android.graphics.SurfaceTexture;

import com.askey.dvr.cdr7010.dashcam.core.camera2.Camera2Controller;
import com.askey.dvr.cdr7010.dashcam.core.recorder.Recorder;
import com.askey.dvr.cdr7010.dashcam.core.renderer.EGLRenderer;
import com.askey.dvr.cdr7010.dashcam.util.Logg;
import com.askey.dvr.cdr7010.dashcam.util.SDCardUtils;

import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class DashCam {

    private static final String TAG = "DashCam";
    private Context mContext;
    private RecordConfig mConfig;
    private Camera2Controller mCamera2Controller;
    private EGLRenderer mRenderer;
    private Recorder mRecorder;
    private SurfaceTexture mSurfaceTexture;
    private Semaphore mRecordLock = new Semaphore(1);
    private StateCallback mStateCallback;
    private boolean mIsRunning;

    public interface StateCallback {
        void onStarted();
        void onStoped();
        void onError();
        void onEventStateChanged(boolean on);
    }

    private Recorder.StateCallback mRecorderCallback = new Recorder.StateCallback() {
        @Override
        public void onStarted() {
            Logg.d(TAG, "RecorderStateCallback: onStarted");
            mIsRunning = true;
            if (mStateCallback != null) {
                mStateCallback.onStarted();
            }
            mRecordLock.release();
        }

        @Override
        public void onStoped() {
            Logg.d(TAG, "RecorderStateCallback: onStoped");
            mIsRunning = false;
            if (mStateCallback != null) {
                mStateCallback.onStoped();
            }
            mRecordLock.release();
        }

        @Override
        public void onInterrupted() {
            Logg.d(TAG, "RecorderStateCallback: onInterrupted");
            mIsRunning = false;

            if (mStateCallback != null) {
                mStateCallback.onError();
            }
            mRecordLock.release();
        }

        @Override
        public void onEventStateChanged(boolean on) {
            Logg.d(TAG, "RecorderStateCallback: onEventStateChanged " + on);
            if (mStateCallback != null) {
                mStateCallback.onEventStateChanged(on);
            }
        }
    };

    public DashCam(Context context, RecordConfig config, StateCallback callback) {
        mContext = context.getApplicationContext();
        mConfig = config;
        mStateCallback = callback;
    }

    public void startVideoRecord() throws IOException {
        Logg.d(TAG, "startVideoRecord");

        if (!SDCardUtils.isSDCardEnable()) {
            if (mStateCallback != null) {
                mStateCallback.onError();
            }
            throw new IOException("SD Card unavailable");
        }

        if (mIsRunning) {
            return;
        }

        try {
            if (!mRecordLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                if (mStateCallback != null) {
                    mStateCallback.onError();
                }
                throw new RuntimeException("Time out waiting to lock recorder start.");
            }

            mCamera2Controller = new Camera2Controller(mContext);
            mCamera2Controller.startBackgroundThread();
            if (mConfig.cameraId() == 0) {
                mCamera2Controller.open(Camera2Controller.CAMERA.MAIN);
            } else {
                mCamera2Controller.open(Camera2Controller.CAMERA.EXT);
            }

            mRecorder = new Recorder(mContext, mConfig, mRecorderCallback);
            try {
                mRecorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mRenderer = new EGLRenderer(mContext,
                    mConfig.videoStampEnable(),
                    new EGLRenderer.OnSurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                    mSurfaceTexture = surfaceTexture;
                    mRenderer.createEncoderSurface(mRecorder.getInputSurface(), mRecorder);
                    startInternal();
                }

                @Override
                public void onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                    mSurfaceTexture = null;
                }
            });
            mRenderer.start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startInternal() {
        Logg.d(TAG, "startInternal");
        if (mRecorder != null) {
            mRecorder.startRecording();
        }

        if (mCamera2Controller != null) {
            mCamera2Controller.startRecordingVideo(mSurfaceTexture);
        }
    }

    public void stopVideoRecord() {
        Logg.d(TAG, "stopVideoRecord");

        if (!mIsRunning) {
            return;
        }

        try {
            if (!mRecordLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock recorder stop.");
            }

            mCamera2Controller.stopRecordingVideo();
            mCamera2Controller.closeCamera();
            mCamera2Controller = null;

            if (mRenderer != null) {
                mRenderer.stop();
            }

            if (mRecorder != null) {
                mRecorder.stopRecording();
                mRecorder.release();
                mRecorder = null;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void mute() {
        Logg.d(TAG, "mute");
        if (mRecorder != null) {
            mRecorder.mute();
        }
    }

    public void demute() {
        Logg.d(TAG, "demute");
        if (mRecorder != null) {
            mRecorder.demute();
        }
    }
}
