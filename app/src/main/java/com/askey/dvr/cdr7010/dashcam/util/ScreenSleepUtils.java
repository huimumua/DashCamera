package com.askey.dvr.cdr7010.dashcam.util;

import android.content.Context;
import android.os.PowerManager;
import android.os.SystemClock;

import com.askey.dvr.cdr7010.dashcam.application.DashCamApplication;
import com.askey.platform.AskeyPowerManager;

public class ScreenSleepUtils{
    private static final String TAG = ScreenSleepUtils.class.getSimpleName();
    private static PowerManager m_PowerManager;
    private static PowerManager.WakeLock mHoldingScreenWakeLock;
    private static AskeyPowerManager mAkeyPowerManager;

    static{
        m_PowerManager = (PowerManager)((DashCamApplication.getAppContext()).getSystemService(Context.POWER_SERVICE));
        mHoldingScreenWakeLock =  m_PowerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "PMS");
        mHoldingScreenWakeLock.setReferenceCounted(false);
        mAkeyPowerManager = AskeyPowerManager.getInstance(DashCamApplication.getAppContext());
    }

    public static void cancelScreenSleep(){
        mHoldingScreenWakeLock.acquire();
    }
    public static void resumeScreenSleep(){
        mHoldingScreenWakeLock.release();
    }
    public static void setScreenOn(){
        mAkeyPowerManager.setScreenOn(SystemClock.uptimeMillis());
    }
    public static void setScreenOff(){
        mAkeyPowerManager.setScreenOff(SystemClock.uptimeMillis());
    }


}