package com.askey.dvr.cdr7010.dashcam.notice;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;

import com.askey.dvr.cdr7010.dashcam.R;
import com.askey.dvr.cdr7010.dashcam.application.DashCamApplication;
import com.askey.dvr.cdr7010.dashcam.mvp.view.NoticeFragment;
import com.askey.dvr.cdr7010.dashcam.util.ActivityUtils;
import com.askey.dvr.cdr7010.dashcam.util.Const;
import com.askey.dvr.cdr7010.dashcam.util.SPUtils;


public class NoticeActivity extends AppCompatActivity implements NoticeFragment.NoticeListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        setContentView(R.layout.activity_notice);
        NoticeFragment noticeFragment = NoticeFragment.newInstance(null);
        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                noticeFragment, R.id.contentFrame);
    }
    @Override
    public void noticeJump(){
        if(SPUtils.contains(DashCamApplication.getAppContext(), Const.IS_FIRST_BOOT)){
            if((Boolean)SPUtils.get(DashCamApplication.getAppContext(),Const.IS_FIRST_BOOT,false)){
                SPUtils.put(DashCamApplication.getAppContext(),Const.IS_FIRST_BOOT,true);
            }
        }else{
            SPUtils.put(DashCamApplication.getAppContext(),Const.IS_FIRST_BOOT,true);
            ActivityUtils.startActivity(this,this.getPackageName(),"com.askey.dvr.cdr7010.dashcam.ui.MainActivity",true);
        }
    }
}