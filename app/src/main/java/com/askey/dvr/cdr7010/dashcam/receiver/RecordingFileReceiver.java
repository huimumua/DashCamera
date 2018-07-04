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

import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.EventRecordingLimitStatusType.EVENT_RECORDING_REACH_LIMIT_CONDITION;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.EventRecordingLimitStatusType.EVENT_RECORDING_UNREACHABLE_LIMIT_CONDITION;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.ParkingRecordingLimitStatusType.PARKING_RECORDING_REACH_LIMIT_CONDITION;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.ParkingRecordingLimitStatusType.PARKING_RECORDING_UNREACHABLE_LIMIT_CONDITION;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_FULL_LIMIT;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_INIT_FAIL;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_INIT_SUCCESS;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_MOUNTED;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_REMOVED;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_SUPPORTED;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_UNRECOGNIZABLE;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_UNSUPPORTED;

public class RecordingFileReceiver extends BroadcastReceiver{
    private static final String LOG_TAG = "RecordingFileReceiver";
    private static final String ACTIVITY_CLASSNAME ="com.askey.dvr.cdr7010.dashcam.ui.MainActivity";

    public static final String ACTION_SDCARD_LIMT = "com.askey.dvr.cdr7010.dashcam.limit";
    public static final String ACTION_SDCARD_STATUS = "action_sdcard_status";

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

