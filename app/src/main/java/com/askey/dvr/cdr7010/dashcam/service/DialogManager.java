package com.askey.dvr.cdr7010.dashcam.service;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.askey.dvr.cdr7010.dashcam.activity.DialogActivity;
import com.askey.dvr.cdr7010.dashcam.domain.Event;
import com.askey.dvr.cdr7010.dashcam.domain.EventInfo;
import com.askey.dvr.cdr7010.dashcam.domain.EventItem;
import com.askey.dvr.cdr7010.dashcam.domain.EventList;
import com.askey.dvr.cdr7010.dashcam.logic.DialogLogic;
import com.askey.dvr.cdr7010.dashcam.receiver.DvrShutDownReceiver;
import com.askey.dvr.cdr7010.dashcam.ui.utils.CancelableRunnable;
import com.askey.dvr.cdr7010.dashcam.util.Logg;

public class DialogManager{
    private static final String TAG = DialogManager.class.getSimpleName();
    private static DialogManager intance;
    private Context mContext;
    private Handler handler;
    private CancelableRunnable mCancelableRunnable;
    private int lastPriority = Integer.MAX_VALUE;
    private DialogLogic dialogLogic;
    private boolean lastDisplay;
    private int lastDialogType =-1;
    private int lastEventType = -1;
    private EventList eventList;
    private EventItem eventItem;


