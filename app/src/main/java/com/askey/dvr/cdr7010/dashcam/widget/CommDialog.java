package com.askey.dvr.cdr7010.dashcam.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.askey.dvr.cdr7010.dashcam.R;
import com.askey.dvr.cdr7010.dashcam.domain.KeyAdapter;
import com.askey.dvr.cdr7010.dashcam.util.Const;
import com.askey.dvr.cdr7010.dashcam.util.Logg;

public class CommDialog extends Dialog{
    private TextView messageText;
    private Context mContext;
    private String msg;
    private int type =TYPE_BUTTON_OK|TYPE_BUTTON_CANCEL;
    public static final int TYPE_BUTTON_OK =1;
    public static final int TYPE_BUTTON_CANCEL = 1<<2;
    public static final int TYPE_BUTTON_HIDE = 0;
    private  String BUTTON_OK_MSG = Const.STR_BUTTON_CONFIRM;
    private  String BUTTON_CANCEL_MSG = Const.STR_BUTTON_CANCEL;
    private OnClickListener mPositiveButtonListener;
    private OnClickListener mNegativeButtonListener;
    private int width = 0;
    private int height = 0;
    private  Button btnCancel =null;
    private  Button btnOk = null;

    public CommDialog(Context context, boolean cancelable, OnCancelListener cancelListener){
        super(context,cancelable,cancelListener);
        mContext =context;
    }
    public CommDialog(Context context, int theme){
        super(context,theme);
        mContext = context;
    }
    public CommDialog(Context context){
        super(context);
        mContext = context;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_comm);
        android.view.WindowManager.LayoutParams parames = getWindow().getAttributes();
        parames.height = height ==0 ? 136 : height;
        parames.width = width == 0 ? 248 : width;
        if(height != 0){
            getWindow().setGravity(Gravity.CENTER_VERTICAL|Gravity.LEFT);
            parames.x = 20;
        }
        getWindow().setAttributes(parames);
        if(type == TYPE_BUTTON_HIDE) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            setCanceledOnTouchOutside(true);
        }else{
            setCanceledOnTouchOutside(false);
        }
        initViews();
    }
    private void initViews(){
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        messageText = (TextView)findViewById(R.id.content);
        messageText.setText(msg);
        messageText.setTextColor(Color.BLACK);
        messageText.setTextSize(TypedValue.COMPLEX_UNIT_PX,18);
        messageText.setGravity(Gravity.CENTER);

        btnOk = (Button) findViewById(R.id.ib_ok);
        btnOk.setOnClickListener(clickListener);
        btnOk.setTextColor(Color.BLACK);
        btnOk.setText(BUTTON_OK_MSG);
        btnOk.setGravity(Gravity.CENTER);
        btnOk.setTextSize(TypedValue.COMPLEX_UNIT_PX,18);
        btnOk.getLayoutParams().width = 80;
        btnOk.getLayoutParams().height = 26;
        ((ViewGroup.MarginLayoutParams)btnOk.getLayoutParams()).leftMargin = 52;

        btnCancel = (Button) findViewById(R.id.ib_cancle);
        btnCancel.setOnClickListener(clickListener);
        btnCancel.setTextColor(Color.BLACK);
        btnCancel.setText(BUTTON_CANCEL_MSG);
        btnCancel.setGravity(Gravity.CENTER);
        btnCancel.setTextSize(TypedValue.COMPLEX_UNIT_PX,18);
        btnCancel.getLayoutParams().width = 80;
        btnCancel.getLayoutParams().height = 26;
        ((ViewGroup.MarginLayoutParams)btnCancel.getLayoutParams()).leftMargin = 20;

        if((type&TYPE_BUTTON_OK) == 0 && (type&TYPE_BUTTON_CANCEL) == 0){
            btnOk.setVisibility(View.GONE);
            btnCancel.setVisibility(View.GONE);
          return;
        }
        if((type&TYPE_BUTTON_OK) == 0){
            btnOk.setVisibility(View.GONE);
            btnCancel.setLayoutParams(layoutParams);
        }
        if((type&TYPE_BUTTON_CANCEL) == 0){
            btnCancel.setVisibility(View.GONE);
            btnOk.setLayoutParams(layoutParams);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(type ==(TYPE_BUTTON_CANCEL|TYPE_BUTTON_OK)){
                    btnOk.requestFocusFromTouch();
                }else {
                    if ((type & TYPE_BUTTON_CANCEL) == 0) {
                        btnOk.requestFocusFromTouch();
                    }
                    if ((type & TYPE_BUTTON_OK) == 0) {
                        btnCancel.requestFocusFromTouch();
                    }
                }
            }
        },20);
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
    public void setType(int type){
        this.type =type;
    }
    public int getType(){
        return type;
    }
    public void setButtonOkMsg(String msg){
        BUTTON_OK_MSG = msg;
    }
    public void setButtonCancelMsg(String msg){
        BUTTON_CANCEL_MSG = msg;
    }
    public void setDialogWidth(int width){
        this.width =width;
    }
    public void setDialogHeight(int height){
        this.height = height;
    }
    public void setPositiveButtonListener(final OnClickListener onClickListener){
        this.mPositiveButtonListener = onClickListener;
    }
    public void setNegativeButtonListener(final OnClickListener onClickListener){
        this.mNegativeButtonListener = onClickListener;
    }
    @Override
    public boolean dispatchKeyEvent(@NonNull KeyEvent event) {
        if (event.getKeyCode() == KeyAdapter.KEY_BACK) {
                return true;
        }
        if(type == (TYPE_BUTTON_OK|TYPE_BUTTON_CANCEL)){
            if(btnOk.hasFocus()){
                if(event.getKeyCode() == KeyAdapter.KEY_VOLUME_UP){
                    btnCancel.requestFocusFromTouch();
                }
            }
            if(btnCancel.hasFocus()){
                if(event.getKeyCode() == KeyAdapter.KEY_VOLUME_DOWN){
                    btnOk.requestFocusFromTouch();
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }
    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.ib_ok:
                    dismiss();
                    if(mPositiveButtonListener != null){
                        mPositiveButtonListener.onClick(CommDialog.this,BUTTON_POSITIVE);
                    }
                    break;
                case R.id.ib_cancle:
                    dismiss();
                    if(mNegativeButtonListener !=null){
                        mNegativeButtonListener.onClick(CommDialog.this,BUTTON_NEGATIVE);
                    }
                    break;
                default:
            }

        }
    };
}