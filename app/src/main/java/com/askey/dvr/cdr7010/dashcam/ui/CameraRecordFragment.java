package com.askey.dvr.cdr7010.dashcam.ui;

import android.Manifest;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.askey.dvr.cdr7010.dashcam.R;
import com.askey.dvr.cdr7010.dashcam.application.DashCamApplication;
import com.askey.dvr.cdr7010.dashcam.core.DashCam;
import com.askey.dvr.cdr7010.dashcam.core.RecordConfig;
import com.askey.dvr.cdr7010.dashcam.core.camera2.CameraHelper;
import com.askey.dvr.cdr7010.dashcam.domain.Event;
import com.askey.dvr.cdr7010.dashcam.domain.MessageEvent;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.jvckenwood.Communication;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.jvckenwood.JvcEventSending;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.jvckenwood.MainApp;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.local.JvcStatusParams;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.local.LocalJvcStatusManager;
import com.askey.dvr.cdr7010.dashcam.logic.GlobalLogic;
import com.askey.dvr.cdr7010.dashcam.service.DialogManager;
import com.askey.dvr.cdr7010.dashcam.service.EventManager;
import com.askey.dvr.cdr7010.dashcam.service.FileManager;
import com.askey.dvr.cdr7010.dashcam.service.GPSStatusManager;
import com.askey.dvr.cdr7010.dashcam.service.LedMananger;
import com.askey.dvr.cdr7010.dashcam.service.SimCardManager;
import com.askey.dvr.cdr7010.dashcam.service.ThermalController;
import com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum;
import com.askey.dvr.cdr7010.dashcam.util.ActivityUtils;
import com.askey.dvr.cdr7010.dashcam.util.AppUtils;
import com.askey.dvr.cdr7010.dashcam.util.EventUtil;
import com.askey.dvr.cdr7010.dashcam.util.Logg;
import com.askey.dvr.cdr7010.dashcam.util.RecordHelper;
import com.askey.dvr.cdr7010.dashcam.util.SDcardHelper;
import com.askey.dvr.cdr7010.dashcam.widget.OSDView;
import com.askey.platform.AskeyIntent;
import com.askey.platform.AskeySettings;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.LTEStatusType.LTE_NONE;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.LTEStatusType.LTE_SIGNAL_STRENGTH_GOOD;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.LTEStatusType.LTE_SIGNAL_STRENGTH_GREAT;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.LTEStatusType.LTE_SIGNAL_STRENGTH_MODERATE;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.LTEStatusType.LTE_SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.LTEStatusType.LTE_SIGNAL_STRENGTH_POOR;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.MICStatusType.MIC_OFF;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.MICStatusType.MIC_ON;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.RecordingPreconditionStatus.BATTERY_STATUS_DISCHARGING;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.RecordingPreconditionStatus.HIGH_TEMPERATURE;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.RecordingPreconditionStatus.LOW_TEMPERATURE;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.RecordingPreconditionStatus.SDCARD_AVAILABLE;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.RecordingPreconditionStatus.SDCARD_EVENT_FILE_OVER_LIMIT;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.RecordingPreconditionStatus.SDCARD_PICTURE_FILE_OVER_LIMIT;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.RecordingPreconditionStatus.SDCARD_RECORDING_FULL_LIMIT;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.RecordingPreconditionStatus.SDCARD_RECORDING_FULL_LIMIT_EXIT;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.RecordingPreconditionStatus.SDCARD_UNAVAILABLE;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.RecordingPreconditionStatus.SDCARD_UNREACH_EVENT_FILE_LIMIT;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.RecordingPreconditionStatus.SDCARD_UNREACH_PICTURE_LIMIT;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.RecordingPreconditionStatus.SWITCH_USER_COMPLETED;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.RecordingPreconditionStatus.SWITCH_USER_STARTED;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.RecordingStatusType.RECORDING_CONTINUOUS;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.RecordingStatusType.RECORDING_EVENT;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_INIT_FAIL;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_INIT_SUCCESS;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_REMOVED;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SdCardAndSimCardCheckStatus.CHECK_START;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SwitchUserEvent.SWITCH_USER_COMPLETE;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SwitchUserEvent.SWITCH_USER_PREPARE;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SwitchUserEvent.SWITCH_USER_START;
import static com.askey.dvr.cdr7010.dashcam.util.SDcardHelper.SDcardStatus.SDCARD_NOT_EXIST;

public class CameraRecordFragment extends Fragment {
    private static final String TAG = CameraRecordFragment.class.getSimpleName();
    private static final String ACTIVITY_CLASSNAME = "com.askey.dvr.cdr7010.dashcam.ui.MainActivity";
    private DashCam mMainCam;
    private DashCam mExtCam;
    private OSDView osdView;
    private TextView tvContent;
    private Handler mHandler;
    private ThermalController thermalController;
    private TelephonyManager mTelephonyManager;
    private ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private boolean isEventRecording;
    private static boolean isBatteryDisconnected = false;
    private boolean canShutDown;
    private boolean isKeyNotValid = false;
    public boolean canRecord = false;

