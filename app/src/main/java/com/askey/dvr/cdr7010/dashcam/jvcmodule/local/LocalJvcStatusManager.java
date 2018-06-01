package com.askey.dvr.cdr7010.dashcam.jvcmodule.local;

import android.content.Context;

import com.askey.dvr.cdr7010.dashcam.application.DashCamApplication;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.local.JvcStatusParams.JvcStatus;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.local.JvcStatusParams.JvcStatusParam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

/***
 * Company: Chengdu Skysoft Info&Tech Co., Ltd.
 * Copyright ©2014-2018 Chengdu Skysoft Info&Tech Co., Ltd.
 * Created by Mark on 2018/6/1.

 * @since:JDK1.6
 * @version:1.0
 * @see
 ***/
public class LocalJvcStatusManager {
    private static List<LocalJvcStatusCallback> mCallbacks = Collections.synchronizedList(new ArrayList<LocalJvcStatusCallback>());

    public static EnumMap<JvcStatusParam, Object> getInsuranceTerm(LocalJvcStatusCallback callback){
        Context appContext = DashCamApplication.getAppContext();
        EnumMap<JvcStatusParam, Object> object = (EnumMap<JvcStatusParam, Object>) ObjectPreference.getObjectFromShare(appContext, JvcStatus.INSURANCE_TERM.getName());
        if(object != null && callback != null){
            mCallbacks.add(callback);
            callback.onDataArriving(object);
        }

        return object;
    }

    public static void setInsuranceTerm(EnumMap<JvcStatusParam, Object> enumMap){
        Context appContext = DashCamApplication.getAppContext();
        ObjectPreference.setObjectToShare(appContext, enumMap, JvcStatus.INSURANCE_TERM.getName());

        for(LocalJvcStatusCallback callback : mCallbacks){
            callback.onDataArriving(enumMap);
        }
    }



    public interface LocalJvcStatusCallback{
        void onDataArriving(EnumMap<JvcStatusParam, Object> enumMap);
    }

}