    private DialogManager(){
        handler = new Handler(Looper.getMainLooper());
        dialogLogic = new DialogLogic();
        eventList = new EventList();
    }
    public static DialogManager getIntance(){
        if (intance == null) {
            synchronized (DialogManager.class) {
                if (intance == null) {
                    intance = new DialogManager();
                }
            }
        }
        return intance;
    }
    public void registerContext(Context context){
        mContext = context;
        dialogLogic.setContext(mContext);
        dialogLogic.setDialogCallBack(dialogCallBack);
    }
    public void unregisterContext(Context context){
        if(mContext == context) {
            mContext = null;
        }
    }
    public void showDialog(int dialogType,String message,boolean resize){
        if(DvrShutDownReceiver.isShutDown()){
            Logg.w(TAG, "showDialog: received ACTION_DVR_SHUTDOWN, not show dialog anymore.");
            return;
        }

        if(mContext != null && (mContext instanceof Activity)){
            Bundle bundle = new Bundle();
            bundle.putString("Message",message);
            if(resize){
                bundle.putInt("Width",240);
                bundle.putInt("Height",136);
            }
            ((DialogActivity)mContext).showDialog(dialogType,bundle);
        }
    }
    public void showDialog(int eventType,long showTime){
        if(DvrShutDownReceiver.isShutDown()){
            Logg.w(TAG, "showDialog: received ACTION_DVR_SHUTDOWN, not show dialog anymore.");
            return;
        }

        EventInfo eventInfo = EventManager.getInstance().getEventInfoByEventType(eventType);
        if(eventInfo !=null) {
            int priority = eventInfo.getPriority();
            int dialogType = eventInfo.getDialogType();
            String message = eventInfo.getEventDescription();
            if (mContext != null && (mContext instanceof Activity) && dialogType > 0) {
                Logg.d(TAG,"lastDisplay="+lastDisplay+",priority="+priority+",lastPriority="+lastPriority);
                if((lastDisplay && priority <= lastPriority)||!lastDisplay){
                    dialogLogic.onFilter(dialogType,eventType,priority,showTime,message);
                }
            }
        }
    }
    public void dismissDialog(int dialogType){
        if(mContext != null && (mContext instanceof Activity)){
            if(((DialogActivity)mContext).getDialogType() == dialogType &&((DialogActivity)mContext).isDialogShowing()) {
                ((DialogActivity) mContext).dismissDialog(dialogType);
            }
        }
    }
    public void dismissDialog(){
        if(mContext != null && (mContext instanceof Activity)){
            ((DialogActivity) mContext).dismissDialog();
            ((DialogActivity) mContext).resetDialogType();
        }
    }
    public void setSdcardInvisible(boolean isInvisible){
        if(eventList != null && eventList.contains(Event.SDCARD_UNFORMATTED)){
            eventList.remove(Event.SDCARD_UNFORMATTED);
        }
        if(eventList != null && eventList.contains(Event.SDCARD_UNSUPPORTED)){
            eventList.remove(Event.SDCARD_UNSUPPORTED);
        }
        if(eventList != null && eventList.contains(Event.SDCARD_ERROR)){
            eventList.remove(Event.SDCARD_ERROR);
        }
        if(eventList != null && eventList.contains(Event.SDCARD_SPACE_INSUFFICIENT)){
            eventList.remove(Event.SDCARD_SPACE_INSUFFICIENT);
        }
        if(eventList != null && eventList.contains(Event.RECORDING_EVENT_FAILED)){
            eventList.remove(Event.RECORDING_EVENT_FAILED);
        }
        if(eventList != null && eventList.contains(Event.RECORDING_PIC_FAILED)){
            eventList.remove(Event.RECORDING_PIC_FAILED);
        }
        if(eventList != null && eventList.contains(Event.SDCARD_UNMOUNTED)){
            eventList.remove(Event.SDCARD_UNMOUNTED);
        }
        if((Event.contains(Event.sdCardAbnormalEvent,lastEventType)
                || Event.contains(Event.limitRecordingFileEvent,lastEventType)
                || Event.contains(Event.sdCardUnMountedEvent,lastEventType))&&lastDisplay){
            dialogLogic.setSdcardInvisible(isInvisible);
            dialogLogic.refreshDialogDisplay(lastDialogType,lastEventType);
        }

    }
    public void setSdcardPulledOut(boolean isSdcardPulledOut){
        if(eventList != null && eventList.contains(Event.SDCARD_UNFORMATTED)){
            eventList.remove(Event.SDCARD_UNFORMATTED);
        }
        if(eventList != null && eventList.contains(Event.SDCARD_UNSUPPORTED)){
            eventList.remove(Event.SDCARD_UNSUPPORTED);
        }
        if(eventList != null && eventList.contains(Event.SDCARD_ERROR)){
            eventList.remove(Event.SDCARD_ERROR);
        }
        if(eventList != null && eventList.contains(Event.SDCARD_SPACE_INSUFFICIENT)){
            eventList.remove(Event.SDCARD_SPACE_INSUFFICIENT);
        }
        if(eventList != null && eventList.contains(Event.RECORDING_STOP)){
            eventList.remove(Event.RECORDING_STOP);
        }
        if((Event.contains(Event.sdCardAbnormalEvent,lastEventType)
                || Event.contains(Event.abnormalStopRecordingEvent,lastEventType))&&lastDisplay){
            dialogLogic.setSdcardPulledOut(isSdcardPulledOut);
            dialogLogic.refreshDialogDisplay(lastDialogType,lastEventType);
        }
    }
    public void setSdcardInserted(boolean isSdcardInserted){
        if(Event.contains(Event.sdCardUnMountedEvent,lastEventType)&&lastDisplay){
            dialogLogic.setSdcardInserted(isSdcardInserted);
            dialogLogic.refreshDialogDisplay(lastDialogType,lastEventType);
        }else{
            if(eventList != null && eventList.contains(Event.SDCARD_UNMOUNTED)){
                eventList.remove(Event.SDCARD_UNMOUNTED);
            }
        }
    }
    public void setPowerOff(boolean isPowerOff){
        if(Event.contains(Event.highTemperatureLv3Event,lastEventType)&&lastDisplay){
            dialogLogic.setPowerOff(isPowerOff);
            dialogLogic.refreshDialogDisplay(lastDialogType,lastEventType);
        }else{
            if(eventList != null && eventList.contains(Event.HIGH_TEMPERATURE_THRESHOLD_LV3)){
                eventList.remove(Event.HIGH_TEMPERATURE_THRESHOLD_LV3);
            }
        }
    }
    public void setSdcardInitSuccess(boolean isSdcardInitSuccess){
        if(eventList != null && eventList.contains(Event.SDCARD_UNFORMATTED)){
            eventList.remove(Event.SDCARD_UNFORMATTED);
        }
        if(eventList != null && eventList.contains(Event.SDCARD_UNSUPPORTED)){
            eventList.remove(Event.SDCARD_UNSUPPORTED);
        }
        if(eventList != null && eventList.contains(Event.SDCARD_ERROR)){
            eventList.remove(Event.SDCARD_ERROR);
        }
        if(eventList != null && eventList.contains(Event.SDCARD_SPACE_INSUFFICIENT)){
            eventList.remove(Event.SDCARD_SPACE_INSUFFICIENT);
        }
        if(Event.contains(Event.sdCardAbnormalEvent,lastEventType)&&lastDisplay){
            dialogLogic.setSdcardInitSuccess(isSdcardInitSuccess);
            dialogLogic.refreshDialogDisplay(lastDialogType,lastEventType);
        }
    }
    public void setStartRecording(boolean isStartRecording){
        if( Event.contains(Event.abnormalStopRecordingEvent,lastEventType)&&lastDisplay){
            dialogLogic.setStartRecording(isStartRecording);
            dialogLogic.refreshDialogDisplay(lastDialogType,lastEventType);
        }else{
            if(eventList != null && eventList.contains(Event.RECORDING_STOP)){
                eventList.remove(Event.RECORDING_STOP);
            }
        }
    }
    public void setResumeRecording(boolean isResumeRecording){
        if(Event.contains(Event.highTemperatureLv2Event,lastEventType)&&lastDisplay){
            dialogLogic.setResumeRecording(isResumeRecording);
            dialogLogic.refreshDialogDisplay(lastDialogType,lastEventType);
        }else{
            if(eventList != null && eventList.contains(Event.HIGH_TEMPERATURE_THRESHOLD_LV2)){
                eventList.remove(Event.HIGH_TEMPERATURE_THRESHOLD_LV2);
            }
        }
    }
    public void setSpeechCompleted(boolean isSpeechCompleted){
        if((Event.contains(Event.noticeEvent,lastEventType)
                || Event.contains(Event.simCardErroeEvent,lastEventType))&&lastDisplay){
            dialogLogic.setSpeechCompleted(isSpeechCompleted);
            dialogLogic.refreshDialogDisplay(lastDialogType,lastEventType);
        }
    }
    private DialogLogic.DialogCallBack dialogCallBack =new DialogLogic.DialogCallBack(){
        @Override
        public void onSave(int dialogType,int eventType,int priority,long beginTime,String message){
            if(eventList != null){
                if(eventList.contains(eventType)){
                    eventList.remove(eventType);
                }
                eventList.add(dialogType,eventType,priority,beginTime,message);
            }
            if(Event.contains(Event.nomalEvent,eventType) || Event.contains(Event.highTemperatureLv1Event,eventType)
                    || Event.contains(Event.limitRecordingFileEvent,eventType)){
                dialogLogic.setCancelableRunnable(true);
            }
        }
        @Override
        public void onShow(int priority,int eventType,int dialogType,boolean display){
            lastPriority = priority;
            lastEventType = eventType;
            lastDialogType = dialogType;
            lastDisplay = display;

        }
        @Override
        public void onDismiss(int priority,int eventType,int dialogType,boolean display){
            if(eventList != null ){
                eventList.remove(eventType);
               if(Event.contains(Event.sdCardUnMountedEvent,eventType)){
                   dialogLogic.setSdcardInserted(false);
                   dialogLogic.setSdcardInvisible(false);
                }else if(Event.contains(Event.sdCardAbnormalEvent,eventType)){
                   dialogLogic.setSdcardPulledOut(false);
                   dialogLogic.setSdcardInitSuccess(false);
                   dialogLogic.setSdcardInvisible(false);
               }else if(Event.contains(Event.highTemperatureLv3Event,eventType)){
                   dialogLogic.setPowerOff(false);
               }else if(Event.contains(Event.highTemperatureLv2Event,eventType)){
                   dialogLogic.setResumeRecording(false);
               }else if(Event.contains(Event.abnormalStopRecordingEvent,eventType)){
                   dialogLogic.setSdcardPulledOut(false);
                   dialogLogic.setStartRecording(false);
               }else if(Event.contains(Event.noticeEvent,eventType)){
                   dialogLogic.setSpeechCompleted(false);
               }else if(Event.contains(Event.limitRecordingFileEvent,eventType)){
                   dialogLogic.setSdcardInvisible(false);
               }
                eventItem = eventList.getNextEventItem();
            }
            dialogLogic.resetLastEvent();
            if(lastEventType == eventType) {
                lastPriority = priority;
                lastEventType = eventType;
                lastDialogType = dialogType;
                lastDisplay = display;
            }
            if(eventItem != null){
                DialogManager.getIntance().showDialog(eventItem.eventType,eventItem.beginTime);
                eventItem = null;
            }
        }
    };
    public void reset(){
        if(eventList != null ){
            eventList.clear();
        }
        if(dialogLogic != null){
            dialogLogic.reset();
        }
        lastPriority = Integer.MAX_VALUE;
        lastDialogType =-1;
        lastEventType = -1;
        lastDisplay =false;
    }


}