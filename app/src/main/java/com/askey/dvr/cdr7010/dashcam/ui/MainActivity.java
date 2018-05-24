package com.askey.dvr.cdr7010.dashcam.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;

import com.askey.dvr.cdr7010.dashcam.R;
import com.askey.dvr.cdr7010.dashcam.activity.DialogActivity;
import com.askey.dvr.cdr7010.dashcam.domain.Event;
import com.askey.dvr.cdr7010.dashcam.domain.EventInfo;
import com.askey.dvr.cdr7010.dashcam.domain.KeyAdapter;
import com.askey.dvr.cdr7010.dashcam.domain.MessageEvent;
import com.askey.dvr.cdr7010.dashcam.logic.GlobalLogic;
import com.askey.dvr.cdr7010.dashcam.service.DialogManager;
import com.askey.dvr.cdr7010.dashcam.service.EventManager;
import com.askey.dvr.cdr7010.dashcam.service.LedMananger;
import com.askey.dvr.cdr7010.dashcam.service.TTSManager;
import com.askey.dvr.cdr7010.dashcam.util.ActivityUtils;
import com.askey.dvr.cdr7010.dashcam.util.Const;
import com.askey.dvr.cdr7010.dashcam.util.EventUtil;
import com.askey.dvr.cdr7010.dashcam.util.Logg;
import com.askey.dvr.cdr7010.dashcam.widget.CommDialog;
import com.askey.dvr.cdr7010.dashcam.widget.WarningDialog;
import com.askey.platform.AskeySettings;


public class MainActivity extends DialogActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private AudioManager audioManager;
    private int maxVolume,currentVolume;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, CameraRecordFragment.newInstance())
                    .commit();
        }
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
        EventManager.getInstance().registPopUpEventCallback(popUpEventCallback);
        EventManager.getInstance().registIconEventCallback(iconEventCallback);
        EventManager.getInstance().registLedEventCallback(ledEventCallback);
        EventManager.getInstance().registTtsEventCallback(ttsEventCallback);
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
                                0);
                    }
                    return true;
                case KeyAdapter.KEY_VOLUME_DOWN:
                    currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)-1;
                    if(currentVolume>=0){
                        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION,currentVolume,
                                0);
                    }
                    return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onDestroy(){
        EventManager.getInstance().unRegistPopUpEventCallback(popUpEventCallback);
        EventManager.getInstance().unRegistIconEventCallback(iconEventCallback);
        EventManager.getInstance().unRegistLedEventCallback(ledEventCallback);
        EventManager.getInstance().unRegistTtsEventCallback(ttsEventCallback);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        int micValue = GlobalLogic.getInstance().getInt(AskeySettings.Global.RECSET_VOICE_RECORD);
        int newVal = (micValue == 0) ? 1 : 0;
        boolean value = GlobalLogic.getInstance().putInt(AskeySettings.Global.RECSET_VOICE_RECORD, newVal);
        EventUtil.sendEvent(new MessageEvent<>(Event.EventCode.EVENT_MIC, value));
        return;
    }
    private EventManager.EventCallback popUpEventCallback = new EventManager.EventCallback(){
        @Override
       public void onEvent(EventInfo eventInfo, long timeStamp){
             DialogManager.getIntance().showDialog(eventInfo.getEventType());
        }
    };
    private EventManager.EventCallback iconEventCallback = new EventManager.EventCallback(){
        @Override
        public void onEvent(EventInfo eventInfo, long timeStamp){

        }
    };
    private EventManager.EventCallback ledEventCallback = new EventManager.EventCallback(){
        @Override
        public void onEvent(EventInfo eventInfo, long timeStamp){
            switch(eventInfo.getEventType()){
                case Event.CONTINUOUS_RECORDING_START:
                case Event.EVENT_RECORDING_START:
                    LedMananger.getInstance().setLedRecStatus(true,true,eventInfo.getPriority());
                    break;
                case Event.CONTINUOUS_RECORDING_END:
                case Event.EVENT_RECORDING_END:
                case Event.RECORDING_STOP:
                case Event.HIGH_TEMPERATURE_THRESHOLD_LV2:
                    LedMananger.getInstance().setLedRecStatus(true,false,eventInfo.getPriority());
                    break;
                case Event.SDCARD_UNMOUNTED:
                case Event.SDCARD_UNFORMATTED:
                case Event.SDCARD_UNSUPPORTED:
                case Event.SDCARD_ERROR:
                    LedMananger.getInstance().setLedRecStatus(false,false,eventInfo.getPriority());
                    break;
                case Event.AUDIO_RECORDING_ON:
                    LedMananger.getInstance().setLedMicStatus(true);
                    break;
                case Event.AUDIO_RECORDING_OFF:
                    LedMananger.getInstance().setLedMicStatus(false);
                    break;
                default:
            }

        }
    };
    private EventManager.EventCallback ttsEventCallback = new EventManager.EventCallback(){
        @Override
        public void onEvent(EventInfo eventInfo, long timeStamp){
            TTSManager.getInstance().ttsEventStart(eventInfo.getVoiceGuidence(),eventInfo.getEventType(),
                    eventInfo.getPriority());
        }
    };
}
