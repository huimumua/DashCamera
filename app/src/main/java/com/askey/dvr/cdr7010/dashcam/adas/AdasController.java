package com.askey.dvr.cdr7010.dashcam.adas;

import android.content.Context;
import android.graphics.ImageFormat;
import android.location.Location;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.askey.dvr.cdr7010.dashcam.logic.GlobalLogic;
import com.askey.dvr.cdr7010.dashcam.service.GPSStatusManager;
import com.askey.dvr.cdr7010.dashcam.util.TimesPerSecondCounter;
import com.askey.platform.AskeySettings;
import com.jvckenwood.adas.detection.Detection;
import com.jvckenwood.adas.detection.FC_INPUT;
import com.jvckenwood.adas.util.Constant;
import com.jvckenwood.adas.util.FC_PARAMETER;
import com.jvckenwood.adas.util.Util;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Queue;

import static com.jvckenwood.adas.util.Constant.ADAS_ERROR_DETECT_ALREADY_RUNNING_DETECTION;

public class AdasController implements Util.AdasCallback, AdasStateListener {
    private static final String TAG = AdasController.class.getSimpleName();
    private static final int CAR_TYPE_NUM = 7;
    private static final int[] INSTALLATION_HEIGHTS = new int[] {120, 135, 120, 135, 120, 135, 200};
    private static final int[] VEHICLE_WIDTHS = new int[] {148, 148, 170, 170, 180, 190, 200};
    private static final int[] VEHICLE_POINT_DISTANCES = new int[] {130, 90, 180, 190, 180, 190, 50};
    private static final boolean DEBUG = false;
    private static final int ADAS_IMAGE_WIDTH = 1280;
    private static final int ADAS_IMAGE_HEIGHT = 720;
    protected static final int BUFFER_NUM = 3;

    /* Properties for debug */
    private static boolean EXCEPTION_WHEN_ERROR;
    private static boolean DEBUG_FPS;
    private static boolean ADAS_DISABLED;
    private static boolean DEBUG_IMAGE_PROCESS;

    /* Handler Messages */
    private static final int MSG_PROCESS = 0;
    private static final int MSG_DID_ADAS_DETECT = 1;
    private static final String PROP_DEBUG_FPS = "persist.dvr.adas.debug_fps";
    private static final String PROP_ADAS_DISABLED = "persist.dvr.adas.disabled";
    private static final String PROP_EXCEPTION = "persist.dvr.adas.exception";
    private static final String PROP_DEBUG_PROCESS = "persist.dvr.adas.dbg_proc";
    private static final String PROP_FAKE_SPEED = "persist.dvr.adas.speed";

    private static AdasController sInstance;

    private Util mAdasImpl;
    private GlobalLogic mGlobalSetting;
    private final TimesPerSecondCounter mTpsc;
    private final TimesPerSecondCounter mTpscDidAdas;
    private final TimesPerSecondCounter mTpscFrameDrop;
    private FC_INPUT mFcInput;
    private Queue<ImageRecord> mProcessingImages;
    private Handler mHandler;
    private List<WeakReference<AdasStateListener>> mListeners;
    private ImageReader mImageReader;
    private boolean mEnabled = true; // TODO: false default
    private AdasStateControl mStateControl;
    private float mFakeSpeed = 0;

    private enum State {
        Uninitialized, Initialing, Initialized, Stopping
    }

    private State mState;

    private AdasController() {
        if (sInstance != null) {
            throw new RuntimeException("Singleton instance is already created!");
        }
        mState = State.Uninitialized;
        mAdasImpl = Util.getInstance();
        mGlobalSetting = GlobalLogic.getInstance();
        mTpsc = new TimesPerSecondCounter(TAG);
        mTpscDidAdas = new TimesPerSecondCounter(TAG + "_didAdas");
        mTpscFrameDrop = new TimesPerSecondCounter(TAG + "_frameDrop");
        mFcInput = new FC_INPUT();
        mProcessingImages = new LinkedList<>();
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mHandler = new AdasHandler(handlerThread.getLooper());
        mListeners = new ArrayList<>();
        mStateControl = new JkcAdasStateControl();
        mStateControl.addListener(this);
        ADAS_DISABLED = SystemPropertiesProxy.getBoolean(PROP_ADAS_DISABLED, false);
        Log.v(TAG, "AdasController: ADAS_DISABLED = " + ADAS_DISABLED);
        DEBUG_FPS = SystemPropertiesProxy.getBoolean(PROP_DEBUG_FPS, false);
        Log.v(TAG, "AdasController: DEBUG_FPS = " + DEBUG_FPS);
        EXCEPTION_WHEN_ERROR = SystemPropertiesProxy.getBoolean(PROP_EXCEPTION, false);
        Log.v(TAG, "AdasController: EXCEPTION_WHEN_ERROR = " + EXCEPTION_WHEN_ERROR);
        DEBUG_IMAGE_PROCESS = SystemPropertiesProxy.getBoolean(PROP_DEBUG_PROCESS, false);
        Log.v(TAG, "AdasController: DEBUG_IMAGE_PROCESS = " + DEBUG_IMAGE_PROCESS);
        mFakeSpeed = SystemPropertiesProxy.getInt(PROP_FAKE_SPEED, 700); // TODO: default 0 to get real speed by default
        Log.v(TAG, "AdasController: mFakeSpeed = " + mFakeSpeed);
    }

