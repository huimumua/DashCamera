package com.askey.dvr.cdr7010.dashcam.core;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.media.Image;
import android.media.ImageReader;

import com.askey.dvr.cdr7010.dashcam.adas.AdasController;
import com.askey.dvr.cdr7010.dashcam.core.StateMachine.EEvent;
import com.askey.dvr.cdr7010.dashcam.core.camera2.Camera2Controller;
import com.askey.dvr.cdr7010.dashcam.core.nmea.NmeaRecorder;
import com.askey.dvr.cdr7010.dashcam.core.recorder.Recorder;
import com.askey.dvr.cdr7010.dashcam.core.renderer.EGLRenderer;
import com.askey.dvr.cdr7010.dashcam.service.FileManager;
import com.askey.dvr.cdr7010.dashcam.util.Logg;

import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class DashCam implements DashCamControl {

    private static final String TAG = "DashCam";
    private Context mContext;
    private RecordConfig mConfig;
    private Camera2Controller mCamera2Controller;
    private EGLRenderer mRenderer;
    private Recorder mRecorder;
    private SurfaceTexture mSurfaceTexture;
    private StateCallback mStateCallback;
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
            mStateMachine.dispatchEvent(new StateMachine.Event(EEvent.OPEN_SUCCESS, ""));
            if (mStateCallback != null) {
                mStateCallback.onStarted();
            }
        }

        @Override
        public void onStoped() {
            Logg.d(TAG, "RecorderStateCallback: onStoped");
            mStateMachine.dispatchEvent(new StateMachine.Event(EEvent.CLOSE_SUCCESS, ""));
            if (mStateCallback != null) {
                mStateCallback.onStoped();
            }
        }

        @Override
        public void onInterrupted() {
            Logg.d(TAG, "RecorderStateCallback: onInterrupted");
            mStateMachine.dispatchEvent(new StateMachine.Event(EEvent.ERROR, ""));
            if (mStateCallback != null) {
                mStateCallback.onError();
            }
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

    public boolean isBusy() {
        return mStateMachine.getCurrentState() != mStateMachine.STATE_CLOSE;
    }

    public void startVideoRecord(String reason) {
        Logg.d(TAG, "startVideoRecord " + reason);
        mStateMachine.dispatchEvent(new StateMachine.Event(EEvent.OPEN, reason));
    }

    public void stopVideoRecord(String reason) {
        Logg.d(TAG, "stopVideoRecord " + reason);
        mStateMachine.dispatchEvent(new StateMachine.Event(EEvent.CLOSE, reason));
    }

    public void mute() {
        mStateMachine.dispatchEvent(new StateMachine.Event(EEvent.MUTE, ""));
    }

    public void demute() {
        mStateMachine.dispatchEvent(new StateMachine.Event(EEvent.UNMUTE, ""));
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

        mCamera2Controller = new Camera2Controller(mContext);
        mCamera2Controller.setImageReader(imageReader);
        mCamera2Controller.startBackgroundThread();
        if (mConfig.cameraId() == 0) {
            mCamera2Controller.open(Camera2Controller.CAMERA.MAIN);
        } else {
            mCamera2Controller.open(Camera2Controller.CAMERA.EXT);
        }

        mRecorder = new Recorder(mContext, mConfig, mRecorderCallback);
        mRecorder.prepare();

        NmeaRecorder.init(mContext);
        mRenderer = new EGLRenderer(mContext,
                mConfig.videoWidth(),
                mConfig.videoHeight(),
                mConfig.videoStampEnable(),
                new EGLRenderer.OnSurfaceTextureListener() {
                    @Override
                    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                        mSurfaceTexture = surfaceTexture;
                        mRenderer.createEncoderSurface(mRecorder.getInputSurface(), mRecorder);
                        try {
                            startInternal();
                        } catch (Exception e) {
                            mStateMachine.dispatchEvent(new StateMachine.Event(EEvent.ERROR, ""));
                        }
                    }

                    @Override
                    public void onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                        mSurfaceTexture = null;
                    }
                });
        mRenderer.start();
    }

    public void takeAPicture(EGLRenderer.SnapshotCallback callback) {
        if (mRenderer != null) {
            mRenderer.takeDisplaySnapshot(callback);
        }
    }

    private void startInternal() throws Exception {
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
        try {
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
        } catch (Exception e) {
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
