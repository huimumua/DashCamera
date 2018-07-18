package com.askey.dvr.cdr7010.dashcam.adas;

import android.content.Context;
import android.graphics.ImageFormat;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.askey.dvr.cdr7010.dashcam.logic.GlobalLogic;
import com.askey.dvr.cdr7010.dashcam.util.TimesPerSecondCounter;
import com.askey.platform.AskeySettings;
import com.jvckenwood.adas.detection.Detection;
import com.jvckenwood.adas.detection.FC_INPUT;
import com.jvckenwood.adas.util.Constant;
import com.jvckenwood.adas.util.FC_PARAMETER;
import com.jvckenwood.adas.util.Util;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

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
    private static final int BUFFER_NUM = 6;
    private boolean DEBUG_FPS;
    private boolean ADAS_DISABLED;
    /* Handler Messages */
    private static final int MSG_PROCESS = 0;
    private static final int MSG_DID_ADAS_DETECT = 1;
    private static final String PROP_DEBUG_FPS = "persist.dvr.adas.debug_fps";
    private static final String PROP_ADAS_DISABLED = "persist.dvr.adas.disabled";

    private static AdasController sInstance;

    private Util mAdasImpl;
    private GlobalLogic mGlobalSetting;
    private final TimesPerSecondCounter mTpsc;
    private final TimesPerSecondCounter mTpscDidAdas;
    private final TimesPerSecondCounter mTpscFrameDrop;
    private FC_INPUT mFcInput;
    private Image mProcessingImage;
    private Handler mHandler;
    private List<AdasStateListener> mListeners;
    private ImageReader mImageReader;
    private boolean mEnabled = true; // TODO: false default
    private AdasStateControl mStateControl;

    private AdasController() {
        if (sInstance != null) {
            throw new RuntimeException("Singleton instance is already created!");
        }
        mAdasImpl = Util.getInstance();
        mGlobalSetting = GlobalLogic.getInstance();
        mTpsc = new TimesPerSecondCounter(TAG);
        mTpscDidAdas = new TimesPerSecondCounter(TAG + "_didAdas");
        mTpscFrameDrop = new TimesPerSecondCounter(TAG + "_frameDrop");
        mFcInput = new FC_INPUT();
        mProcessingImage = null;
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

    public void start(Context context) {
        Log.i(TAG, "start");
        if (ADAS_DISABLED) {
            Log.e(TAG, "start: not start because ADAS_DISABLED");
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
            Log.e(TAG, "start: initAdas() failed with return value = " + result);
        }
        mStateControl.start();
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

    public void process(Image image) {
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
            Log.v(TAG, "process: image = " + image);
        }

        if (mProcessingImage != null) {
            if (DEBUG_FPS) mTpscFrameDrop.update();
            image.close();
            return;
        }

        mFcInput.VehicleSpeed = 70; // TODO: get real value
        mFcInput.CaptureMilliSec = System.currentTimeMillis(); // TODO: confirm the parameter
        mFcInput.CaptureTime = System.currentTimeMillis(); // TODO: confirm the parameter

        ByteBuffer y = image.getPlanes()[0].getBuffer();
        ByteBuffer u = image.getPlanes()[1].getBuffer();
        ByteBuffer v = image.getPlanes()[2].getBuffer();
        int result = Detection.adasDetect(y, u, v, mFcInput);

        if (result == ADAS_ERROR_DETECT_ALREADY_RUNNING_DETECTION) {
            image.close();
        } else if (result != Constant.ADAS_SUCCESS) {
            Log.e(TAG, "process: adasDetect() failed with return value = " + result);
            image.close();
        } else {
            assert result == Constant.ADAS_SUCCESS;
            mProcessingImage = image;
        }
    }
    public void stop() {
        Log.v(TAG, "stop");
        if (ADAS_DISABLED) {
            return;
        }
        int result = mAdasImpl.finishAdas();
        if (result != Constant.ADAS_SUCCESS) {
            Log.e(TAG, "start: finishAdas() failed with return value = " + result);
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
    
    private void didAdasDetect_internal(long captureTime) {
        if (DEBUG) {
            Log.v(TAG, "didAdasDetect: captureTime = " + captureTime);
        }
        assert mProcessingImage != null;
        if (DEBUG) {
            Log.v(TAG, "didAdasDetect: close image = " + mProcessingImage);
        }
        mProcessingImage.close();
        mProcessingImage = null;
    }

    public float getXscale() {
        return 6; // TODO: calculate the scale according to the ADAS image size & LCD resolution
    }

    public float getYscale() {
        return 4.5f; // TODO: calculate the scale according to the ADAS image size & LCD resolution
    }

    public void addListener(AdasStateListener listener) {
        synchronized (mListeners) {
            mListeners.add(listener);
        }
    }

    public void removeListener(AdasStateListener listener) {
        synchronized (mListeners) {
            mListeners.remove(listener);
        }
    }

    public ImageReader getImageReader() {
        if (mImageReader == null) {
            mImageReader = ImageReader.newInstance(ADAS_IMAGE_WIDTH, ADAS_IMAGE_HEIGHT, ImageFormat.YUV_420_888, BUFFER_NUM);
            mImageReader.setOnImageAvailableListener(reader -> {
                Image image = reader.acquireLatestImage();
                if (image != null) {
                    process(image);
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
            for (AdasStateListener listener : mListeners) {
                listener.onAdasStarted();
            }

        }
        //TODO: start thread?
    }

    @Override
    public void onAdasStopped() {
        Log.v(TAG, "onAdasStopped");
        mEnabled = false;
        synchronized (mListeners) {
            for (AdasStateListener listener : mListeners) {
                listener.onAdasStopped();
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
}
