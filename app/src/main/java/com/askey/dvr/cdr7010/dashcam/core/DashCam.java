package com.askey.dvr.cdr7010.dashcam.core;


import android.content.Context;
import android.view.Surface;

import com.askey.dvr.cdr7010.dashcam.core.camera2.Camera2Controller;

public class DashCam {

    private Context mContext;
    private Surface mPreviewSurface;
    private Camera2Controller mCameraController;

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
    }

    public void startPreview() {
        if (mCameraController != null) {
            mCameraController.startPreview();
        }
    }

    public void setPreviewSurface(Surface surface) {
        mPreviewSurface = surface;
        mCameraController.addSurface(surface);
    }
}
