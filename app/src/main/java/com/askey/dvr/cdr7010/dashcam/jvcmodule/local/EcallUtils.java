package com.askey.dvr.cdr7010.dashcam.jvcmodule.local;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.askey.dvr.cdr7010.dashcam.application.DashCamApplication;
import com.askey.dvr.cdr7010.dashcam.logic.GlobalLogic;
import com.askey.dvr.cdr7010.dashcam.util.Logg;
import com.askey.dvr.cdr7010.dashcam.util.NetUtil;

/***
 * Company: Chengdu Skysoft Info&Tech Co., Ltd.
 * Copyright ©2014-2018 Chengdu Skysoft Info&Tech Co., Ltd.
 * Created by Mark on 2018/5/30.

 * @since:JDK1.6
 * @version:1.0
 * @see
 ***/
public class EcallUtils {
    public static final String EXTRA_ORDER = "order";
    public static final String VOIP_PACKAGE_NAME = "voip.cdr7010.dvr.askey.com.voipapp";
    public static final String VOIP_CLASS_NAME = "voip.cdr7010.dvr.askey.com.voipapp.activity.WalkieTalkieActivity";

    private static final String LOG_TAG = "EcallUtils";

    public static void startVoipActivity(int order) {
        if (!NetUtil.isNetworkAvailable()) {
            Logg.d(LOG_TAG, "no network, can not start voip");
            return;
        }
        if (GlobalLogic.getInstance().isECallNotAllow()) {
            Logg.d(LOG_TAG, "insurance is not in the term, can not start voip");
            return;
        }
        Logg.d(LOG_TAG, "startVoipActivity: ");
        Context appContext = DashCamApplication.getAppContext();
        Intent intent = new Intent();
        ComponentName componentName = new ComponentName(VOIP_PACKAGE_NAME, VOIP_CLASS_NAME);
        intent.setComponent(componentName);
        intent.putExtra(EXTRA_ORDER, order);
        appContext.startActivity(intent);
    }
}
