package com.askey.dvr.cdr7010.dashcam.adas;

import com.askey.dvr.cdr7010.dashcam.logic.GlobalLogic;
import com.askey.platform.AskeySettings;
import com.jvckenwood.adas.util.FC_PARAMETER;

import static com.askey.dvr.cdr7010.dashcam.adas.AdasController.ADAS_IMAGE_HEIGHT;
import static com.askey.dvr.cdr7010.dashcam.adas.AdasController.ADAS_IMAGE_WIDTH;

class FcGetter {
    private static final int CAR_TYPE_NUM = 7;
    private static final int[] INSTALLATION_HEIGHTS = new int[] {120, 135, 120, 135, 120, 135, 200};
    private static final int[] VEHICLE_WIDTHS = new int[] {148, 148, 170, 170, 180, 190, 200};
    private static final int[] VEHICLE_POINT_DISTANCES = new int[] {130, 90, 180, 190, 180, 190, 50};

    static {
        if (INSTALLATION_HEIGHTS.length != CAR_TYPE_NUM) {
            throw new RuntimeException("INSTALLATION_HEIGHTS not defined well");
        }
        if (VEHICLE_POINT_DISTANCES.length != CAR_TYPE_NUM) {
            throw new RuntimeException("VEHICLE_POINT_DISTANCES not defined well");
        }
        if (VEHICLE_WIDTHS.length != CAR_TYPE_NUM) {
            throw new RuntimeException("VEHICLE_WIDTHS not defined well");
        }
    }

    public static FC_PARAMETER getFCParam() {
        GlobalLogic globalLogic = GlobalLogic.getInstance();
        FC_PARAMETER fp = new FC_PARAMETER();
        fp.PitchAngle = globalLogic.getInt(AskeySettings.Global.ADAS_PITCH_ANGLE);
        fp.YawhAngle = globalLogic.getInt(AskeySettings.Global.ADAS_YAW_ANGLE);
        fp.HorizonY = (int) (globalLogic.getInt(AskeySettings.Global.ADAS_SKYLINE_RANGE) * getYscale());
        fp.BonnetY = (int) (globalLogic.getInt(AskeySettings.Global.ADAS_BONNETY) * getYscale());
        fp.CenterX = (int) (globalLogic.getInt(AskeySettings.Global.ADAS_CENTERX) * getXscale());
        int carType = globalLogic.getInt(AskeySettings.Global.CAR_TYPE);
        fp.InstallationHeight = getInstallationHeight(carType);
        fp.CenterDiff = globalLogic.getInt(AskeySettings.Global.ADAS_MOUNT_POSITION);
        fp.VehicleWidth = getVehicleWidth(carType);
        fp.VehiclePointDistance = getVehiclePointDistance(carType);
        fp.SelectIP = getSelectIP();
        fp.CarCollisionSpeed = globalLogic.getInt(AskeySettings.Global.ADAS_CAR_COLLISION_SPEED);
        fp.CarCollisionTime = globalLogic.getInt(AskeySettings.Global.ADAS_CAR_COLLISION_TIME);
        fp.LaneDepartureSpeed = globalLogic.getInt(AskeySettings.Global.ADAS_LANE_DEPARTURE_SPEED);
        fp.LaneDepartureRange = globalLogic.getInt(AskeySettings.Global.ADAS_LANE_DEPARTURE_RANGE);
        fp.LaneDepartureTime = globalLogic.getInt(AskeySettings.Global.ADAS_LANE_DEPARTURE_TIME);
        fp.DepartureDelay = globalLogic.getInt(AskeySettings.Global.ADAS_DELAY_START_DISTANCE);
        fp.DepartureRange = globalLogic.getInt(AskeySettings.Global.ADAS_DELAY_START_RANGE);
        fp.PedCollisionSpeed = globalLogic.getInt(AskeySettings.Global.ADAS_PED_COLLISION_TIME);
        fp.PedCollisionWidth = globalLogic.getInt(AskeySettings.Global.ADAS_PED_COLLISION_WIDTH);
        fp.PedCollisionLowSpeed = globalLogic.getInt(AskeySettings.Global.ADAS_PED_COLLISION_SPEED_LOW);
        fp.PedCollisionHighSpeed = globalLogic.getInt(AskeySettings.Global.ADAS_PED_COLLISION_SPEED_HIGH);
        return fp;
    }

    private static float getXscale() {
        return (float)ADAS_IMAGE_WIDTH / 320; // TODO: literal number = display width
    }

    private static float getYscale() {
        return (float)ADAS_IMAGE_HEIGHT / 240; // TODO: literal number = display height
    }

    private static int getSelectIP() {
        GlobalLogic globalLogic = GlobalLogic.getInstance();
        int result = 0;
        boolean bFCWS = (1 == globalLogic.getInt(AskeySettings.Global.ADAS_FCWS));
        boolean bLDS = (1 == globalLogic.getInt(AskeySettings.Global.ADAS_LDS));
        boolean bDelayStart = (1 == globalLogic.getInt(AskeySettings.Global.ADAS_DELAY_START));
        boolean bPedColl = (1 == globalLogic.getInt(AskeySettings.Global.ADAS_PEDESTRIAN_COLLISION));
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

    private static int getVehiclePointDistance(int carType) {
        return VEHICLE_POINT_DISTANCES[carType];
    }

    private static int getVehicleWidth(int carType) {
        return VEHICLE_WIDTHS[carType];
    }

    private static int getInstallationHeight(int carType) {
        return INSTALLATION_HEIGHTS[carType];
    }
}
