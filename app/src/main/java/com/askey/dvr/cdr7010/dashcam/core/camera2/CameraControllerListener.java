package com.askey.dvr.cdr7010.dashcam.core.camera2;

public interface CameraControllerListener {
    void onCameraOpened();
    void onCameraClosed();
    void onCaptureStarted();
    void onCaptureStopped();
}
