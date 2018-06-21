package com.askey.dvr.cdr7010.dashcam.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.askey.dvr.cdr7010.dashcam.R;
import com.askey.dvr.cdr7010.dashcam.domain.KeyAdapter;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.jvckenwood.VersionUpReceiver;
import com.askey.dvr.cdr7010.dashcam.service.TTSManager;
import com.askey.dvr.cdr7010.dashcam.util.EventUtil;
import com.askey.dvr.cdr7010.dashcam.util.Logg;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class VersionUpActivity extends Activity {
    private ImageView btn_ok;
    private TextView textView;
    private boolean isNeedTransparent = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logg.i("VersionUpActivity", "==onCreate=");
        EventUtil.register(this);
    }

    @Override
    public void setTheme(int resid) {
        Logg.i("VersionUpActivity", "==setTheme=" + isNeedTransparent + "===" + getTheme());
        if (isNeedTransparent) {
            super.setTheme(android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
            Logg.i("VersionUpActivity", "==Theme_Translucent_NoTitleBar=" + android.R.style.Theme_Translucent_NoTitleBar_Fullscreen + "=getTheme()==" + getTheme());
        } else {
            super.setTheme(resid);
            Logg.i("VersionUpActivity", "==setTheme(resid)=" + resid);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHandleEvent(Object eventType) {
        if (eventType instanceof VersionUpReceiver.StartUpInfo || eventType instanceof VersionUpReceiver.UpdateCompleteInfo) {
//            Logg.d("VersionUpActivity", "onMessageEvent onHandleEvent,eventType=" + ((VersionUpReceiver.StartUpInfo) eventType).updateInfo);
            isNeedTransparent = false;
            setTheme(android.R.style.Theme_NoTitleBar_Fullscreen); //自定义的非透明的主题
            setContentView(R.layout.activity_version_up);
            btn_ok = (ImageView) findViewById(R.id.btn_ok);
            textView = (TextView) findViewById(R.id.content);
            if (eventType instanceof VersionUpReceiver.StartUpInfo && ((VersionUpReceiver.StartUpInfo) eventType).updateInfo == 0) {//None
                toNoticeActivity();
            }/* else if (((VersionUpReceiver.StartUpInfo) eventType).updateInfo == 1) {//Update failed
                btn_ok.setVisibility(View.VISIBLE);
                textView.setVisibility(View.VISIBLE);
                Logg.i("VersionUpActivity", "=system_update_fail==" + getResources().getString(R.string.system_update_fail));
                textView.setText(getResources().getString(R.string.system_update_fail));
                versionUpTimer.start();
                TTSManager.getInstance().ttsNormalStart(200, new int[]{0x0A04});
            } else if (((VersionUpReceiver.StartUpInfo) eventType).updateInfo == 2) {//Update completed
                versionUpTimer.start();
                btn_ok.setVisibility(View.VISIBLE);
                Logg.i("VersionUpActivity", "=system_update_success==" + getResources().getString(R.string.system_update_success));
                textView.setVisibility(View.VISIBLE);
                textView.setText(getResources().getString(R.string.system_update_success));
                TTSManager.getInstance().ttsNormalStart(201, new int[]{0x0A03});
            }*/ else if (eventType instanceof VersionUpReceiver.UpdateCompleteInfo) {
                if (((VersionUpReceiver.UpdateCompleteInfo) eventType).type == 0) {//OTA
                    if (((VersionUpReceiver.UpdateCompleteInfo) eventType).result == 0) {//成功
                        versionUpTimer.start();
//                        btn_ok.setVisibility(View.VISIBLE);
                        Logg.i("VersionUpActivity", "=system_update_success==" + getResources().getString(R.string.system_update_success));
                        textView.setVisibility(View.VISIBLE);
                        textView.setText(getResources().getString(R.string.system_update_success));
                        TTSManager.getInstance().ttsNormalStart(201, new int[]{0x0A03});
                    } else if (((VersionUpReceiver.UpdateCompleteInfo) eventType).result == -1) {//アップデート失敗
//                        btn_ok.setVisibility(View.VISIBLE);
                        textView.setVisibility(View.VISIBLE);
                        Logg.i("VersionUpActivity", "=system_update_fail==" + getResources().getString(R.string.system_update_fail));
                        textView.setText(getResources().getString(R.string.system_update_fail));
                        versionUpTimer.start();
                        TTSManager.getInstance().ttsNormalStart(200, new int[]{0x0A04});
                    }

                } else if (((VersionUpReceiver.UpdateCompleteInfo) eventType).type == 2) {//SDカード
                    if (((VersionUpReceiver.UpdateCompleteInfo) eventType).result == 0) {//成功
                        versionUpTimer.start();
                        btn_ok.setVisibility(View.VISIBLE);
                        Logg.i("VersionUpActivity", "=system_update_success==" + getResources().getString(R.string.system_update_success));
                        textView.setVisibility(View.VISIBLE);
                        textView.setText(getResources().getString(R.string.system_update_success));
                        TTSManager.getInstance().ttsNormalStart(201, new int[]{0x0A03});
                    } else if (((VersionUpReceiver.UpdateCompleteInfo) eventType).result == -1) {//アップデート失敗
                        btn_ok.setVisibility(View.VISIBLE);
                        textView.setVisibility(View.VISIBLE);
                        Logg.i("VersionUpActivity", "=system_update_fail==" + getResources().getString(R.string.system_update_fail));
                        textView.setText(getResources().getString(R.string.system_update_fail));
                        versionUpTimer.start();
                        TTSManager.getInstance().ttsNormalStart(200, new int[]{0x0A04});
                    }

                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventUtil.unregister(this);
        if (versionUpTimer != null) {
            versionUpTimer.cancel();
            versionUpTimer = null;
        }
        isNeedTransparent = true;
        btn_ok.setVisibility(View.GONE);
        textView.setVisibility(View.GONE);
        setTheme(android.R.style.Theme_Translucent_NoTitleBar);
    }

    private CountDownTimer versionUpTimer = new CountDownTimer(5000, 1000) {

        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            toNoticeActivity();
        }
    };

    private void toNoticeActivity() {
        Intent intent = new Intent();
        intent.setClass(this, NoticeActivity.class);
        startActivity(intent);
        finish();
    }

    private int keydowmRepeatCount = 0;

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        keydowmRepeatCount++;
        if (keydowmRepeatCount == 1) {
            onKeyHoldHalfASecond(keyCode);
        } else if (keydowmRepeatCount == 2) {
            onKeyHoldOneSecond(keyCode);
        } else if (keydowmRepeatCount == 3) {
            onKeyHoldThreeSecond(keyCode);
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        event.startTracking();
        if (keydowmRepeatCount == 1) {
            onContinueKeyHoldHalfASecond(keyCode);
        } else if (keydowmRepeatCount == 2) {
            onContinueKeyHoldOneSecond(keyCode);
        } else if (keydowmRepeatCount == 3) {
            onContinueKeyHoldThreeSecond(keyCode);
        }
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        event.startTracking();
        if (keydowmRepeatCount == 0) {
            onKeyShortPressed(keyCode);
        } else {
            keydowmRepeatCount = 0;
        }
        return true;
    }

    public void onKeyShortPressed(int keyCode) {
        switch (keyCode) {
            case KeyAdapter.KEY_ENTER:
                Logg.i("VersionUpActivity", "==KEY_ENTER=");
                if (null != btn_ok && btn_ok.getVisibility() == View.VISIBLE && null != textView && textView.getVisibility() == View.VISIBLE) {
                    toNoticeActivity();
                    if (versionUpTimer != null) {
                        versionUpTimer.cancel();
                        versionUpTimer = null;
                    }
                }
        }
    }

    public void onKeyHoldHalfASecond(int keyCode) {

    }

    public void onKeyHoldOneSecond(int keyCode) {

    }

    public void onKeyHoldThreeSecond(int keyCode) {

    }

    public void onContinueKeyHoldHalfASecond(int keyCode) {

    }

    public void onContinueKeyHoldOneSecond(int keyCode) {

    }

    public void onContinueKeyHoldThreeSecond(int keyCode) {

    }
}
