package com.askey.dvr.cdr7010.dashcam.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.KeyEvent;

import com.askey.dvr.cdr7010.dashcam.R;
import com.askey.dvr.cdr7010.dashcam.activity.DialogActivity;
import com.askey.dvr.cdr7010.dashcam.core.camera2.CameraHelper;
import com.askey.dvr.cdr7010.dashcam.domain.Event;
import com.askey.dvr.cdr7010.dashcam.domain.KeyAdapter;
import com.askey.dvr.cdr7010.dashcam.domain.MessageEvent;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.jvckenwood.MainApp;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.jvckenwood.MainAppSending;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.local.JvcEventHandoutInfo;
import com.askey.dvr.cdr7010.dashcam.logic.GlobalLogic;
import com.askey.dvr.cdr7010.dashcam.receiver.DvrShutDownReceiver;
import com.askey.dvr.cdr7010.dashcam.service.DialogManager;
import com.askey.dvr.cdr7010.dashcam.service.EventManager;
import com.askey.dvr.cdr7010.dashcam.service.GPSStatusManager;
import com.askey.dvr.cdr7010.dashcam.service.LedMananger;
import com.askey.dvr.cdr7010.dashcam.util.ActivityUtils;
import com.askey.dvr.cdr7010.dashcam.util.AppUtils;
import com.askey.dvr.cdr7010.dashcam.util.Const;
import com.askey.dvr.cdr7010.dashcam.util.EventUtil;
import com.askey.dvr.cdr7010.dashcam.util.GpsHelper;
import com.askey.dvr.cdr7010.dashcam.util.Logg;
import com.askey.dvr.cdr7010.dashcam.util.RecordHelper;
import com.askey.dvr.cdr7010.dashcam.util.SDcardHelper;
import com.askey.dvr.cdr7010.dashcam.util.ScreenSleepUtils;
import com.askey.platform.AskeyIntent;
import com.askey.platform.AskeySettings;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Timer;
import java.util.TimerTask;

import static com.askey.dvr.cdr7010.dashcam.domain.Event.AdDVICE_BEFORE_DRIVING;
import static com.askey.dvr.cdr7010.dashcam.domain.Event.DRIVING_REPORT;
import static com.askey.dvr.cdr7010.dashcam.domain.Event.MONTHLY_DRIVING_REPORT;
import static com.askey.dvr.cdr7010.dashcam.domain.Event.NOTICE_START;
import static com.askey.dvr.cdr7010.dashcam.service.EventManager.EventCallback;
import static com.askey.dvr.cdr7010.dashcam.service.EventManager.getInstance;
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
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_ASKEY_NOT_SUPPORTED;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_EVENT_FILE_LIMIT;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_EVENT_PICTURE_LIMIT;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_INIT_FAIL;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_INIT_SUCCESS;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_MOUNTED;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_REMOVED;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_SUPPORTED;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_UNRECOGNIZABLE;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_UNSUPPORTED;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SecondCameraStatusType.CONNECTED;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SecondCameraStatusType.DISCONNECTED;


