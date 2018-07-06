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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.location.Location;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
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
import com.askey.dvr.cdr7010.dashcam.core.DashCam;
import com.askey.dvr.cdr7010.dashcam.core.RecordConfig;
import com.askey.dvr.cdr7010.dashcam.core.recorder.ExifHelper;
import com.askey.dvr.cdr7010.dashcam.domain.Event;
import com.askey.dvr.cdr7010.dashcam.domain.MessageEvent;
import com.askey.dvr.cdr7010.dashcam.exception.SDCardUnavailableException;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.jvckenwood.JvcEventSending;
import com.askey.dvr.cdr7010.dashcam.logic.GlobalLogic;
import com.askey.dvr.cdr7010.dashcam.service.DialogManager;
import com.askey.dvr.cdr7010.dashcam.service.EventManager;
import com.askey.dvr.cdr7010.dashcam.service.FileManager;
import com.askey.dvr.cdr7010.dashcam.service.GPSStatusManager;
import com.askey.dvr.cdr7010.dashcam.service.LedMananger;
import com.askey.dvr.cdr7010.dashcam.service.SimCardManager;
import com.askey.dvr.cdr7010.dashcam.service.ThermalController;
import com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum;
import com.askey.dvr.cdr7010.dashcam.util.AppUtils;
import com.askey.dvr.cdr7010.dashcam.util.Const;
import com.askey.dvr.cdr7010.dashcam.util.EventUtil;
import com.askey.dvr.cdr7010.dashcam.util.FileUtils;
import com.askey.dvr.cdr7010.dashcam.util.Logg;
import com.askey.dvr.cdr7010.dashcam.util.SDcardHelper;
import com.askey.dvr.cdr7010.dashcam.widget.OSDView;
import com.askey.platform.AskeyIntent;
import com.askey.platform.AskeySettings;

import net.sf.marineapi.nmea.util.Position;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
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
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.RecordingStatusType.RECORDING_CONTINUOUS;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.RecordingStatusType.RECORDING_EVENT;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_INIT_FAIL;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_INIT_SUCCESS;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_REMOVED;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_UNMOUNTED;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SdCardAndSimCardCheckStatus.CHECK_START;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SwitchUserEvent.SWITCH_USER_COMPLETE;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SwitchUserEvent.SWITCH_USER_PREPARE;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SwitchUserEvent.SWITCH_USER_START;

public class CameraRecordFragment extends Fragment {
    private static final String TAG = CameraRecordFragment.class.getSimpleName();
    private static final String ACTIVITY_CLASSNAME = "com.askey.dvr.cdr7010.dashcam.ui.MainActivity";
    private DashCam mMainCam;
    private OSDView osdView;
    private TextView tvContent;
    private Handler mHandler;
    private ThermalController thermalController;
    private TelephonyManager mTelephonyManager;
    private int mRecordingFlags;
    private ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private boolean hasStopped;
    private boolean isEventRecording;
    private boolean isChargeDisconnect = false;//电源状态，默认是连接的

    private static final int FLAG_SDCARD_AVAILABLE = 1;
    private static final int FLAG_SDCARD_SPACE_NOT_FULL = 1 << 1;
    private static final int FLAG_BATTERY_CHARGING = 1 << 2;
    private static final int FLAG_LOW_TEMPERATURE = 1 << 3;
    private static final int FLAG_SWITCH_USER = 1 << 4;

