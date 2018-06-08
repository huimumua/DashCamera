package com.askey.dvr.cdr7010.dashcam.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;

import com.askey.dvr.cdr7010.dashcam.R;
import com.askey.dvr.cdr7010.dashcam.application.DashCamApplication;
import com.askey.dvr.cdr7010.dashcam.domain.KeyAdapter;
import com.askey.dvr.cdr7010.dashcam.logic.GlobalLogic;
import com.askey.dvr.cdr7010.dashcam.mvp.view.NoticeFragment;
import com.askey.dvr.cdr7010.dashcam.mvp.view.UpdateFragment;
import com.askey.dvr.cdr7010.dashcam.service.DialogManager;
import com.askey.dvr.cdr7010.dashcam.util.ActivityUtils;
import com.askey.dvr.cdr7010.dashcam.util.Const;
import com.askey.dvr.cdr7010.dashcam.util.Logg;
import com.askey.dvr.cdr7010.dashcam.util.SPUtils;
import com.askey.platform.AskeySettings;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;


public class NoticeActivity extends DialogActivity implements NoticeFragment.NoticeListener,UpdateFragment.UpdateListener {
    private NoticeFragment noticeFragment ;
    private UpdateFragment updateFragment;
    private boolean isUpdate;
    private UpdateInfo updateInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice);
        noticeFragment = NoticeFragment.newInstance(null);
        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                noticeFragment, R.id.contentFrame);
    }
    @Override
    public void onKeyShortPressed(int keyCode) {
        switch (keyCode) {
            case KeyAdapter.KEY_ENTER:
                if(updateInfo != null && (updateInfo.updateResultState == Const.UPDATE_SUCCESS)
                        &&(updateInfo.updateType == Const.SDCARD_UPDATE)) {
                    startNextActivity();
                }
        }
    }


    @Override
    public void noticeJump() {
        updateInfo = parseFile("/cache/recovery_result");
            if (updateInfo != null && ((updateInfo.updateResultState == Const.UPDATE_SUCCESS)
                     ||(updateInfo.updateResultState == Const.UPDATE_FAIL))) {
                isUpdate = true;
                updateFragment = new UpdateFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("updateType", (updateInfo.updateType == Const.OTA_UPDATE) ? Const.OTA_UPDATE : Const.SDCARD_UPDATE);
                updateFragment.setArguments(bundle);
                ActivityUtils.hideFragment(getSupportFragmentManager(), noticeFragment);
                ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                        updateFragment, R.id.contentFrame);
            } else{
                startNextActivity();
            }
        }

    @Override
    public void onBackPressed() {
    }
    @Override
    protected  boolean handleKeyEvent(KeyEvent event){
        return false;
    }
    @Override
    public void updateCompleteJump() {
        startNextActivity();
    }
    @Override
    public void displayTipInfo() {
        if(updateInfo != null &&(updateInfo.updateResultState == Const.UPDATE_SUCCESS)) {
            DialogManager.getIntance().showDialog(DialogActivity.DIALOG_TYPE_UPDATE, getResources().getString(R.string.system_update_success), true);
        }else if(updateInfo != null &&(updateInfo.updateResultState == Const.UPDATE_FAIL)){
            DialogManager.getIntance().showDialog(DialogActivity.DIALOG_TYPE_UPDATE, getResources().getString(R.string.system_update_fail), true);
        }
    }
    private UpdateInfo parseFile(String filePath){
        UpdateInfo updateInfo =null;
        if(TextUtils.isEmpty(filePath)){
            return null;
        }
        File dir = new File(filePath);
        if (dir.exists()) {
            try {
                updateInfo = new UpdateInfo();
                FileReader mFileReader = new FileReader("/cache/recovery_result");
                BufferedReader mBufferedReader = new BufferedReader(mFileReader);
                String mReadText = "";
                String mTextLine = mBufferedReader.readLine();
                while (mTextLine!=null) {
                    mReadText += mTextLine+"\n";
                    mTextLine = mBufferedReader.readLine();
                }
                updateInfo.updateType =Const.SDCARD_UPDATE;
                if(mReadText.contains("0")) {
                    updateInfo.updateResultState =Const.UPDATE_SUCCESS;
                }
                else {
                    updateInfo.updateResultState =Const.UPDATE_FAIL;
                }
            } catch(Exception e) {
            }
        }
        return updateInfo;
    }
    public void startNextActivity(){
        if (GlobalLogic.getInstance().getInt(AskeySettings.Global.SETUP_WIZARD_AVAILABLE,1) == Const.FIRST_INIT_SUCCESS) {
            ActivityUtils.startActivity(this, this.getPackageName(), "com.askey.dvr.cdr7010.dashcam.ui.MainActivity", true);
        } else {
            ActivityUtils.startActivity(this, Const.PACKAGE_NAME, Const.CLASS_NAME, true);
        }
    }
    @Override
    public void onDestroy() {
        updateInfo = null;
        if(isUpdate) {
            DialogManager.getIntance().dismissDialog(DialogActivity.DIALOG_TYPE_UPDATE);
            isUpdate = false;
        }
        super.onDestroy();
    }
    private class UpdateInfo{
       public int updateType =-1;
       public int updateResultState =-1;
    }
}