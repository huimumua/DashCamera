package com.askey.dvr.cdr7010.dashcam.service;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.askey.dvr.cdr7010.dashcam.activity.DialogActivity;

public class DialogManager{
    private static final String TAG = DialogManager.class.getSimpleName();
    private static DialogManager intance;
    private Context mContext;
    private int lastPriority = Integer.MAX_VALUE;


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
    public void showDialog(int dialogType,int eventType){
        int priority = EventManager.getInstance().getEventInfoByEventType(eventType).getPriority();
        String message = EventManager.getInstance().getEventInfoByEventType(eventType).getEventDescription();
        if(mContext != null && (mContext instanceof Activity)){
            if(((DialogActivity)mContext).isDialogShowing()){
                if(priority < lastPriority){
                    ((DialogActivity)mContext).dismissDialog();
                    Bundle bundle = new Bundle();
                    bundle.putString("Message",message);
                    ((DialogActivity)mContext).showDialog(dialogType,bundle);
                    lastPriority = priority;
                }
            }else{
                Bundle bundle = new Bundle();
                bundle.putString("Message",message);
                ((DialogActivity)mContext).showDialog(dialogType,bundle);
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
}