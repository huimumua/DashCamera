package com.askey.dvr.cdr7010.dashcam.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.RemoteException;
import android.util.Log;

import com.askey.dvr.cdr7010.dashcam.adas.AdasController;
import com.askey.dvr.cdr7010.dashcam.adas.AdasStateListener;
import com.askey.dvr.cdr7010.dashcam.core.StateMachine.EEvent;
import com.askey.dvr.cdr7010.dashcam.core.camera2.Camera2Controller;
import com.askey.dvr.cdr7010.dashcam.core.camera2.CameraControllerListener;
import com.askey.dvr.cdr7010.dashcam.core.recorder.Recorder;
import com.askey.dvr.cdr7010.dashcam.core.renderer.EGLRenderer;
import com.askey.dvr.cdr7010.dashcam.service.FileManager;
import com.askey.dvr.cdr7010.dashcam.util.Logg;
import com.askey.dvr.cdr7010.dashcam.util.SDcardHelper;
import com.askey.dvr.cdr7010.dashcam.util.SetUtils;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

public class DashCam implements DashCamControl {

    private String TAG = "DashCam";
    private final HandlerThread mHandlerThread;
    private final Handler mHandler;
    private Context mContext;
    private RecordConfig mConfig;
    private Camera2Controller mCamera2Controller;
    private EGLRenderer mRenderer;
    private Recorder mRecorder;
    private SurfaceTexture mSurfaceTexture;
    private StateCallback mStateCallback;
    private StateMachine mStateMachine;
    private AdasController mAdasController;
    private boolean mTerminating;

    /**
     * Functions which need to start Camera
     * Use Function enum & EnumSet to know which function is enabled
     * And does Camera needs to be re-start to disable some outputs (Surface/ImageReader)
     */
    private enum Function {
        RECORD, ADAS
    }

    // Current Enabled Functions
    private final EnumSet<Function> mEnabledFunctions = EnumSet.noneOf(Function.class);

    // User Set / Conditions Control may disable/enable functions and set to this variable
    private final EnumSet<Function> mSetEnabledFunctions = EnumSet.noneOf(Function.class);

    // Check Function is ready to start see @setFunctionReady
    private final EnumSet<Function> mReadyFunctions = EnumSet.noneOf(Function.class);


    private enum Error {
        RECORD_ERROR, CAMERA_ERROR, CAMERA_ACCESS_EXCEPTION
    }
    // Error flags to know what error was happened
    private final EnumSet<Error> mErrorFlag = EnumSet.noneOf(Error.class);

    public interface StateCallback {
        void onStarted();

        void onStoped();

        void onError();

        void onEventStateChanged(boolean on);

        void onEventCompleted(int eventId, long timestamp, List<String> pictures, String video);

        void onEventTerminated(int eventId, int reason);
    }