    public static AdasController getsInstance() {
        if (sInstance == null) {
            synchronized (AdasController.class) {
                if (sInstance == null) {
                    sInstance = new AdasController();
                }
            }
        }
        return sInstance;
    }

    public void init(Context context) {
        Log.i(TAG, "init");
        if (mState != State.Uninitialized) {
            handleError("finish", "Unexpected mState = " + mState);
        }

        if (ADAS_DISABLED) {
            Log.w(TAG, "init: not init because ADAS_DISABLED");
            return;
        }
        if (INSTALLATION_HEIGHTS.length != CAR_TYPE_NUM) {
            throw new RuntimeException("INSTALLATION_HEIGHTS not defined well");
        }
        if (VEHICLE_POINT_DISTANCES.length != CAR_TYPE_NUM) {
            throw new RuntimeException("VEHICLE_POINT_DISTANCES not defined well");
        }
        if (VEHICLE_WIDTHS.length != CAR_TYPE_NUM) {
            throw new RuntimeException("VEHICLE_WIDTHS not defined well");
        }

        mState = State.Initialing;
        FC_PARAMETER fp = new FC_PARAMETER();
        fp.PitchAngle = mGlobalSetting.getInt(AskeySettings.Global.ADAS_PITCH_ANGLE);
        fp.YawhAngle = mGlobalSetting.getInt(AskeySettings.Global.ADAS_YAW_ANGLE);
        fp.HorizonY = (int) (mGlobalSetting.getInt(AskeySettings.Global.ADAS_SKYLINE_RANGE) * getYscale());
        fp.BonnetY = (int) (mGlobalSetting.getInt(AskeySettings.Global.ADAS_BONNETY) * getYscale());
        fp.CenterX = (int) (mGlobalSetting.getInt(AskeySettings.Global.ADAS_CENTERX) * getXscale());
        int carType = mGlobalSetting.getInt(AskeySettings.Global.CAR_TYPE);
        fp.InstallationHeight = getInstallationHeight(carType);
        fp.CenterDiff = mGlobalSetting.getInt(AskeySettings.Global.ADAS_MOUNT_POSITION);
        fp.VehicleWidth = getVehicleWidth(carType);
        fp.VehiclePointDistance = getVehiclePointDistance(carType);
        fp.SelectIP = getSelectIP();
        fp.CarCollisionSpeed = mGlobalSetting.getInt(AskeySettings.Global.ADAS_CAR_COLLISION_SPEED);
        fp.CarCollisionTime = mGlobalSetting.getInt(AskeySettings.Global.ADAS_CAR_COLLISION_TIME);
        fp.LaneDepartureSpeed = mGlobalSetting.getInt(AskeySettings.Global.ADAS_LANE_DEPARTURE_SPEED);
        fp.LaneDepartureRange = mGlobalSetting.getInt(AskeySettings.Global.ADAS_LANE_DEPARTURE_RANGE);
        fp.LaneDepartureTime = mGlobalSetting.getInt(AskeySettings.Global.ADAS_LANE_DEPARTURE_TIME);
        fp.DepartureDelay = mGlobalSetting.getInt(AskeySettings.Global.ADAS_DELAY_START_DISTANCE);
        fp.DepartureRange = mGlobalSetting.getInt(AskeySettings.Global.ADAS_DELAY_START_RANGE);
        fp.PedCollisionSpeed = mGlobalSetting.getInt(AskeySettings.Global.ADAS_PED_COLLISION_TIME);
        fp.PedCollisionWidth = mGlobalSetting.getInt(AskeySettings.Global.ADAS_PED_COLLISION_WIDTH);
        fp.PedCollisionLowSpeed = mGlobalSetting.getInt(AskeySettings.Global.ADAS_PED_COLLISION_SPEED_LOW);
        fp.PedCollisionHighSpeed = mGlobalSetting.getInt(AskeySettings.Global.ADAS_PED_COLLISION_SPEED_HIGH);
        Log.v(TAG, "initAdas: FC_PARAMETER = " + fp);
        int result = mAdasImpl.initAdas(fp, context, this);
        if (result != 0) {
            handleError("init", "initAdas() failed with return value = " + result);
        }

        mHandler.postDelayed(() -> mState = State.Initialized, 300);
    }
    private int getVehiclePointDistance(int carType) {
        return VEHICLE_POINT_DISTANCES[carType];
    }

