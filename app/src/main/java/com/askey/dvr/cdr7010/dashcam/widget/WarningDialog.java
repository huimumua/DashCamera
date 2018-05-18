package com.askey.dvr.cdr7010.dashcam.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.askey.dvr.cdr7010.dashcam.R;

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
        initViews();
    }
    private void initViews(){
        waringImg = (ImageView)findViewById(R.id.warning_img);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        param.width = 248;
        param.height = 66;
        param.topMargin = 17;
        param.gravity = Gravity.CENTER;
        waringImg.setLayoutParams(param);
        waringImg.setImageResource(R.drawable.dialog_warning_img);

        messageText = (TextView)findViewById(R.id.content);
        messageText.setText(msg);
       // ((ViewGroup.MarginLayoutParams)messageText.getLayoutParams()).topMargin = 8;
        messageText.setTextColor(0xff000000);
        messageText.setGravity(Gravity.CENTER);
        messageText.setTextSize(TypedValue.COMPLEX_UNIT_PX,22);
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