package com.askey.dvr.cdr7010.dashcam.jvcmodule.local;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.askey.dvr.cdr7010.dashcam.application.DashCamApplication;
import com.askey.dvr.cdr7010.dashcam.util.Logg;

/***
 * Company: Chengdu Skysoft Info&Tech Co., Ltd.
 * Copyright ©2014-2018 Chengdu Skysoft Info&Tech Co., Ltd.
 * Created by Mark on 2018/5/30.

 * @since:JDK1.6
 * @version:1.0
 * @see
 ***/
public class EcallUtils {
    public static final String EXTRA_IS_USER_CALL = "isUserCall";
    public static final String VOIP_PACKAGE_NAME = "voip.cdr7010.dvr.askey.com.voipapp";
    public static final String VOIP_CLASS_NAME = "voip.cdr7010.dvr.askey.com.voipapp.activity.WalkieTalkieActivity";

    private static final String LOG_TAG = "EcallUtils";

    public static void startVoipActivity(boolean isUserCall){
        Logg.d(LOG_TAG, "startVoipActivity: isUserCall=" + isUserCall);
        Context appContext = DashCamApplication.getAppContext();
        Intent intent = new Intent();
        ComponentName componentName = new ComponentName(VOIP_PACKAGE_NAME, VOIP_CLASS_NAME);
        intent.setComponent(componentName);
        intent.putExtra(EXTRA_IS_USER_CALL, isUserCall);
        appContext.startActivity(intent);
    }

}
