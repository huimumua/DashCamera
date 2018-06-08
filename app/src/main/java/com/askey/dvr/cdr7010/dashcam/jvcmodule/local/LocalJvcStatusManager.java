package com.askey.dvr.cdr7010.dashcam.jvcmodule.local;

import android.content.Context;

import com.askey.dvr.cdr7010.dashcam.application.DashCamApplication;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.local.JvcStatusParams.JvcStatus;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.local.JvcStatusParams.JvcStatusParam;
import com.askey.dvr.cdr7010.dashcam.util.Logg;

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
    private static final String LOG_TAG = "LocalJvcStatusManager";
    private static List<LocalJvcStatusCallback> mCallbacks = Collections.synchronizedList(new ArrayList<LocalJvcStatusCallback>());
    private static boolean isInsuranceTermArriving;

    public static EnumMap<JvcStatusParam, Object> getInsuranceTerm(LocalJvcStatusCallback callback) {
        Logg.d(LOG_TAG, "getInsuranceTerm: isInsuranceTermArriving=" + isInsuranceTermArriving);
        Context appContext = DashCamApplication.getAppContext();
        EnumMap<JvcStatusParam, Object> object = (EnumMap<JvcStatusParam, Object>) ObjectPreference.getObjectFromShare(appContext, JvcStatus.INSURANCE_TERM.getName());

        Logg.d(LOG_TAG, "getInsuranceTerm: callback null=" + (callback == null) + ", object null=" + (object == null));
        if (callback != null) {
            if(isInsuranceTermArriving && object != null){
                Logg.d(LOG_TAG, "getInsuranceTerm: start callback. ");
                callback.onDataArriving(object);
            }else {
                Logg.d(LOG_TAG, "getInsuranceTerm: add callback. ");
                mCallbacks.add(callback);
            }
        }

        return object;
    }

    public static void setInsuranceTerm(EnumMap<JvcStatusParam, Object> enumMap, boolean checkBeforeSave){
        Logg.d(LOG_TAG, "setInsuranceTerm: ");
        if(enumMap != null){
            isInsuranceTermArriving = true;
            int oos = (int)enumMap.get(JvcStatusParams.JvcStatusParam.OOS);
            //圏外の場合は取得できなかったことを通知するのでMainAPPで保持している設定値(前回値)を使用すること
            if(!checkBeforeSave || (checkBeforeSave && oos == 1)) {
                Context appContext = DashCamApplication.getAppContext();
                ObjectPreference.setObjectToShare(appContext, enumMap, JvcStatus.INSURANCE_TERM.getName());
            }

            Context appContext = DashCamApplication.getAppContext();
            EnumMap<JvcStatusParam, Object> object = (EnumMap<JvcStatusParam, Object>) ObjectPreference.getObjectFromShare(appContext, JvcStatus.INSURANCE_TERM.getName());
            Logg.d(LOG_TAG, "setInsuranceTerm: oos=" + oos + ", saveObject null=" + (object==null));
            if(oos == 0 || (oos == 1 && object == null)){
                Logg.d(LOG_TAG, "setInsuranceTerm: start callback, callbackSize=" + mCallbacks.size());
                for(LocalJvcStatusCallback callback : mCallbacks){
                    callback.onDataArriving(enumMap);
                    mCallbacks.remove(callback); //返回后移除该callback
                }
            }else if(object != null){
                Logg.d(LOG_TAG, "setInsuranceTerm: start callback, callbackSize=" + mCallbacks.size());
                for(LocalJvcStatusCallback callback : mCallbacks){
                    callback.onDataArriving(object);
                    mCallbacks.remove(callback); //返回后移除该callback
                }
            }else {
                Logg.w(LOG_TAG, "setInsuranceTerm: warning.");
            }
        }

    }

    public static EnumMap<JvcStatusParam, Object> getUserList(){
        Context appContext = DashCamApplication.getAppContext();
        @SuppressWarnings("unchecked")
        EnumMap<JvcStatusParam, Object> object = (EnumMap<JvcStatusParam, Object>) ObjectPreference.getObjectFromShare(appContext, JvcStatus.USER_LIST.getName());
        return object;
    }

    public static void setUserList(EnumMap<JvcStatusParam, Object> enumMap) {
        Context appContext = DashCamApplication.getAppContext();
        ObjectPreference.setObjectToShare(appContext, enumMap, JvcStatus.USER_LIST.getName());
    }



    public interface LocalJvcStatusCallback{
        void onDataArriving(EnumMap<JvcStatusParam, Object> enumMap);
    }

}