    private static final String ACTION_SDCARD_STATUS = "action_sdcard_status";
    private static final String SDCARD_FULL_LIMIT = "show_sdcard_full_limit";
    private static final String SDCARD_FULL_LIMIT_EXIT = "show_unreach_sdcard_full_limit";
    private static final String ACTION_SDCARD_LIMT = "com.askey.dvr.cdr7010.dashcam.limit";

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
                        mRecordingFlags |= FLAG_SDCARD_AVAILABLE;
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
                    mRecordingFlags &= (~FLAG_SDCARD_SPACE_NOT_FULL);
                    stopVideoRecord("SDCARD_FULL_LIMIT");
                } else if (SDCARD_FULL_LIMIT_EXIT.equals(ex)) {
                    Logg.d(TAG, "SDCARD_FULL_LIMIT_EXIT");
                    try {
                        mRecordingFlags |= FLAG_SDCARD_SPACE_NOT_FULL;
                        startVideoRecord("SDCARD_FULL_LIMIT_EXIT");
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
            Logg.d(TAG, "mSdBadRemovalListener...action==" + intent.getAction());
            if (Intent.ACTION_MEDIA_BAD_REMOVAL.equals(intent.getAction())) {
                Logg.d(TAG, "SD Card MEDIA_BAD_REMOVAL");
                mRecordingFlags &= (~FLAG_SDCARD_AVAILABLE);
                stopVideoRecord("SD MEDIA_BAD_REMOVAL");
            } else if (Intent.ACTION_MEDIA_REMOVED.equals(intent.getAction())) {
                //存储卡异常情况下拔卡
                EventManager.getInstance().handOutEventInfo(110);
                EventUtil.sendEvent(new MessageEvent<>(Event.EventCode.EVENT_SDCARD, SDCARD_REMOVED));
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
            if (Intent.ACTION_SHUTDOWN.equals(intent.getAction())) {
                Logg.d(TAG, "BroadcastReceiver: Intent.ACTION_SHUTDOWN");
                stopVideoRecord("Intent.ACTION_SHUTDOWN");
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
                        Log.i(TAG, "battery status is charging");
                        if (isChargeDisconnect) {
                            try {
                                mRecordingFlags |= FLAG_BATTERY_CHARGING;
                                startVideoRecord("Intent.BATTERY_STATUS_CHARGING");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        isChargeDisconnect = false;
                        break;
                    case BatteryManager.BATTERY_STATUS_DISCHARGING:
                        isChargeDisconnect = true;
                        Log.i(TAG, "battery status is discharging");
                        mMainCam.takeAPicture((data, width, height, timeStamp) -> handler.post(() -> {
                            Bitmap bmp = null;
                            BufferedOutputStream bos = null;
                            try {
                                String filePathForPicture = FileManager.getInstance(getActivity()).getFilePathForPicture(timeStamp);
                                ByteBuffer buf = ByteBuffer.wrap(data);
                                bos = new BufferedOutputStream(new FileOutputStream(filePathForPicture));
                                bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                                bmp.copyPixelsFromBuffer(buf);
                                bmp = convertBmp(bmp);
                                bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                                Location currentLocation = GPSStatusManager.getInstance().getCurrentLocation();
                                Position position = null;
                                if (currentLocation != null) {
                                    Logg.d(TAG, "currentLocation!=null,getLatitude==" + currentLocation.getLatitude() + ",getLongitude==" + currentLocation.getLongitude());
                                    position = new Position(currentLocation.getLatitude(), currentLocation.getLongitude());
                                }
                                Logg.d(TAG, "timeStamp==" + timeStamp);
                                ExifHelper.build(filePathForPicture, timeStamp, position);
                                // TODO: 2018/6/28 上傳文件
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                if (bmp != null) {
                                    bmp.recycle();
                                }
                                if (bos != null) {
                                    try {
                                        bos.flush();
                                        bos.close();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                handler.sendEmptyMessage(0);
                            }
                        }));
                        break;
                }
            }
        }
    };

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 0) {
                if (isChargeDisconnect) {
                    mRecordingFlags &= (~FLAG_BATTERY_CHARGING);
                    stopVideoRecord("Intent.BATTERY_STATUS_DISCHARGING");
                    //关机
                    Logg.d(TAG, "SHUT DOWN...");
                    Intent intentShutDown = new Intent();
                    intentShutDown.setAction(AskeyIntent.ACTION_DVR_SHUTDOWN);
                    getContext().sendBroadcast(intentShutDown);
                }
            }
            return true;
        }
    });

    //by beck 将图片水平镜像翻转
    private Bitmap convertBmp(Bitmap bmp) {
        int w = bmp.getWidth();
        int h = bmp.getHeight();
        Bitmap convertBmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);// 创建一个新的和SRC长度宽度一样的位图
        Canvas cv = new Canvas(convertBmp);
        Matrix matrix = new Matrix();