    private boolean mRecording = false;
    private Recorder.StateCallback mRecorderCallback = new Recorder.StateCallback() {
        @Override
        public void onStarted() {
            Logg.d(TAG, "RecorderStateCallback: onStarted");
            mRecording = true;
            if (mStateCallback != null) {
                mStateCallback.onStarted();
            }
        }

        @Override
        public void onStoped() {
            Logg.d(TAG, "RecorderStateCallback: onStoped");
            mRecorder.release();
            mRecorder = null;

            mRecording = false;
            checkCloseSuccess();
            if (mStateCallback != null) {
                mStateCallback.onStoped();
            }
        }

        @Override
        public void onInterrupted() {
            Logg.d(TAG, "RecorderStateCallback: onInterrupted");
            mRecording = false;
            mStateMachine.dispatchEvent(new StateMachine.Event(EEvent.ERROR,
                    "RecorderStateCallback: onInterrupted"));
            if (mStateCallback != null) {
                mStateCallback.onError();
            }
            checkCloseSuccess();
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

        @Override
        public void onEventTerminated(int eventId, int reason) {
            Logg.d(TAG, "RecorderStateCallback: onEventTerminated ");
            if (mStateCallback != null) {
                mStateCallback.onEventTerminated(eventId, reason);
            }
        }
    };

    public DashCam(Context context, RecordConfig config, StateCallback callback) {
        TAG = TAG + "-" + config.cameraId();
        mContext = context.getApplicationContext();
        mConfig = config;
        mStateCallback = callback;
        mStateMachine = new StateMachine(this, config.cameraId());
        mHandlerThread = new HandlerThread("TAG");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        if (config.adasEnable()) {
            mAdasController = AdasController.getsInstance();
        }
    }

    public boolean isBusy() {
        return mStateMachine.getCurrentState() != mStateMachine.STATE_CLOSE;
    }

    public void startVideoRecord(String reason) {
        Logg.d(TAG, "startVideoRecord " + reason);
        enableFunction(Function.RECORD);
    }

    public void stopVideoRecord(String reason) {
        Logg.d(TAG, "stopVideoRecord " + reason);
        disableFunction(Function.RECORD);
    }

    public void enableAdas(boolean enabled) {
        Logg.v(TAG, "enableAdas: enabled=" + enabled);
        if (mAdasController == null) {
            Logg.e(TAG, "enableAdas: mAdasController=null");
            return;
        }
        if (enabled) {
            mAdasController.init(mContext);
            mAdasController.addListener(mAdasStateListener);
            enableFunction(Function.ADAS);
        }
    }

    /**
     * Make this API a synchronized method for CameraRecordFragment to call
     * To ensure all resource are released before the Activity become Background
     */
    public void terminate() {
        Logg.v(TAG, "terminate");
        long startMillis = System.currentTimeMillis();
        waitState(mStateMachine.STATE_OPEN, 600);
        mTerminating = true;
        mSetEnabledFunctions.clear();
        mHandler.removeCallbacks(checkEnabledFunctions);
        mStateMachine.dispatchEvent(new StateMachine.Event(EEvent.CLOSE, "terminate"));
        waitState(mStateMachine.STATE_CLOSE, 600);
        mHandlerThread.quit();
        Logg.v(TAG, "terminate: elapsed = " +
                (System.currentTimeMillis() - startMillis) + " ms");
    }

    private void waitState(StateMachine.State state, long millis) {
        synchronized (mStateMachine) {
            long startNano = System.nanoTime();
            long elapsed = 0;
            while (mStateMachine.getCurrentState() != state && elapsed < millis) {
                try {
                    mStateMachine.wait(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    elapsed = (System.nanoTime() - startNano) / 1000000;
                }
            }
        }
    }

    private synchronized void enableFunction(Function function) {
        Logg.v(TAG, "enableFunction: " + function.name());
        if (mSetEnabledFunctions.contains(function)) {
            return;
        }
        mSetEnabledFunctions.add(function);
        mHandler.removeCallbacks(checkEnabledFunctions);
        mHandler.postDelayed(checkEnabledFunctions, 100);
    }

    private synchronized void disableFunction(Function function) {
        Logg.v(TAG, "disableFunction: " + function.name());
        if (!mSetEnabledFunctions.contains(function)) {
            return;
        }
        mSetEnabledFunctions.remove(function);
        mHandler.removeCallbacks(checkEnabledFunctions);
        mHandler.postDelayed(checkEnabledFunctions, 100);
    }

    private synchronized void clearFunctionReady() {
        mHandler.post(() -> {
            mReadyFunctions.clear();
        });

    }

    /**
     * A function may need sometimes to be ready.
     * This method set and check all functions is ready to start the Camera
     *
     * @param function the function which is ready to start
     */
    private void setFunctionReady(Function function) {
        mHandler.post(() -> {
            Logg.v(TAG, "setFunctionReady: " + function);
            if (mReadyFunctions.contains(function)) {
                return;
            }
            mReadyFunctions.add(function);
            Logg.v(TAG, "setFunctionReady: mReadyFunctions=" + mReadyFunctions + ", mSetEnabledFunctions=" + mSetEnabledFunctions);
            if (SetUtils.equals(mReadyFunctions, mSetEnabledFunctions)) {
                try {
                    startCamera();
                } catch (CameraAccessException e) {
                    goToErrorState(Error.CAMERA_ACCESS_EXCEPTION, e);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Check the changes of functions to change the state of StateMachine
     */
    private Runnable checkEnabledFunctions = () -> {
        Logg.v(TAG, "checkEnabledFunctions: currentTimeMillis=" + System.currentTimeMillis());
        Logg.v(TAG, "checkEnabledFunctions: mSetEnabledFunctions=" + mSetEnabledFunctions + ", mEnabledFunctions=" + mEnabledFunctions);
        if (SetUtils.equals(mSetEnabledFunctions, mEnabledFunctions)) {
            return;
        }

        StateMachine.EEvent event = null;
        String reason = null;
        if (!mEnabledFunctions.contains(Function.RECORD) && mSetEnabledFunctions.contains(Function.RECORD)) {
            event = StateMachine.EEvent.OPEN;
            reason = "No-RECORD --> RECORD";
        } else if (mEnabledFunctions.contains(Function.RECORD) && !mSetEnabledFunctions.contains(Function.RECORD)) {
            event = StateMachine.EEvent.CLOSE;
            reason = "RECORD --> No-RECORD";
        } else if (mSetEnabledFunctions.contains(Function.RECORD) || mSetEnabledFunctions.contains(Function.ADAS)) {
            event = StateMachine.EEvent.OPEN;
            reason = "RECORD or ADAS is enabled: " + mSetEnabledFunctions;
        } else if (mSetEnabledFunctions.size() == 0) {
            event = StateMachine.EEvent.CLOSE;
            reason = "RECORD and ADAS are both disabled";
        }

        if (event != null) {
            mStateMachine.dispatchEvent(new StateMachine.Event(event, reason));
        }

        mEnabledFunctions.clear();
        mEnabledFunctions.addAll(mSetEnabledFunctions);
    };

    public void mute() {
        mStateMachine.dispatchEvent(new StateMachine.Event(EEvent.MUTE, ""));
    }

    public void demute() {
        mStateMachine.dispatchEvent(new StateMachine.Event(EEvent.UNMUTE, ""));
    }


    @Override
    public void onOpenCamera() throws Exception {
        Logg.v(TAG, "onOpenCamera");
        clearFunctionReady();
        mCamera2Controller = new Camera2Controller(mContext, mCameraListener, mHandler);
        mCamera2Controller.startBackgroundThread();
        mCamera2Controller.open(mConfig.cameraId());
    }

    @Override
    public void onCameraClosed() {
        Logg.v(TAG, "onCameraClosed: mErrorFlag=" + mErrorFlag +
                ", mTerminating=" + mTerminating);
        if (mTerminating) {
            synchronized (mStateMachine) {
                mStateMachine.notify();
            }
            return;
        }
        if (mSetEnabledFunctions.size() != 0) {
            long delayMillis = 0;
            if (mErrorFlag.size() != 0) {
                // Any errors, wait for 3 seconds and try again
                delayMillis = 3000;
                mErrorFlag.clear();
            }
            String reason = "onCameraClosed but mSetEnabledFunctions = " + mSetEnabledFunctions;
            StateMachine.Event event = new StateMachine.Event(EEvent.OPEN, reason);
            mStateMachine.dispatchEventDelayed(event, delayMillis);
        }
    }

    @Override
    public void onStartVideoRecord() {
        Logg.d(TAG, "onStartVideoRecord");
        if (mTerminating) {
            synchronized (mStateMachine) {
                mStateMachine.notify();
            }
            return;
        }
    }

//    public void takeAPicture(final Handler handler) {
//        if (mRenderer != null) {
//            mRenderer.takeDisplaySnapshot(new EGLRenderer.SnapshotCallback() {
//                @Override
//                public void onSnapshotAvailable(byte[] data, int width, int height, long timeStamp) {
//                    Bitmap bmp = null;
//                    BufferedOutputStream bos = null;
//                    try {
//                        String filePathForPicture = FileManager.getInstance(mContext).getFilePathForPicture(mConfig.cameraId(), timeStamp);
//                        ByteBuffer buf = ByteBuffer.wrap(data);
//                        bos = new BufferedOutputStream(new FileOutputStream(filePathForPicture));
//                        bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//                        bmp.copyPixelsFromBuffer(buf);
//                        bmp = convertBmp(bmp);
//                        bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
//                        Location currentLocation = GPSStatusManager.getInstance().getCurrentLocation();
//                        Position position = null;
//                        if (currentLocation != null) {
//                            Logg.d(TAG, "currentLocation!=null,getLatitude==" + currentLocation.getLatitude() + ",getLongitude==" + currentLocation.getLongitude());
//                            position = new Position(currentLocation.getLatitude(), currentLocation.getLongitude());
//                        }
//                        Logg.d(TAG, "timeStamp==" + timeStamp);
//                        ExifHelper.build(filePathForPicture, timeStamp, position);
//                        // TODO: 2018/6/28 上傳文件
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    } finally {
//                        if (bmp != null) {
//                            bmp.recycle();
//                        }
//                        if (bos != null) {
//                            try {
//                                bos.flush();
//                                bos.close();
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
//                        handler.sendEmptyMessage(0);
//                    }
//                }
//            });
//        }
//    }

    private void startCamera() throws CameraAccessException {
        Logg.d(TAG, "startCamera");
        mCamera2Controller.startRecordingVideo();
        if (mRecorder != null) {
            mRecorder.startRecording();
        }
    }

    @Override
    public void onStopVideoRecord() {
        Logg.d(TAG, "onStopVideoRecord");
        mCamera2Controller.stopRecordingVideo();
        if (mAdasController != null) {
            mAdasController.stop();
        }
        try {
            mCamera2Controller.closeCamera();
        } catch (CameraAccessException e) {
            // Nothing we can do here, looks like must fix if any error occurs
            Logg.e(TAG, e.getMessage());
        } finally {
            if (mRenderer != null) {
                mRenderer.stop();
            }

            if (mRecorder != null) {
                mRecorder.stopRecording();
            }
            checkCloseSuccess();
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

    private CameraControllerListener mCameraListener = new CameraControllerListener() {
        @Override
        public void onCameraOpened() {
            Logg.v(TAG, "onCameraOpened");
            if (mSetEnabledFunctions.contains(Function.RECORD)) {
                try {
                    prepareRecorder();
                } catch (IOException e) {
                    goToErrorState(Error.RECORD_ERROR, e);
                    return;
                }
            }
            if (mAdasController != null) {
                prepareAdas(mCamera2Controller);
            }
        }

        @Override
        public void onCameraClosed() {
            Logg.v(TAG, "onCameraClosed");
        }

        @Override
        public void onCaptureStarted() {
            Logg.v(TAG, "onCaptureStarted");
            mStateMachine.dispatchEvent(new StateMachine.Event(StateMachine.EEvent.OPEN_SUCCESS, "onCaptureStarted"));
        }

        @Override
        public void onCaptureStopped() {
            Logg.v(TAG, "onCaptureStopped");
            checkCloseSuccess();
        }

        @Override
        public void onCameraError(int error) {
            Logg.e(TAG, "onCameraError: error=" + error);
            goToErrorState(Error.CAMERA_ERROR, new CameraAccessException(error));
        }
    };

    private void checkCloseSuccess() {
        if (mAdasController != null) {
            // Check ADAS
            Log.v(TAG, "checkCloseSuccess: mCamera2Controller.getState()=" + mCamera2Controller.getState() +
                    ", mRecording=" + mRecording +
                    ", mAdasController.getState()=" + mAdasController.getState());
            if (mCamera2Controller.getState() == Camera2Controller.State.STOPPED
                    && mRecording == false
                    && (mAdasController.getState() == AdasController.State.Stopped || mAdasController.getState() == AdasController.State.Uninitialized)) {
                mStateMachine.dispatchEvent(new StateMachine.Event(StateMachine.EEvent.CLOSE_SUCCESS, "checkCloseSuccess"));
            }
        } else {
            // No Check ADAS
            Log.v(TAG, "checkCloseSuccess: mCamera2Controller.getState()=" + mCamera2Controller.getState() +
                    ", mRecording=" + mRecording);
            if (mCamera2Controller.getState() == Camera2Controller.State.STOPPED
                    && mRecording == false) {
                mStateMachine.dispatchEvent(new StateMachine.Event(StateMachine.EEvent.CLOSE_SUCCESS, "checkCloseSuccess"));
            }

        }
    }

    private void prepareRecorder() throws IOException {
        Logg.v(TAG, "prepareRecorder");

        try {
            int sdcardStatus = FileManager.getInstance(mContext).checkSdcardAvailable();
            if (!SDcardHelper.isSDCardAvailable(sdcardStatus)) {
                throw new IOException("!SDcardHelper.isSDCardAvailable(sdcardStatus)");
            }
        } catch (RemoteException e) {
            throw new IOException(e);
        }

        mRecorder = new Recorder(mContext, mConfig, mRecorderCallback);
        mRecorder.prepare();

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
                            mCamera2Controller.setSurface(surfaceTexture);
                            setFunctionReady(Function.RECORD);
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

    private void prepareAdas(Camera2Controller camera2Controller) {
        Logg.v(TAG, "prepareAdas");
        if (mAdasController != null) {
            mAdasController.start();
        }
    }

    private static Bitmap convertBmp(Bitmap bmp) {
        int w = bmp.getWidth();
        int h = bmp.getHeight();
        Bitmap convertBmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);// 创建一个新的和SRC长度宽度一样的位图
        Canvas cv = new Canvas(convertBmp);
        Matrix matrix = new Matrix();
//        matrix.postScale(1, -1); //镜像垂直翻转
        matrix.postScale(-1, 1); //镜像水平翻转
        matrix.postRotate(-180); //旋转-180度
        Bitmap newBmp = Bitmap.createBitmap(bmp, 0, 0, w, h, matrix, true);
        cv.drawBitmap(newBmp, new Rect(0, 0, newBmp.getWidth(), newBmp.getHeight()), new Rect(0, 0, w, h), null);
        newBmp.recycle();
        bmp.recycle();
        return convertBmp;
    }

    private void goToErrorState(Error error, Exception e) {
        Logg.w(TAG, e.getMessage());
        mErrorFlag.add(error);
        if (mStateCallback != null) {
            mStateCallback.onError();
        }
        mStateMachine.dispatchEvent(new StateMachine.Event(EEvent.ERROR, e.getMessage()));
    }

    private AdasStateListener mAdasStateListener = new AdasStateListener() {
        @Override
        public void onStateChanged(AdasController.State state) {
            Logg.v(TAG, "mAdasStateListener-onStateChanged: " + state);
            switch (state) {
                case Started:
                    mCamera2Controller.setImageReader(mAdasController.getImageReader());
                    setFunctionReady(Function.ADAS);
                    break;
                case Stopped:
                    checkCloseSuccess();
                    if (mTerminating) {
                        mAdasController.finish();
                    }
                    break;
                case Uninitialized:
                    mAdasController.removeListener(mAdasStateListener);
                    break;
            }
        }
    };
}
