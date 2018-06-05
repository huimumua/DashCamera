package com.askey.dvr.cdr7010.dashcam.ui;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.KeyEvent;

import com.askey.dvr.cdr7010.dashcam.R;
import com.askey.dvr.cdr7010.dashcam.activity.DialogActivity;
import com.askey.dvr.cdr7010.dashcam.application.DashCamApplication;
import com.askey.dvr.cdr7010.dashcam.domain.Event;
import com.askey.dvr.cdr7010.dashcam.domain.EventInfo;
import com.askey.dvr.cdr7010.dashcam.domain.KeyAdapter;
import com.askey.dvr.cdr7010.dashcam.domain.MessageEvent;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.jvckenwood.MainApp;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.local.JvcStatusParams;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.local.LocalJvcStatusManager;
import com.askey.dvr.cdr7010.dashcam.logic.GlobalLogic;
import com.askey.dvr.cdr7010.dashcam.service.DialogManager;
import com.askey.dvr.cdr7010.dashcam.service.EventManager;
import com.askey.dvr.cdr7010.dashcam.service.LedMananger;
import com.askey.dvr.cdr7010.dashcam.service.TTSManager;
import com.askey.dvr.cdr7010.dashcam.util.ActivityUtils;
import com.askey.dvr.cdr7010.dashcam.util.Const;
import com.askey.dvr.cdr7010.dashcam.util.EventUtil;
import com.askey.dvr.cdr7010.dashcam.util.Logg;
import com.askey.platform.AskeySettings;

import org.json.JSONObject;

import java.util.EnumMap;


