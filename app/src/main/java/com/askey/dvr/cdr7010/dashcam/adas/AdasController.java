package com.askey.dvr.cdr7010.dashcam.adas;

import android.content.Context;
import android.location.LocationManager;
import android.media.Image;
import android.util.Log;

import com.askey.dvr.cdr7010.dashcam.logic.GlobalLogic;
import com.askey.dvr.cdr7010.dashcam.util.TimesPerSecondCounter;
import com.askey.platform.AskeySettings;
import com.jvckenwood.adas.detection.FC_INPUT;
import com.jvckenwood.adas.util.Constant;
import com.jvckenwood.adas.util.FC_PARAMETER;
import com.jvckenwood.adas.util.Util;
import com.jvckenwood.adas.detection.Detection;

import java.util.HashMap;
import java.util.Map;

public class AdasController implements Util.AdasCallback {
    private static final String TAG = AdasController.class.getSimpleName();
    private static final int CAR_TYPE_NUM = 7;
    private static final int[] INSTALLATION_HEIGHTS = new int[] {120, 135, 120, 135, 120, 135, 200};
    private static final int[] VEHICLE_WIDTHS = new int[] {148, 148, 170, 170, 180, 190, 200};
    private static final int[] VEHICLE_POINT_DISTANCES = new int[] {130, 90, 180, 190, 180, 190, 50};
    private static final boolean DEBUG = false;

    private static AdasController sInstance;
    private Util mAdasImpl;
    private GlobalLogic mGlobalSetting;
    private LocationManager mLocationManager;
    private TimesPerSecondCounter mTpsc;
    private int yscale;
    private FC_INPUT mFcInput;
    private Map<Long, Image> mProcessingImages;

    private AdasController() {
        if (sInstance != null) {
            throw new RuntimeException("Singleton instance is already created!");
        }
        mAdasImpl = Util.getInstance();
        mGlobalSetting = GlobalLogic.getInstance();
        mTpsc = new TimesPerSecondCounter(TAG);
        mFcInput = new FC_INPUT();
        mProcessingImages = new HashMap<>();
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

    public int getSelectIP() {
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
        if (true) { // TODO: Remove this after fix the crash issue (adasDetect)
            image.close();
            return;
        }
        if (DEBUG)
            Log.v(TAG, "process: image = " + image);
        mTpsc.update();
        mFcInput.VehicleSpeed = 70; // TODO: get real value
        mFcInput.CaptureMilliSec = System.currentTimeMillis(); // TODO: confirm the parameter
        mFcInput.CaptureTime = System.currentTimeMillis(); // TODO: confirm the parameter
        if (mProcessingImages.containsKey(mFcInput.CaptureTime)) {
            image.close();
            throw new RuntimeException("same capture time");
        }
        mProcessingImages.put(mFcInput.CaptureTime, image);
        int result = Detection.adasDetect(image.getPlanes()[0].getBuffer(), mFcInput);
        if (result != Constant.ADAS_SUCCESS) {
            Log.e(TAG, "process: adasDetect() failed with return value = " + result);
        }
    }
    public void stop() {
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
        Log.v(TAG, "didAdasDetect: captureTime" + captureTime);
        if (!mProcessingImages.containsKey(captureTime)) {
            Log.e(TAG, "didAdasDetect: captureTime" + captureTime);
            throw new RuntimeException("captureTime: " + captureTime + " is not in the mProcessingImages");
        }
        Image image = mProcessingImages.remove(captureTime);
        image.close();
    }

    public float getXscale() {
        return 6; // TODO: calculate the scale according to the ADAS image size & LCD resolution
    }

    public float getYscale() {
        return 4.5f; // TODO: calculate the scale according to the ADAS image size & LCD resolution
    }
}
