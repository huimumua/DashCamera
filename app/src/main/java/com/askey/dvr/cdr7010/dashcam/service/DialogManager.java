package com.askey.dvr.cdr7010.dashcam.service;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.askey.dvr.cdr7010.dashcam.activity.DialogActivity;
import com.askey.dvr.cdr7010.dashcam.domain.Event;
import com.askey.dvr.cdr7010.dashcam.domain.EventInfo;
import com.askey.dvr.cdr7010.dashcam.ui.utils.CancelableRunnable;

public class DialogManager{
    private static final String TAG = DialogManager.class.getSimpleName();
    private static DialogManager intance;
    private static final int DELAY_TIME =5000;
    private Context mContext;
    private Handler handler;
    private CancelableRunnable mCancelableRunnable;
    private int lastPriority = Integer.MAX_VALUE;


    private DialogManager(){
        handler = new Handler(Looper.getMainLooper());
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
    }
    public void unregisterContext(){
        mContext = null;
    }
    public void showDialog(int dialogType,String message,boolean resize){
        if(mContext != null && (mContext instanceof Activity)){
            Bundle bundle = new Bundle();
            bundle.putString("Message",message);
            if(resize){
                bundle.putInt("Width",300);
                bundle.putInt("Height",136);
            }
            ((DialogActivity)mContext).showDialog(dialogType,bundle);
        }
    }
    public void showDialog(int eventType){
        EventInfo eventInfo = EventManager.getInstance().getEventInfoByEventType(eventType);
        if(eventInfo !=null) {
            int priority = eventInfo.getPriority();
            int dialogType = eventInfo.getDialogType();
            String message = eventInfo.getEventDescription();
            if (mContext != null && (mContext instanceof Activity) && dialogType > 0) {
                if (((DialogActivity) mContext).isDialogShowing()) {
                    if (priority <= lastPriority) {
                        ((DialogActivity) mContext).dismissDialog();
                        Bundle bundle = new Bundle();
                        bundle.putString("Message", message);
                        ((DialogActivity) mContext).showDialog(dialogType, bundle);
                        lastPriority = priority;
                    }
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putString("Message", message);
                    ((DialogActivity) mContext).showDialog(dialogType, bundle);
                    lastPriority = priority;
                }
                delayCancelDialogDisplay(eventType,dialogType);
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
            if(((DialogActivity)mContext).isDialogShowing()) {
                ((DialogActivity) mContext).dismissDialog();
            }
        }
    }
    private void delayCancelDialogDisplay(int eventType,int dialogType){
        switch(eventType){
            case Event.NOTICE_START:
            case Event.DRIVING_REPORT:
            case Event.MONTHLY_DRIVING_REPORT:
            case Event.AdDVICE_BEFORE_DRIVING:
            case Event.GPS_LOCATION_INFORMATION:
            case Event.GPS_LOCATION_INFORMATION_ERROR:
            case Event.RECORDING_FAILED:
            case Event.HIGH_TEMPERATURE_THRESHOLD_LV1:
            case Event.EVENT_RECORDING_START:
                delayHideDialogDisplay(DELAY_TIME,dialogType);
                break;
            default:
        }
    }
    private void delayHideDialogDisplay(long waitTime,final int dialogType){
        CancelableRunnable cancelableRunnable = mCancelableRunnable;
        if(cancelableRunnable != null){
            cancelableRunnable._cancel();
        }
        mCancelableRunnable = new CancelableRunnable() {
            @Override
            protected void doRun() {
                dismissDialog(dialogType);
            }
        };
        handler.postDelayed(mCancelableRunnable,waitTime);
    }
}