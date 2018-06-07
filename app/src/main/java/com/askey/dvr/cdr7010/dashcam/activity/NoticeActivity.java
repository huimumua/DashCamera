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
import com.askey.dvr.cdr7010.dashcam.util.SPUtils;
import com.askey.platform.AskeySettings;

import org.json.JSONObject;


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
        String updateResult = (String)SPUtils.get(DashCamApplication.getAppContext(), Const.PREFERENCE_KEY_UPDATE_COMPLETED, "");
        updateInfo = parseJson(updateResult);
            if (updateInfo != null && (updateInfo.updateResultState == Const.UPDATE_SUCCESS) ) {
                isUpdate = true;
                SPUtils.remove(DashCamApplication.getAppContext(), Const.PREFERENCE_KEY_UPDATE_COMPLETED);
                updateFragment = new UpdateFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("updateType", (updateInfo.updateType == Const.OTA_UPDATE)? Const.OTA_UPDATE:Const.SDCARD_UPDATE);
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
        DialogManager.getIntance().showDialog(DialogActivity.DIALOG_TYPE_UPDATE,getResources().getString(R.string.system_update_completed),true);
    }
    private UpdateInfo parseJson(String updateResult){
        if(TextUtils.isEmpty(updateResult)){
            return null;
        }
        UpdateInfo updateInfo = new UpdateInfo();
        updateInfo.updateType = 2;
        updateInfo.updateResultState = 0;
        try {
            JSONObject jsonObject = new JSONObject(updateResult);
            updateInfo.updateType = jsonObject.getInt("type");
            updateInfo.updateResultState = jsonObject.getInt("result");
        }catch(Exception e){
            e.printStackTrace();
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