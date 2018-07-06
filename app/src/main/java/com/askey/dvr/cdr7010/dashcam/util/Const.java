package com.askey.dvr.cdr7010.dashcam.util;

public class Const {
    public static final String PREFERENCE_KEY_UPDATE_COMPLETED = "updateCompleted";
    public static final String PACKAGE_NAME = "com.askey.dvr.cdr7010.setting";
    public static final String CLASS_NAME = "com.askey.dvr.cdr7010.setting.SettingsActivity";
    public static final String LCD_POWER_STATUS_PATH ="/sys/class/graphics/fb0/askey_lcd_power_on_state";
    public static final String LCD_THERMAL_STATUS_PATH="/sys/class/graphics/fb0/askey_lcd_thermal_protection_state";
    public static final String STR_BUTTON_CONFIRM = "OK";
    public static final String STR_BUTTON_CANCEL = "X";

    public static final String IS_START_RECORD = "is_start_record";
    public static final String EXTRA_INFO = "extra_info";
    public static final int FIRST_INIT_SUCCESS =0;
    public static final int NONE_UPDATE = 1;
//    public static final int OTA_UPDATE = 0;
//    public static final int SDCARD_UPDATE = 2;
    public static final int UPDATE_SUCCESS = 0;
    public static final int UPDATE_FAIL = -1;
    public static final int UPDATE_READY = 1;
    public static final int OOS_SUCCESS = 0;
}