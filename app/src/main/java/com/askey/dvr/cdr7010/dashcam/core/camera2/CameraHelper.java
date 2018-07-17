package com.askey.dvr.cdr7010.dashcam.core.camera2;

import android.content.Context;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.support.annotation.IntDef;

import com.askey.dvr.cdr7010.dashcam.application.DashCamApplication;

public class CameraHelper {

    public static final int CAMERA_MAIN = CameraCharacteristics.LENS_FACING_BACK;
    public static final int CAMERA_EXT  = CameraCharacteristics.LENS_FACING_FRONT;

    @IntDef({CAMERA_MAIN, CAMERA_EXT})
    public @interface CameraName {}

    private static boolean checkCameraFacing(int facing) {
        CameraManager cm = (CameraManager) DashCamApplication.getAppContext().getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : cm.getCameraIdList()) {
                CameraCharacteristics characteristics = cm.getCameraCharacteristics(cameraId);
                Integer fc = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (fc != null && fc == facing) {
                    return true;
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return false;
    }

    public static boolean hasMainCamera() {
        return checkCameraFacing(CAMERA_MAIN);
    }

    public static boolean hasExtCamera() {
        return checkCameraFacing(CAMERA_EXT);
    }
}
