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
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.askey.dvr.cdr7010.dashcam.R;
import com.askey.dvr.cdr7010.dashcam.activity.DialogActivity;
import com.askey.dvr.cdr7010.dashcam.activity.NoticeActivity;
import com.askey.dvr.cdr7010.dashcam.core.DashCam;
import com.askey.dvr.cdr7010.dashcam.core.RecordConfig;
import com.askey.dvr.cdr7010.dashcam.domain.Event;
import com.askey.dvr.cdr7010.dashcam.domain.MessageEvent;
import com.askey.dvr.cdr7010.dashcam.logic.DialogLogic;
import com.askey.dvr.cdr7010.dashcam.logic.GlobalLogic;
import com.askey.dvr.cdr7010.dashcam.service.DialogManager;
import com.askey.dvr.cdr7010.dashcam.service.EventManager;
import com.askey.dvr.cdr7010.dashcam.service.FileManager;
import com.askey.dvr.cdr7010.dashcam.service.GPSStatusManager;
import com.askey.dvr.cdr7010.dashcam.service.LedMananger;
import com.askey.dvr.cdr7010.dashcam.service.SimCardManager;
import com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum;
import com.askey.dvr.cdr7010.dashcam.util.Const;
import com.askey.dvr.cdr7010.dashcam.util.EventUtil;
import com.askey.dvr.cdr7010.dashcam.util.Logg;
import com.askey.dvr.cdr7010.dashcam.util.SPUtils;
import com.askey.dvr.cdr7010.dashcam.widget.OSDView;
import com.askey.platform.AskeySettings;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.LTEStatusType.LTE_SIGNAL_STRENGTH_GOOD;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.LTEStatusType.LTE_SIGNAL_STRENGTH_GREAT;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.LTEStatusType.LTE_SIGNAL_STRENGTH_MODERATE;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.LTEStatusType.LTE_SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.LTEStatusType.LTE_SIGNAL_STRENGTH_POOR;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.MICStatusType.MIC_OFF;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.MICStatusType.MIC_ON;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.RecordingStatusType.RECORDING_CONTINUOUS;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.RecordingStatusType.RECORDING_EVENT;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_INIT_FAIL;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_INIT_SUCCESS;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_UNMOUNTED;


public class CameraRecordFragment extends Fragment {
    private static final String TAG = CameraRecordFragment.class.getSimpleName();
    private DashCam mMainCam;
    private OSDView osdView;
    private Handler mHandler;
    private TelephonyManager mTelephonyManager;
    private UIElementStatusEnum.LTEStatusType lteLevel;
    private ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private boolean isStartRecord;

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

