package com.askey.dvr.cdr7010.dashcam.logic;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.askey.dvr.cdr7010.dashcam.activity.DialogActivity;
import com.askey.dvr.cdr7010.dashcam.domain.Event;
import com.askey.dvr.cdr7010.dashcam.ui.utils.CancelableRunnable;
import com.askey.dvr.cdr7010.dashcam.util.EventUtil;


public class DialogLogic{
    private static final String TAG = DialogLogic.class.getSimpleName();
    private static final int DELAY_TIME =5000;
    private boolean display;
    private Context mContext;
    private long beginTime = 0;
    private int lastPriority = Integer.MAX_VALUE;
    private int lastDialogType;
    private int lastEventType;
    private String lastMessage;
    private int curPriority;
    private int curDialogType;
    private int curEventType;
    private String curMessage;
    private long showTime;
    private boolean isSdcardPulledOut ;
    private boolean isSdcardInserted;
    private boolean isPowerOFF;
    private boolean isStartRecording;
    private boolean isResumeRecording;
    private boolean isSpeechCompleted;
    private boolean isSdcardInitSuccess;
    private Handler handler;
    private CancelableRunnable mCancelableRunnable;
    private DialogCallBack dialogCallBack;

    public DialogLogic(){
        handler = new Handler(Looper.getMainLooper());
    }

    public void onFilter(int dialogType,int eventType,int priority,long showTime,String message){
        curDialogType = dialogType;
        curEventType = eventType;
        curPriority = priority;
        curMessage = message;
        this.showTime =showTime;
        if(display){
            if (mContext != null && (mContext instanceof Activity) && curPriority <= lastPriority && lastDialogType > 0) {
                ((DialogActivity) mContext).dismissDialog(lastDialogType);
                if(dialogCallBack != null){
                    if(!((Event.contains(Event.nomalEvent,curEventType) &&Event.contains(Event.nomalEvent,lastEventType))
                    || (Event.contains(Event.noticeEvent,lastEventType)))) {
                        dialogCallBack.onSave(lastDialogType, lastEventType, lastPriority, beginTime, lastMessage);
                    }
                }
            }
        }
        onShow();
    }

    public void onShow(){
        if(mContext != null && (mContext instanceof Activity)){
            Bundle bundle = new Bundle();
            bundle.putString("Message", curMessage);
            ((DialogActivity) mContext).showDialog(curDialogType, bundle);
            if(((DialogActivity)mContext).isDialogShowing()){
                if(dialogCallBack != null){
                    dialogCallBack.onShow(curPriority,curEventType,curDialogType,true);
                }
                lastDialogType = curDialogType;
                lastEventType = curEventType;
                lastPriority = curPriority;
                lastMessage = curMessage;
                beginTime = System.currentTimeMillis();
                display =true;
            }
            dialogDismissAlgorithm(lastDialogType,lastEventType);
        }
    }
    private void dialogDismissAlgorithm(int dialogType,int eventType){
      if(Event.contains(Event.nomalEvent,eventType)
              || Event.contains(Event.highTemperatureLv1Event,eventType)
              || Event.contains(Event.limitRecordingEvent,eventType)){
          if(showTime >0){
              if(System.currentTimeMillis() -showTime < DELAY_TIME) {
                  delayHideDialogDisplay(System.currentTimeMillis() - showTime, dialogType, eventType);
              }else{
                  onDismiss(dialogType,eventType);
              }
          }else {
              delayHideDialogDisplay(DELAY_TIME, dialogType, eventType);
          }
      } else if(Event.contains(Event.sdCardUnMountedEvent,eventType)){
          if(isSdcardInserted){
              onDismiss(dialogType,eventType);
          }
      } else if(Event.contains(Event.sdCardAbnormalEvent,eventType)){
          if(isSdcardPulledOut || isSdcardInitSuccess){
              onDismiss(dialogType,eventType);
          }
      } else if(Event.contains(Event.highTemperatureLv3Event,eventType)){
          if(isPowerOFF){
              onDismiss(dialogType,eventType);
          }
      } else if(Event.contains(Event.highTemperatureLv2Event,eventType)){
          if(isResumeRecording){
              onDismiss(dialogType,eventType);
          }
      } else if(Event.contains(Event.abnormalStopRecordingEvent,eventType)){
          if(isSdcardPulledOut || isStartRecording){
              onDismiss(dialogType,eventType);
          }
      } else if(Event.contains(Event.noticeEvent,eventType)
              ||Event.contains(Event.simCardErroeEvent,eventType)){
          if(isSpeechCompleted){
              onDismiss(dialogType,eventType);
          }
      }
    }
    public void refreshDialogDisplay(int dialogType,int eventType){
        dialogDismissAlgorithm(dialogType,eventType);
    }
    public void onDismiss(int dialogType,int eventType){
        if(mContext != null && (mContext instanceof Activity)){
            if(((DialogActivity)mContext).getDialogType() == dialogType &&((DialogActivity)mContext).isDialogShowing()) {
                ((DialogActivity) mContext).dismissDialog(dialogType);
                if(dialogCallBack != null){
                    dialogCallBack.onDismiss(lastPriority,eventType,dialogType,false);
                }
            }
        }
    }
    public void resetLastEvent(){
        lastMessage = "";
        lastPriority = Integer.MAX_VALUE;
        lastEventType =-1;
        lastDialogType = -1;
        display =false;
    }
    public void reset(){
        lastMessage = "";
        lastPriority = Integer.MAX_VALUE;
        lastEventType =-1;
        lastDialogType = -1;
        display =false;
        showTime = 0;
        isSdcardPulledOut =false;
        isSdcardInserted =false;
        isPowerOFF =false;
        isStartRecording =false;
        isResumeRecording = false;
        isSpeechCompleted =false;
        isSdcardInitSuccess = false;
        if(mCancelableRunnable != null){
            mCancelableRunnable._cancel();
            mCancelableRunnable = null;
        }

    }

