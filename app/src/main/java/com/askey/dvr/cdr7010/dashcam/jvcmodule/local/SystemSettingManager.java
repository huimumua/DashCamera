package com.askey.dvr.cdr7010.dashcam.jvcmodule.local;

import android.content.ContentResolver;
import android.content.Context;

import com.askey.dvr.cdr7010.dashcam.application.DashCamApplication;
import com.askey.dvr.cdr7010.dashcam.domain.Event;
import com.askey.dvr.cdr7010.dashcam.service.EventManager;
import com.askey.dvr.cdr7010.dashcam.util.Logg;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.EnumMap; /**
 * 项目名称：mainapp
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2018/6/26 10:01
 * 修改人：skysoft
 * 修改时间：2018/6/26 10:01
 * 修改备注：
 */
public class SystemSettingManager {

    private static final String LOG_TAG = "SystemSettingManager";
    private static EnumMap<JvcStatusParams.JvcStatusParam, Object> mReportMap;

    public static void systemSetting(EnumMap<JvcStatusParams.JvcStatusParam, Object> enumMap) {
        Logg.d(LOG_TAG, "systemSetting: ");
        mReportMap = enumMap;
        Context appContext = DashCamApplication.getAppContext();
        ContentResolver contentResolver = appContext.getContentResolver();
        if(enumMap != null){
            int oos = (int)enumMap.get(JvcStatusParams.JvcStatusParam.OOS);
            //圏外の場合は取得できなかったことを通知するのでMainAPPで保持している設定値(前回値)を使用すること
            if(oos == 0) { // 0:圈内
                String response = (String)enumMap.get(JvcStatusParams.JvcStatusParam.RESPONSE);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int status = jsonObject.getInt("status");
                    if(status == 0) { // 0:成功

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public static void settingsUpdate(EnumMap<JvcStatusParams.JvcStatusParam, Object> enumMap) {
        Logg.d(LOG_TAG, "settingsUpdate: ");
        int oos = (int)enumMap.get(JvcStatusParams.JvcStatusParam.OOS);
        if(oos == 0) { // 0:圈内
            String response = (String)enumMap.get(JvcStatusParams.JvcStatusParam.RESPONSE);
            try {
                JSONObject jsonObject = new JSONObject(response);
                int status = jsonObject.getInt("status");
                if(status == 0) { // 0:成功
                    //0:更新なし 1:更新あり
                    int updateSetting = jsonObject.getInt("updateSetting");
                    int updateUser = jsonObject.getInt("updateUser");
                    int updateUserDef = jsonObject.getInt("updateUserDef");
                    int updateUserSelect = jsonObject.getInt("updateUserSelect");
                    if(updateSetting == 1){
                        EventManager.getInstance().handOutEventInfo(Event.CHANGE_SETTINGS_BY_SERVER);
                    }
                    if(updateUserSelect == 1){
                        EventManager.getInstance().handOutEventInfo(Event.USER_SWITCH_BY_SERVER);
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


}