public class MainActivity extends DialogActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String ACTIVITY_CLASSNAME = "com.askey.dvr.cdr7010.dashcam.ui.MainActivity";
    public static final String ACTION_SDCARD_STATUS = "action_sdcard_status";
    public static final String CMD_SHOW_SDCARD_NOT_SUPPORTED = "show_sdcard_not_supported";//sdcard格式不支持可用
    public static final String CMD_SHOW_SDCARD_SUPPORTED = "show_sdcard_supported";//sdcard可用
    public static final String CMD_SHOW_SDCARD_INIT_FAIL = "show_sdcard_init_fail";//init 失败
    public static final String CMD_SHOW_SDCARD_INIT_SUCC = "show_sdcard_init_success";//init成功
    public static final String CMD_SHOW_SDCARD_UNRECOGNIZABLE = "show_sdcard_unrecognizable";//不被识别
    public static final String CMD_SHOW_SDCARD_NOT_EXIST = "show_sdcard_not_exist";//sdcard 拔卡
    public static final String CMD_SHOW_SDCARD_MOUNTED = "show_sdcard_mounted";//sdcard 挂载成功
    public static final String ACTION_SDCARD_LIMT = "com.askey.dvr.cdr7010.dashcam.limit";
    public static final String CMD_SHOW_REACH_EVENT_FILE_LIMIT = "show_reach_event_file_limit";//超过限制
    public static final String CMD_SHOW_UNREACH_EVENT_FILE_LIMIT = "show_unreach_event_file_limit";//限制解除
    public static final String CMD_SHOW_REACH_EVENT_FILE_OVER_LIMIT = "show_reach_event_file_over_limit";
    public static final String CMD_SHOW_REACH_PARKING_FILE_LIMIT = "show_reach_parking_file_limit";//超过限制
    public static final String CMD_SHOW_UNREACH_PARKING_FILE_LIMIT = "show_unreach_parking_file_limit";//限制解除
    public static final String CMD_SHOW_REACH_PICTURE_FILE_LIMIT = "show_reach_picture_file_limit";//超过限制
    public static final String CMD_SHOW_BOTH_EVENT_AND_PICTURE_FOLDER_LIMIT = "show_both_event_and_picture_file_limit";//超过限制
    public static final String CMD_SHOW_UNREACH_PICTURE_FILE_LIMIT = "show_unreach_picture_file_limit";//限制解除
    public static final String CMD_SHOW_REACH_PICTURE_FILE_OVER_LIMIT = "show_reach_picture_file_over_limit";
    public static final String CMD_SHOW_BOTH_EVENT_AND_PICTURE_FOLDER_OVER_LIMIT = "show_both_event_and_picture_file_over_limit";//超过限制_
    public static final String CMD_SHOW_SDCARD_FULL_LIMIT = "show_sdcard_full_limit";//超过限制
    public static final String CMD_SHOW_UNREACH_SDCARD_FULL_LIMIT = "show_unreach_sdcard_full_limit";//限制解除
    public static final String CMD_SHOW_SDCARD_ASKEY_NOT_SUPPORTED = "show_sdcard_askey_not_supported";//askey 7010不支持此卡
    private static final int FROM_MAINAPP = 0;
    private AudioManager audioManager;
    private int maxVolume, currentVolume;
    CameraRecordFragment fragment;
    private final DvrShutDownReceiver mDvrShutDownBroadCastReceiver = new DvrShutDownReceiver();
    private boolean mAllowGotoMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        EventUtil.register(this);
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

        getInstance().registPopUpEventCallback(popUpEventCallback);
        getInstance().registIconEventCallback(iconEventCallback);
        getInstance().registLedEventCallback(ledEventCallback);

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
        GpsHelper.checkGpsSignalStrength();
        EventUtil.sendEvent(new MessageEvent<>(Event.EventCode.EVENT_SECOND_CAMERIA, CameraHelper.hasExtCamera() ? CONNECTED : DISCONNECTED));
        ScreenSleepUtils.resumeScreenSleep();

        // PUCDR-2209: After Record Screen, wait for a while then can enter MENU
        mAllowGotoMenu = false;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                mAllowGotoMenu = true;
            }
        }, 1000);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHandleEvent ( JvcEventHandoutInfo jvcReportInfo){
        if(jvcReportInfo != null && jvcReportInfo.getOos() == Const.OOS_SUCCESS && jvcReportInfo.getStatus() == 0) {
            StringBuilder stringBuilder = new StringBuilder();
            String voiceCode =null;
            switch (jvcReportInfo.getEventType()) {
                case NOTICE_START:
                    if(jvcReportInfo.getType() == 1) {
                        voiceCode = stringBuilder.append("0201").append(",").append("0202").toString();
                        EventManager.getInstance().getEventInfoByEventType(NOTICE_START).setVoiceGuidence(voiceCode);
                    }
                    break;
                case DRIVING_REPORT:
                    if(jvcReportInfo.getResult() == 1){
                        voiceCode = stringBuilder.append("0401").append(",")
                                .append("0402").append(",").append("0405").toString();
                    }else if(jvcReportInfo.getResult() == 2){
                        voiceCode = stringBuilder.append("0401").append(",")
                                .append("0403").append(",").append("0405").toString();
                    }else if(jvcReportInfo.getResult() == 3){
                        voiceCode = stringBuilder.append("0401").append(",")
                                .append("0404").append(",").append("0405").toString();
                    }
                    EventManager.getInstance().getEventInfoByEventType(DRIVING_REPORT).setVoiceGuidence(voiceCode);
                    break;
                case MONTHLY_DRIVING_REPORT:
                    if(jvcReportInfo.getResult() >= 0 && jvcReportInfo.getResult() <= 100){
                        voiceCode = stringBuilder.append("0501").append(",").append("0502").toString();
                        EventManager.getInstance().getEventInfoByEventType(MONTHLY_DRIVING_REPORT).setVoiceGuidence(voiceCode);
                    }
                    break;
                case AdDVICE_BEFORE_DRIVING:
                    EventManager.getInstance().getEventInfoByEventType(AdDVICE_BEFORE_DRIVING)
                            .setVoiceGuidence(jvcReportInfo.getCode());
                    break;
                default:
            }
            EventManager.getInstance().handOutEventInfo(jvcReportInfo.getEventType());
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!SDcardHelper.isSDCardEnable(this)) {
            LedMananger.getInstance().setLedRecStatus(true, false, 0);
        }
        ScreenSleepUtils.cancelScreenSleep();
    }

    @Override
    public void onDestroy() {
        removeListeners();
        unregisterReceiver(mSdCardStatusListener);
        //add by Mark
        MainApp.getInstance().unBindJvcMainAppService();
        //end add
        unregisterReceiver(mDvrShutDownBroadCastReceiver);
        EventUtil.unregister(this);
        super.onDestroy();
    }

    private void removeListeners() {
        getInstance().unRegistPopUpEventCallback(popUpEventCallback);
        getInstance().unRegistIconEventCallback(iconEventCallback);
        getInstance().unRegistLedEventCallback(ledEventCallback);
    }


    private EventCallback popUpEventCallback = (eventInfo, timeStamp) -> DialogManager.getIntance().showDialog(eventInfo.getEventType(), 0);

    private EventCallback iconEventCallback = (eventInfo, timeStamp) -> Logg.d(TAG, "iconEventCallback...eventInfo:" + eventInfo.toString());

    private EventCallback ledEventCallback = (eventInfo, timeStamp) -> {
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
    };

    @Override
    protected boolean handleKeyEvent(KeyEvent event) {
        return GlobalLogic.getInstance().isStartSwitchUser() || fragment.isKeyNotValid();
    }

    private void handleVolumeUpKeyEvent(){
        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION) + 1;
        if (currentVolume <= maxVolume) {
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, currentVolume,
                    0);
        }
    }
    private void handleVolumeDownKeyEvent(){
        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION) - 1;
        if (currentVolume >= 0) {
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, currentVolume,
                    0);
        }
    }
    private void handleBackKeyEvent(){
        int micValue = GlobalLogic.getInstance().getInt(AskeySettings.Global.RECSET_VOICE_RECORD);
        int newVal = (micValue == 0) ? 1 : 0;
        boolean value = GlobalLogic.getInstance().putInt(AskeySettings.Global.RECSET_VOICE_RECORD, newVal);
        EventUtil.sendEvent(new MessageEvent<>(Event.EventCode.EVENT_MIC, value));
    }
    private void handleMenuKeyEvent(){
        if (!mAllowGotoMenu) { // PUCDR-2209
            // Prevent leave recording too fast and cause tasks interrupt
            return;
        }
        Location currentLocation = GPSStatusManager.getInstance().getCurrentLocation();
        if (currentLocation == null || currentLocation.getSpeed() <= 0.0f) {
            SDcardHelper.disMissSdcardDialog();
            MainAppSending.menuTransition(FROM_MAINAPP);
            fragment.terminateVideoRecord();
            ActivityUtils.startActivity(this, Const.PACKAGE_NAME, Const.CLASS_NAME, false);
        }
    }

    @Override
    public void onContinueKeyHoldOneSecond(int keyCode) {
        switch (keyCode) {
            case KeyAdapter.KEY_VOLUME_UP:
                handleVolumeUpKeyEvent();
                break;
            case KeyAdapter.KEY_VOLUME_DOWN:
                handleVolumeDownKeyEvent();
                break;
        }
    }
    @Override
    public void onContinueKeyHoldThreeSecond(int keyCode) {
        switch (keyCode) {
            case KeyAdapter.KEY_VOLUME_UP:
                handleVolumeUpKeyEvent();
                break;
            case KeyAdapter.KEY_VOLUME_DOWN:
                handleVolumeDownKeyEvent();
                break;
        }
    }

    @Override
    public void onKeyShortPressedDown(int keyCode) {
        switch (keyCode) {
            case KeyAdapter.KEY_VOLUME_UP:
                handleVolumeUpKeyEvent();
                break;
            case KeyAdapter.KEY_VOLUME_DOWN:
                handleVolumeDownKeyEvent();
                break;
            case KeyAdapter.KEY_BACK:
                handleBackKeyEvent();
                break;
            default:
        }
    }
    @Override
    public void onKeyShortPressedUp(int keyCode) {
        switch (keyCode) {
            case KeyAdapter.KEY_MENU:
                handleMenuKeyEvent();
                break;
            default:
        }
    }
    @Override
    public  void onKeyHoldHalfASecondUp(int keyCode){
        switch (keyCode) {
            case KeyAdapter.KEY_MENU:
                handleMenuKeyEvent();
                break;
            default:
        }
    }
    @Override
    public  void onKeyHoldOneSecondUp(int keyCode){
        switch (keyCode) {
            case KeyAdapter.KEY_MENU:
                handleMenuKeyEvent();
                break;
            default:
        }
    }

    @Override
    public void onKeyHoldOneSecondDown(int keyCode) {
        switch (keyCode) {
            case KeyAdapter.KEY_VOLUME_UP:
                handleVolumeUpKeyEvent();
                break;
            case KeyAdapter.KEY_VOLUME_DOWN:
                handleVolumeDownKeyEvent();
                break;
            default:
        }
    }

    private BroadcastReceiver mSdCardStatusListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Logg.d(TAG, "action=" + action);
            if (ACTION_SDCARD_STATUS.equals(action)) {
                String data = intent.getStringExtra("data");
                Logg.d(TAG, "action=" + action + ",data=" + data);
                switch (data) {
                    case CMD_SHOW_SDCARD_NOT_SUPPORTED:
                        handleSdCardEvent(context, SDCARD_UNSUPPORTED, 111);
                        break;
                    case CMD_SHOW_SDCARD_SUPPORTED:
                        handleSdCardEvent(context, SDCARD_SUPPORTED, -1);
                        break;
                    case CMD_SHOW_SDCARD_INIT_FAIL:
                        handleSdCardEvent(context, SDCARD_INIT_FAIL, -1);
                        break;
                    case CMD_SHOW_SDCARD_INIT_SUCC:
                        //               LedMananger.getInstance().setLedRecStatus(true, false, 0);
                        RecordHelper.setRecordingPrecondition(SDCARD_UNREACH_PICTURE_LIMIT);
                        RecordHelper.setRecordingPrecondition(SDCARD_UNREACH_EVENT_FILE_LIMIT);
                        RecordHelper.setRecordingPrecondition(SDCARD_RECORDING_FULL_LIMIT_EXIT);
                        handleSdCardEvent(context, SDCARD_INIT_SUCCESS, -1);
                        EventUtil.sendEvent(new MessageEvent<>(Event.EventCode.EVENT_RECORDING_FILE_LIMIT,
                                EVENT_RECORDING_UNREACHABLE_LIMIT_CONDITION));
                        break;
                    case CMD_SHOW_SDCARD_UNRECOGNIZABLE:
                        handleSdCardEvent(context, SDCARD_UNRECOGNIZABLE, 113);
                        break;
                    case CMD_SHOW_SDCARD_NOT_EXIST:
                        handleSdCardEvent(context, SDCARD_REMOVED, 110);
                        break;
                    case CMD_SHOW_SDCARD_MOUNTED:
                        EventUtil.sendEvent(new MessageEvent<>(Event.EventCode.EVENT_SDCARD, SDCARD_MOUNTED));
                        break;
                    case CMD_SHOW_SDCARD_ASKEY_NOT_SUPPORTED:
                        handleSdCardEvent(context, SDCARD_ASKEY_NOT_SUPPORTED, 112);
                        break;
                }
            } else if (ACTION_SDCARD_LIMT.equals(action)) {
                String cmd_ex = intent.getStringExtra("cmd_ex");
                Logg.d(TAG, "action=" + action + ",cmd_ex=" + cmd_ex);
                if (CMD_SHOW_REACH_EVENT_FILE_LIMIT.equals(cmd_ex)) {
                    if (AppUtils.isActivityTop(context, ACTIVITY_CLASSNAME)) {
                        EventUtil.sendEvent(new MessageEvent<>(Event.EventCode.EVENT_RECORDING_FILE_LIMIT, EVENT_RECORDING_REACH_LIMIT_CONDITION));
                        EventUtil.sendEvent(new MessageEvent<>(Event.EventCode.EVENT_SDCARD, SDCARD_EVENT_FILE_LIMIT));
                    }
                } else if (CMD_SHOW_UNREACH_EVENT_FILE_LIMIT.equals(cmd_ex)) {
                    RecordHelper.setRecordingPrecondition(SDCARD_UNREACH_EVENT_FILE_LIMIT);
                    EventUtil.sendEvent(new MessageEvent<>(Event.EventCode.EVENT_RECORDING_FILE_LIMIT, EVENT_RECORDING_UNREACHABLE_LIMIT_CONDITION));
                } else if (CMD_SHOW_REACH_EVENT_FILE_OVER_LIMIT.equals(cmd_ex)) {
                    RecordHelper.setRecordingPrecondition(SDCARD_EVENT_FILE_OVER_LIMIT);
                    handleSdCardEvent(context, SDCARD_INIT_FAIL, 115);
                } else if (CMD_SHOW_REACH_PARKING_FILE_LIMIT.equals(cmd_ex)) {
                    EventUtil.sendEvent(new MessageEvent<>(Event.EventCode.EVENT_PARKING_RECODING_FILE_LIMIT, PARKING_RECORDING_REACH_LIMIT_CONDITION));
                } else if (CMD_SHOW_UNREACH_PARKING_FILE_LIMIT.equals(cmd_ex)) {
                    EventUtil.sendEvent(new MessageEvent<>(Event.EventCode.EVENT_PARKING_RECODING_FILE_LIMIT, PARKING_RECORDING_UNREACHABLE_LIMIT_CONDITION));
                } else if (CMD_SHOW_SDCARD_FULL_LIMIT.equals(cmd_ex)) {
                    RecordHelper.setRecordingPrecondition(SDCARD_RECORDING_FULL_LIMIT);
                    handleSdCardEvent(context, SDCARD_INIT_FAIL, 116);
                } else if (CMD_SHOW_UNREACH_SDCARD_FULL_LIMIT.equals(cmd_ex)) {
                    RecordHelper.setRecordingPrecondition(SDCARD_RECORDING_FULL_LIMIT_EXIT);
                } else if (CMD_SHOW_REACH_PICTURE_FILE_LIMIT.equals(cmd_ex)) {
                    EventUtil.sendEvent(new MessageEvent<>(Event.EventCode.EVENT_SDCARD, SDCARD_EVENT_PICTURE_LIMIT));
                    EventUtil.sendEvent(new MessageEvent<>(Event.EventCode.EVENT_RECORDING_FILE_LIMIT,
                            EVENT_RECORDING_UNREACHABLE_LIMIT_CONDITION));
                } else if (CMD_SHOW_REACH_PICTURE_FILE_OVER_LIMIT.equals(cmd_ex)) {
                    RecordHelper.setRecordingPrecondition(SDCARD_PICTURE_FILE_OVER_LIMIT);
                    handleSdCardEvent(context, SDCARD_INIT_FAIL, 129);
                } else if (CMD_SHOW_UNREACH_PICTURE_FILE_LIMIT.equals(cmd_ex)) {
                    RecordHelper.setRecordingPrecondition(SDCARD_UNREACH_PICTURE_LIMIT);
                } else if (CMD_SHOW_BOTH_EVENT_AND_PICTURE_FOLDER_LIMIT.equals(cmd_ex)) {
                    if (AppUtils.isActivityTop(context, ACTIVITY_CLASSNAME)) {
                        EventUtil.sendEvent(new MessageEvent<>(Event.EventCode.EVENT_SDCARD, SDCARD_EVENT_PICTURE_LIMIT));
                        EventUtil.sendEvent(new MessageEvent<>(Event.EventCode.EVENT_RECORDING_FILE_LIMIT, EVENT_RECORDING_REACH_LIMIT_CONDITION));
                        EventUtil.sendEvent(new MessageEvent<>(Event.EventCode.EVENT_SDCARD, SDCARD_EVENT_FILE_LIMIT));
                    }

                } else if (CMD_SHOW_BOTH_EVENT_AND_PICTURE_FOLDER_OVER_LIMIT.equals(cmd_ex)) {
                    RecordHelper.setRecordingPrecondition(SDCARD_EVENT_FILE_OVER_LIMIT);
                    RecordHelper.setRecordingPrecondition(SDCARD_PICTURE_FILE_OVER_LIMIT);
                    handleSdCardEvent(context, SDCARD_INIT_FAIL, 115);
                    handleSdCardEvent(context, SDCARD_INIT_FAIL, 129);
                }
            }
        }
    };

    private void handleSdCardEvent(Context context, SDcardStatusType sdCardStatus, int eventType) {
        if (AppUtils.isActivityTop(context, ACTIVITY_CLASSNAME)) {
            EventUtil.sendEvent(new MessageEvent<>(Event.EventCode.EVENT_SDCARD, sdCardStatus));
            EventUtil.sendEvent(new MessageEvent<>(Event.EventCode.EVENT_RECORDING_FILE_LIMIT,
                    EVENT_RECORDING_UNREACHABLE_LIMIT_CONDITION));
            if (eventType >= 0) {
                getInstance().handOutEventInfo(eventType);
            }
        }
    }
}
