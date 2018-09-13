package com.askey.dvr.cdr7010.dashcam.adas;

public interface AdasStateListener {
    void onStateChanged(AdasController.State state);
}