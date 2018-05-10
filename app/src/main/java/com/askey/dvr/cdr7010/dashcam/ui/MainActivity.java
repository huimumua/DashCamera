package com.askey.dvr.cdr7010.dashcam.ui;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;

import com.askey.dvr.cdr7010.dashcam.R;
import com.askey.dvr.cdr7010.dashcam.domain.Event;
import com.askey.dvr.cdr7010.dashcam.domain.KeyAdapter;
import com.askey.dvr.cdr7010.dashcam.domain.MessageEvent;
import com.askey.dvr.cdr7010.dashcam.logic.GlobalLogic;
import com.askey.dvr.cdr7010.dashcam.service.FileManager;
import com.askey.dvr.cdr7010.dashcam.util.ActivityUtils;
import com.askey.dvr.cdr7010.dashcam.util.Const;
import com.askey.dvr.cdr7010.dashcam.util.EventUtil;
import com.askey.platform.AskeySettings;


public class MainActivity extends AppCompatActivity {
    private AudioManager audioManager;
    private int maxVolume,currentVolume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        FileManager.getInstance(this); // bindService
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, CameraRecordFragment.newInstance())
                    .commit();
        }
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyAdapter.KEY_MENU:
                    ActivityUtils.startActivity(this, Const.PACKAGE_NAME, Const.CLASS_NAME, false);
                    return true;
                case KeyAdapter.KEY_VOLUME_UP:
                    currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)+1;
                    if(currentVolume<=maxVolume){
                        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION,currentVolume,
                                AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
                    }
                    return true;
                case KeyAdapter.KEY_VOLUME_DOWN:
                    currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)-1;
                    if(currentVolume>=0){
                        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION,currentVolume,
                                AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
                    }
                    return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        int micValue = GlobalLogic.getInstance().getInt(AskeySettings.Global.RECSET_VOICE_RECORD);
        int newVal = (micValue == 0) ? 1 : 0;
        boolean value = GlobalLogic.getInstance().putInt(AskeySettings.Global.RECSET_VOICE_RECORD, newVal);
        EventUtil.sendEvent(new MessageEvent<>(Event.EventCode.EVENT_MIC, value));
        return;
    }
}
