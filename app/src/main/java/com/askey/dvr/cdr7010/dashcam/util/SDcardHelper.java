package com.askey.dvr.cdr7010.dashcam.util;

import android.content.Context;
import android.os.Environment;
import android.os.RemoteException;

import com.askey.dvr.cdr7010.dashcam.domain.Event;
import com.askey.dvr.cdr7010.dashcam.domain.MessageEvent;
import com.askey.dvr.cdr7010.dashcam.logic.GlobalLogic;
import com.askey.dvr.cdr7010.dashcam.service.DialogManager;
import com.askey.dvr.cdr7010.dashcam.service.EventManager;
import com.askey.dvr.cdr7010.dashcam.service.FileManager;
import com.askey.dvr.cdr7010.dashcam.service.LedMananger;
import com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum;

import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.EventRecordingLimitStatusType.EVENT_RECORDING_REACH_LIMIT_CONDITION;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.EventRecordingLimitStatusType.EVENT_RECORDING_UNREACHABLE_LIMIT_CONDITION;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.RecordingPreconditionStatus.SDCARD_RECORDING_FULL_LIMIT;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_MOUNTED;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_REMOVED;
import static com.askey.dvr.cdr7010.dashcam.util.SDcardHelper.SDcardStatus.SDCARD_EVENT_FILE_REACH_LIMIT;
import static com.askey.dvr.cdr7010.dashcam.util.SDcardHelper.SDcardStatus.SDCARD_INIT_SUCCESS;
import static com.askey.dvr.cdr7010.dashcam.util.SDcardHelper.SDcardStatus.SDCARD_NOT_SUPPORTED;
import static com.askey.dvr.cdr7010.dashcam.util.SDcardHelper.SDcardStatus.SDCARD_PICTURE_FILE_REACH_LIMIT;

public class SDcardHelper{
    private static final String TAG = SDcardHelper.class.getSimpleName();

    public static class SDcardStatus{
        public static final int SDCARD_NOT_SUPPORTED = 0;
        public static final int SDCARD_UNRECOGNIZABLE = 1;
        public static final int SDCARD_NOT_EXIST =2;
        public static final int SDCARD_MOUNTED = 3;
        public static final int SDCARD_INIT_SUCCESS = 4;
        public static final int SDCARD_INIT_FAIL = 5;
        public static final int SDCARD_EVENT_FILE_REACH_LIMIT =6;
        public static final int SDCARD_EVENT_FILE_OVER_LIMIT = 7;
        public static final int SDCARD_PICTURE_FILE_REACH_LIMIT = 8;
        public static final int SDCARD_PICTURE_FILE_OVER_LIMIT = 9;
        public static final int SDCARD_FULL_LIMIT =10;
    }
    public static boolean isSDCardAvailable(int sdcardStatus){
        if(sdcardStatus == SDCARD_INIT_SUCCESS
                || sdcardStatus == SDCARD_EVENT_FILE_REACH_LIMIT
                || sdcardStatus == SDCARD_PICTURE_FILE_REACH_LIMIT){
            return true;
        }
        return false;
    }
    public static boolean isSDCardEnable()
    {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);

    }
    public static void checkSdcardState(Context context){
        int sdcardStatus = SDcardStatus.SDCARD_NOT_EXIST;
        try {
            sdcardStatus = FileManager.getInstance(context).checkSdcardAvailable();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        Logg.d(TAG,"checkSdcardState sdcardStatus="+sdcardStatus);
        switch (sdcardStatus) {
            case SDcardStatus.SDCARD_NOT_SUPPORTED:
                EventManager.getInstance().handOutEventInfo(111);
                break;
            case SDcardStatus.SDCARD_UNRECOGNIZABLE:
                EventManager.getInstance().handOutEventInfo(113);
                break;
            case SDcardStatus.SDCARD_NOT_EXIST:
                EventUtil.sendEvent(new MessageEvent<UIElementStatusEnum.EventRecordingLimitStatusType>(Event.EventCode.EVENT_RECORDING_FILE_LIMIT,EVENT_RECORDING_UNREACHABLE_LIMIT_CONDITION));
                EventManager.getInstance().handOutEventInfo(110);
                EventUtil.sendEvent(new MessageEvent<UIElementStatusEnum.SDcardStatusType>(Event.EventCode.EVENT_SDCARD, SDCARD_REMOVED));
                break;
            case SDcardStatus.SDCARD_MOUNTED:
                EventUtil.sendEvent(new MessageEvent<UIElementStatusEnum.SDcardStatusType>(Event.EventCode.EVENT_SDCARD, SDCARD_MOUNTED));
                break;
            case SDcardStatus.SDCARD_INIT_SUCCESS:
                LedMananger.getInstance().setLedRecStatus(true, false, 0);
                EventUtil.sendEvent(new MessageEvent<UIElementStatusEnum.SDcardStatusType>(Event.EventCode.EVENT_SDCARD, SDCARD_MOUNTED));
                EventUtil.sendEvent(new MessageEvent<UIElementStatusEnum.SDcardStatusType>(Event.EventCode.EVENT_SDCARD,
                        UIElementStatusEnum.SDcardStatusType.SDCARD_INIT_SUCCESS));
                break;
            case SDcardStatus.SDCARD_INIT_FAIL:
                EventManager.getInstance().handOutEventInfo(112);
                break;
            case SDCARD_EVENT_FILE_REACH_LIMIT:
                EventUtil.sendEvent(new MessageEvent<UIElementStatusEnum.EventRecordingLimitStatusType>(
                        Event.EventCode.EVENT_RECORDING_FILE_LIMIT,EVENT_RECORDING_REACH_LIMIT_CONDITION));
                break;
            case SDcardStatus.SDCARD_EVENT_FILE_OVER_LIMIT:
                EventManager.getInstance().handOutEventInfo(115);
                break;
            case SDCARD_PICTURE_FILE_REACH_LIMIT:

                break;
            case SDcardStatus.SDCARD_PICTURE_FILE_OVER_LIMIT:
                EventManager.getInstance().handOutEventInfo(129);
                break;
            case SDcardStatus.SDCARD_FULL_LIMIT:
                RecordHelper.setRecordingPrecondition(SDCARD_RECORDING_FULL_LIMIT);
                EventManager.getInstance().handOutEventInfo(116);
                break;
            default:
        }
    }
    public static void disMissSdcardDialog(){
        DialogManager.getIntance().setSdcardInvisible(true);
    }
}