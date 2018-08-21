package com.askey.dvr.cdr7010.dashcam.core.camera2;

import android.hardware.camera2.CameraDevice;

public interface CameraControllerListener {
    void onCameraOpened();
    void onCameraClosed();
    void onCaptureStarted();
    void onCaptureStopped();
    void onCameraError(int error);
}
