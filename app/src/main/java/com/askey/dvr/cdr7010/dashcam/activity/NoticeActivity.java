package com.askey.dvr.cdr7010.dashcam.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;

import com.askey.dvr.cdr7010.dashcam.R;
import com.askey.dvr.cdr7010.dashcam.application.DashCamApplication;
import com.askey.dvr.cdr7010.dashcam.domain.KeyAdapter;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.jvckenwood.VersionUpReceiver;
import com.askey.dvr.cdr7010.dashcam.logic.GlobalLogic;
import com.askey.dvr.cdr7010.dashcam.mvp.view.NoticeFragment;
import com.askey.dvr.cdr7010.dashcam.mvp.view.UpdateFragment;
import com.askey.dvr.cdr7010.dashcam.service.DialogManager;
import com.askey.dvr.cdr7010.dashcam.service.TTSManager;
import com.askey.dvr.cdr7010.dashcam.util.ActivityUtils;
import com.askey.dvr.cdr7010.dashcam.util.Const;
import com.askey.dvr.cdr7010.dashcam.util.Logg;
import com.askey.dvr.cdr7010.dashcam.util.SPUtils;
import com.askey.platform.AskeySettings;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;


public class NoticeActivity extends DialogActivity implements NoticeFragment.NoticeListener, UpdateFragment.UpdateListener {
    private static final String TAG = "NoticeActivity";
    private NoticeFragment noticeFragment;
    private UpdateFragment updateFragment;
    private boolean isUpdate;
    private UpdateInfo updateInfo;
    private boolean isNoticeFinish = false;

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
                if (updateInfo != null && /*(updateInfo.updateType == Const.SDCARD_UPDATE) && */isUpdate) {
                    startNextActivity();
                }
        }
    }

    @Override
    public void noticeTimerFinish() {
        isNoticeFinish = true;
    }

    @Override
    public void noticeJump() {
//        String updateResult = (String)SPUtils.get(DashCamApplication.getAppContext(), Const.PREFERENCE_KEY_UPDATE_COMPLETED, "");
//        updateInfo = parseJson(updateResult);
//        //updateInfo = parseFile("/cache/recovery_result");
//            if (updateInfo != null && ((updateInfo.updateResultState == Const.UPDATE_SUCCESS)
//                     ||(updateInfo.updateResultState == Const.UPDATE_FAIL))) {
//                SPUtils.remove(DashCamApplication.getAppContext(), Const.PREFERENCE_KEY_UPDATE_COMPLETED);
//                isUpdate = true;
//                updateFragment = new UpdateFragment();
//                Bundle bundle = new Bundle();
//                bundle.putInt("updateType", (updateInfo.updateType == Const.OTA_UPDATE) ? Const.OTA_UPDATE : Const.SDCARD_UPDATE);
//                updateFragment.setArguments(bundle);
//                ActivityUtils.hideFragment(getSupportFragmentManager(), noticeFragment);
//                ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
//                        updateFragment, R.id.contentFrame);
//            } else{
//                startNextActivity();
//            }
        if (updateInfo != null && updateInfo.updateType == Const.NONE_UPDATE) {
            Logg.i(TAG, "=noticeJump=None=startNextActivity");
            startNextActivity();
        } else if (updateInfo != null && ((updateInfo.updateResultState == Const.UPDATE_SUCCESS)
                || (updateInfo.updateResultState == Const.UPDATE_FAIL))) {
            Logg.i(TAG, "=noticeJump=UpdateFragment=startNextActivity");
            isUpdate = true;
            updateFragment = new UpdateFragment();
//            Bundle bundle = new Bundle();
//            bundle.putInt("updateType", (updateInfo.updateType == Const.OTA_UPDATE) ? Const.OTA_UPDATE : Const.SDCARD_UPDATE);
//            updateFragment.setArguments(bundle);
            ActivityUtils.hideFragment(getSupportFragmentManager(), noticeFragment);
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    updateFragment, R.id.contentFrame);
        } else {
            Logg.i(TAG, "=noticeJump=no update info===");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHandleEvent(Object eventType) {
        if (eventType instanceof VersionUpReceiver.StartUpInfo || eventType instanceof VersionUpReceiver.UpdateCompleteInfo) {
            updateInfo = new UpdateInfo();
            if (eventType instanceof VersionUpReceiver.StartUpInfo && ((VersionUpReceiver.StartUpInfo) eventType).updateInfo == 0) {//None
                Logg.i(TAG, "=system_update=None=");
                updateInfo.updateType = Const.NONE_UPDATE;
            } else if (eventType instanceof VersionUpReceiver.UpdateCompleteInfo) {
                if (((VersionUpReceiver.UpdateCompleteInfo) eventType).result == 0) {//成功
                        updateInfo.updateResultState = Const.UPDATE_SUCCESS;
                        Logg.i(TAG, "=system_update_success==");
                    } else if (((VersionUpReceiver.UpdateCompleteInfo) eventType).result == -1) {//アップデート失敗
                        updateInfo.updateResultState = Const.UPDATE_FAIL;
                        Logg.i(TAG, "=system_update_fail==");
                    }
//                if (((VersionUpReceiver.UpdateCompleteInfo) eventType).type == 0) {//OTA
////                    updateInfo.updateType = Const.OTA_UPDATE;
//                    if (((VersionUpReceiver.UpdateCompleteInfo) eventType).result == 0) {//成功
//                        updateInfo.updateResultState = Const.UPDATE_SUCCESS;
//                        Logg.i(TAG, "=system_update_success=OTA_UPDATE=");
//                    } else if (((VersionUpReceiver.UpdateCompleteInfo) eventType).result == -1) {//アップデート失敗
//                        updateInfo.updateResultState = Const.UPDATE_FAIL;
//                        Logg.i(TAG, "=system_update_fail=OTA_UPDATE=");
//                    }
//
//                } else if (((VersionUpReceiver.UpdateCompleteInfo) eventType).type == 2) {//SDカード
////                    updateInfo.updateType = Const.SDCARD_UPDATE;
//                    if (((VersionUpReceiver.UpdateCompleteInfo) eventType).result == 0) {//成功
//                        updateInfo.updateResultState = Const.UPDATE_SUCCESS;
//                        Logg.i(TAG, "=system_update_success=SDCARD_UPDATE=");
//                    } else if (((VersionUpReceiver.UpdateCompleteInfo) eventType).result == -1) {//アップデート失敗
//                        updateInfo.updateResultState = Const.UPDATE_FAIL;
//                        Logg.i(TAG, "=system_update_fail=SDCARD_UPDATE=");
//                    }
//                }
            }
            Logg.i(TAG, "=onHandleEvent=isNoticeFinish=" + isNoticeFinish);
            if (isNoticeFinish) {
                noticeJump();
                isNoticeFinish = false;
            }
        }
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    protected boolean handleKeyEvent(KeyEvent event) {
        return false;
    }

    @Override
    public void updateCompleteJump() {
        startNextActivity();
    }

    @Override
    public void displayTipInfo() {
        if (updateInfo != null && (updateInfo.updateResultState == Const.UPDATE_SUCCESS)) {
            DialogManager.getIntance().showDialog(DialogActivity.DIALOG_TYPE_UPDATE, getResources().getString(R.string.system_update_success), true);
            TTSManager.getInstance().ttsNormalStart(201, new int[]{0x0A03});
        } else if (updateInfo != null && (updateInfo.updateResultState == Const.UPDATE_FAIL)) {
            DialogManager.getIntance().showDialog(DialogActivity.DIALOG_TYPE_UPDATE, getResources().getString(R.string.system_update_fail), true);
            TTSManager.getInstance().ttsNormalStart(200, new int[]{0x0A04});
        }
    }

//        private UpdateInfo parseJson(String updateResult){
//        if(TextUtils.isEmpty(updateResult)){
//            return null;
//        }
//        UpdateInfo updateInfo = new UpdateInfo();
//        try {
//                JSONObject jsonObject = new JSONObject(updateResult);
//                updateInfo.updateType = jsonObject.getInt("type");
//                updateInfo.updateResultState = jsonObject.getInt("result");
//            }catch(Exception e){
//                e.printStackTrace();
//            }
//            return updateInfo;
//    }
//
//    private UpdateInfo parseFile(String filePath){
//        UpdateInfo updateInfo =null;
//        if(TextUtils.isEmpty(filePath)){
//            return null;
//        }
//        File dir = new File(filePath);
//        if (dir.exists()) {
//            try {
//                updateInfo = new UpdateInfo();
//                FileReader mFileReader = new FileReader("/cache/recovery_result");
//                BufferedReader mBufferedReader = new BufferedReader(mFileReader);
//                String mReadText = "";
//                String mTextLine = mBufferedReader.readLine();
//                while (mTextLine!=null) {
//                    mReadText += mTextLine+"\n";
//                    mTextLine = mBufferedReader.readLine();
//                }
//                updateInfo.updateType =Const.SDCARD_UPDATE;
//                if(mReadText.contains("0")) {
//                    updateInfo.updateResultState =Const.UPDATE_SUCCESS;
//                }
//                else {
//                    updateInfo.updateResultState =Const.UPDATE_FAIL;
//                }
//            } catch(Exception e) {
//            }
//        }
//        return updateInfo;
//    }
    public void startNextActivity() {
        if (GlobalLogic.getInstance().getInt(AskeySettings.Global.SETUP_WIZARD_AVAILABLE, 1) == Const.FIRST_INIT_SUCCESS) {
            ActivityUtils.startActivity(this, this.getPackageName(), "com.askey.dvr.cdr7010.dashcam.ui.MainActivity", true);
        } else {
            ActivityUtils.startActivity(this, Const.PACKAGE_NAME, Const.CLASS_NAME, true);
        }
    }

    @Override
    public void onDestroy() {
        updateInfo = null;
        if (isUpdate) {
            DialogManager.getIntance().dismissDialog(DialogActivity.DIALOG_TYPE_UPDATE);
            isUpdate = false;
        }
        super.onDestroy();
    }

    private class UpdateInfo {
        public int updateType = -1;
        public int updateResultState = -1;
    }
}