    private int getVehicleWidth(int carType) {
        return VEHICLE_WIDTHS[carType];
    }

    private int getInstallationHeight(int carType) {
        return INSTALLATION_HEIGHTS[carType];
    }

    private int getSelectIP() {
        int result = 0;
        boolean bFCWS = (1 == mGlobalSetting.getInt(AskeySettings.Global.ADAS_FCWS));
        boolean bLDS = (1 == mGlobalSetting.getInt(AskeySettings.Global.ADAS_LDS));
        boolean bDelayStart = (1 == mGlobalSetting.getInt(AskeySettings.Global.ADAS_DELAY_START));
        boolean bPedColl = (1 == mGlobalSetting.getInt(AskeySettings.Global.ADAS_PEDESTRIAN_COLLISION));
        if (bFCWS) {
            result |= 0x01;
        }
        if (bLDS) {
            result |= 0x02;
        }
        if (bDelayStart) {
            result |= 0x04;
        }
        if (bPedColl) {
            result |= 0x08;
        }
        return result;
    }

    private void process(Image image) {
        if (DEBUG_FPS) mTpsc.update();
        if (ADAS_DISABLED) {
            image.close();
            return;
        }

        Message msg = mHandler.obtainMessage(MSG_PROCESS, 0, 0, image);
        msg.sendToTarget();
    }

    private void process_internal(Image image) {
        if (DEBUG) {
            Log.v(TAG, "process_internal: image = " + image);
        }

        mFcInput.VehicleSpeed = (int) (getSpeed() * 10);
        long timestamp = image.getTimestamp() / 1000000; // nano to ms
        mFcInput.CaptureTime = timestamp / 1000;
        mFcInput.CaptureMilliSec = timestamp % 1000;
        ImageRecord imageRecord = ImageRecord.obtain(mFcInput.CaptureMilliSec, image);

        if (Detection.isRunningDetection()) {
            if (DEBUG_FPS) {
                mTpscFrameDrop.update();
            }
            imageRecord.recycle();
            return;
        }

        ByteBuffer y = image.getPlanes()[0].getBuffer();
        ByteBuffer u = image.getPlanes()[1].getBuffer();
        ByteBuffer v = image.getPlanes()[2].getBuffer();
        int result = Detection.adasDetect(y, u, v, mFcInput);

        if (result == ADAS_ERROR_DETECT_ALREADY_RUNNING_DETECTION) {
            if (DEBUG_IMAGE_PROCESS) {
                Log.v(TAG, "process_internal: ADAS_ERROR_DETECT_ALREADY_RUNNING_DETECTION: close " + imageRecord);
            }
            if (DEBUG_FPS) {
                mTpscFrameDrop.update();
            }
            imageRecord.recycle();
        } else if (result != Constant.ADAS_SUCCESS) {
            Log.e(TAG, "process_internal: adasDetect() result = " + result + ", close " + imageRecord);
            imageRecord.recycle();
        } else {
            assert result == Constant.ADAS_SUCCESS;

            mProcessingImages.add(imageRecord);
            if (DEBUG_IMAGE_PROCESS) {
                Log.v(TAG, "process_internal: new image queued: " + mProcessingImages.size()
                        + ", " + imageRecord + ", speed = " + mFcInput.VehicleSpeed);
            }

        }
    }

    private float getSpeed() {
        if (mFakeSpeed != 0) {
            return mFakeSpeed;
        }
        Location location = GPSStatusManager.getInstance().getCurrentLocation();
        if (location == null) {
            return 0;
        }
        return location.getSpeed();
    }

    public synchronized void finish() {
        Log.v(TAG, "finish");
        if (ADAS_DISABLED) {
            return;
        }

        if (mState != State.Initialized) {
            handleError("finish", "Unexpected mState = " + mState);
        }
        mState = State.Stopping;

        int result = mAdasImpl.finishAdas();
        if (result != Constant.ADAS_SUCCESS) {
            handleError("finish",
                    "finishAdas() failed with return value = " + result);
        }
    }

    @Override
    public void adasFailure(int i) {
        Log.e(TAG, "adasFailure: " + i);
    }

    @Override
    public void didAdasDetect(long captureTime) {
        if (DEBUG_FPS) mTpscDidAdas.update();
        Message msg = mHandler.obtainMessage(MSG_DID_ADAS_DETECT, 0, 0, captureTime);
        msg.sendToTarget();
    }

