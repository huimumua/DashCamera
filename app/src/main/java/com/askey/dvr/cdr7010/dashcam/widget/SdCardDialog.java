package com.askey.dvr.cdr7010.dashcam.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;

import com.askey.dvr.cdr7010.dashcam.R;

public class SdCardDialog extends Dialog{
    private TextView messageText;
    private Context mContext;
    private String msg;
    public SdCardDialog(Context context, boolean cancelable, OnCancelListener cancelListener){
        super(context,cancelable,cancelListener);
        mContext =context;
    }
    public SdCardDialog(Context context,int theme){
        super(context,theme);
        mContext = context;
    }
    public SdCardDialog(Context context){
        super(context);
        mContext = context;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_sdcard);
        android.view.WindowManager.LayoutParams parames = getWindow().getAttributes();
        parames.height = 136;
        parames.width = 248;
        getWindow().setAttributes(parames);
        setCanceledOnTouchOutside(false);
        initViews();
    }
    private void initViews(){
        messageText = (TextView)findViewById(R.id.content);
        messageText.setText(msg);
        messageText.setTextColor(0xccffffff);
        messageText.setTextSize(TypedValue.COMPLEX_UNIT_PX,22);
        messageText.setGravity(Gravity.CENTER);
        messageText.setLineSpacing(1.0f,1.2f);
    }
    public void setMessage(int resId){
        this.msg = mContext.getResources().getString(resId);
        if(messageText != null){
            messageText.setText(msg);
        }
    }
    public void setMessage(String msg){
        this.msg = msg;
        if(messageText != null){
            messageText.setText(msg);
        }
    }
}