package com.askey.dvr.cdr7010.dashcam.core.camera;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class CameraPreviewCallback implements android.hardware.Camera.PreviewCallback {
    private int previewWidth;
    private int previewHeight;
    private List<FrameListener> listeners = new ArrayList<>();
    private final Object mSync = new Object();

    public interface FrameListener {
        void onFrameAvailable(byte[] data, int width, int height);
    }

    public CameraPreviewCallback() {
    }

    public void setPreviewSize(int width, int height) {
        previewWidth = width;
        previewHeight = height;
    }

    public void addFrameListener(FrameListener listener) {
        synchronized (mSync) {
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            }
        }
    }

    public void removeFrameListener(FrameListener listener) {
        synchronized (mSync) {
            listeners.remove(listener);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onPreviewFrame(byte[] data, android.hardware.Camera camera) {
        synchronized (mSync) {
            for (FrameListener l : listeners) {
                l.onFrameAvailable(data, previewWidth, previewHeight);
            }
        }
        camera.addCallbackBuffer(data);
    }
}