    @Override
    public synchronized void didAdasFinish(int i) {
        mState = State.Uninitialized;
        Log.v(TAG, "didAdasFinish");
    }

    private void didAdasDetect_internal(long captureTimeMs) {
        if (DEBUG_IMAGE_PROCESS) {
            Log.v(TAG, "didAdasDetect_internal: captureTimeMs = " + captureTimeMs);
        }

        ImageRecord imageRecord = mProcessingImages.remove();
        if (imageRecord.getTimestamp() != captureTimeMs) {
            throw new RuntimeException("callback captureTimeMs=" + captureTimeMs + ", but oldest record timestamp=" + imageRecord.getTimestamp());
        }
        if (DEBUG_IMAGE_PROCESS) {
            Log.v(TAG, "didAdasDetect_internal: close image = " + imageRecord);
            Log.v(TAG, "didAdasDetect_internal: image dequeued = " + processingImagesToString());
        }
        imageRecord.recycle();
    }

    private String processingImagesToString() {
        StringBuffer sb = new StringBuffer();
        for (ImageRecord ir :
                mProcessingImages) {
            sb.append(ir + ", ");
        }
        return sb.toString();
    }

    public float getXscale() {
        return 6; // TODO: calculate the scale according to the ADAS image size & LCD resolution
    }

    public float getYscale() {
        return 4.5f; // TODO: calculate the scale according to the ADAS image size & LCD resolution
    }

    public void addListener(AdasStateListener listener) {
        // Log.v(TAG, "addListener: " + listener);
        WeakReference<AdasStateListener> weakListener =
                new WeakReference<>(listener);
        synchronized (mListeners) {
            if (mListeners.contains(weakListener)) {
                handleError("addListener", "Add a listener multi-times");
                return;
            }

            mListeners.add(weakListener);
            // Log.v(TAG, "addListener: listeners size = " + mListeners.size());
        }
    }

    public void removeListener(AdasStateListener listener) {
        // Log.v(TAG, "removeListener: " + listener);
        boolean found = false;
        synchronized (mListeners) {
            Iterator<WeakReference<AdasStateListener>> iterator = mListeners.iterator();
            while (iterator.hasNext()) {
                WeakReference<AdasStateListener> weakRef = iterator.next();
                if (weakRef.get() == null) {
                    iterator.remove();
                    continue;
                }
                if (weakRef.get() == listener) {
                    iterator.remove();
                    found = true;
                }
            }
        }
        if (!found) {
            handleError("removeListener", listener + " never added");
        }
    }

    public ImageReader getImageReader() {
        if (mImageReader == null) {
            mImageReader = ImageReader.newInstance(ADAS_IMAGE_WIDTH, ADAS_IMAGE_HEIGHT, ImageFormat.YUV_420_888, BUFFER_NUM);
            mImageReader.setOnImageAvailableListener(reader -> {
                Image image = null;
                try {
                    image = reader.acquireLatestImage();
                    if (image != null) {
                        process(image);
                    }
                } catch (IllegalStateException e) {
                    // FIXME: find the root cause
                    handleError( "onImageAvailable", "FIXME, " + e.getMessage());
                }
            }, null);
        }
        return mImageReader;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    @Override
    public void onAdasStarted() {
        Log.v(TAG, "onAdasStarted");
        mEnabled = true;
        synchronized (mListeners) {
            Iterator<WeakReference<AdasStateListener>> iterator = mListeners.iterator();
            while (iterator.hasNext()) {
                WeakReference<AdasStateListener> weakRef = iterator.next();
                if (weakRef.get() == null) {
                    iterator.remove();
                    continue;
                }
                weakRef.get().onAdasStarted();
            }
        }
        //TODO: start thread?
    }

    @Override
    public void onAdasStopped() {
        Log.v(TAG, "onAdasStopped");
        mEnabled = false;
        synchronized (mListeners) {
            Iterator<WeakReference<AdasStateListener>> iterator = mListeners.iterator();
            while (iterator.hasNext()) {
                WeakReference<AdasStateListener> weakRef = iterator.next();
                if (weakRef.get() == null) {
                    iterator.remove();
                    continue;
                }
                weakRef.get().onAdasStopped();
            }
        }
        //TODO: stop thread?
    }

    private class AdasHandler extends Handler {
        public AdasHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_PROCESS:
                    Image image = (Image) msg.obj;
                    process_internal(image);
                    break;
                case MSG_DID_ADAS_DETECT:
                    long captureTime = (long) msg.obj;
                    didAdasDetect_internal(captureTime);
                    break;
            }
        }
    }

    private void handleError(String func, String message) {
        String completeMessage = func + ", " + message;
        if (EXCEPTION_WHEN_ERROR) {
            throw new RuntimeException(completeMessage);
        }
        Log.e(TAG, completeMessage);
    }
}
