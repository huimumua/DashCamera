package com.askey.dvr.cdr7010.dashcam.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;

import com.askey.dvr.cdr7010.dashcam.R;
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
import com.askey.platform.AskeySettings;
import com.askey.platform.LogoSelect;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;


public class NoticeActivity extends DialogActivity implements NoticeFragment.NoticeListener, UpdateFragment.UpdateListener, VersionUpReceiver.PowerOnRelativeCallback {
    private static final String TAG = "NoticeActivity";
    private NoticeFragment noticeFragment;
    private UpdateFragment updateFragment;
    private boolean isUpdate;
    private UpdateInfos updateInfos;
    private boolean isNoticeFinish = false;
    private static final String ACTION_EVENT_STARTUP = "com.jvckenwood.versionup.STARTUP";
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Logg.d(TAG, "onReceive: " + action);
            if (ACTION_EVENT_STARTUP.equals(action)) {
                int bootinfo = intent.getIntExtra("bootinfo", -1);
                int updateInfo = intent.getIntExtra("updateInfo", -10);
                String farmver = intent.getStringExtra("farmver");
                String soundver = intent.getStringExtra("soundver");
                Logg.i(TAG, "onReceive: STARTUP: bootinfo=" + bootinfo + ", updateInfo=" + updateInfo);
                if (updateInfo == 0) {//None
                    onStartUp(bootinfo, updateInfo, farmver, soundver);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice);
//        initEncryptKey();
        noticeFragment = NoticeFragment.newInstance(null);
        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                noticeFragment, R.id.contentFrame);
        //add by Mark for PUCDR-1262
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_EVENT_STARTUP);
        registerReceiver(receiver, filter);
        //end add
        VersionUpReceiver.registerPowerOnRelativeCallback(this);
        setSystemLogo();
    }

//    public static final String AES_KEY = "CaH5U?<5no_z3S,0Zx,8Ua<0Qo&5Ep/0";

