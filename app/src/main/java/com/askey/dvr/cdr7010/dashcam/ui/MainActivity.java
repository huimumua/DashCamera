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
import com.askey.dvr.cdr7010.dashcam.receiver.DvrShutDownReceiver;
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
import com.askey.dvr.cdr7010.dashcam.util.RecordHelper;
import com.askey.dvr.cdr7010.dashcam.util.SDcardHelper;
import com.askey.platform.AskeyIntent;
import com.askey.platform.AskeySettings;

import org.json.JSONObject;

import java.util.EnumMap;

import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.EventRecordingLimitStatusType.EVENT_RECORDING_REACH_LIMIT_CONDITION;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.EventRecordingLimitStatusType.EVENT_RECORDING_UNREACHABLE_LIMIT_CONDITION;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.ParkingRecordingLimitStatusType.PARKING_RECORDING_REACH_LIMIT_CONDITION;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.ParkingRecordingLimitStatusType.PARKING_RECORDING_UNREACHABLE_LIMIT_CONDITION;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.RecordingPreconditionStatus.SDCARD_EVENT_FILE_OVER_LIMIT;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.RecordingPreconditionStatus.SDCARD_PICTURE_FILE_OVER_LIMIT;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.RecordingPreconditionStatus.SDCARD_RECORDING_FULL_LIMIT;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.RecordingPreconditionStatus.SDCARD_RECORDING_FULL_LIMIT_EXIT;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.RecordingPreconditionStatus.SDCARD_UNREACH_EVENT_FILE_LIMIT;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.RecordingPreconditionStatus.SDCARD_UNREACH_PICTURE_LIMIT;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_ASKEY_NOT_SUPPORTED;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_EVENT_FILE_LIMIT;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_EVENT_PICTURE_LIMIT;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_FULL_LIMIT;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_FULL_LIMIT_EXIT;
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
    public static final String CMD_SHOW_SDCARD_NOT_EXIST ="show_sdcard_not_exist";//sdcard 拔卡
    public static final String CMD_SHOW_SDCARD_MOUNTED ="show_sdcard_mounted";//sdcard 挂载成功
    public static final String ACTION_SDCARD_LIMT = "com.askey.dvr.cdr7010.dashcam.limit";
    public static final String CMD_SHOW_REACH_EVENT_FILE_LIMIT ="show_reach_event_file_limit";//超过限制
    public static final String CMD_SHOW_UNREACH_EVENT_FILE_LIMIT = "show_unreach_event_file_limit";//限制解除
    public static final String CMD_SHOW_REACH_EVENT_FILE_OVER_LIMIT ="show_reach_event_file_over_limit";
    public static final String CMD_SHOW_REACH_PARKING_FILE_LIMIT = "show_reach_parking_file_limit";//超过限制
    public static final String CMD_SHOW_UNREACH_PARKING_FILE_LIMIT = "show_unreach_parking_file_limit";//限制解除
    public static final String CMD_SHOW_REACH_PICTURE_FILE_LIMIT ="show_reach_picture_file_limit";//超过限制
    public static final String CMD_SHOW_UNREACH_PICTURE_FILE_LIMIT = "show_unreach_picture_file_limit";//限制解除
    public static final String CMD_SHOW_REACH_PICTURE_FILE_OVER_LIMIT ="show_reach_picture_file_over_limit";
    public static final String CMD_SHOW_SDCARD_FULL_LIMIT ="show_sdcard_full_limit";//超过限制
    public static final String CMD_SHOW_UNREACH_SDCARD_FULL_LIMIT = "show_unreach_sdcard_full_limit";//限制解除
    public static final String CMD_SHOW_SDCARD_ASKEY_NOT_SUPPORTED ="show_sdcard_askey_not_supported";//askey 7010不支持此卡
    private static final int FROM_MAINAPP =0;
    private AudioManager audioManager;
    private int maxVolume,currentVolume;
    private CameraRecordFragment fragment;
    private ContentResolver contentResolver;
    private int setUpWizardType;
    private final DvrShutDownReceiver mDvrShutDownBroadCastReceiver = new DvrShutDownReceiver();

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

        IntentFilter sdcardFilter = new IntentFilter();
        sdcardFilter.addAction(ACTION_SDCARD_STATUS);
        sdcardFilter.addAction(ACTION_SDCARD_LIMT);
        registerReceiver(mSdCardStatusListener, sdcardFilter);

        EventManager.getInstance().registPopUpEventCallback(popUpEventCallback);
        EventManager.getInstance().registIconEventCallback(iconEventCallback);
        EventManager.getInstance().registLedEventCallback(ledEventCallback);

        LocalJvcStatusManager.getInsuranceTerm(jvcStatusCallback);
        contentResolver = getContentResolver();

        //add by Mark for bug PUCDR-1445 in 20180717
        IntentFilter dvrShutDownIntentFilter = new IntentFilter(AskeyIntent.ACTION_DVR_SHUTDOWN);
        dvrShutDownIntentFilter.setPriority(1000);
        registerReceiver(mDvrShutDownBroadCastReceiver, dvrShutDownIntentFilter);
        //end add
    }

    @Override
    protected void onResume() {
        super.onResume();
        SDcardHelper.checkSdcardState(this);
    }
    @Override
    protected  void onPause(){
        super.onPause();
        if(!SDcardHelper.isSDCardEnable(this)){
            LedMananger.getInstance().setLedRecStatus(true, false, 0);
        }
    }

    @Override
    public void onDestroy() {
        removeListeners();
        unregisterReceiver(mSdCardStatusListener);
        if (timeFinishApp != null) {
            timeFinishApp.cancel();
            timeFinishApp = null;
        }
        //add by Mark
        MainApp.getInstance().unBindJvcMainAppService();
        //end add
        unregisterReceiver(mDvrShutDownBroadCastReceiver);
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
                case Event.SDCARD_SPACE_INSUFFICIENT:
                case Event.EQUIPMENT_FAILURE:
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
                int micValue = GlobalLogic.getInstance().getInt(AskeySettings.Global.RECSET_VOICE_RECORD);
                int newVal = (micValue == 0) ? 1 : 0;
                boolean value = GlobalLogic.getInstance().putInt(AskeySettings.Global.RECSET_VOICE_RECORD, newVal);
                EventUtil.sendEvent(new MessageEvent<>(Event.EventCode.EVENT_MIC, value));
                break;
            case KeyAdapter.KEY_MENU:
                Location currentLocation = GPSStatusManager.getInstance().getCurrentLocation();
                if (currentLocation != null && currentLocation.getSpeed() > 0.0f) { //有GPS信号，且速度大于0
                    //
                } else {
                    SDcardHelper.disMissSdcardDialog();
                    MainAppSending.menuTransition(FROM_MAINAPP);
                    ActivityUtils.startActivity(this, Const.PACKAGE_NAME, Const.CLASS_NAME, false);
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
    private BroadcastReceiver mSdCardStatusListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Logg.d(TAG, "action=" + action);
            if(action.equals(ACTION_SDCARD_STATUS)){
                String data = intent.getStringExtra("data");
                Logg.d(TAG, "action=" + action+",data="+data);
                if (data.equals(CMD_SHOW_SDCARD_NOT_SUPPORTED)) {
                    handleSdCardEvent(context,SDCARD_UNSUPPORTED,111);
                } else if (data.equals(CMD_SHOW_SDCARD_SUPPORTED)) {
                    handleSdCardEvent(context,SDCARD_SUPPORTED,-1);
                } else if (data.equals(CMD_SHOW_SDCARD_INIT_FAIL)) {
                    handleSdCardEvent(context,SDCARD_INIT_FAIL,-1);
                } else if (data.equals(CMD_SHOW_SDCARD_INIT_SUCC)) {
                    LedMananger.getInstance().setLedRecStatus(true, false, 0);
                    RecordHelper.setRecordingPrecondition(SDCARD_UNREACH_PICTURE_LIMIT);
                    RecordHelper.setRecordingPrecondition(SDCARD_UNREACH_EVENT_FILE_LIMIT);
                    RecordHelper.setRecordingPrecondition(SDCARD_RECORDING_FULL_LIMIT_EXIT);
                    handleSdCardEvent(context,SDCARD_INIT_SUCCESS,-1);
                    EventUtil.sendEvent(new MessageEvent<UIElementStatusEnum.EventRecordingLimitStatusType>(Event.EventCode.EVENT_RECORDING_FILE_LIMIT,
                            EVENT_RECORDING_UNREACHABLE_LIMIT_CONDITION));
                } else if (data.equals(CMD_SHOW_SDCARD_UNRECOGNIZABLE)) {
                    handleSdCardEvent(context,SDCARD_UNRECOGNIZABLE,113);
                } else if(data.equals(CMD_SHOW_SDCARD_NOT_EXIST)){
                    handleSdCardEvent(context,SDCARD_REMOVED,110);
                } else if(data.equals(CMD_SHOW_SDCARD_MOUNTED)){
                    EventUtil.sendEvent(new MessageEvent<UIElementStatusEnum.SDcardStatusType>(Event.EventCode.EVENT_SDCARD, SDCARD_MOUNTED));
                } else if(data.equals(CMD_SHOW_SDCARD_ASKEY_NOT_SUPPORTED)){
                    handleSdCardEvent(context,SDCARD_ASKEY_NOT_SUPPORTED,112);
                }
            } else if(action.equals(ACTION_SDCARD_LIMT)){
                String cmd_ex = intent.getStringExtra("cmd_ex");
                Logg.d(TAG, "action=" + action+",cmd_ex="+cmd_ex);
                if(CMD_SHOW_REACH_EVENT_FILE_LIMIT.equals(cmd_ex)){
                    if(AppUtils.isActivityTop(context,ACTIVITY_CLASSNAME)) {
                        EventUtil.sendEvent(new MessageEvent<UIElementStatusEnum.EventRecordingLimitStatusType>(
                                Event.EventCode.EVENT_RECORDING_FILE_LIMIT, EVENT_RECORDING_REACH_LIMIT_CONDITION));
                        EventUtil.sendEvent(new MessageEvent<UIElementStatusEnum.SDcardStatusType>(Event.EventCode.EVENT_SDCARD, SDCARD_EVENT_FILE_LIMIT));
                    }
                }else if(CMD_SHOW_UNREACH_EVENT_FILE_LIMIT.equals(cmd_ex)){
                    RecordHelper.setRecordingPrecondition(SDCARD_UNREACH_EVENT_FILE_LIMIT);
                    EventUtil.sendEvent(new MessageEvent<UIElementStatusEnum.EventRecordingLimitStatusType>(
                            Event.EventCode.EVENT_RECORDING_FILE_LIMIT,EVENT_RECORDING_UNREACHABLE_LIMIT_CONDITION));
                } else if(CMD_SHOW_REACH_EVENT_FILE_OVER_LIMIT.equals(cmd_ex)){
                    RecordHelper.setRecordingPrecondition(SDCARD_EVENT_FILE_OVER_LIMIT);
                    handleSdCardEvent(context,SDCARD_INIT_FAIL,115);
                } else if(CMD_SHOW_REACH_PARKING_FILE_LIMIT.equals(cmd_ex)){
                    EventUtil.sendEvent(new MessageEvent<UIElementStatusEnum.ParkingRecordingLimitStatusType>(
                            Event.EventCode.EVENT_PARKING_RECODING_FILE_LIMIT,PARKING_RECORDING_REACH_LIMIT_CONDITION));
                } else if(CMD_SHOW_UNREACH_PARKING_FILE_LIMIT.equals(cmd_ex)){
                    EventUtil.sendEvent(new MessageEvent<UIElementStatusEnum.ParkingRecordingLimitStatusType>(
                            Event.EventCode.EVENT_PARKING_RECODING_FILE_LIMIT,PARKING_RECORDING_UNREACHABLE_LIMIT_CONDITION));
                } else if(CMD_SHOW_SDCARD_FULL_LIMIT.equals(cmd_ex)){
                    RecordHelper.setRecordingPrecondition(SDCARD_RECORDING_FULL_LIMIT);
                    handleSdCardEvent(context,SDCARD_INIT_FAIL,116);
                } else if(CMD_SHOW_UNREACH_SDCARD_FULL_LIMIT.equals(cmd_ex)) {
                    RecordHelper.setRecordingPrecondition(SDCARD_RECORDING_FULL_LIMIT_EXIT);
                } else if(CMD_SHOW_REACH_PICTURE_FILE_LIMIT.equals(cmd_ex)){
                    EventUtil.sendEvent(new MessageEvent<UIElementStatusEnum.SDcardStatusType>(Event.EventCode.EVENT_SDCARD, SDCARD_EVENT_PICTURE_LIMIT));
                } else if(CMD_SHOW_REACH_PICTURE_FILE_OVER_LIMIT.equals(cmd_ex)){
                    RecordHelper.setRecordingPrecondition(SDCARD_PICTURE_FILE_OVER_LIMIT);
                    handleSdCardEvent(context,SDCARD_INIT_FAIL,129);
                }else if(CMD_SHOW_UNREACH_PICTURE_FILE_LIMIT.equals(cmd_ex)){
                    RecordHelper.setRecordingPrecondition(SDCARD_UNREACH_PICTURE_LIMIT);
                }
            }
        }
    };

    private void handleSdCardEvent(Context context,UIElementStatusEnum.SDcardStatusType sdCardStatus,int eventType){
        if(AppUtils.isActivityTop(context,ACTIVITY_CLASSNAME)){
            EventUtil.sendEvent(new MessageEvent<UIElementStatusEnum.SDcardStatusType>(Event.EventCode.EVENT_SDCARD, sdCardStatus));
            if(eventType >= 0){
                EventManager.getInstance().handOutEventInfo(eventType);
            }
        }
        return;
    }


}
