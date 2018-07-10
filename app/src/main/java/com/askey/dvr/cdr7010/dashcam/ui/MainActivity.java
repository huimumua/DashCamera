package com.askey.dvr.cdr7010.dashcam.ui;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.view.KeyEvent;

import com.askey.dvr.cdr7010.dashcam.R;
import com.askey.dvr.cdr7010.dashcam.activity.DialogActivity;
import com.askey.dvr.cdr7010.dashcam.application.DashCamApplication;
import com.askey.dvr.cdr7010.dashcam.domain.Event;
import com.askey.dvr.cdr7010.dashcam.domain.EventInfo;
import com.askey.dvr.cdr7010.dashcam.domain.KeyAdapter;
import com.askey.dvr.cdr7010.dashcam.domain.MessageEvent;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.jvckenwood.MainApp;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.jvckenwood.MainAppSending;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.local.JvcStatusParams;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.local.LocalJvcStatusManager;
import com.askey.dvr.cdr7010.dashcam.logic.GlobalLogic;
import com.askey.dvr.cdr7010.dashcam.service.DialogManager;
import com.askey.dvr.cdr7010.dashcam.service.EventManager;
import com.askey.dvr.cdr7010.dashcam.service.GPSStatusManager;
import com.askey.dvr.cdr7010.dashcam.service.LedMananger;
import com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum;
import com.askey.dvr.cdr7010.dashcam.util.ActivityUtils;
import com.askey.dvr.cdr7010.dashcam.util.AppUtils;
import com.askey.dvr.cdr7010.dashcam.util.Const;
import com.askey.dvr.cdr7010.dashcam.util.EventUtil;
import com.askey.dvr.cdr7010.dashcam.util.Logg;
import com.askey.dvr.cdr7010.dashcam.util.SDcardHelper;
import com.askey.platform.AskeySettings;

import org.json.JSONObject;

import java.util.EnumMap;

import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_INIT_FAIL;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_INIT_SUCCESS;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_MOUNTED;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_REMOVED;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_SUPPORTED;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_UNRECOGNIZABLE;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_UNSUPPORTED;


