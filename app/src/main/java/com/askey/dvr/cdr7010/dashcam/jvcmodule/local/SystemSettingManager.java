package com.askey.dvr.cdr7010.dashcam.jvcmodule.local;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.askey.dvr.cdr7010.dashcam.application.DashCamApplication;
import com.askey.dvr.cdr7010.dashcam.util.Logg;
import com.askey.platform.AskeySettings;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.EnumMap;
import java.util.concurrent.CountDownLatch;

/**
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

    public static void systemSetting(EnumMap<JvcStatusParams.JvcStatusParam, Object> enumMap ,CountDownLatch countDownLatch) {
        Logg.d(LOG_TAG, "systemSetting: ");
        mReportMap = enumMap;
        Context appContext = DashCamApplication.getAppContext();
        ContentResolver contentResolver = appContext.getContentResolver();
        if(enumMap != null){
            int oos = (int)enumMap.get(JvcStatusParams.JvcStatusParam.OOS);
            //圏外の場合は取得できなかったことを通知するのでMainAPPで保持している設定値(前回値)を使用すること
            if(oos == 0) { // 0:圈内
                String response = (String) enumMap.get(JvcStatusParams.JvcStatusParam.RESPONSE);
                Gson gson = new Gson();
                JvcSystemSettingInfo systemSettingInfo = gson.fromJson(response, JvcSystemSettingInfo.class);
                if (systemSettingInfo != null) {
                    int status = systemSettingInfo.status;
                    if (status == 0) {
                        Settings.Global.putInt(contentResolver, AskeySettings.Global.ADAS_CAR_COLLISION_SPEED, systemSettingInfo.car_collision_speed);
                        Settings.Global.putInt(contentResolver, AskeySettings.Global.ADAS_CAR_COLLISION_TIME, systemSettingInfo.car_collision_time);
                        Settings.Global.putInt(contentResolver, AskeySettings.Global.ADAS_LANE_DEPARTURE_SPEED, systemSettingInfo.lane_departure_speed);
                        Settings.Global.putInt(contentResolver, AskeySettings.Global.ADAS_LANE_DEPARTURE_RANGE, systemSettingInfo.lane_departure_range);
                        Settings.Global.putInt(contentResolver, AskeySettings.Global.ADAS_LANE_DEPARTURE_TIME, systemSettingInfo.lane_departure_time);
                        Settings.Global.putInt(contentResolver, AskeySettings.Global.ADAS_DELAY_START_DISTANCE, systemSettingInfo.delay_start_distance);
                        Settings.Global.putInt(contentResolver, AskeySettings.Global.ADAS_DELAY_START_RANGE, systemSettingInfo.delay_start_range);
                        Settings.Global.putInt(contentResolver, AskeySettings.Global.ADAS_PED_COLLISION_TIME, systemSettingInfo.ped_collision_time);
                        Settings.Global.putInt(contentResolver, AskeySettings.Global.ADAS_PED_COLLISION_WIDTH, systemSettingInfo.ped_collision_width);
                        Settings.Global.putInt(contentResolver, AskeySettings.Global.ADAS_PED_COLLISION_SPEED_LOW, systemSettingInfo.ped_collision_speed_low);
                        Settings.Global.putInt(contentResolver, AskeySettings.Global.ADAS_PED_COLLISION_SPEED_HIGH, systemSettingInfo.ped_collision_speed_high);
                        Settings.Global.putString(contentResolver, AskeySettings.Global.COMM_EMERGENCY_NAME, systemSettingInfo.contact_name);
                        Settings.Global.putString(contentResolver, AskeySettings.Global.COMM_EMERGENCY_NUMBER, systemSettingInfo.contact_no);
                        Settings.Global.putInt(contentResolver, AskeySettings.Global.CONTRACT_INFO, systemSettingInfo.contract_info);
                        Settings.Global.putInt(contentResolver, AskeySettings.Global.EVENT_ACCIDENT, systemSettingInfo.event_accident);
                        Settings.Global.putInt(contentResolver, AskeySettings.Global.EVENT_ACCELERATION, systemSettingInfo.event_accele);
                        Settings.Global.putInt(contentResolver, AskeySettings.Global.EVENT_DECELERATION, systemSettingInfo.event_decele);
                        Settings.Global.putInt(contentResolver, AskeySettings.Global.EVENT_ABRUPT_HANDLE, systemSettingInfo.event_handle);
                        Settings.Global.putInt(contentResolver, AskeySettings.Global.EVENT_FLUCTUATION, systemSettingInfo.event_wobble);
                        Settings.Global.putInt(contentResolver, AskeySettings.Global.EVENT_FCWS, systemSettingInfo.event_coll);
                        Settings.Global.putInt(contentResolver, AskeySettings.Global.EVENT_LDS, systemSettingInfo.event_devi);
                        Settings.Global.putInt(contentResolver, AskeySettings.Global.EVENT_DELAY_START, systemSettingInfo.event_delay);
                        Settings.Global.putInt(contentResolver, AskeySettings.Global.EVENT_NORMAL_RECODING, systemSettingInfo.anytime_rec);
                        Settings.Global.putInt(contentResolver, AskeySettings.Global.ADAS_CAR_COLLISION_SPEED, systemSettingInfo.car_collision_speed);
                        Settings.Global.putInt(contentResolver, AskeySettings.Global.EVENT_REVERSE_RUN, systemSettingInfo.event_reverse);
                        Settings.Global.putInt(contentResolver, AskeySettings.Global.EVENT_COLLISION_WARNING, systemSettingInfo.collision_warning);
                        if (countDownLatch!=null){
                            countDownLatch.countDown();
                            Log.d(LOG_TAG,"abby countDown~~03~!");
                        }
                    }
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
//                    //0:更新なし 1:更新あり
//                    int updateSetting = jsonObject.getInt("updateSetting");
//                    int updateUser = jsonObject.getInt("updateUser");
//                    int updateUserDef = jsonObject.getInt("updateUserDef");
//                    int updateUserSelect = jsonObject.getInt("updateUserSelect");
//                    if(updateSetting == 1){
//                        EventManager.getInstance().handOutEventInfo(Event.CHANGE_SETTINGS_BY_SERVER);
//                    }
//                    if(updateUserSelect == 1){
//                        EventManager.getInstance().handOutEventInfo(Event.USER_SWITCH_BY_SERVER);
//                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


}
