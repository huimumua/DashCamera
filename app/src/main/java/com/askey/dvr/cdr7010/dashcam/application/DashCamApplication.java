package com.askey.dvr.cdr7010.dashcam.application;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

import com.askey.dvr.cdr7010.dashcam.EventBusIndex;
import com.askey.dvr.cdr7010.dashcam.service.TTSManager;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by lly on 18-4-9.
 */

public class DashCamApplication extends Application {
    private static Context appContext;

    public DashCamApplication() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.builder().addIndex(new EventBusIndex()).installDefaultEventBus();
        setAppContext(this);
        TTSManager.getInstance().initTTS();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }

    @Override
    public void registerActivityLifecycleCallbacks(ActivityLifecycleCallbacks callback) {
        super.registerActivityLifecycleCallbacks(callback);
    }

    @Override
    public void unregisterActivityLifecycleCallbacks(ActivityLifecycleCallbacks callback) {
        super.unregisterActivityLifecycleCallbacks(callback);
    }

    public static Context getAppContext() {
        return appContext;
    }

    private static void setAppContext(Context context) {
        appContext = context;
    }
}