    private static final String ACTION_SDCARD_STATUS = "action_sdcard_status";
    private static final String SDCARD_FULL_LIMIT = "show_sdcard_full_limit";
    private static final String SDCARD_FULL_LIMIT_EXIT = "show_unreach_sdcard_full_limit";
    private static final String ACTION_SDCARD_LIMT = "com.askey.dvr.cdr7010.dashcam.limit";
    private static final String CMD_SHOW_REACH_PICTURE_FILE_OVER_LIMIT = "show_reach_picture_file_over_limit";
    private static final String CMD_SHOW_REACH_EVENT_FILE_OVER_LIMIT = "show_reach_event_file_over_limit";
    public static final String CMD_SHOW_BOTH_EVENT_AND_PICTURE_FOLDER_OVER_LIMIT = "show_both_event_and_picture_file_over_limit";//超过限制
    private static final String CMD_SHOW_UNREACH_EVENT_FILE_LIMIT = "show_unreach_event_file_limit";//限制解除
    private static final String CMD_SHOW_UNREACH_PICTURE_FILE_LIMIT = "show_unreach_picture_file_limit";//限制解除

    private static final int REQUEST_VIDEO_PERMISSIONS = 1001;
    private static final int REQUEST_GPS_PERMISSIONS = 1002;
    private static final int REQUEST_SIMCARD_PERMISSIONS = 1003;
    private static final String[] VIDEO_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
    };
    private static final String[] GPS_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,

    };
    private static final String[] SIM_PERMISSIONS = {
            Manifest.permission.READ_PHONE_STATE,
    };

    private BroadcastReceiver mSdStatusListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_SDCARD_STATUS.equals(intent.getAction())) {
                Logg.d(TAG, "mSdStatusListener,ACTION_SDCARD_STATUS");
                final String ex = intent.getStringExtra("data");
                Logg.d(TAG, "mSdStatusListener,ex==" + ex);
                if ("show_sdcard_init_success".equals(ex)) {
                    Logg.d(TAG, "SD Card available");
                    try {
                        LedMananger.getInstance().setLedRecStatus(true, false, 0);
                        RecordHelper.setRecordingPrecondition(SDCARD_AVAILABLE);
                        startVideoRecord("SD become available");
                    } catch (Exception e) {
                        Logg.e(TAG, "start video record fail with exception: " + e.getMessage());
                    }
                }
            } else if (ACTION_SDCARD_LIMT.equals(intent.getAction())) {
                Logg.d(TAG, "mSdStatusListener,ACTION_SDCARD_LIMT");
                final String ex = intent.getStringExtra("cmd_ex");
                Logg.d(TAG, "mSdStatusListener,ex==" + ex);
                if (SDCARD_FULL_LIMIT.equals(ex)) {
                    Logg.d(TAG, "SDCARD_FULL_LIMIT");
                    RecordHelper.setRecordingPrecondition(SDCARD_RECORDING_FULL_LIMIT);
                    stopVideoRecord("SDCARD_FULL_LIMIT");
                } else if (SDCARD_FULL_LIMIT_EXIT.equals(ex)) {
                    Logg.d(TAG, "SDCARD_FULL_LIMIT_EXIT");
                    try {
                        LedMananger.getInstance().setLedRecStatus(true, false, 0);
                        RecordHelper.setRecordingPrecondition(SDCARD_RECORDING_FULL_LIMIT_EXIT);
                        startVideoRecord("SDCARD_FULL_LIMIT_EXIT");
                    } catch (Exception e) {
                        Logg.e(TAG, "start video record fail with exception: " + e.getMessage());
                    }
                } else if (CMD_SHOW_REACH_PICTURE_FILE_OVER_LIMIT.equals(ex)) {
                    Logg.d(TAG, "SDCARD_REACH_PICTURE_FILE_OVER_LIMIT");
                    RecordHelper.setRecordingPrecondition(SDCARD_PICTURE_FILE_OVER_LIMIT);
                    stopVideoRecord("SDCARD_REACH_PICTURE_FILE_OVER_LIMIT");
                } else if (CMD_SHOW_UNREACH_PICTURE_FILE_LIMIT.equals(ex)) {
                    Logg.d(TAG, "SDCARD_UNREACH_PICTURE_FILE_LIMIT");
                    try {
                        LedMananger.getInstance().setLedRecStatus(true, false, 0);
                        RecordHelper.setRecordingPrecondition(SDCARD_UNREACH_PICTURE_LIMIT);
                        startVideoRecord("SDCARD_UNREACH_PICTURE_FILE_LIMIT");
                    } catch (Exception e) {
                        Logg.e(TAG, "start video record fail with exception: " + e.getMessage());
                    }
                } else if (CMD_SHOW_REACH_EVENT_FILE_OVER_LIMIT.equals(ex)) {
                    Logg.d(TAG, "SDCARD_REACH_EVENT_FILE_OVER_LIMIT");
                    RecordHelper.setRecordingPrecondition(SDCARD_EVENT_FILE_OVER_LIMIT);
                    stopVideoRecord("SDCARD_REACH_EVENT_FILE_OVER_LIMIT");
                } else if (CMD_SHOW_UNREACH_EVENT_FILE_LIMIT.equals(ex)) {
                    Logg.d(TAG, "SDCARD_UNREACH_EVENT_FILE_LIMIT.");
                    try {
                        LedMananger.getInstance().setLedRecStatus(true, false, 0);
                        RecordHelper.setRecordingPrecondition(SDCARD_UNREACH_EVENT_FILE_LIMIT);
                        startVideoRecord("SDCARD_UNREACH_EVENT_FILE_LIMIT");
                    } catch (Exception e) {
                        Logg.e(TAG, "start video record fail with exception: " + e.getMessage());
                    }
                } else if (CMD_SHOW_BOTH_EVENT_AND_PICTURE_FOLDER_OVER_LIMIT.equals(ex)) {
                    Logg.d(TAG, "CMD_SHOW_BOTH_EVENT_AND_PICTURE_FOLDER_OVER_LIMIT");
                    RecordHelper.setRecordingPrecondition(SDCARD_EVENT_FILE_OVER_LIMIT);
                    RecordHelper.setRecordingPrecondition(SDCARD_PICTURE_FILE_OVER_LIMIT);
                    stopVideoRecord("CMD_SHOW_BOTH_EVENT_AND_PICTURE_FOLDER_OVER_LIMIT");
                }
            }
        }
    };

    private BroadcastReceiver mSdBadRemovalListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logg.d(TAG, "mSdBadRemovalListener...action==" + intent.getAction());
            if (Intent.ACTION_MEDIA_EJECT.equals(intent.getAction())) {
                Logg.d(TAG, "SD Card ACTION_MEDIA_EJECT");
                RecordHelper.setRecordingPrecondition(SDCARD_UNAVAILABLE);
                stopVideoRecord("SD ACTION_MEDIA_EJECT");
            }
        }
    };

    private BroadcastReceiver simCardReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logg.d(TAG, "onReceive intent=" + intent);
            EventUtil.sendEvent(new MessageEvent(Event.EventCode.EVENT_SIMCARD));
        }
    };

    private BroadcastReceiver mShutdownReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SHUTDOWN.equals(intent.getAction()) || AskeyIntent.ACTION_DVR_SHUTDOWN.equals(intent.getAction())) {
                Logg.d(TAG, "BroadcastReceiver: " + intent.getAction());
                stopVideoRecord(intent.getAction());
            }
        }
    };

    private BroadcastReceiver mBatteryStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                int status = intent.getIntExtra("status", 0);
                switch (status) {
                    case BatteryManager.BATTERY_STATUS_CHARGING:
                    case BatteryManager.BATTERY_STATUS_FULL:
                        Logg.d(TAG, "battery status is charging");
                        isBatteryDisconnected = false;
                        break;
                    case BatteryManager.BATTERY_STATUS_DISCHARGING:
                        Logg.d(TAG, "battery status is discharging");
                        isBatteryDisconnected = true;
                        handler.sendEmptyMessageDelayed(0, 2000);//一秒内(考虑到实际接收广播的时间更长一些，delay2秒)，用户又插上不关机
                        break;
                }
            }
        }
    };

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 0) {
                if (isBatteryDisconnected) {
                    canShutDown = true;
                    Logg.d(TAG, "battery status....isBatteryDisconnected");
                    sendShutDownBroadCast();
                    RecordHelper.setRecordingPrecondition(BATTERY_STATUS_DISCHARGING);
                    stopVideoRecord("Intent.BATTERY_STATUS_DISCHARGING");
                    if (!isEventRecording) {
                        //关机
                        handler.sendEmptyMessageDelayed(1, 2000);
                    }
                }
            } else if (msg.what == 1) {
                Logg.d(TAG, "SHUT DOWN...");
                ActivityUtils.shutDown(DashCamApplication.getAppContext());
            } else if (msg.what == 2) {
                int flg = Settings.Global.getInt(getActivity().getContentResolver(), AskeySettings.Global.SYSSET_STARTUP_INFO, -1);
                Logg.d(TAG, "flg FROM SETTINGS==" + flg);
                dealFlg(flg);
            }
            return true;
        }
    });

    /**
     * PUCDR-2294
     */
    private void sendShutDownBroadCast() {
        Intent intent = new Intent(AskeyIntent.ACTION_ACC_OFF);
        getActivity().sendBroadcast(intent);
    }

    DashCam.StateCallback mDashCallback = new DashCam.StateCallback() {
        @Override
        public void onStarted() {
            Logg.d(TAG, "DashState: onStarted");
            EventUtil.sendEvent(new MessageEvent<>(Event.EventCode.EVENT_RECORDING,
                    RECORDING_CONTINUOUS));
            isEventRecording = false;
            mHandler.post(() -> EventManager.getInstance().handOutEventInfo(104));
            mHandler.post(() -> EventManager.getInstance().handOutEventInfo(130));
        }

        @Override
        public void onStoped() {
            Logg.d(TAG, "DashState: onStoped");
            EventUtil.sendEvent(new MessageEvent<>(Event.EventCode.EVENT_RECORDING,
                    UIElementStatusEnum.RecordingStatusType.RECORDING_STOP));
            if (!isEventRecording) {
                mHandler.post(() -> EventManager.getInstance().handOutEventInfo(105));
                mHandler.post(() -> EventManager.getInstance().handOutEventInfo(131));
            } else {
                mHandler.post(() -> EventManager.getInstance().handOutEventInfo(124));
            }
        }

        @Override
        public void onError() {
            Logg.d(TAG, "DashState: onError");
            EventUtil.sendEvent(new MessageEvent<>(Event.EventCode.EVENT_RECORDING,
                    UIElementStatusEnum.RecordingStatusType.RECORDING_ERROR));
            mHandler.post(() -> EventManager.getInstance().handOutEventInfo(114));
        }

        @Override
        public void onEventStateChanged(final boolean on) {
            // 注意：禁止在这里进行耗时操作
            Logg.e("iamlbccc", "UI onEventStateChanged...> " + on);
            Logg.d(TAG, "DashState: onEventStateChanged " + on);
            EventUtil.sendEvent(new MessageEvent<>(Event.EventCode.EVENT_RECORDING,
                    on ? RECORDING_EVENT : RECORDING_CONTINUOUS));
            final boolean event = isEventRecording;
            mHandler.post(() -> {
                if (on) { // event recording
                    EventManager.getInstance().handOutEventInfo(105); // Continuous Recording end
                    EventManager.getInstance().handOutEventInfo(123); // Event Recording start
                } else { // Continuous recording
                    if (event) {
                        EventManager.getInstance().handOutEventInfo(124); // Event Recording end
                    }
                    EventManager.getInstance().handOutEventInfo(104); // Continuous Recording start
                }
            });
            isEventRecording = on;
        }

        @Override
        public void onEventCompleted(int eventId, long timestamp, List<String> pictures, String video) {
            Logg.d(TAG, "DashState: onEventCompleted ");
            Integer[] data = new Integer[]{102, 0, 103, 103, 103, 0, 0, 0};
            if (video != null) {
                data[0] = 1;
            }
            if (pictures != null) {
                for (int i = 0; i < pictures.size(); i++) {
                    String path = pictures.get(i);
                    if (!TextUtils.isEmpty(path)) {
                        if (i == 0) {
                            data[2] = 1;
                        } else if (i == 1) {
                            data[3] = 1;
                        } else if (i == 2) {
                            data[4] = 1;
                        }
                    }
                }
            }
            ArrayList<String> files = new ArrayList<>();
            if (isBatteryDisconnected) {//掉电不传video和image3
                data[4] = 0;
                data[0] = 0;
                if (pictures != null && pictures.size() >= 3) {
                    files.add(pictures.get(0));
                    files.add(pictures.get(1));
                }
            } else {//有供电才传video
                files.add(video);
                files.addAll(pictures);
            }
            ArrayList<Integer> results = new ArrayList<>(Arrays.asList(data));
            JvcEventSending.recordResponse(eventId, results, files);
            delay2ShutDown();
        }

        @Override
        public void onEventTerminated(int eventId, int reason) {
            Logg.d(TAG, "DashState: onEventTerminated ");
            ArrayList<Integer> results = new ArrayList<>(Arrays.asList(reason, 0, reason, reason, reason, 0, 0, 0));
            ArrayList<String> files = new ArrayList<>();
            JvcEventSending.recordResponse(eventId, results, files);
            delay2ShutDown();
        }
    };

    private void delay2ShutDown() {
        if (canShutDown) {
            //关机,2018.9.5新需求，最多7秒后关机，此处按最长时间处理
            handler.sendEmptyMessageDelayed(1, 7000);
        }
    }

    private ThermalController.ThermalListener thermalListener = new ThermalController.ThermalListener() {
        @Override
        public void startRecording() {
            if (AppUtils.isActivityTop(getActivity(), ACTIVITY_CLASSNAME)) {
                try {
                    if (RecordHelper.isHighTemperature()) {
                        Logg.d(TAG, "ThermalController startRecording");
                        RecordHelper.setRecordingPrecondition(LOW_TEMPERATURE);
                        startVideoRecord("cpu low temperature");
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
        }

        @Override
        public void closeRecording() {
            if (AppUtils.isActivityTop(getActivity(), ACTIVITY_CLASSNAME)) {
                if (RecordHelper.isRecodingEnable()) {
                    Logg.d(TAG, "ThermalController closeRecording");
                    stopVideoRecord("CPU reach high temperature");
                    RecordHelper.setRecordingPrecondition(HIGH_TEMPERATURE);
                }
            }
        }

        @Override
        public void closeLcdPanel() {
        }

        @Override
        public void startLcdPanel() {
        }
    };

    private LocalJvcStatusManager.LocalJvcStatusCallback jvcStatusCallback = new LocalJvcStatusManager.LocalJvcStatusCallback() {
        @Override
        public void onDataArriving(EnumMap<JvcStatusParams.JvcStatusParam, Object> enumMap) {
            LocalJvcStatusManager.removeInsuranceCallback(this);
            Logg.d(TAG, "onDataArriving...");
            int oos = -1;
            String response = null;
            if (enumMap != null) {
                if (enumMap.containsKey(JvcStatusParams.JvcStatusParam.OOS)) {
                    oos = (int) enumMap.get(JvcStatusParams.JvcStatusParam.OOS);
                    Logg.d(TAG, "oos..." + oos);
                }
                if (enumMap.containsKey(JvcStatusParams.JvcStatusParam.RESPONSE)) {
                    response = (String) enumMap.get(JvcStatusParams.JvcStatusParam.RESPONSE);
                    Logg.d(TAG, "response.." + response);
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
                                        dealFlg(flg);
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
                        int flg = Settings.Global.getInt(getActivity().getContentResolver(), AskeySettings.Global.SYSSET_STARTUP_INFO, -1);
                        Logg.d(TAG, "flg FROM SETTINGS==" + flg);
                        dealFlg(flg);
                        break;
                }
            }
        }
    };

    public static CameraRecordFragment newInstance() {
        return new CameraRecordFragment();
    }

    public static boolean isBatteryDisconnected() {
        return isBatteryDisconnected;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logg.d(TAG, "onCreate");
        mTelephonyManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        mHandler = new Handler(Looper.getMainLooper());
        thermalController = new ThermalController(thermalListener, getActivity());
        EventUtil.register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera_record, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        osdView = (OSDView) view.findViewById(R.id.osd_view);
        tvContent = (TextView) view.findViewById(R.id.tv_notice_content);
        requestVideoPermissions();
        requestGPSPermissions();
        requestSIMCardPermissions();
        GPSStatusManager.getInstance().recordLocation(true);
        osdView.init(1000);
        dealInsurance();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        intentFilter.setPriority(1000);
        getActivity().registerReceiver(mBatteryStateReceiver, intentFilter);
    }

    private void dealInsurance() {
        int launched = Settings.Global.getInt(getActivity().getContentResolver(), "SYSSET_dashcam_launched", 0);
        Logg.d(TAG, "SYSSET_dashcam_launched==" + launched);
        if (launched == 1) {
            handler.sendEmptyMessageDelayed(2, 500);
        } else {
            LocalJvcStatusManager.getInsuranceTerm(jvcStatusCallback);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        IntentFilter simCardFilter = new IntentFilter();
        simCardFilter.addAction("android.intent.action.PHONE_STATE");
        simCardFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
        getActivity().registerReceiver(simCardReceiver, simCardFilter);
    }

    @Override
    public void onResume() {
        super.onResume();
        Logg.d(TAG, "onResume");
        onMessageEvent(new MessageEvent(Event.EventCode.EVENT_MIC));
        onMessageEvent(new MessageEvent(Event.EventCode.EVENT_REFRESH_USER_NAME));
        LedMananger.getInstance().setLedMicStatus(getMicphoneEnable());
        mTelephonyManager.listen(mListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addDataScheme("file");
        getActivity().registerReceiver(mSdBadRemovalListener, filter);

        IntentFilter filter2 = new IntentFilter();
        filter2.addAction(ACTION_SDCARD_STATUS);
        filter2.addAction(ACTION_SDCARD_LIMT);
        getActivity().registerReceiver(mSdStatusListener, filter2);

        IntentFilter dvrShutDownIntentFilter = new IntentFilter(AskeyIntent.ACTION_DVR_SHUTDOWN);
        dvrShutDownIntentFilter.addAction(Intent.ACTION_SHUTDOWN);
        dvrShutDownIntentFilter.setPriority(1000);
        getActivity().registerReceiver(mShutdownReceiver, dvrShutDownIntentFilter);

        final boolean stamp = getRecordStamp();
        final boolean audio = getMicphoneEnable();
        RecordConfig mainConfig = RecordConfig.builder()
                .cameraId(CameraHelper.CAMERA_MAIN)
                .videoWidth(1920)
                .videoHeight(1080)
                .videoFPS(27)
                .videoBitRate((int) (9.6 * 1024 * 1024)) // 10Mbps
                .videoStampEnable(stamp)
                .audioRecordEnable(true)
                .audioMute(!audio)
                .adasEnable(true)
                .nmeaRecordEnable(true)
                .build();
        mMainCam = new DashCam(getActivity(), mainConfig, mDashCallback);
        mMainCam.enableAdas(true);

        if (CameraHelper.hasExtCamera()) {
            RecordConfig extConfig = RecordConfig.builder()
                    .cameraId(CameraHelper.CAMERA_EXT)
                    .videoWidth(1280)
                    .videoHeight(720)
                    .videoFPS(15)
                    .videoBitRate(5 * 1024 * 1024) // 5Mbps
                    .videoStampEnable(stamp)
                    .audioRecordEnable(false)
                    .audioMute(true)
                    .adasEnable(false)
                    .nmeaRecordEnable(false)
                    .build();
            mExtCam = new DashCam(getActivity(), extConfig, null);
        }
        try {
            startVideoRecord("Fragment onResume");
        } catch (Exception e) {
            Logg.e(TAG, "onResume: start video record fail with exception: " + e.getMessage());
        }
        thermalController.startThermalMonitor();
    }

    @Override
    public void onPause() {
        Logg.d(TAG, "onPause");

        // Kenny comment below because they method is not synchronized and may occur leaking
        /*
        mMainCam.enableAdas(false);
        stopVideoRecord("Fragment onPause");
        */

        getActivity().unregisterReceiver(mSdStatusListener);
        getActivity().unregisterReceiver(mSdBadRemovalListener);
        getActivity().unregisterReceiver(mShutdownReceiver);
        mTelephonyManager.listen(mListener, PhoneStateListener.LISTEN_NONE);
        LedMananger.getInstance().setLedMicStatus(false);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Logg.d(TAG, "onDestroy");
        EventUtil.unregister(this);
        osdView.unInit();
        thermalController.stopThermalMonitor();
        GPSStatusManager.getInstance().recordLocation(false);
        getActivity().unregisterReceiver(simCardReceiver);
        getActivity().unregisterReceiver(mBatteryStateReceiver);
        if (timeFinishShow != null) {
            timeFinishShow.cancel();
        }
        if (timeFinishApp != null) {
            timeFinishApp.cancel();
            timeFinishApp = null;
        }
        super.onDestroy();
    }

    @Override
    public void onStop() {
        Logg.d(TAG, "onStop");
        super.onStop();
    }

    private void requestVideoPermissions() {
        if (hasPermissionsNotGranted(VIDEO_PERMISSIONS)) {
            requestPermissions(VIDEO_PERMISSIONS, REQUEST_VIDEO_PERMISSIONS);
        }
    }

    private void requestGPSPermissions() {
        if (hasPermissionsNotGranted(GPS_PERMISSIONS)) {
            requestPermissions(GPS_PERMISSIONS, REQUEST_GPS_PERMISSIONS);
        }
    }

    private void requestSIMCardPermissions() {
        if (hasPermissionsNotGranted(SIM_PERMISSIONS)) {
            requestPermissions(SIM_PERMISSIONS, REQUEST_SIMCARD_PERMISSIONS);
        }
    }

    private boolean hasPermissionsNotGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(getActivity(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        return false;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent messageEvent) {
        if (messageEvent != null) {
            Logg.d(TAG, "onMessageEvent messageEvent=" + messageEvent.getData());
            handleMessageEvent(messageEvent);
        }
    }

    private void handleMessageEvent(MessageEvent messageEvent) {
        if (messageEvent.getCode() == Event.EventCode.EVENT_RECORDING) {
            GlobalLogic.getInstance().setRecordingStatus((UIElementStatusEnum.RecordingStatusType) messageEvent.getData());
            if (messageEvent.getData() == RECORDING_EVENT) {
                osdView.startRecordingCountDown();
                DialogManager.getIntance().setResumeRecording(true);
            }
            if (messageEvent.getData() == RECORDING_CONTINUOUS) {
                DialogManager.getIntance().setStartRecording(true);
                DialogManager.getIntance().setResumeRecording(true);
            }
        } else if (messageEvent.getCode() == Event.EventCode.EVENT_RECORDING_FILE_LIMIT) {
            GlobalLogic.getInstance().setEventRecordingLimitStatus((UIElementStatusEnum.EventRecordingLimitStatusType) messageEvent.getData());
        } else if (messageEvent.getCode() == Event.EventCode.EVENT_PARKING_RECODING_FILE_LIMIT) {
            GlobalLogic.getInstance().setParkingRecordingLimitStatus((UIElementStatusEnum.ParkingRecordingLimitStatusType) messageEvent.getData());
        } else if (messageEvent.getCode() == Event.EventCode.EVENT_GPS) {
            GlobalLogic.getInstance().setGPSStatus((UIElementStatusEnum.GPSStatusType) messageEvent.getData());
        } else if (messageEvent.getCode() == Event.EventCode.EVENT_SDCARD) {
            GlobalLogic.getInstance().setSDCardStatus((UIElementStatusEnum.SDcardStatusType) messageEvent.getData());
            handleSdCardDialog((UIElementStatusEnum.SDcardStatusType) messageEvent.getData());
        } else if (messageEvent.getCode() == Event.EventCode.EVENT_MIC) {
            GlobalLogic.getInstance().setMicStatus(getMicphoneEnable() ? MIC_ON : MIC_OFF);
        } else if (messageEvent.getCode() == Event.EventCode.EVENT_FOTA_UPDATE) {
            GlobalLogic.getInstance().setFOTAFileStatus((UIElementStatusEnum.FOTAFileStatus) messageEvent.getData());
        } else if (messageEvent.getCode() == Event.EventCode.EVENT_SIMCARD) {
            int simState = SimCardManager.getInstant().getSimState();
            SimCardManager.getInstant().setSimState(simState);
            if (simState == TelephonyManager.SIM_STATE_ABSENT
                    || simState == SimCardManager.SIM_STATE_NOT_READY) {
                GlobalLogic.getInstance().setLTEStatus(LTE_NONE);
            }
            if (simState != TelephonyManager.SIM_STATE_ABSENT
                    && simState != TelephonyManager.SIM_STATE_READY
                    && simState != TelephonyManager.SIM_STATE_UNKNOWN
                    && simState != SimCardManager.SIM_STATE_NOT_READY) {
                EventManager.getInstance().handOutEventInfo(Event.EVENT_SIMCARD_ERROR);
            }
        } else if (messageEvent.getCode() == Event.EventCode.EVENT_SWITCH_USER) {
            if (messageEvent.getData() == SWITCH_USER_PREPARE) {
                onSwitchUserPrepare();
            } else if (messageEvent.getData() == SWITCH_USER_START) {
                onSwitchUserStart();
            } else if (messageEvent.getData() == SWITCH_USER_COMPLETE) {
                onSwitchUserComplete();
            }
        } else if (messageEvent.getCode() == Event.EventCode.EVENT_CHECK_SDCARD_AND_SIMCARD) {
            if (messageEvent.getData() == CHECK_START) {
                checkSdcardAndSimcardStatus();
            }
        } else if (messageEvent.getCode() == Event.EventCode.EVENT_SECOND_CAMERIA) {
            GlobalLogic.getInstance().setSecondCameraStatus((UIElementStatusEnum.SecondCameraStatusType) messageEvent.getData());
        } else if (messageEvent.getCode() == Event.EventCode.EVENT_REFRESH_USER_NAME) {
            refreshUserInfo();
        }
        osdView.invalidateView();
    }

    private void handleSdCardDialog(UIElementStatusEnum.SDcardStatusType sDcardStatus) {
        switch (sDcardStatus) {
            case SDCARD_MOUNTED:
                DialogManager.getIntance().setSdcardInserted(true);
                break;
            case SDCARD_REMOVED:
                DialogManager.getIntance().setSdcardPulledOut(true);
                break;
            case SDCARD_INIT_SUCCESS:
                DialogManager.getIntance().setSdcardInitSuccess(true);
                break;
            default:
        }
    }

    private final PhoneStateListener mListener = new PhoneStateListener() {
        @Override
        public void onSignalStrengthsChanged(SignalStrength sStrength) {
            super.onSignalStrengthsChanged(sStrength);
            try {
                Method method;
                int strength = 0;
                int simState = SimCardManager.getInstant().getSimState();
                if (simState == TelephonyManager.SIM_STATE_ABSENT
                        || simState == SimCardManager.SIM_STATE_NOT_READY) {
                    return;
                }
                int networkType = mTelephonyManager.getNetworkType();
                // if networkType is LTE, using custom dbm to distinguish signal strength level
                UIElementStatusEnum.LTEStatusType lteLevel = LTE_NONE;
                if (networkType == TelephonyManager.NETWORK_TYPE_LTE) {
                    method = sStrength.getClass().getDeclaredMethod("getLteDbm");
                    int lteRsrp = (int) method.invoke(sStrength);
                    lteLevel = LTE_SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
                    if (lteRsrp > -44) {
                        lteLevel = LTE_SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
                    } else if (lteRsrp >= -97) {
                        lteLevel = LTE_SIGNAL_STRENGTH_GREAT;
                    } else if (lteRsrp >= -105) {
                        lteLevel = LTE_SIGNAL_STRENGTH_GOOD;
                    } else if (lteRsrp >= -113) {
                        lteLevel = LTE_SIGNAL_STRENGTH_MODERATE;
                    } else if (lteRsrp >= -120) {
                        lteLevel = LTE_SIGNAL_STRENGTH_POOR;
                    } else if (lteRsrp >= -140) {
                        lteLevel = LTE_SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
                    }
                }
                Logg.i(TAG, "SignalStrengthLevel: " + Integer.toString(strength) + ",lteStatusType=" + lteLevel);
                if (lteLevel == GlobalLogic.getInstance().getLTEStatus()) {
                    return;
                }
                GlobalLogic.getInstance().setLTEStatus(lteLevel);
                osdView.invalidateView();
            } catch (Exception ignored) {
                Logg.e(TAG, "Exception: " + ignored.toString());
            }
        }
    };

    private boolean getMicphoneEnable() {
        ContentResolver contentResolver = getActivity().getContentResolver();
        int on = 1;
        try {
            on = Settings.Global.getInt(contentResolver,
                    AskeySettings.Global.RECSET_VOICE_RECORD);
        } catch (Settings.SettingNotFoundException e) {
            Logg.e(TAG, "SettingNotFoundException MIC");
            Settings.Global.putInt(contentResolver,
                    AskeySettings.Global.RECSET_VOICE_RECORD, 1);
        }
        return (on != 0);
    }

    private boolean getRecordStamp() {
        ContentResolver contentResolver = getActivity().getContentResolver();
        int on = 1;
        try {
            on = Settings.Global.getInt(contentResolver,
                    AskeySettings.Global.RECSET_INFO_STAMP);
        } catch (Settings.SettingNotFoundException e) {
            Logg.e(TAG, "SettingNotFoundException RECSET_INFO_STAMP");
            Settings.Global.putInt(contentResolver,
                    AskeySettings.Global.RECSET_INFO_STAMP, 1);
        }
        return (on != 0);
    }

    private void startVideoRecord(String reason) throws Exception {
        if (mMainCam == null) {
            Logg.d(TAG, "dashcam unavailable");
            return;
        }
        if (!canRecord) {
            Logg.d(TAG, "can not record at this time.");
            return;
        }
        int sdcardStatus = FileManager.getInstance(getContext()).checkSdcardAvailable();
        Logg.d(TAG, "startVideoRecord sdcardStatus=" + sdcardStatus);

        if (!SDcardHelper.isSDCardAvailable(sdcardStatus)) {
            RecordHelper.setRecordingPrecondition(SDCARD_UNAVAILABLE);
            if (sdcardStatus == SDCARD_NOT_EXIST) {
                onMessageEvent(new MessageEvent<>(Event.EventCode.EVENT_SDCARD, SDCARD_REMOVED));
            } else {
                onMessageEvent(new MessageEvent<>(Event.EventCode.EVENT_SDCARD, SDCARD_INIT_FAIL));
            }
            throw new RuntimeException("sd card unavailable");
        } else {
            RecordHelper.setRecordingPrecondition(SDCARD_AVAILABLE);
            onMessageEvent(new MessageEvent<>(Event.EventCode.EVENT_SDCARD, SDCARD_INIT_SUCCESS));
        }
        // TODO: TBD: need to check?
        // Or don't check here Let DashCam decide it can start or not
        // if (mMainCam.isBusy()) {
        //     throw new RuntimeException("dashcam is busy");
        // }
        if (RecordHelper.isRecodingEnable()) {

            mMainCam.startVideoRecord(reason);

            ContentResolver contentResolver = getActivity().getContentResolver();
            contentResolver.registerContentObserver(
                    Settings.Global.getUriFor(AskeySettings.Global.RECSET_VOICE_RECORD),
                    false,
                    mMicphoneSettingsObserver);
        } else {
            throw new RuntimeException(RecordHelper.getErrorString());
        }

        if (mExtCam != null) {
            mExtCam.startVideoRecord(reason);
        }
    }

    private void stopVideoRecord(String reason) {
        if (mExtCam != null) {
            mExtCam.stopVideoRecord(reason);
        }
        if (mMainCam != null) {
            mMainCam.stopVideoRecord(reason);
        }
        ContentResolver contentResolver = getActivity().getContentResolver();
        contentResolver.unregisterContentObserver(mMicphoneSettingsObserver);
    }

    /**
     * This API is for close all resource on time as possible
     * Before the process become background
     */
    public void terminateVideoRecord() {
        Logg.v(TAG, "terminateVideoRecord");
        long startMillis = System.currentTimeMillis();
        // Use DashCam.terminate() and make the method synchronized - As Possible
        // to close all resource (files) in time
        /* Terminate DashCam */
        if (mExtCam != null) {
            mExtCam.terminate();
        }
        if (mMainCam != null) {
            mMainCam.terminate();
        }
        ContentResolver contentResolver = getActivity().getContentResolver();
        contentResolver.unregisterContentObserver(mMicphoneSettingsObserver);
        Logg.v(TAG, "terminateVideoRecord: elapsed = " +
                (System.currentTimeMillis() - startMillis) + " ms");
    }

    private ContentObserver mMicphoneSettingsObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            if (mMainCam != null && mExecutor != null) {
                mExecutor.submit(() -> {
                    if (getMicphoneEnable()) {
                        mMainCam.demute();
                        EventManager.getInstance().handOutEventInfo(106); //Audio recording ON
                    } else {
                        mMainCam.mute();
                        EventManager.getInstance().handOutEventInfo(107); //Audio recording OFF
                    }
                });
            }
            super.onChange(selfChange);
        }
    };

    private void dealFlg(int flg) {
        switch (flg) {
            case 1://始期日以前
                beforeContractDayStart();
                break;
            case 0://対象の証券無し
                MainApp.sendStatUpNotify();
                inContractDay();
                Log.d(TAG, "対象の証券無し  flag=0 sendStatUpNotify~~~");
                break;
            case 2://証券期間中 do nothing
                inContractDay();
                break;
            case 3://"満期日+14日"以降
                afterContractDayEnd();
                Log.d(TAG, "満期日+14日\"以降  flag=3 sendStatUpNotify~~~");
                break;
        }
    }

    public void inContractDay() {
        Log.d(TAG, "inContractDay");
        canRecord = true;
        GlobalLogic.getInstance().setECallAllow(true);
        getActivity().runOnUiThread(() -> {
            checkSdcardAndSimcardStatus();
            try {
                startVideoRecord("get insurance");
            } catch (Exception e) {
                e.printStackTrace();
                Logg.e(TAG, "startVideoRecord when get insurance failed");
            }
        });
    }

    /**
     * 合约开始日之前，停止界面更新，停止录像
     */
    public void beforeContractDayStart() {
        Log.d(TAG, "beforeContractDayStart");
        getActivity().runOnUiThread(() -> {
            isKeyNotValid = true;
            GlobalLogic.getInstance().setECallAllow(false);
            tvContent.setVisibility(View.VISIBLE);
            osdView.setVisibility(View.GONE);
            tvContent.setText(getString(R.string.before_contract_day_start));
            timeFinishShow.start();
        });
    }

    private CountDownTimer timeFinishShow = new CountDownTimer(60000, 1000) {

        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            canRecord = true;
            isKeyNotValid = false;
            tvContent.setVisibility(View.GONE);
            osdView.setVisibility(View.VISIBLE);
            try {
                startVideoRecord("get insurance");
            } catch (Exception e) {
                Logg.e(TAG, "startVideoRecord when get insurance failed");
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

    public void afterContractDayEnd() {
        Log.d(TAG, "afterContractDayEnd");
        getActivity().runOnUiThread(() -> {
            timeFinishApp.start();
            isKeyNotValid = true;
            canRecord = false;
            GlobalLogic.getInstance().setECallAllow(false);
            tvContent.setVisibility(View.VISIBLE);
            osdView.setVisibility(View.GONE);
            tvContent.setText(getString(R.string.after_contract_day_stop));
            MainApp.sendStatUpNotify();
        });
    }

    private void onSwitchUserPrepare() {
        GlobalLogic.getInstance().setStartSwitchUser(true);
        DialogManager.getIntance().dismissDialog();
        DialogManager.getIntance().reset();
    }

    private void onSwitchUserStart() {
        Logg.d(TAG, "onSwitchUserStart closeRecording");
        RecordHelper.setRecordingPrecondition(SWITCH_USER_STARTED);
        stopVideoRecord("switch user start");
    }

    private void onSwitchUserComplete() {
        GlobalLogic.getInstance().setStartSwitchUser(false);
        try {
            RecordHelper.setRecordingPrecondition(SWITCH_USER_COMPLETED);
            startVideoRecord("switch user complete");
        } catch (Exception e) {
            Logg.e(TAG, "start video record fail with exception: " + e.getMessage());
        }
        refreshUserInfo();
        int userId = GlobalLogic.getInstance().getInt(AskeySettings.Global.SYSSET_USER_ID);
        Communication.getInstance().changeUserID(userId);
        if(GlobalLogic.getInstance().isFirstUserChange()) {
            EventManager.getInstance().handOutEventInfo(136);
        }else{
            EventManager.getInstance().handOutEventInfo(127);
        }
    }

    private void checkSdcardAndSimcardStatus() {
        SDcardHelper.checkSdcardState(getContext());
        int simState = SimCardManager.getInstant().getSimState();
        if (simState != TelephonyManager.SIM_STATE_ABSENT
                && simState != TelephonyManager.SIM_STATE_READY
                && simState != TelephonyManager.SIM_STATE_UNKNOWN
                && simState != SimCardManager.SIM_STATE_NOT_READY) {
            EventManager.getInstance().handOutEventInfo(Event.EVENT_SIMCARD_ERROR);
        }
    }

    public void refreshUserInfo() {
        String userName = GlobalLogic.getInstance().getString(AskeySettings.Global.SYSSET_USER_NAME, "");
        if (!TextUtils.isEmpty(userName)) {
            GlobalLogic.getInstance().setUserInfo(userName);
            osdView.invalidateView();
        }
    }

    public boolean isKeyNotValid() {
        return isKeyNotValid;
    }
}