    private BroadcastReceiver mSdAvailableListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("action_sdcard_status")) {
                final String ex = intent.getStringExtra("data");
                if ("show_sdcard_init_success".equals(ex)) {
                    Logg.d(TAG, "SD Card available");
                    try {
                        startVideoRecord("SD become available");
                    } catch (Exception e) {
                        Logg.e(TAG, "start video record fail with exception: " + e.getMessage());
                    }
                }
            }
        }
    };

    private BroadcastReceiver mSdBadRemovalListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_MEDIA_BAD_REMOVAL)) {
                Logg.d(TAG, "SD Card MEDIA_BAD_REMOVAL");
                stopVideoRecord("SD MEDIA_BAD_REMOVAL");
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
            if (intent.getAction().equals(Intent.ACTION_SHUTDOWN)) {
                Logg.d(TAG, "BroadcastReceiver: Intent.ACTION_SHUTDOWN");
                stopVideoRecord("Intent.ACTION_SHUTDOWN");
            }
        }
    };

    DashCam.StateCallback mDashCallback = new DashCam.StateCallback() {
        @Override
        public void onStarted() {
            Logg.d(TAG, "DashState: onStarted");
            if (mExecutor != null) {
                mExecutor.submit(new Runnable() {
                    @Override
                    public void run() {
                        if (!getMicphoneEnable()) {
                            mMainCam.mute();
                        }
                    }
                });
            }
            EventUtil.sendEvent(new MessageEvent<>(Event.EventCode.EVENT_RECORDING,
                    RECORDING_CONTINUOUS));
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    EventManager.getInstance().handOutEventInfo(104);
                }
            });

        }

        @Override
        public void onStoped() {
            Logg.d(TAG, "DashState: onStoped");
            EventUtil.sendEvent(new MessageEvent<>(Event.EventCode.EVENT_RECORDING,
                    UIElementStatusEnum.RecordingStatusType.RECORDING_STOP));

        }

        @Override
        public void onError() {
            Logg.d(TAG, "DashState: onError");
            EventUtil.sendEvent(new MessageEvent<>(Event.EventCode.EVENT_RECORDING,
                    UIElementStatusEnum.RecordingStatusType.RECORDING_ERROR));
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    EventManager.getInstance().handOutEventInfo(114);
                }
            });

        }

        @Override
        public void onEventStateChanged(final boolean on) {
            Logg.d(TAG, "DashState: onEventStateChanged " + on);
            EventUtil.sendEvent(new MessageEvent<>(Event.EventCode.EVENT_RECORDING,
                    on ? RECORDING_EVENT : RECORDING_CONTINUOUS));
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (on) { // event recording
                        EventManager.getInstance().handOutEventInfo(105); // Continuous Recording end
                        EventManager.getInstance().handOutEventInfo(123); // Event Recording start
                    } else { // Continuous recording
                        EventManager.getInstance().handOutEventInfo(124); // Event Recording end
                        EventManager.getInstance().handOutEventInfo(104); // Continuous Recording start
                    }
                }
            });

        }
    };

    public static CameraRecordFragment newInstance() {
        return new CameraRecordFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logg.d(TAG, "onCreate");
        isStartRecord= (boolean) SPUtils.get(getActivity(), Const.IS_START_RECORD,true);
        mTelephonyManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        mHandler = new Handler(Looper.getMainLooper());
        EventManager.getInstance().loadXML("zh");
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
        if (isStartRecord) {
            requestVideoPermissions();
            requestGPSPermissions();
            requestSIMCardPermissions();
            GPSStatusManager.getInstance().recordLocation(true);
            osdView.init(1000);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (isStartRecord) {
            checkSdCardExist();
            IntentFilter simCardFilter = new IntentFilter();
            simCardFilter.addAction("android.intent.action.PHONE_STATE");
            simCardFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
            getActivity().registerReceiver(simCardReceiver, simCardFilter);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Logg.d(TAG, "onResume");
        if (isStartRecord) {
            onMessageEvent(new MessageEvent(Event.EventCode.EVENT_MIC));
            LedMananger.getInstance().setLedMicStatus(getMicphoneEnable());
            mTelephonyManager.listen(mListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
            filter.addDataScheme("file");
            getActivity().registerReceiver(mSdBadRemovalListener, filter);

            IntentFilter filter2 = new IntentFilter();
            filter2.addAction("action_sdcard_status");
            getActivity().registerReceiver(mSdAvailableListener, filter2);

            getActivity().registerReceiver(mShutdownReceiver, new IntentFilter(Intent.ACTION_SHUTDOWN));

            final boolean stamp = getRecordStamp();
            final boolean audio = getMicphoneEnable();
            RecordConfig mainConfig = RecordConfig.builder()
                    .cameraId(0)
                    .videoWidth(1920)
                    .videoHeight(1080)
                    .videoStampEnable(stamp)
                    .audioRecordEnable(audio)
                    .build();
            mMainCam = new DashCam(getActivity(), mainConfig, mDashCallback);

            try {
                startVideoRecord("Fragment onResume");
            } catch (Exception e) {
                Logg.e(TAG, "onResume: start video record fail with exception: " + e.getMessage());
            }
        }
    }

    @Override
    public void onPause() {
        Logg.d(TAG, "onPause");
        if (isStartRecord) {
            stopVideoRecord("Fragment onPause");
            getActivity().unregisterReceiver(mSdAvailableListener);
            getActivity().unregisterReceiver(mSdBadRemovalListener);
            getActivity().unregisterReceiver(mShutdownReceiver);
            mTelephonyManager.listen(mListener, PhoneStateListener.LISTEN_NONE);
            LedMananger.getInstance().setLedMicStatus(false);
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Logg.d(TAG, "onDestroy");
        EventUtil.unregister(this);
        if (isStartRecord) {
            osdView.unInit();
            GPSStatusManager.getInstance().recordLocation(false);
            getActivity().unregisterReceiver(simCardReceiver);
        }
        super.onDestroy();
    }

    @Override
    public void onStop() {
        Logg.d(TAG, "onStop");
        super.onStop();
    }

    private void requestVideoPermissions() {
        if (!hasPermissionsGranted(VIDEO_PERMISSIONS)) {
            requestPermissions(VIDEO_PERMISSIONS, REQUEST_VIDEO_PERMISSIONS);
        }
    }

    private void requestGPSPermissions() {
        if (!hasPermissionsGranted(GPS_PERMISSIONS)) {
            requestPermissions(GPS_PERMISSIONS, REQUEST_GPS_PERMISSIONS);
        }
    }

    private void requestSIMCardPermissions() {
        if (!hasPermissionsGranted(SIM_PERMISSIONS)) {
            requestPermissions(SIM_PERMISSIONS, REQUEST_SIMCARD_PERMISSIONS);
        }
    }

    private boolean hasPermissionsGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(getActivity(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent messageEvent) {
        Logg.d(TAG, "onMessageEvent messageEvent=" + messageEvent.getData());
        if (messageEvent != null) {
            handleMessageEvent(messageEvent);
        }
    }

    private void handleMessageEvent(MessageEvent messageEvent) {
        if (messageEvent.getCode() == Event.EventCode.EVENT_RECORDING) {
            GlobalLogic.getInstance().setRecordingStatus((UIElementStatusEnum.RecordingStatusType) messageEvent.getData());
            if (messageEvent.getData() == RECORDING_EVENT) {
                osdView.startRecordingCountDown();
            }
            if(messageEvent.getData() ==  RECORDING_CONTINUOUS){
                DialogManager.getIntance().setStartRecording(true);
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
            SimCardManager.getInstant().setSimState(SimCardManager.getInstant().getSimState());
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

    private void checkSdCardExist() {
        String status = Environment.getExternalStorageState();
        if (status.equalsIgnoreCase(Environment.MEDIA_REMOVED)
                || status.equalsIgnoreCase(Environment.MEDIA_BAD_REMOVAL)
                || status.equalsIgnoreCase(Environment.MEDIA_UNMOUNTED)) {
            EventManager.getInstance().handOutEventInfo(110);
        }
    }

    private final PhoneStateListener mListener = new PhoneStateListener() {
        @Override
        public void onSignalStrengthsChanged(SignalStrength sStrength) {
            super.onSignalStrengthsChanged(sStrength);
            try {
                Method method;
                int strength = 0;
                int networkType = mTelephonyManager.getNetworkType();
                // if networkType is LTE, using custom dbm to distinguish signal strength level
                lteLevel = LTE_SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
                if (networkType == TelephonyManager.NETWORK_TYPE_LTE) {
                    method = sStrength.getClass().getDeclaredMethod("getLteDbm");
                    int lteRsrp = (int) method.invoke(sStrength);
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
                    GlobalLogic.getInstance().setLTEStatus(lteLevel);
                    osdView.invalidateView();
                }
                Logg.i(TAG, "SignalStrengthLevel: " + Integer.toString(strength) + ",lteStatusType=" + lteLevel);
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
        boolean sdcardAvailable = FileManager.getInstance(getContext()).isSdcardAvailable();
        onMessageEvent(new MessageEvent(Event.EventCode.EVENT_SDCARD,
                sdcardAvailable ? SDCARD_INIT_SUCCESS : Environment.getExternalStorageState().
                        equalsIgnoreCase(Environment.MEDIA_REMOVED) ? SDCARD_UNMOUNTED : SDCARD_INIT_FAIL));
        if (!sdcardAvailable) {
            throw new RuntimeException("sd card unavailable");
        }
        if (mMainCam == null) {
            throw new RuntimeException("camera unavailable");
        }

        mMainCam.startVideoRecord(reason);
        if (!getMicphoneEnable()) {
            mMainCam.mute();
        }

        ContentResolver contentResolver = getActivity().getContentResolver();
        contentResolver.registerContentObserver(
                Settings.Global.getUriFor(AskeySettings.Global.RECSET_VOICE_RECORD),
                false,
                mMicphoneSettingsObserver);
    }

    private void stopVideoRecord(String reason) {
        if (mMainCam != null) {
            mMainCam.stopVideoRecord(reason);
        }
        ContentResolver contentResolver = getActivity().getContentResolver();
        contentResolver.unregisterContentObserver(mMicphoneSettingsObserver);
    }

    private ContentObserver mMicphoneSettingsObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            if (mMainCam != null && mExecutor != null) {
                mExecutor.submit(new Runnable() {
                    @Override
                    public void run() {
                        if (getMicphoneEnable()) {
                            mMainCam.demute();
                            EventManager.getInstance().handOutEventInfo(106); //Audio recording ON
                        } else {
                            mMainCam.mute();
                            EventManager.getInstance().handOutEventInfo(107); //Audio recording OFF
                        }
                    }
                });
            }
            super.onChange(selfChange);
        }
    };
}
