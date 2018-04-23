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

public class RecordingFileReceiver extends BroadcastReceiver{
    private static final String TAG = RecordingFileReceiver.class.getSimpleName();
    public static final String CMD_SHOW_REACH_EVENT_FILE_LIMIT ="show_reach_event_file_limit";
    public static final String CMD_SHOW_UNREACH_EVENT_FILE_LIMIT = "show_unreach_event_file_limit";
    public static final String CMD_SHOW_REACH_PARKING_FILE_LIMIT = "show_reach_parking_file_limit";
    public static final String CMD_SHOW_UNREACH_PARKING_FILE_LIMIT = "show_unreach_parking_file_limit";
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
        }
    }
}