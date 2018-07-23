package com.askey.dvr.cdr7010.dashcam.jvcmodule.local;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;

import com.askey.dvr.cdr7010.dashcam.application.DashCamApplication;
import com.askey.dvr.cdr7010.dashcam.domain.Event;
import com.askey.dvr.cdr7010.dashcam.util.Logg;
import com.askey.platform.AskeySettings;

/***
 * Company: Chengdu Skysoft Info&Tech Co., Ltd.
 * Copyright ©2014-2018 Chengdu Skysoft Info&Tech Co., Ltd.
 * Created by Mark on 2018/7/4.

 * @since:JDK1.6
 * @version:1.0
 * @see
 ***/
public class EventTyeTtsOnOffCheck {
    private static final String LOG_TAG = "EventTyeTtsOnOffCheck";

    public static boolean checkTtsOnOff(int eventType) {
        int result = 1;
        Context appContext = DashCamApplication.getAppContext();
        ContentResolver contentResolver = appContext.getContentResolver();
        try {
            if (eventType == Event.FRONT_COLLISION_WARNING) {
                result = Settings.Global.getInt(contentResolver, AskeySettings.Global.ADAS_FCWS);
            } else if (eventType == Event.LEFT_LANE_DEPARTURE_WARNING ||
                    eventType == Event.RIGHT_LANE_DEPARTURE_WARNING) {
                result = Settings.Global.getInt(contentResolver, AskeySettings.Global.ADAS_LDS);
            } else if (eventType == Event.START_DELAY) {
                result = Settings.Global.getInt(contentResolver, AskeySettings.Global.ADAS_DELAY_START);
            }else if (eventType == Event.PEDESTRIAN_COLLISION_WARNING) {
                result = Settings.Global.getInt(contentResolver, AskeySettings.Global.ADAS_PEDESTRIAN_COLLISION);
            }else if (eventType == Event.REVERSE_RUN) {
                result = Settings.Global.getInt(contentResolver, AskeySettings.Global.NOTIFY_REVERSE_RUN);
            }else if (eventType == Event.ZONE_30) {
                result = Settings.Global.getInt(contentResolver, AskeySettings.Global.NOTIFY_SPEED_LIMIT_AREA);
            }else if (eventType == Event.VIOLATION_OF_SIGNS) {
                result = Settings.Global.getInt(contentResolver, AskeySettings.Global.NOTIFY_STOP);
            }else if (eventType == Event.ACCIDENT_FREQUENT_PLACE) {
                result = Settings.Global.getInt(contentResolver, AskeySettings.Global.NOTIFY_FREQ_ACCIDENT_AREA);
            }else if (eventType == Event.LONG_RUNNING) {
                result = Settings.Global.getInt(contentResolver, AskeySettings.Global.NOTIFY_DRIVING_TIME);
            }else if (eventType == Event.RAPID_ACCELERATION ||
                    eventType == Event.RAPID_DECELERATION) {
                result = Settings.Global.getInt(contentResolver, AskeySettings.Global.NOTIFY_INTENSE_DRIVING);
            }else if (eventType == Event.ABRUPT_HANDLE ||
                    eventType == Event.ABRUPT_HANDLE_LEFT ||
                    eventType == Event.ABRUPT_HANDLE_RIGHT) {
                result = Settings.Global.getInt(contentResolver, AskeySettings.Global.NOTIFY_ABNORMAL_HANDING);
            }else if (eventType == Event.WOBBLE) {
                result = Settings.Global.getInt(contentResolver, AskeySettings.Global.NOTIFY_FLUCTUATION_DETECTION);
            }else if (eventType == Event.DRIVING_OUTSIDE_THE_DESIGNATED_AREA) {
                result = Settings.Global.getInt(contentResolver, AskeySettings.Global.NOTIFY_OUT_OF_AREA);
            }else if (eventType == Event.DRIVING_REPORT ||
                    eventType == Event.MONTHLY_DRIVING_REPORT) {
                result = Settings.Global.getInt(contentResolver, AskeySettings.Global.NOTIFY_DRIVING_REPORT);
            }else if (eventType == Event.AdDVICE_BEFORE_DRIVING) {
                result = Settings.Global.getInt(contentResolver, AskeySettings.Global.NOTIFY_ADVICE);
            }else if (eventType == Event.NOTICE_START) {
                result = Settings.Global.getInt(contentResolver, AskeySettings.Global.NOTIFY_NOTIFICATION);
            }else if (eventType == Event.TYPHOON_WARNING ||
                    eventType == Event.TYPHOON_ALERT ||
                    eventType == Event.WEATHER_ALERT_SPECIAL ||
                    eventType == Event.WEATHER_ALERT) {
                result = Settings.Global.getInt(contentResolver, AskeySettings.Global.NOTIFY_WEATHER_INFO);
            }else if (eventType == Event.BEWARE_OF_ANIMALS_DEER ||
                    eventType == Event.BEWARE_OF_ANIMALS_RACOON ||
                    eventType == Event.BEWARE_OF_ANIMALS_HARE ||
                    eventType == Event.BEWARE_OF_ANIMALS_YAMBARU_QUEENA ||
                    eventType == Event.BEWARE_OF_ANIMALS_YAMANEKO ||
                    eventType == Event.BEWARE_OF_ANIMALS ||
                    eventType == Event.BEWARE_OF_ANIMALS_RARE) {
                result = Settings.Global.getInt(contentResolver, AskeySettings.Global.NOTIFY_ROAD_KILL);
            }else if (eventType == Event.GPS_LOCATION_INFORMATION ||
                    eventType == Event.GPS_LOCATION_INFORMATION_ERROR) {
                result = Settings.Global.getInt(contentResolver, AskeySettings.Global.NOTIFY_LOCATION_INFO);
                Logg.d(LOG_TAG,"checkTtsOnOff GPS result="+result);
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        return (result == 1);
    }



}