public class MainActivity extends DialogActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private AudioManager audioManager;
    private int maxVolume,currentVolume;
    private CameraRecordFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        if (savedInstanceState == null) {
            fragment = CameraRecordFragment.newInstance();
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
        }
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
        }
        EventManager.getInstance().registPopUpEventCallback(popUpEventCallback);
        EventManager.getInstance().registIconEventCallback(iconEventCallback);
        EventManager.getInstance().registLedEventCallback(ledEventCallback);
        EventManager.getInstance().registTtsEventCallback(ttsEventCallback);

        LocalJvcStatusManager.getInsuranceTerm(jvcStatusCallback);
    }

    @Override
    public void onDestroy() {
        removeListeners();
        EventManager.getInstance().unRegistTtsEventCallback(ttsEventCallback);
        if (timeFinishApp != null) {
            timeFinishApp.cancel();
            timeFinishApp = null;
        }
        //add by Mark
        MainApp.getInstance().endInitialSetup();
        //end add
        super.onDestroy();
    }

    private void removeListeners() {
        EventManager.getInstance().unRegistPopUpEventCallback(popUpEventCallback);
        EventManager.getInstance().unRegistIconEventCallback(iconEventCallback);
        EventManager.getInstance().unRegistLedEventCallback(ledEventCallback);
    }


    private EventManager.EventCallback popUpEventCallback = new EventManager.EventCallback() {
        @Override
        public void onEvent(EventInfo eventInfo, long timeStamp) {
            DialogManager.getIntance().showDialog(eventInfo.getEventType(), 0);
        }
    };

    private EventManager.EventCallback iconEventCallback = new EventManager.EventCallback() {
        @Override
        public void onEvent(EventInfo eventInfo, long timeStamp) {

        }
    };

    private LocalJvcStatusManager.LocalJvcStatusCallback jvcStatusCallback = new LocalJvcStatusManager.LocalJvcStatusCallback() {
        @Override
        public void onDataArriving(EnumMap<JvcStatusParams.JvcStatusParam, Object> enumMap) {
            Logg.d("NoticePresenter", "onDataArriving...");
            int oos = -1;
            String response = null;
            if (enumMap != null) {
                if (enumMap.containsKey(JvcStatusParams.JvcStatusParam.OOS)) {
                    oos = (int) enumMap.get(JvcStatusParams.JvcStatusParam.OOS);
                    Logg.d("NoticePresenter", "oos..." + oos);
                }
                if (enumMap.containsKey(JvcStatusParams.JvcStatusParam.RESPONSE)) {
                    response = (String) enumMap.get(JvcStatusParams.JvcStatusParam.RESPONSE);
                    Logg.d("NoticePresenter", "response.." + response);
                }
                switch (oos) {
                    case 0://成功
                        if (response != null) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                int status = jsonObject.optInt("status");
                                switch (status) {
                                    case 0://正常
                                        int flg = jsonObject.optInt("flg");
                                        switch (flg) {
                                            case 1://始期日以前
                                                removeListeners();
                                                fragment.beforeContractDayStart();
                                                break;
                                            case 2://証券期間中 do nothing
                                                break;
                                            case 0://対象の証券無し
                                            case 3://"満期日+14日"以降
                                                timeFinishApp.start();
                                                fragment.afterContractDayEnd();
                                                break;
                                        }
                                        break;
                                    case -1://想定外の例外
                                        break;
                                    case -100://IMEIが未入力
                                        break;
                                    case -700://IMEIがDBに未登録
                                        break;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    default://圏外
                        break;
                }
            }
        }
    };

    private CountDownTimer timeFinishApp = new CountDownTimer(60000, 1000) {

        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            Logg.d(TAG, "关闭电源");
            ActivityUtils.shutDown(DashCamApplication.getAppContext());
        }
    };

    private EventManager.EventCallback ledEventCallback = new EventManager.EventCallback() {
        @Override
        public void onEvent(EventInfo eventInfo, long timeStamp) {
            switch (eventInfo.getEventType()) {
                case Event.CONTINUOUS_RECORDING_START:
                case Event.EVENT_RECORDING_START:
                    LedMananger.getInstance().setLedRecStatus(true, true, eventInfo.getPriority());
                    break;
                case Event.CONTINUOUS_RECORDING_END:
                case Event.EVENT_RECORDING_END:
                case Event.RECORDING_STOP:
                case Event.HIGH_TEMPERATURE_THRESHOLD_LV2:
                    LedMananger.getInstance().setLedRecStatus(true, false, eventInfo.getPriority());
                    break;
                case Event.SDCARD_UNMOUNTED:
                case Event.SDCARD_UNFORMATTED:
                case Event.SDCARD_UNSUPPORTED:
                case Event.SDCARD_ERROR:
                    LedMananger.getInstance().setLedRecStatus(false, false, eventInfo.getPriority());
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

    private EventManager.EventCallback ttsEventCallback = new EventManager.EventCallback() {
        @Override
        public void onEvent(EventInfo eventInfo, long timeStamp) {
            TTSManager.getInstance().ttsEventStart( eventInfo.getEventType(),
                    eventInfo.getPriority(),new int[]{eventInfo.getVoiceGuidence()});
        }
    };

    @Override
    protected boolean handleKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyAdapter.KEY_MENU:
                    ActivityUtils.startActivity(this, Const.PACKAGE_NAME, Const.CLASS_NAME, false);
                    return true;
            }
        }
        return false;
    }
    @Override
    public  void onContinueKeyHoldOneSecond(int keyCode){

    }
    @Override
    public void onKeyShortPressed(int keyCode) {
        switch (keyCode) {
            case KeyAdapter.KEY_VOLUME_UP:
                currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION) + 1;
                if (currentVolume <= maxVolume) {
                    audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, currentVolume,
                            0);
                }
                break;
            case KeyAdapter.KEY_VOLUME_DOWN:
                currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION) - 1;
                if (currentVolume >= 0) {
                    audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, currentVolume,
                            0);
                }
                break;
            case KeyAdapter.KEY_BACK:
                int micValue = GlobalLogic.getInstance().getInt(AskeySettings.Global.RECSET_VOICE_RECORD);
                int newVal = (micValue == 0) ? 1 : 0;
                boolean value = GlobalLogic.getInstance().putInt(AskeySettings.Global.RECSET_VOICE_RECORD, newVal);
                EventUtil.sendEvent(new MessageEvent<>(Event.EventCode.EVENT_MIC, value));
        }
    }
    @Override
    public void onKeyHoldOneSecond(int keyCode) {
        switch (keyCode) {
            case KeyAdapter.KEY_VOLUME_UP:
                currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION) + 1;
                if (currentVolume <= maxVolume) {
                    audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, currentVolume,
                            0);
                }
                break;
            case KeyAdapter.KEY_VOLUME_DOWN:
                currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION) - 1;
                if (currentVolume >= 0) {
                    audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, currentVolume,
                            0);
                }
                break;
        }
    }
}
