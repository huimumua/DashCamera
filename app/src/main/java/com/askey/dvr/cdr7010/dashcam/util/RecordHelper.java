package com.askey.dvr.cdr7010.dashcam.util;

import com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum;

import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.RecordingPreconditionStatus.BATTERY_STATUS_CHARGING;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.RecordingPreconditionStatus.BATTERY_STATUS_DISCHARGING;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.RecordingPreconditionStatus.HIGH_TEMPERATURE;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.RecordingPreconditionStatus.LOW_TEMPERATURE;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.RecordingPreconditionStatus.SDCARD_AVAILABLE;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.RecordingPreconditionStatus.SDCARD_RECORDING_FULL_LIMIT;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.RecordingPreconditionStatus.SDCARD_RECORDING_FULL_LIMIT_EXIT;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.RecordingPreconditionStatus.SDCARD_UNAVAILABLE;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.RecordingPreconditionStatus.SWITCH_USER_COMPLETED;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.RecordingPreconditionStatus.SWITCH_USER_STARTED;

public class RecordHelper{
    private static final String TAG = RecordHelper.class.getSimpleName();
    private static final int FLAG_SDCARD_AVAILABLE = 1;
    private static final int FLAG_SDCARD_SPACE_NOT_FULL = 1 << 1;
    private static final int FLAG_BATTERY_CHARGING = 1 << 2;
    private static final int FLAG_LOW_TEMPERATURE = 1 << 3;
    private static final int FLAG_SWITCH_USER = 1 << 4;
    private static int mRecordingFlags = FLAG_BATTERY_CHARGING | FLAG_SDCARD_AVAILABLE | FLAG_SDCARD_SPACE_NOT_FULL
            | FLAG_LOW_TEMPERATURE | FLAG_SWITCH_USER;

    public static void setRecordingPrecondition(UIElementStatusEnum.RecordingPreconditionStatus recordingPrecondition){
        if(recordingPrecondition == BATTERY_STATUS_CHARGING){
            mRecordingFlags |= FLAG_BATTERY_CHARGING;
        } else if (recordingPrecondition == BATTERY_STATUS_DISCHARGING){
            mRecordingFlags &= (~FLAG_BATTERY_CHARGING);
        } else if (recordingPrecondition == SDCARD_AVAILABLE){
            mRecordingFlags |= FLAG_SDCARD_AVAILABLE;
        } else if (recordingPrecondition == SDCARD_UNAVAILABLE){
            mRecordingFlags &= (~FLAG_SDCARD_AVAILABLE);
        } else if (recordingPrecondition == SDCARD_RECORDING_FULL_LIMIT){
            mRecordingFlags &= (~FLAG_SDCARD_SPACE_NOT_FULL);
        } else if (recordingPrecondition == SDCARD_RECORDING_FULL_LIMIT_EXIT){
            mRecordingFlags |= FLAG_SDCARD_SPACE_NOT_FULL;
        } else if (recordingPrecondition == SWITCH_USER_STARTED){
            mRecordingFlags &= (~FLAG_SWITCH_USER);
        } else if (recordingPrecondition == SWITCH_USER_COMPLETED){
            mRecordingFlags |= FLAG_SWITCH_USER;
        } else if (recordingPrecondition == LOW_TEMPERATURE){
            mRecordingFlags |= FLAG_LOW_TEMPERATURE;
        } else if (recordingPrecondition == HIGH_TEMPERATURE){
            mRecordingFlags &= (~FLAG_LOW_TEMPERATURE);
        }
    }
    public static boolean isRecodingEnable(){
        Logg.d(TAG,"mRecordingFlags="+mRecordingFlags);
        return (mRecordingFlags & FLAG_SDCARD_SPACE_NOT_FULL) > 0
                && (mRecordingFlags & FLAG_BATTERY_CHARGING) > 0
                && (mRecordingFlags & FLAG_LOW_TEMPERATURE) > 0
                && (mRecordingFlags & FLAG_SWITCH_USER) > 0
                && (mRecordingFlags & FLAG_SDCARD_AVAILABLE) > 0;
    }
    public static boolean isHighTemperature(){
        return !((mRecordingFlags & FLAG_LOW_TEMPERATURE) > 0);
    }
}