//    private void initEncryptKey() {
//        try {
//            String encrypt = KeyStoreUtils.getInstance().encryptByPublicKey(AES_KEY);
//            Logg.d(TAG, "encrypt==" + encrypt);
//            SPUtils.put(this, SPUtils.STR_ENCODE, encrypt);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    public void onKeyShortPressed(int keyCode) {
        switch (keyCode) {
            case KeyAdapter.KEY_ENTER:
                if (updateInfos != null && (updateInfos.updateResultState != Const.UPDATE_READY) && isUpdate) {
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
        if (updateInfos != null && updateInfos.updateType == Const.NONE_UPDATE) {
            Logg.i(TAG, "=noticeJump=None=startNextActivity");
            startNextActivity();
        } else if (updateInfos != null && ((updateInfos.updateResultState == Const.UPDATE_SUCCESS)
                || (updateInfos.updateResultState == Const.UPDATE_FAIL) || updateInfos.updateResultState == Const.UPDATE_READY)) {
            Logg.i(TAG, "=noticeJump=UpdateFragment=startNextActivity");
            isUpdate = true;
            updateFragment = new UpdateFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("updateResultState", updateInfos.updateResultState);
            updateFragment.setArguments(bundle);
            ActivityUtils.hideFragment(getSupportFragmentManager(), noticeFragment);
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    updateFragment, R.id.contentFrame);
        } else {
            Logg.i(TAG, "=noticeJump=no update info===");
//            Intent intent =new Intent(Intent.ACTION_BOOT_COMPLETED);
//            sendBroadcast(intent);
//            Intent intent2 = new Intent(Intent.ACTION_REBOOT);
//            intent2.putExtra("nowait", 1);
//            intent2.putExtra("interval", 1);
//            intent2.putExtra("window", 0);
//            sendBroadcast(intent2);
        }
    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onHandleEvent(Object eventType) {
//        if (eventType instanceof VersionUpReceiver.StartUpInfo || eventType instanceof VersionUpReceiver.UpdateCompleteInfo
//                || eventType instanceof VersionUpReceiver.UpdateReadyInfo) {
//            updateInfos = new UpdateInfos();
//            if (eventType instanceof VersionUpReceiver.StartUpInfo && ((VersionUpReceiver.StartUpInfo) eventType).updateInfo == 0) {//None
//                Logg.i(TAG, "=system_update=None=");
//                updateInfos.updateType = Const.NONE_UPDATE;
//            } else if (eventType instanceof VersionUpReceiver.UpdateCompleteInfo) {
//                if (((VersionUpReceiver.UpdateCompleteInfo) eventType).result == 0) {//成功
//                    updateInfos.updateResultState = Const.UPDATE_SUCCESS;
//                    Logg.i(TAG, "=system_update_success==");
//                } else if (((VersionUpReceiver.UpdateCompleteInfo) eventType).result == -1) {//アップデート失敗
//                    updateInfos.updateResultState = Const.UPDATE_FAIL;
//                    Logg.i(TAG, "=system_update_fail==");
//                }
////                if (((VersionUpReceiver.UpdateCompleteInfo) eventType).type == 0) {//OTA
//////                    updateInfo.updateType = Const.OTA_UPDATE;
////                    if (((VersionUpReceiver.UpdateCompleteInfo) eventType).result == 0) {//成功
////                        updateInfo.updateResultState = Const.UPDATE_SUCCESS;
////                        Logg.i(TAG, "=system_update_success=OTA_UPDATE=");
////                    } else if (((VersionUpReceiver.UpdateCompleteInfo) eventType).result == -1) {//アップデート失敗
////                        updateInfo.updateResultState = Const.UPDATE_FAIL;
////                        Logg.i(TAG, "=system_update_fail=OTA_UPDATE=");
////                    }
////
////                } else if (((VersionUpReceiver.UpdateCompleteInfo) eventType).type == 2) {//SDカード
//////                    updateInfo.updateType = Const.SDCARD_UPDATE;
////                    if (((VersionUpReceiver.UpdateCompleteInfo) eventType).result == 0) {//成功
////                        updateInfo.updateResultState = Const.UPDATE_SUCCESS;
////                        Logg.i(TAG, "=system_update_success=SDCARD_UPDATE=");
////                    } else if (((VersionUpReceiver.UpdateCompleteInfo) eventType).result == -1) {//アップデート失敗
////                        updateInfo.updateResultState = Const.UPDATE_FAIL;
////                        Logg.i(TAG, "=system_update_fail=SDCARD_UPDATE=");
////                    }
////                }
//            } else if (eventType instanceof VersionUpReceiver.UpdateReadyInfo) {
//                updateInfos.updateResultState = Const.UPDATE_READY;
//            }
//            Logg.i(TAG, "=onHandleEvent=isNoticeFinish=" + isNoticeFinish);
//            if (isNoticeFinish) {
//                noticeJump();
//                isNoticeFinish = false;
//            }
//        }
//    }

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
        if (updateInfos != null && (updateInfos.updateResultState == Const.UPDATE_SUCCESS)) {
            DialogManager.getIntance().showDialog(DialogActivity.DIALOG_TYPE_UPDATE, getResources().getString(R.string.system_update_success), true);
            TTSManager.getInstance().ttsNormalStart(201, new int[]{0x0A03});
        } else if (updateInfos != null && (updateInfos.updateResultState == Const.UPDATE_FAIL)) {
            DialogManager.getIntance().showDialog(DialogActivity.DIALOG_TYPE_UPDATE, getResources().getString(R.string.system_update_fail), true);
            TTSManager.getInstance().ttsNormalStart(200, new int[]{0x0A04});
        } else if (updateInfos != null && (updateInfos.updateResultState == Const.UPDATE_READY)) {
            DialogManager.getIntance().showDialog(DialogActivity.DIALOG_TYPE_UPDATE, getResources().getString(R.string.system_update_ready), true);
            TTSManager.getInstance().ttsNormalStart(202, new int[]{0x0A02});
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
        if (updateInfos.updateResultState != Const.UPDATE_READY) {//update ready 界面时不跳转
            if (GlobalLogic.getInstance().getInt(AskeySettings.Global.SETUP_WIZARD_AVAILABLE, 1) == Const.FIRST_INIT_SUCCESS) {
                ActivityUtils.startActivity(this, this.getPackageName(), "com.askey.dvr.cdr7010.dashcam.ui.MainActivity", true);
            } else {
                ActivityUtils.startActivity(this, Const.PACKAGE_NAME, Const.CLASS_NAME, true);
            }
        }
    }

    @Override
    public void onDestroy() {
        updateInfos = null;
        if (isUpdate) {
            DialogManager.getIntance().dismissDialog(DialogActivity.DIALOG_TYPE_UPDATE);
            isUpdate = false;
        }
        //add by Mark for PUCDR-1262
        unregisterReceiver(receiver);
        //end add
        VersionUpReceiver.unRegisterPowerOnRelativeCallback(this);
        super.onDestroy();
    }

    @Override
    public void onUpdateReady() {
        updateInfos = new UpdateInfos();
        updateInfos.updateResultState = Const.UPDATE_READY;

        Logg.i(TAG, "onUpdateReady: isNoticeFinish=" + isNoticeFinish);
        if (isNoticeFinish) {
            noticeJump();
            isNoticeFinish = false;
        }
    }

    @Override
    public void onUpdateCompleted(int type, int result) {
        Logg.i(TAG, "onUpdateCompleted: type=" + type + ", result=" + result);
        updateInfos = new UpdateInfos();
        if (result == 0) {//成功
            updateInfos.updateResultState = Const.UPDATE_SUCCESS;
        } else if (result == -1) {//アップデート失敗
            updateInfos.updateResultState = Const.UPDATE_FAIL;
        }

        Logg.i(TAG, "onUpdateCompleted: isNoticeFinish=" + isNoticeFinish);
        if (isNoticeFinish) {
            noticeJump();
            isNoticeFinish = false;
        }
    }

    @Override
    public void onStartUp(int bootinfo, int updateInfo, String farmver, String soundver) {
        updateInfos = new UpdateInfos();
        if (updateInfo == 0) {//None
            updateInfos.updateType = Const.NONE_UPDATE;
        }

        Logg.i(TAG, "onStartUp: isNoticeFinish=" + isNoticeFinish);
        if (isNoticeFinish) {
            noticeJump();
            isNoticeFinish = false;
        }
    }

    private class UpdateInfos {
        public int updateType = -1;
        public int updateResultState = -1;
    }

    private void setSystemLogo() {

        Log.d(TAG, "setSystemLogo~~~~");
        String filePath = "/storage/self/primary/";

        FileFilter fileFilter = null;
        File fileLogo = new File(filePath);
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            matchFileAndSetLogo(fileLogo);
        }

    }

    private void matchFileAndSetLogo(File fileLogo) {
        String logRegEx = ".AD_";
        String log1RegEx = ".AD_REWRITE";
        String imgRegEx = ".MD_";
        String img1RegEx = ".MS_REWRITE";
        File[] fileList = fileLogo.listFiles();
        if (fileList != null && fileList.length > 0) {
            for (File file : fileList) {
                if (file.getName().startsWith(log1RegEx)) {
                    writeSystemFile("/storage/self/primary/bootanimation1.zip", "/persist/media/bootanimation1.zip");
                    LogoSelect.writeLogoImage(1);
                } else if (file.getName().startsWith(logRegEx)) {
                    LogoSelect.setLogo(1);
                }
                if (file.getName().startsWith(img1RegEx)) {
                    writeSystemFile("/storage/self/primary/bootanimation2.zip", "/persist/media/bootanimation2.zip");
                    LogoSelect.writeLogoImage(2);
                } else if (file.getName().startsWith(imgRegEx)) {
                    LogoSelect.setLogo(2);
                }
            }
        }
    }

    class FileFilter implements FilenameFilter {
        private String mRegEx;

        public FileFilter(String regEx) {
            this.mRegEx = regEx;
        }

        @Override
        public boolean accept(File dir, String name) {
            return name.toLowerCase().startsWith(mRegEx);
        }

    }

    private void writeSystemFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) {
                InputStream inStream = new FileInputStream(oldPath);
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
                Log.d(TAG,"writeSystemFile   success~");
                try {
                    String command = "chmod 777 " +newPath;
                    Log.i(TAG, "command = " + command);
                    Runtime runtime = Runtime.getRuntime();

                    Process proc = runtime.exec(command);
                } catch (IOException e) {
                    Log.i(TAG,"chmod fail!!!!");
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e) {
            Log.d(TAG,"writeSystemFile  exception=="+e.getMessage());
            e.printStackTrace();

        }

    }

}