package com.askey.dvr.cdr7010.dashcam.activity;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;

import com.askey.dvr.cdr7010.dashcam.R;
import com.askey.dvr.cdr7010.dashcam.application.DashCamApplication;
import com.askey.dvr.cdr7010.dashcam.mvp.view.NoticeFragment;
import com.askey.dvr.cdr7010.dashcam.util.ActivityUtils;
import com.askey.dvr.cdr7010.dashcam.util.Const;
import com.askey.dvr.cdr7010.dashcam.util.SPUtils;

import java.io.File;


public class NoticeActivity extends AppCompatActivity implements NoticeFragment.NoticeListener {
    private static final String UPDATE_SUC_FILE_NAME = "update_suc.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice);
        NoticeFragment noticeFragment = NoticeFragment.newInstance(null);
        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                noticeFragment, R.id.contentFrame);
    }

    @Override
    public void noticeJump(boolean isStartRecord) {
        SPUtils.put(this, Const.IS_START_RECORD, isStartRecord);
        if (checkUpdateResult()) {
            ActivityUtils.startActivity(this, this.getPackageName(), "com.askey.dvr.cdr7010.dashcam.activity.UpdateCompleteActivity", true);
        } else {
            if (SPUtils.contains(DashCamApplication.getAppContext(), Const.IS_FIRST_BOOT)) {
                if ((Boolean) SPUtils.get(DashCamApplication.getAppContext(), Const.IS_FIRST_BOOT, false)) {
                    SPUtils.put(DashCamApplication.getAppContext(), Const.IS_FIRST_BOOT, true);
                    ActivityUtils.startActivity(this, this.getPackageName(), "com.askey.dvr.cdr7010.dashcam.ui.MainActivity", true);
                }
            } else {
                SPUtils.put(DashCamApplication.getAppContext(), Const.IS_FIRST_BOOT, true);
                ActivityUtils.startActivity(this, Const.PACKAGE_NAME, Const.CLASS_NAME, true);
            }
        }
    }

    @Override
    public void onBackPressed() {
        return;
    }

    public boolean checkUpdateResult() {
        final String SDCardRoot = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
        File file = new File(SDCardRoot + UPDATE_SUC_FILE_NAME);
        if (file.exists()) {
            return true;
        } else {
            return false;
        }
    }
}