public class MainActivity extends DialogActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String ACTIVITY_CLASSNAME ="com.askey.dvr.cdr7010.dashcam.ui.MainActivity";
    public static final String ACTION_SDCARD_STATUS = "action_sdcard_status";
    public static final String CMD_SHOW_SDCARD_NOT_SUPPORTED ="show_sdcard_not_supported";//sdcard格式不支持可用
    public static final String CMD_SHOW_SDCARD_SUPPORTED = "show_sdcard_supported";//sdcard可用
    public static final String CMD_SHOW_SDCARD_INIT_FAIL ="show_sdcard_init_fail";//init 失败
    public static final String CMD_SHOW_SDCARD_INIT_SUCC = "show_sdcard_init_success";//init成功
    public static final String CMD_SHOW_SDCARD_UNRECOGNIZABLE ="show_sdcard_unrecognizable";//不被识别
    private static final int FROM_MAINAPP =0;
    private AudioManager audioManager;
    private int maxVolume,currentVolume;
    private CameraRecordFragment fragment;
    private boolean isFromOtherApp =false;
    private ContentResolver contentResolver;
    private int setUpWizardType;

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

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addDataScheme("file");
        registerReceiver(mSdCardMountAndRemoveListener, filter);

        IntentFilter sdcardFilter = new IntentFilter();
        sdcardFilter.addAction(ACTION_SDCARD_STATUS);
        registerReceiver(mSdCardStatusListener, sdcardFilter);

        EventManager.getInstance().registPopUpEventCallback(popUpEventCallback);
        EventManager.getInstance().registIconEventCallback(iconEventCallback);
        EventManager.getInstance().registLedEventCallback(ledEventCallback);

        LocalJvcStatusManager.getInsuranceTerm(jvcStatusCallback);
        contentResolver = getContentResolver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logg.d(TAG,"isFromOtherApp="+isFromOtherApp);
        if(!isFromOtherApp){
            SDcardHelper.checkSdCardExist();
        }else {
            SDcardHelper.handleSdcardAbnormalEvent();
        }
    }
    @Override
    protected  void onPause(){
        super.onPause();
        UIElementStatusEnum.SDcardStatusType sdcardStatus = GlobalLogic.getInstance().getSDCardCurrentStatus();
        if(sdcardStatus != SDCARD_MOUNTED && sdcardStatus != SDCARD_SUPPORTED
                && sdcardStatus != SDCARD_INIT_SUCCESS) {
            LedMananger.getInstance().setLedRecStatus(true, false, 0);
        }
    }

    @Override
    public void onDestroy() {
        removeListeners();
        unregisterReceiver(mSdCardMountAndRemoveListener);
        unregisterReceiver(mSdCardStatusListener);
        if (timeFinishApp != null) {
            timeFinishApp.cancel();
            timeFinishApp = null;
        }
        //add by Mark
        MainApp.getInstance().unBindJvcMainAppService();
        //end add
        super.onDestroy();
    }

    private void removeListeners() {
        EventManager.getInstance().unRegistPopUpEventCallback(popUpEventCallback);
        EventManager.getInstance().unRegistIconEventCallback(iconEventCallback);
        EventManager.getInstance().unRegistLedEventCallback(ledEventCallback);
    }


    private  EventManager.EventCallback popUpEventCallback = new EventManager.EventCallback() {
        @Override
        public void onEvent(EventInfo eventInfo, long timeStamp) {
            DialogManager.getIntance().showDialog(eventInfo.getEventType(), 0);
        }
    };

    private  EventManager.EventCallback iconEventCallback = new EventManager.EventCallback() {
        @Override
        public void onEvent(EventInfo eventInfo, long timeStamp) {
            Logg.d(TAG,"iconEventCallback...eventInfo:"+eventInfo.toString());
        }
    };

    private  EventManager.EventCallback ledEventCallback = new EventManager.EventCallback() {
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





    @Override
    protected boolean handleKeyEvent(KeyEvent event) {
        if(GlobalLogic.getInstance().isStartSwitchUser()){
            return true;
        }
        return false;
    }
    @Override
    public  void onContinueKeyHoldOneSecond(int keyCode){
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
                // add by Lisa start
                setUpWizardType = Settings.Global.getInt(contentResolver, AskeySettings.Global.SETUP_WIZARD_AVAILABLE, 1);
                Logg.i(TAG,"=KEY_BACK==SettingsActivity=setUpWizardType="+setUpWizardType);
                if(setUpWizardType == 1){
                    Settings.Global.putInt(contentResolver, AskeySettings.Global.SETUP_WIZARD_AVAILABLE, 0);
                }else {// add by Lisa end 防止在设置向导时最后的按键被mainAPP中监听到
                    int micValue = GlobalLogic.getInstance().getInt(AskeySettings.Global.RECSET_VOICE_RECORD);
                    int newVal = (micValue == 0) ? 1 : 0;
                    boolean value = GlobalLogic.getInstance().putInt(AskeySettings.Global.RECSET_VOICE_RECORD, newVal);
                    EventUtil.sendEvent(new MessageEvent<>(Event.EventCode.EVENT_MIC, value));
                }
                break;
            case KeyAdapter.KEY_MENU:
                // add by Lisa start
                setUpWizardType = Settings.Global.getInt(contentResolver, AskeySettings.Global.SETUP_WIZARD_AVAILABLE, 1);
                Logg.i(TAG, "=KEY_MENU==SettingsActivity=setUpWizardType="+setUpWizardType);
                if(setUpWizardType == 1){
                    Settings.Global.putInt(contentResolver, AskeySettings.Global.SETUP_WIZARD_AVAILABLE, 0);
                }else {// add by Lisa end 防止在设置向导时最后的按键被mainAPP中监听到
                    Location currentLocation = GPSStatusManager.getInstance().getCurrentLocation();
                    if (currentLocation != null && currentLocation.getSpeed() > 0.0f) { //有GPS信号，且速度大于0
                        //
                    } else {
                        isFromOtherApp = true;
                        MainAppSending.menuTransition(FROM_MAINAPP);
                        ActivityUtils.startActivity(this, Const.PACKAGE_NAME, Const.CLASS_NAME, false);
                    }
                }
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
    private BroadcastReceiver mSdCardMountAndRemoveListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Logg.d(TAG, "action=" + action);
            if(action.equals(Intent.ACTION_MEDIA_MOUNTED)){
                GlobalLogic.getInstance().setSDCardCurrentStatus(SDCARD_MOUNTED);
                if(AppUtils.isActivityTop(context,ACTIVITY_CLASSNAME)) {
                    EventUtil.sendEvent(new MessageEvent<UIElementStatusEnum.SDcardStatusType>(Event.EventCode.EVENT_SDCARD, SDCARD_MOUNTED));
                }else{
                    DialogManager.getIntance().setSdcardInserted(true);
                }
            }else if(action.equals(Intent.ACTION_MEDIA_EJECT )){
                GlobalLogic.getInstance().setSDCardCurrentStatus(SDCARD_REMOVED);
                if(AppUtils.isActivityTop(context,ACTIVITY_CLASSNAME)) {
                    EventManager.getInstance().handOutEventInfo(110);
                    EventUtil.sendEvent(new MessageEvent<UIElementStatusEnum.SDcardStatusType>(Event.EventCode.EVENT_SDCARD, SDCARD_REMOVED));
                }
            }
        }
    };
    private BroadcastReceiver mSdCardStatusListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Logg.d(TAG, "action=" + action);
            if(action.equals(ACTION_SDCARD_STATUS)){
                String data = intent.getStringExtra("data");
                Logg.d(TAG, "action=" + action+",data="+data);
                if (data.equals(CMD_SHOW_SDCARD_NOT_SUPPORTED)) {
                    handleSdCardEvent(context,SDCARD_UNSUPPORTED,112);
                } else if (data.equals(CMD_SHOW_SDCARD_SUPPORTED)) {
                    handleSdCardEvent(context,SDCARD_SUPPORTED,-1);
                } else if (data.equals(CMD_SHOW_SDCARD_INIT_FAIL)) {
                    handleSdCardEvent(context,SDCARD_INIT_FAIL,111);
                } else if (data.equals(CMD_SHOW_SDCARD_INIT_SUCC)) {
                    LedMananger.getInstance().setLedRecStatus(true, false, 0);
                    handleSdCardEvent(context,SDCARD_INIT_SUCCESS,-1);
                } else if (data.equals(CMD_SHOW_SDCARD_UNRECOGNIZABLE)) {
                    handleSdCardEvent(context,SDCARD_UNRECOGNIZABLE,113);
                }
            }
        }
    };

    private void handleSdCardEvent(Context context,UIElementStatusEnum.SDcardStatusType sdCardStatus,int eventType){
        GlobalLogic.getInstance().setSDCardCurrentStatus(sdCardStatus);
        if(AppUtils.isActivityTop(context,ACTIVITY_CLASSNAME)){
            EventUtil.sendEvent(new MessageEvent<UIElementStatusEnum.SDcardStatusType>(Event.EventCode.EVENT_SDCARD, sdCardStatus));
            if(eventType >= 0){
                EventManager.getInstance().handOutEventInfo(eventType);
            }
        }
        return;
    }


}
