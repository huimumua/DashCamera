package com.askey.dvr.cdr7010.dashcam.core;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.media.Image;
import android.media.ImageReader;

import com.askey.dvr.cdr7010.dashcam.adas.AdasController;
import com.askey.dvr.cdr7010.dashcam.core.camera2.Camera2Controller;
import com.askey.dvr.cdr7010.dashcam.core.nmea.NmeaRecorder;
import com.askey.dvr.cdr7010.dashcam.core.recorder.Recorder;
import com.askey.dvr.cdr7010.dashcam.core.renderer.EGLRenderer;
import com.askey.dvr.cdr7010.dashcam.service.FileManager;
import com.askey.dvr.cdr7010.dashcam.util.Logg;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class DashCam implements DashCamControl{

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
    private StateMachine mStateMachine;
    private AdasController mAdasController;

    public interface StateCallback {
        void onStarted();
        void onStoped();
        void onError();
        void onEventStateChanged(boolean on);
        void onEventCompleted(int evevtId, long timestamp, List<String> pictures, String video);
    }

    private Recorder.StateCallback mRecorderCallback = new Recorder.StateCallback() {
        @Override
        public void onStarted() {
            Logg.d(TAG, "RecorderStateCallback: onStarted");
            mIsRunning = true;
            mStateMachine.dispatchEvent(new StateMachine.Event(StateMachine.EVENT_OPEN_SUCCESS, ""));
            if (mStateCallback != null) {
                mStateCallback.onStarted();
            }
            mRecordLock.release();
        }

        @Override
        public void onStoped() {
            Logg.d(TAG, "RecorderStateCallback: onStoped");
            mIsRunning = false;
            mStateMachine.dispatchEvent(new StateMachine.Event(StateMachine.EVENT_CLOSE_SUCCESS, ""));
            if (mStateCallback != null) {
                mStateCallback.onStoped();
            }
            mRecordLock.release();
        }

        @Override
        public void onInterrupted() {
            Logg.d(TAG, "RecorderStateCallback: onInterrupted");
            mIsRunning = false;
            mStateMachine.dispatchEvent(new StateMachine.Event(StateMachine.EVENT_ERROR, ""));
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

        @Override
        public void onEventCompleted(int eventId, long timestamp, List<String> pictures, String video) {
            Logg.d(TAG, "RecorderStateCallback: onEventCompleted ");
            if (mStateCallback != null) {
                mStateCallback.onEventCompleted(eventId, timestamp, pictures, video);
            }
        }
    };

    public DashCam(Context context, RecordConfig config, StateCallback callback) {
        mContext = context.getApplicationContext();
        mConfig = config;
        mStateCallback = callback;
        mStateMachine = new StateMachine(this);
        mAdasController = AdasController.getsInstance();
    }

    public void startVideoRecord(String reason) {
        Logg.d(TAG, "startVideoRecord " + reason);
        mStateMachine.dispatchEvent(new StateMachine.Event(StateMachine.EVENT_OPEN, reason));
    }

    public void stopVideoRecord(String reason) {
        Logg.d(TAG, "stopVideoRecord " + reason);
        mStateMachine.dispatchEvent(new StateMachine.Event(StateMachine.EVENT_CLOSE, reason));
    }

    public void mute() {
        mStateMachine.dispatchEvent(new StateMachine.Event(StateMachine.EVENT_AUDIO_MUTE, ""));
    }

    public void demute() {
        mStateMachine.dispatchEvent(new StateMachine.Event(StateMachine.EVENT_AUDIO_DEMUTE, ""));
    }

    @Override
    public void onStartVideoRecord() throws Exception {
        Logg.d(TAG, "onStartVideoRecord");
        mAdasController.start(mContext);
        ImageReader imageReader = ImageReader.newInstance(1280, 720, ImageFormat.YUV_420_888, 6);
        imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                Image image = reader.acquireLatestImage();
                if (image != null) {
                    mAdasController.process(image);
                    image.close();
                }
            }
        }, null);


        boolean sdcardAvailable = FileManager.getInstance(mContext).isSdcardAvailable();
        if (!sdcardAvailable) {
            if (mStateCallback != null) {
                mStateCallback.onError();
            }
            throw new RuntimeException("sd card unavailable");
        }

        if (mIsRunning) {
            return;
        }

        if (!mRecordLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
            if (mStateCallback != null) {
                mStateCallback.onError();
            }
            throw new RuntimeException("Time out waiting to lock recorder start.");
        }

        mCamera2Controller = new Camera2Controller(mContext);
        mCamera2Controller.setImageReader(imageReader);
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
        NmeaRecorder.init(mContext);
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

    @Override
    public void onStopVideoRecord() {
        Logg.d(TAG, "onStopVideoRecord");

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
            NmeaRecorder.deinit(mContext);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMuteAudio() {
        Logg.d(TAG, "onMuteAudio");
        if (mRecorder != null) {
            mRecorder.mute();
        }
    }

    @Override
    public void onDemuteAudio() {
        Logg.d(TAG, "onDemuteAudio");
        if (mRecorder != null) {
            mRecorder.demute();
        }
    }
}
