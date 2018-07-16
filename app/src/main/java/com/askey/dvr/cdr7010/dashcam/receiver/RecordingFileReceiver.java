package com.askey.dvr.cdr7010.dashcam.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.askey.dvr.cdr7010.dashcam.domain.Event;
import com.askey.dvr.cdr7010.dashcam.domain.MessageEvent;
import com.askey.dvr.cdr7010.dashcam.logic.GlobalLogic;
import com.askey.dvr.cdr7010.dashcam.service.EventManager;
import com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum;
import com.askey.dvr.cdr7010.dashcam.util.AppUtils;
import com.askey.dvr.cdr7010.dashcam.util.EventUtil;
import com.askey.dvr.cdr7010.dashcam.util.Logg;
import com.askey.dvr.cdr7010.dashcam.util.RecordHelper;

import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.EventRecordingLimitStatusType.EVENT_RECORDING_REACH_LIMIT_CONDITION;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.EventRecordingLimitStatusType.EVENT_RECORDING_UNREACHABLE_LIMIT_CONDITION;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.ParkingRecordingLimitStatusType.PARKING_RECORDING_REACH_LIMIT_CONDITION;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.ParkingRecordingLimitStatusType.PARKING_RECORDING_UNREACHABLE_LIMIT_CONDITION;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.RecordingPreconditionStatus.SDCARD_RECORDING_FULL_LIMIT;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.RecordingPreconditionStatus.SDCARD_RECORDING_FULL_LIMIT_EXIT;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_FULL_LIMIT;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_FULL_LIMIT_EXIT;

public class RecordingFileReceiver extends BroadcastReceiver{
    private static final String LOG_TAG = "RecordingFileReceiver";

    public static final String ACTION_SDCARD_LIMT = "com.askey.dvr.cdr7010.dashcam.limit";

    public static final String CMD_SHOW_REACH_EVENT_FILE_LIMIT ="show_reach_event_file_limit";//超过限制
    public static final String CMD_SHOW_UNREACH_EVENT_FILE_LIMIT = "show_unreach_event_file_limit";//限制解除
    public static final String CMD_SHOW_REACH_PARKING_FILE_LIMIT = "show_reach_parking_file_limit";//超过限制
    public static final String CMD_SHOW_UNREACH_PARKING_FILE_LIMIT = "show_unreach_parking_file_limit";//限制解除
    public static final String CMD_SHOW_REACH_NORMAL_FILE_LIMIT ="show_reach_normal_file_limit";//超过限制
    public static final String CMD_SHOW_UNREACH_NORMAL_FILE_LIMIT = "show_unreach_normal_file_limit";//限制解除
    public static final String CMD_SHOW_REACH_MANUAL_FILE_LIMIT ="show_reach_manual_file_limit";//超过限制
    public static final String CMD_SHOW_UNREACH_MANUAL_FILE_LIMIT = "show_unreach_manual_file_limit";//限制解除
    public static final String CMD_SHOW_REACH_PICTURE_FILE_LIMIT ="show_reach_picture_file_limit";//超过限制
    public static final String CMD_SHOW_UNREACH_PICTURE_FILE_LIMIT = "show_unreach_picture_file_limit";//限制解除
    public static final String CMD_SHOW_REACH_SYSTEM_FILE_LIMIT ="show_reach_system_file_limit";//超过限制
    public static final String CMD_SHOW_UNREACH_SYSTEM_FILE_LIMIT = "show_unreach_system_file_limit";//限制解除
    public static final String CMD_SHOW_SDCARD_FULL_LIMIT ="show_sdcard_full_limit";//超过限制
    public static final String CMD_SHOW_UNREACH_SDCARD_FULL_LIMIT = "show_unreach_sdcard_full_limit";//限制解除

    @Override
    public void onReceive(Context context, final Intent intent){
        String action = intent.getAction();
        if(action.equals(ACTION_SDCARD_LIMT)){
            String cmd_ex = intent.getStringExtra("cmd_ex");
            Logg.d(LOG_TAG, "action=" + action+",cmd_ex="+cmd_ex);
            if(CMD_SHOW_REACH_EVENT_FILE_LIMIT.equals(cmd_ex)){
                EventUtil.sendEvent(new MessageEvent<UIElementStatusEnum.EventRecordingLimitStatusType>(Event.EventCode.EVENT_RECORDING_FILE_LIMIT,EVENT_RECORDING_REACH_LIMIT_CONDITION));
                handOutEventInfo(115); // defined to 115 in assets
            }else if(CMD_SHOW_UNREACH_EVENT_FILE_LIMIT.equals(cmd_ex)){
                EventUtil.sendEvent(new MessageEvent<UIElementStatusEnum.EventRecordingLimitStatusType>(Event.EventCode.EVENT_RECORDING_FILE_LIMIT,EVENT_RECORDING_UNREACHABLE_LIMIT_CONDITION));
            } else if(CMD_SHOW_REACH_PARKING_FILE_LIMIT.equals(cmd_ex)){
                EventUtil.sendEvent(new MessageEvent<UIElementStatusEnum.ParkingRecordingLimitStatusType>(Event.EventCode.EVENT_PARKING_RECODING_FILE_LIMIT,PARKING_RECORDING_REACH_LIMIT_CONDITION));
            } else if(CMD_SHOW_UNREACH_PARKING_FILE_LIMIT.equals(cmd_ex)){
                EventUtil.sendEvent(new MessageEvent<UIElementStatusEnum.ParkingRecordingLimitStatusType>(Event.EventCode.EVENT_PARKING_RECODING_FILE_LIMIT,PARKING_RECORDING_UNREACHABLE_LIMIT_CONDITION));
            } else if(CMD_SHOW_SDCARD_FULL_LIMIT.equals(cmd_ex)){
                RecordHelper.setRecordingPrecondition(SDCARD_RECORDING_FULL_LIMIT);
                GlobalLogic.getInstance().setSDCardCurrentStatus(SDCARD_FULL_LIMIT);
                handOutEventInfo(116); // defined to 116 in assets
            } else if(CMD_SHOW_UNREACH_SDCARD_FULL_LIMIT.equals(cmd_ex)) {
                RecordHelper.setRecordingPrecondition(SDCARD_RECORDING_FULL_LIMIT_EXIT);
                GlobalLogic.getInstance().setSDCardCurrentStatus(SDCARD_FULL_LIMIT_EXIT);
            } else if(CMD_SHOW_REACH_PICTURE_FILE_LIMIT.equals(cmd_ex)){
                handOutEventInfo(129); // defined to 129 in assets
            } else if(CMD_SHOW_UNREACH_PICTURE_FILE_LIMIT.equals(cmd_ex)){

            }
        }
    }

    private void handOutEventInfo(int eventType){
        EventManager.getInstance().handOutEventInfo(eventType);
    }
}