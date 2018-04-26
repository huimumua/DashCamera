package com.askey.dvr.cdr7010.dashcam.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.askey.dvr.cdr7010.dashcam.domain.Event;
import com.askey.dvr.cdr7010.dashcam.domain.MessageEvent;
import com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum;
import com.askey.dvr.cdr7010.dashcam.util.EventUtil;

import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.EventRecordingLimitStatusType.EVENT_RECORDING_REACH_LIMIT_CONDITION;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.EventRecordingLimitStatusType.EVENT_RECORDING_UNREACHABLE_LIMIT_CONDITION;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.ParkingRecordingLimitStatusType.PARKING_RECORDING_REACH_LIMIT_CONDITION;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.ParkingRecordingLimitStatusType.PARKING_RECORDING_UNREACHABLE_LIMIT_CONDITION;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDCardInitStatus.INIT_FAIL;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDCardInitStatus.INIT_SUCCESS;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.MEDIA_UNKNOWN;

public class RecordingFileReceiver extends BroadcastReceiver{
    private static final String TAG = RecordingFileReceiver.class.getSimpleName();
    public static final String CMD_SHOW_REACH_EVENT_FILE_LIMIT ="show_reach_event_file_limit";
    public static final String CMD_SHOW_UNREACH_EVENT_FILE_LIMIT = "show_unreach_event_file_limit";
    public static final String CMD_SHOW_REACH_PARKING_FILE_LIMIT = "show_reach_parking_file_limit";
    public static final String CMD_SHOW_UNREACH_PARKING_FILE_LIMIT = "show_unreach_parking_file_limit";
    public static final String CMD_SHOW_SDCARD_INIT_FAIL = "show_sdcard_init_fail";
    public static final String CMD_SHOW_SDCARD_INIT_SUCCESS = "show_sdcard_init_success";
    @Override
    public void onReceive(Context context, final Intent intent){
        String cmd_ex = intent.getStringExtra("cmd_ex");
        if(CMD_SHOW_REACH_EVENT_FILE_LIMIT.equals(cmd_ex)){
            EventUtil.sendEvent(new MessageEvent<UIElementStatusEnum.EventRecordingLimitStatusType>(Event.EventCode.EVENT_RECORDING_FILE_LIMIT,EVENT_RECORDING_REACH_LIMIT_CONDITION));
        }else if(CMD_SHOW_UNREACH_EVENT_FILE_LIMIT.equals(cmd_ex)){
            EventUtil.sendEvent(new MessageEvent<UIElementStatusEnum.EventRecordingLimitStatusType>(Event.EventCode.EVENT_RECORDING_FILE_LIMIT,EVENT_RECORDING_UNREACHABLE_LIMIT_CONDITION));
        } else if(CMD_SHOW_REACH_PARKING_FILE_LIMIT.equals(cmd_ex)){
            EventUtil.sendEvent(new MessageEvent<UIElementStatusEnum.ParkingRecordingLimitStatusType>(Event.EventCode.EVENT_PARKING_RECODING_FILE_LIMIT,PARKING_RECORDING_REACH_LIMIT_CONDITION));
        }else if(CMD_SHOW_UNREACH_PARKING_FILE_LIMIT.equals(cmd_ex)){
            EventUtil.sendEvent(new MessageEvent<UIElementStatusEnum.ParkingRecordingLimitStatusType>(Event.EventCode.EVENT_PARKING_RECODING_FILE_LIMIT,PARKING_RECORDING_UNREACHABLE_LIMIT_CONDITION));
        }else if(CMD_SHOW_SDCARD_INIT_FAIL.equals(cmd_ex)){
            EventUtil.sendEvent(new MessageEvent<UIElementStatusEnum.SDCardInitStatus>(Event.EventCode.EVENT_SDCARD, INIT_FAIL));
        }else if(CMD_SHOW_SDCARD_INIT_SUCCESS.equals(cmd_ex)){
            EventUtil.sendEvent(new MessageEvent<UIElementStatusEnum.SDCardInitStatus>(Event.EventCode.EVENT_SDCARD, INIT_SUCCESS));
        }
    }
}