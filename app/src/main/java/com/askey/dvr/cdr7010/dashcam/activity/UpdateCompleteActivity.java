package com.askey.dvr.cdr7010.dashcam.activity;

import android.os.Bundle;
import android.os.CountDownTimer;

import com.askey.dvr.cdr7010.dashcam.R;
import com.askey.dvr.cdr7010.dashcam.application.DashCamApplication;
import com.askey.dvr.cdr7010.dashcam.service.DialogManager;
import com.askey.dvr.cdr7010.dashcam.util.ActivityUtils;
import com.askey.dvr.cdr7010.dashcam.util.Const;
import com.askey.dvr.cdr7010.dashcam.util.SPUtils;

public class UpdateCompleteActivity extends DialogActivity{

    private CountDownTimer timer = new CountDownTimer(5000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {

        }
        @Override public void onFinish() {
            DialogManager.getIntance().dismissDialog(DIALOG_TYPE_COMM_TEXT);
            if(SPUtils.contains(DashCamApplication.getAppContext(), Const.IS_FIRST_BOOT)){
                if((Boolean)SPUtils.get(DashCamApplication.getAppContext(),Const.IS_FIRST_BOOT,false)){
                    SPUtils.put(DashCamApplication.getAppContext(),Const.IS_FIRST_BOOT,true);
                    ActivityUtils.startActivity(UpdateCompleteActivity.this, UpdateCompleteActivity.this.getPackageName(),"com.askey.dvr.cdr7010.dashcam.ui.MainActivity",true);
                }
            }else{
                SPUtils.put(DashCamApplication.getAppContext(),Const.IS_FIRST_BOOT,true);
                ActivityUtils.startActivity(UpdateCompleteActivity.this, Const.PACKAGE_NAME,Const.CLASS_NAME,true);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_succ);
        DialogManager.getIntance().showDialog(DIALOG_TYPE_COMM_TEXT,"システムを更新しました",true);
        timer.start();
    }
    @Override
    public void onBackPressed() {
        return;
    }
    @Override
    public void onDestroy(){
        if (timer != null) {
            timer.cancel();
        }
        super.onDestroy();
    }
}