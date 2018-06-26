package com.askey.dvr.cdr7010.dashcam.util;

import android.util.Log;

public class TimesPerSecondCounter {

    private static final String TAG = "TPSC";
    private String mTag = null;
    private int mCountPeriodInMs;
    private long mLastTime;
    private int mCount;
    private boolean mShowOnUpdate;
    private TpscListener mListener;

    public TimesPerSecondCounter(String name) {
        this(name, 3000, true);
    }

    public TimesPerSecondCounter(String name, int ms, boolean showOnUpdate) {
        mTag = TAG + "[" + name + "]";
        mCount = 0;
        mLastTime = System.currentTimeMillis();
        mCountPeriodInMs = ms;
        mShowOnUpdate = showOnUpdate;
    }


    public void update() {
        update(1);
    }

    public void update(int inc) {
        mCount += inc;
        long period = System.currentTimeMillis() - mLastTime;
        if (period > mCountPeriodInMs) {
            if (mShowOnUpdate) {
                show(period);
            }
            if (mListener != null) {
                mListener.update(mCount, period);
            }
            mCount = 0;
            mLastTime = System.currentTimeMillis();
        }
    }
    public void show(long period) {
        //Log.v(mTag, String.format("%d/%d, %.2f per second", mCount, period, (float) mCount * 1000 / period));
    }

    public void setListener(TpscListener listener) {
        mListener = listener;
    }

    public interface TpscListener {
        void update(int Count, long period);
    }
}
