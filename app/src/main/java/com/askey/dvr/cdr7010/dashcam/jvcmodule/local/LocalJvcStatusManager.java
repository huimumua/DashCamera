package com.askey.dvr.cdr7010.dashcam.jvcmodule.local;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;

import com.askey.dvr.cdr7010.dashcam.application.DashCamApplication;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.local.JvcStatusParams.JvcStatusParam;
import com.askey.dvr.cdr7010.dashcam.util.Logg;
import com.askey.dvr.cdr7010.dashcam.util.SPUtils;
import com.askey.platform.AskeySettings;

import org.json.JSONException;
import org.json.JSONObject;

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
    private static final String PREFER_KEY_CONTRACT_FLG = "ContractFlg";

    private static final String LOG_TAG = "LocalJvcStatusManager";
    private static List<LocalJvcStatusCallback> mInsuranceCallbacks = Collections.synchronizedList(new ArrayList<LocalJvcStatusCallback>());
    private static boolean isInsuranceTermArriving;
    private static EnumMap<JvcStatusParam, Object> mReportMap;

    public synchronized static EnumMap<JvcStatusParam, Object> getInsuranceTerm(LocalJvcStatusCallback callback) {
        Logg.d(LOG_TAG, "getInsuranceTerm: isInsuranceTermArriving=" + isInsuranceTermArriving + ", callback null=" + (callback == null));
        if (isInsuranceTermArriving) {
            Logg.d(LOG_TAG, "getInsuranceTerm: start callback. ");
            EnumMap<JvcStatusParam, Object> localInsuranceTerm = getLocalInsuranceTerm();
            if(localInsuranceTerm == null){ //report的oos不在圈内或者status不为success
                if (callback != null) callback.onDataArriving(mReportMap);
                return mReportMap;
            }else {
                if (callback != null) callback.onDataArriving(localInsuranceTerm);
                return localInsuranceTerm;
            }
        } else {
            Logg.d(LOG_TAG, "getInsuranceTerm: add callback. ");
            if (callback != null) mInsuranceCallbacks.add(callback);
        }

        return null;
    }

    public synchronized static void setInsuranceTerm(EnumMap<JvcStatusParam, Object> enumMap){
        Logg.d(LOG_TAG, "setInsuranceTerm: ");
        mReportMap = enumMap;
        Context appContext = DashCamApplication.getAppContext();
        ContentResolver contentResolver = appContext.getContentResolver();
        if(enumMap != null){
            isInsuranceTermArriving = true;
            int oos = (int)enumMap.get(JvcStatusParam.OOS);
            //圏外の場合は取得できなかったことを通知するのでMainAPPで保持している設定値(前回値)を使用すること
            if(oos == 0) { // 0:圈内
                String response = (String)enumMap.get(JvcStatusParam.RESPONSE);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int status = jsonObject.getInt("status");
                    if(status == 0) { // 0:成功
                        String sdate = jsonObject.getString("sdate");
                        String edate = jsonObject.getString("edate");
                        String gdate = jsonObject.getString("gdate");
                        int flg = jsonObject.getInt("flg");
                        Settings.Global.putInt(contentResolver, AskeySettings.Global.CONTRACT_INFO, 1); //1:设置过，0:没有设置过
                        Settings.Global.putString(contentResolver, AskeySettings.Global.CONTRACT_START, sdate);
                        Settings.Global.putString(contentResolver, AskeySettings.Global.CONTRACT_END, edate);
                        Settings.Global.putString(contentResolver, AskeySettings.Global.CONTRACT_GRACE_PERIOD, gdate);
                        SPUtils.put(appContext, PREFER_KEY_CONTRACT_FLG, flg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            try {
                EnumMap<JvcStatusParam, Object> localInsuranceTerm = getLocalInsuranceTerm();
                //圈内，获取中，圈外且本地无保存
                if (oos == 0 || oos == 2 || (oos == 1 && localInsuranceTerm == null)) { // 0:圈内;  2:取得中
                    Logg.d(LOG_TAG, "setInsuranceTerm: start callback report data, insuranceCallbacks=" + mInsuranceCallbacks.size());
                    for (LocalJvcStatusCallback callback : mInsuranceCallbacks) {
                        callback.onDataArriving(enumMap);
//                    mCallbacks.remove(callback); //返回后移除该callback
                    }
                //圈外且本地有保存
                } else if (oos == 1 && localInsuranceTerm != null) { // 1:圏外
                    Logg.d(LOG_TAG, "setInsuranceTerm: start callback local data, insuranceCallbacks=" + mInsuranceCallbacks.size());
                    for (LocalJvcStatusCallback callback : mInsuranceCallbacks) {
                        callback.onDataArriving(localInsuranceTerm);
//                        mCallbacks.remove(callback); //返回后移除该callback
                    }
                } else {
                    Logg.w(LOG_TAG, "setInsuranceTerm: warning.");
                }
            } catch (Exception e) {
                Logg.e(LOG_TAG, "setInsuranceTerm: error: " + e.getMessage());
            }
        }
    }

    private static EnumMap<JvcStatusParam, Object> getLocalInsuranceTerm() {
        Context appContext = DashCamApplication.getAppContext();
        ContentResolver contentResolver = appContext.getContentResolver();
        EnumMap<JvcStatusParam, Object> enumMap = null;
        try {
            int info = Settings.Global.getInt(contentResolver, AskeySettings.Global.CONTRACT_INFO);
            if (info == 1) {
                enumMap = new EnumMap<>(JvcStatusParams.JvcStatusParam.class);
                enumMap.put(JvcStatusParams.JvcStatusParam.OOS, 0);
                String sdate = Settings.Global.getString(contentResolver, AskeySettings.Global.CONTRACT_START);
                String edate = Settings.Global.getString(contentResolver, AskeySettings.Global.CONTRACT_END);
                String gdate = Settings.Global.getString(contentResolver, AskeySettings.Global.CONTRACT_GRACE_PERIOD);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("status", 0);
                jsonObject.put("sdate", sdate);
                jsonObject.put("edate", edate);
                jsonObject.put("gdate", gdate);
//                @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
//                Date sDate = simpleDateFormat.parse(sdate);
                jsonObject.put("flg", (int)SPUtils.get(appContext, PREFER_KEY_CONTRACT_FLG, 2));
                String response = jsonObject.toString();
                Logg.d(LOG_TAG, "getLocalInsuranceTerm: response=" + response);
                enumMap.put(JvcStatusParams.JvcStatusParam.RESPONSE, response);
            }else {
                Logg.d(LOG_TAG, "getLocalInsuranceTerm: CONTRACT_INFO=" + info);
            }
        } catch (Exception e) {
            Logg.e(LOG_TAG, "getLocalInsuranceTerm: error: " + e.getMessage());
        }

        return enumMap;
    }

    public static void removeInsuranceCallback(LocalJvcStatusCallback callback){
        mInsuranceCallbacks.remove(callback);
    }



    public interface LocalJvcStatusCallback{
        void onDataArriving(EnumMap<JvcStatusParam, Object> enumMap);
    }

}