    public static final String CMD_SHOW_SDCARD_NOT_SUPPORTED ="show_sdcard_not_supported";//sdcard格式不支持可用
    public static final String CMD_SHOW_SDCARD_SUPPORTED = "show_sdcard_supported";//sdcard可用
    public static final String CMD_SHOW_SDCARD_INIT_FAIL ="show_sdcard_init_fail";//init 失败
    public static final String CMD_SHOW_SDCARD_INIT_SUCC = "show_sdcard_init_success";//init成功
    public static final String CMD_SHOW_SDCARD_UNRECOGNIZABLE ="show_sdcard_unrecognizable";//不被识别
    @Override
    public void onReceive(Context context, final Intent intent){
        String action = intent.getAction();
        Logg.d(LOG_TAG, "action=" + action);
        if(action.equals(ACTION_SDCARD_LIMT)){
            String cmd_ex = intent.getStringExtra("cmd_ex");
            if(CMD_SHOW_REACH_EVENT_FILE_LIMIT.equals(cmd_ex)){
                EventUtil.sendEvent(new MessageEvent<UIElementStatusEnum.EventRecordingLimitStatusType>(Event.EventCode.EVENT_RECORDING_FILE_LIMIT,EVENT_RECORDING_REACH_LIMIT_CONDITION));
                handOutEventInfo(115); // defined to 115 in assets
            }else if(CMD_SHOW_UNREACH_EVENT_FILE_LIMIT.equals(cmd_ex)){
                EventUtil.sendEvent(new MessageEvent<UIElementStatusEnum.EventRecordingLimitStatusType>(Event.EventCode.EVENT_RECORDING_FILE_LIMIT,EVENT_RECORDING_UNREACHABLE_LIMIT_CONDITION));
            } else if(CMD_SHOW_REACH_PARKING_FILE_LIMIT.equals(cmd_ex)){
                EventUtil.sendEvent(new MessageEvent<UIElementStatusEnum.ParkingRecordingLimitStatusType>(Event.EventCode.EVENT_PARKING_RECODING_FILE_LIMIT,PARKING_RECORDING_REACH_LIMIT_CONDITION));
            }else if(CMD_SHOW_UNREACH_PARKING_FILE_LIMIT.equals(cmd_ex)){
                EventUtil.sendEvent(new MessageEvent<UIElementStatusEnum.ParkingRecordingLimitStatusType>(Event.EventCode.EVENT_PARKING_RECODING_FILE_LIMIT,PARKING_RECORDING_UNREACHABLE_LIMIT_CONDITION));
            }else if(CMD_SHOW_SDCARD_FULL_LIMIT.equals(cmd_ex)){
                GlobalLogic.getInstance().setSDCardCurrentStatus(SDCARD_FULL_LIMIT);
                handOutEventInfo(116); // defined to 116 in assets
            }
        }else if(action.equals(ACTION_SDCARD_STATUS)){
            String data = intent.getStringExtra("data");
            Logg.d(LOG_TAG, "action=" + action+",data="+data);
            if(AppUtils.isActivityTop(context,ACTIVITY_CLASSNAME)) {
                if (data.equals(CMD_SHOW_SDCARD_NOT_SUPPORTED)) {
                    GlobalLogic.getInstance().setSDCardCurrentStatus(SDCARD_UNSUPPORTED);
                    EventUtil.sendEvent(new MessageEvent<UIElementStatusEnum.SDcardStatusType>(Event.EventCode.EVENT_SDCARD, SDCARD_UNSUPPORTED));
                    handOutEventInfo(112); // defined to 112 in assets
                } else if (data.equals(CMD_SHOW_SDCARD_SUPPORTED)) {
                    GlobalLogic.getInstance().setSDCardCurrentStatus(SDCARD_SUPPORTED);
                    EventUtil.sendEvent(new MessageEvent<UIElementStatusEnum.SDcardStatusType>(Event.EventCode.EVENT_SDCARD, SDCARD_SUPPORTED));
                } else if (data.equals(CMD_SHOW_SDCARD_INIT_FAIL)) {
                    GlobalLogic.getInstance().setSDCardCurrentStatus(SDCARD_INIT_FAIL);
                    EventUtil.sendEvent(new MessageEvent<UIElementStatusEnum.SDcardStatusType>(Event.EventCode.EVENT_SDCARD, SDCARD_INIT_FAIL));
                    handOutEventInfo(111); // defined to 111 in assets
                } else if (data.equals(CMD_SHOW_SDCARD_INIT_SUCC)) {
                    GlobalLogic.getInstance().setSDCardCurrentStatus(SDCARD_INIT_SUCCESS);
                    EventUtil.sendEvent(new MessageEvent<UIElementStatusEnum.SDcardStatusType>(Event.EventCode.EVENT_SDCARD, SDCARD_INIT_SUCCESS));
                } else if (data.equals(CMD_SHOW_SDCARD_UNRECOGNIZABLE)) {
                    GlobalLogic.getInstance().setSDCardCurrentStatus(SDCARD_UNRECOGNIZABLE);
                    EventUtil.sendEvent(new MessageEvent<UIElementStatusEnum.SDcardStatusType>(Event.EventCode.EVENT_SDCARD, SDCARD_UNRECOGNIZABLE));
                    handOutEventInfo(113); // defined to 113 in assets
                }
            }
        }else if(action.equals("android.intent.action.MEDIA_MOUNTED")){
            if(AppUtils.isActivityTop(context,ACTIVITY_CLASSNAME)) {
                GlobalLogic.getInstance().setSDCardCurrentStatus(SDCARD_MOUNTED);
                EventUtil.sendEvent(new MessageEvent<UIElementStatusEnum.SDcardStatusType>(Event.EventCode.EVENT_SDCARD, SDCARD_MOUNTED));
            }
        }else if(action.equals("android.intent.action.MEDIA_EJECT")){
            if(AppUtils.isActivityTop(context,ACTIVITY_CLASSNAME)) {
                GlobalLogic.getInstance().setSDCardCurrentStatus(SDCARD_REMOVED);
                handOutEventInfo(110);
                EventUtil.sendEvent(new MessageEvent<UIElementStatusEnum.SDcardStatusType>(Event.EventCode.EVENT_SDCARD, SDCARD_REMOVED));
            }
             // defined to 110 in assets
        }
    }

    private void handOutEventInfo(int eventType){
        EventManager.getInstance().handOutEventInfo(eventType);
    }
}