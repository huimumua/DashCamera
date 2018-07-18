package com.askey.dvr.cdr7010.dashcam.adas;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public abstract class AdasStateControl {
    private static final String TAG = AdasStateControl.class.getSimpleName();
    private List<AdasStateListener> mListeners;

    public AdasStateControl() {
        mListeners = new ArrayList<>();
    }
    public void addListener(AdasStateListener listener) {
        Log.v(TAG, "addListener: " + listener);
        synchronized (mListeners) {
            if (!mListeners.contains(listener)) {
                mListeners.add(listener);
            }
        }
    }

    public void removeListener(AdasStateListener listener) {
        Log.v(TAG, "removeListener: " + listener);
        synchronized (mListeners) {
            if (mListeners.contains(listener)) {
                mListeners.add(listener);
            }
        }
    }

    protected void onStart() {
        synchronized (mListeners) {
            for (AdasStateListener mListener : mListeners) {
                mListener.onAdasStarted();
            }
        }
    }

    protected void onStop() {
        synchronized (mListeners) {
            for (AdasStateListener mListener : mListeners) {
                mListener.onAdasStopped();
            }
        }
    }

    public void start() {
        Log.v(TAG, "start");
        onStart();
    }

}