    public void setSdcardPulledOut(boolean isSdcardPulledOut){
        this.isSdcardPulledOut = isSdcardPulledOut;
    }
    public void setSdcardInserted(boolean isSdcardInserted){
        this.isSdcardInserted = isSdcardInserted;
    }
    public void setPowerOff(boolean isPowerOFF){
        this.isPowerOFF = isPowerOFF;
    }
    public void setStartRecording(boolean isStartRecording){
        this.isStartRecording = isStartRecording;
    }
    public void setResumeRecording(boolean isResumeRecording){
        this.isResumeRecording = isResumeRecording;
    }
    public void setSpeechCompleted(boolean isSpeechCompleted){
        this.isSpeechCompleted =isSpeechCompleted;
    }
    public void setSdcardInitSuccess(boolean isSdcardInitSuccess){
        this.isSdcardInitSuccess =isSdcardInitSuccess;
    }
    private void delayHideDialogDisplay(long waitTime,final int dialogType,final int eventType){
        CancelableRunnable cancelableRunnable = mCancelableRunnable;
        if(cancelableRunnable != null){
            cancelableRunnable._cancel();
        }
        mCancelableRunnable = new CancelableRunnable() {
            @Override
            protected void doRun() {
                showTime = 0;
                onDismiss(dialogType,eventType);
                if(eventType == Event.HIGH_TEMPERATURE_THRESHOLD_LV1){
                    EventUtil.sendEvent(Integer.valueOf(eventType));
                }
            }
        };
        handler.postDelayed(mCancelableRunnable,waitTime);
    }
    public interface DialogCallBack{
        void onSave(int dialogType, int eventType, int priority, long beginTime, String message);
        void onShow(int priority, int eventType, int dialogType, boolean display);
        void onDismiss(int priority, int eventType, int dialogType, boolean display);
    }
    public void setDialogCallBack(DialogCallBack dialogCallBack){
        this.dialogCallBack = dialogCallBack;
    }
    public void setContext(Context context){
        mContext = context;
    }
    public void setCancelableRunnable(boolean isCancel){
        if(isCancel && mCancelableRunnable != null){
            mCancelableRunnable._cancel();
        }
    }

}