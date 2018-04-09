package com.askey.dvr.cdr7010.dashcam.core.encoder;

import android.view.Surface;

public interface IEncodeSurfaceCallback {
    void surfaceCreated(Surface surface);
    void surfaceDestroyed();
}