//        matrix.postScale(1, -1); //镜像垂直翻转
        matrix.postScale(-1, 1); //镜像水平翻转
        matrix.postRotate(-180); //旋转-180度
        Bitmap newBmp = Bitmap.createBitmap(bmp, 0, 0, w, h, matrix, true);
        cv.drawBitmap(newBmp, new Rect(0, 0, newBmp.getWidth(), newBmp.getHeight()), new Rect(0, 0, w, h), null);
        newBmp.recycle();
        bmp.recycle();
        return convertBmp;
    }

    DashCam.StateCallback mDashCallback = new DashCam.StateCallback() {
        @Override
        public void onStarted() {
            Logg.d(TAG, "DashState: onStarted");
            EventUtil.sendEvent(new MessageEvent<>(Event.EventCode.EVENT_RECORDING,
                    RECORDING_CONTINUOUS));
            isEventRecording = false;
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
            if (!isEventRecording) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        EventManager.getInstance().handOutEventInfo(105);
                    }
                });
            } else {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        EventManager.getInstance().handOutEventInfo(124);
                    }
                });
            }

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
            // 注意：禁止在这里进行耗时操作
            Logg.d(TAG, "DashState: onEventStateChanged " + on);
            EventUtil.sendEvent(new MessageEvent<>(Event.EventCode.EVENT_RECORDING,
                    on ? RECORDING_EVENT : RECORDING_CONTINUOUS));
            final boolean event = isEventRecording;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (on) { // event recording
                        EventManager.getInstance().handOutEventInfo(105); // Continuous Recording end
                        EventManager.getInstance().handOutEventInfo(123); // Event Recording start
                    } else { // Continuous recording
                        if (event) {
                            EventManager.getInstance().handOutEventInfo(124); // Event Recording end
                        }
                        EventManager.getInstance().handOutEventInfo(104); // Continuous Recording start
                    }
                }
            });
            isEventRecording = on;
        }

        @Override
        public void onEventCompleted(int eventId, long timestamp, List<String> pictures, String video) {
            Logg.d(TAG, "DashState: onEventCompleted ");
            ArrayList<Integer> results = new ArrayList<>(Arrays.asList(1, 0, 1, 1, 1, 0, 0, 0));
            ArrayList<String> files = new ArrayList<>();
            files.add(video);
            files.addAll(pictures);
            JvcEventSending.recordResponse(eventId, results, files);
        }
    };
    private ThermalController.ThermalListener thermalListener = new ThermalController.ThermalListener() {
        @Override
        public void startRecording() {
            if (AppUtils.isActivityTop(getActivity(), ACTIVITY_CLASSNAME)) {
                Logg.d(TAG, "ThermalController startRecording");
                try {
                    mRecordingFlags |= FLAG_LOW_TEMPERATURE;
                    startVideoRecord("cpu low temperature");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

        @Override
        public void closeRecording() {
            if (AppUtils.isActivityTop(getActivity(), ACTIVITY_CLASSNAME)) {
                if ((mRecordingFlags & FLAG_SDCARD_SPACE_NOT_FULL) > 0
                        && (mRecordingFlags & FLAG_BATTERY_CHARGING) > 0
                        && (mRecordingFlags & FLAG_SDCARD_AVAILABLE) > 0
                        && (mRecordingFlags & FLAG_LOW_TEMPERATURE) > 0) {
                    Logg.d(TAG, "ThermalController closeRecording");
                    stopVideoRecord("CPU reach high temperature");
                    mRecordingFlags &= (~FLAG_LOW_TEMPERATURE);
                }
            }
        }

        @Override
        public void closeLcdPanel() {
            if (AppUtils.isActivityTop(getActivity(), ACTIVITY_CLASSNAME)) {
                Logg.d(TAG, "ThermalController closeLcdPanel");
            }

        }
        @Override
        public void startLcdPanel() {
            if (AppUtils.isActivityTop(getActivity(), ACTIVITY_CLASSNAME)) {
                Logg.d(TAG, "ThermalController startLcdPanel");
            }
        }
    };

    public static CameraRecordFragment newInstance() {
        return new CameraRecordFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logg.d(TAG, "onCreate");
        mTelephonyManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        mHandler = new Handler(Looper.getMainLooper());
        thermalController = new ThermalController(thermalListener);
        mRecordingFlags = FLAG_BATTERY_CHARGING | FLAG_SDCARD_AVAILABLE | FLAG_SDCARD_SPACE_NOT_FULL
                | FLAG_LOW_TEMPERATURE | FLAG_SWITCH_USER;
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
        LedMananger.getInstance().setLedMicStatus(getMicphoneEnable());
        mTelephonyManager.listen(mListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        filter.addDataScheme("file");
        getActivity().registerReceiver(mSdBadRemovalListener, filter);

        IntentFilter filter2 = new IntentFilter();
        filter2.addAction(ACTION_SDCARD_STATUS);
        filter2.addAction(ACTION_SDCARD_LIMT);
        getActivity().registerReceiver(mSdStatusListener, filter2);

        getActivity().registerReceiver(mShutdownReceiver, new IntentFilter(Intent.ACTION_SHUTDOWN));

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        getActivity().registerReceiver(mBatteryStateReceiver, intentFilter);

        final boolean stamp = getRecordStamp();
        final boolean audio = getMicphoneEnable();
        RecordConfig mainConfig = RecordConfig.builder()
                .cameraId(0)
                .videoWidth(1920)
                .videoHeight(1080)
                .videoFPS(27)
                .videoBitRate((int) (9.6 * 1024 * 1024)) // 10Mbps
                .videoStampEnable(stamp)
                .audioRecordEnable(audio)
                .build();
        mMainCam = new DashCam(getActivity(), mainConfig, mDashCallback);

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
        if (!hasStopped) {
            stopVideoRecord("Fragment onPause");
            getActivity().unregisterReceiver(mSdStatusListener);
            getActivity().unregisterReceiver(mSdBadRemovalListener);
            getActivity().unregisterReceiver(mShutdownReceiver);
            getActivity().unregisterReceiver(mBatteryStateReceiver);
            mTelephonyManager.listen(mListener, PhoneStateListener.LISTEN_NONE);
            LedMananger.getInstance().setLedMicStatus(false);
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Logg.d(TAG, "onDestroy");
        if (!hasStopped) {
            EventUtil.unregister(this);
            osdView.unInit();
            thermalController.stopThermalMonitor();
            GPSStatusManager.getInstance().recordLocation(false);
            getActivity().unregisterReceiver(simCardReceiver);
        }
        if (timeFinishShow != null) {
            timeFinishShow.cancel();
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
                int networkType = mTelephonyManager.getNetworkType();
                // if networkType is LTE, using custom dbm to distinguish signal strength level
                UIElementStatusEnum.LTEStatusType lteLevel = LTE_SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
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
        onMessageEvent(new MessageEvent<>(Event.EventCode.EVENT_SDCARD,
                sdcardAvailable ? SDCARD_INIT_SUCCESS : Environment.getExternalStorageState().
                        equalsIgnoreCase(Environment.MEDIA_REMOVED) ? SDCARD_UNMOUNTED : SDCARD_INIT_FAIL));
        if (!sdcardAvailable) {
            mRecordingFlags &= (~FLAG_SDCARD_AVAILABLE);
            throw new RuntimeException("sd card unavailable");
        } else {
            mRecordingFlags |= FLAG_SDCARD_AVAILABLE;
        }

        if (mMainCam == null) {
            throw new RuntimeException("camera unavailable");
        }
        if ((mRecordingFlags & FLAG_SDCARD_SPACE_NOT_FULL) > 0
                && (mRecordingFlags & FLAG_BATTERY_CHARGING) > 0
                && (mRecordingFlags & FLAG_LOW_TEMPERATURE) > 0
                && (mRecordingFlags & FLAG_SWITCH_USER) > 0) {

            mMainCam.startVideoRecord(reason);

            ContentResolver contentResolver = getActivity().getContentResolver();
            contentResolver.registerContentObserver(
                    Settings.Global.getUriFor(AskeySettings.Global.RECSET_VOICE_RECORD),
                    false,
                    mMicphoneSettingsObserver);
        } else {
            throw new RuntimeException("startVideoRecord because: " + reason + " with excption: flags=" + mRecordingFlags);
        }
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

    /**
     * 合约开始日之前，停止界面更新，停止录像
     */
    public void beforeContractDayStart() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvContent.setVisibility(View.VISIBLE);
                osdView.setVisibility(View.GONE);
                tvContent.setText(getString(R.string.before_contract_day_start));
                releaseAll();
                timeFinishShow.start();
            }
        });
    }

    private CountDownTimer timeFinishShow = new CountDownTimer(60000, 1000) {

        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            tvContent.setVisibility(View.GONE);
            osdView.setVisibility(View.VISIBLE);
        }
    };

    private void releaseAll() {
        stopVideoRecord("Fragment onPause");
        GPSStatusManager.getInstance().recordLocation(false);
        getActivity().unregisterReceiver(mSdStatusListener);
        getActivity().unregisterReceiver(mSdBadRemovalListener);
        getActivity().unregisterReceiver(mShutdownReceiver);
        getActivity().unregisterReceiver(mBatteryStateReceiver);
        getActivity().unregisterReceiver(simCardReceiver);
        mTelephonyManager.listen(mListener, PhoneStateListener.LISTEN_NONE);
        LedMananger.getInstance().setLedMicStatus(false);
        EventUtil.unregister(this);
        osdView.unInit();
        hasStopped = true;
    }

    public void afterContractDayEnd() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvContent.setVisibility(View.VISIBLE);
                osdView.setVisibility(View.GONE);
                tvContent.setText(getString(R.string.after_contract_day_stop));
                releaseAll();
            }
        });
    }

    private void onSwitchUserPrepare() {
        GlobalLogic.getInstance().setStartSwitchUser(true);
        DialogManager.getIntance().dismissDialog();
        DialogManager.getIntance().reset();
    }

    private void onSwitchUserStart() {
        Logg.d(TAG, "onSwitchUserStart closeRecording");
        mRecordingFlags &= (~FLAG_SWITCH_USER);
        stopVideoRecord("switch user start");
    }

    private void onSwitchUserComplete() {
        GlobalLogic.getInstance().setStartSwitchUser(false);
        try {
            mRecordingFlags |= FLAG_SWITCH_USER;
            startVideoRecord("switch user complete");
        } catch (Exception e) {
            Logg.e(TAG, "start video record fail with exception: " + e.getMessage());
        }
        refreshUserInfo();
        EventManager.getInstance().handOutEventInfo(127);

    }

    private void checkSdcardAndSimcardStatus() {
        SDcardHelper.handleSdcardAbnormalEvent();
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
}
