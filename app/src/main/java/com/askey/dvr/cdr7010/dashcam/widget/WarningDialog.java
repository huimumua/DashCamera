package com.askey.dvr.cdr7010.dashcam.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.askey.dvr.cdr7010.dashcam.R;
import com.askey.dvr.cdr7010.dashcam.domain.Event;

public class WarningDialog extends Dialog {
    private ImageView waringImg;
    private TextView messageText;
    private String msg;
    private Context mContext;
    public WarningDialog(Context context, boolean cancelable, OnCancelListener cancelListener){
        super(context,cancelable,cancelListener);
        mContext =context;
    }
    public WarningDialog(Context context, int theme){
        super(context,theme);
        mContext = context;
    }
    public WarningDialog(Context context){
        super(context);
        mContext = context;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_warning);
        android.view.WindowManager.LayoutParams parames = getWindow().getAttributes();
        parames.height = 136;
        parames.width = 248;
        getWindow().setAttributes(parames);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        setCanceledOnTouchOutside(true);
        initViews();
    }
    private void initViews(){
        waringImg = (ImageView)findViewById(R.id.warning_img);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        param.width = 248;
        param.height = 66;
        param.topMargin = 18;
        param.gravity = Gravity.CENTER;
        waringImg.setLayoutParams(param);
        waringImg.setImageResource(R.drawable.dialog_warning_img);

        LinearLayout.LayoutParams messageParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,24);
        messageParam.topMargin = 10;
        messageText = (TextView)findViewById(R.id.content);
        messageText.setText(msg);
        messageText.setTextColor(Color.BLACK);
        messageText.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.TOP);
        messageText.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
        messageText.setPadding(10,0,10,0);
        messageText.setLayoutParams(messageParam);
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
    public void setImageResourceByEventType(int eventType){
        if(waringImg != null) {
            switch (eventType) {
                case Event.ABRUPT_HANDLE:
                    waringImg.setImageResource(R.drawable.dialog_handle);
                    break;
                case Event.RAPID_ACCELERATION:
                    waringImg.setImageResource(R.drawable.dialog_speedup);
                    break;
                case Event.RAPID_DECELERATION:
                    waringImg.setImageResource(R.drawable.dialog_speeddown);
                    break;
                case Event.REVERSE_RUN:
                    waringImg.setImageResource(R.drawable.dialog_reverse);
                    break;
                case Event.DRIVING_OUTSIDE_THE_DESIGNATED_AREA:
                    waringImg.setImageResource(R.drawable.dialog_outside);
                    break;
                default:
                    waringImg.setImageResource(R.drawable.dialog_warning_img);
            }
        }
    }

}