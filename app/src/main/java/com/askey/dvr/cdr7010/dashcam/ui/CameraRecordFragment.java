package com.askey.dvr.cdr7010.dashcam.ui;

import android.Manifest;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.askey.dvr.cdr7010.dashcam.R;
import com.askey.dvr.cdr7010.dashcam.core.DashCam;
import com.askey.dvr.cdr7010.dashcam.domain.Event;
import com.askey.dvr.cdr7010.dashcam.domain.MessageEvent;
import com.askey.dvr.cdr7010.dashcam.logic.GlobalLogic;
import com.askey.dvr.cdr7010.dashcam.service.GPSStatusManager;
import com.askey.dvr.cdr7010.dashcam.service.LedMananger;
import com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum;
import com.askey.dvr.cdr7010.dashcam.util.EventUtil;
import com.askey.dvr.cdr7010.dashcam.util.Logg;
import com.askey.dvr.cdr7010.dashcam.widget.OSDView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.lang.reflect.Method;

import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.LTEStatusType.LTE_SIGNAL_STRENGTH_GOOD;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.LTEStatusType.LTE_SIGNAL_STRENGTH_GREAT;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.LTEStatusType.LTE_SIGNAL_STRENGTH_MODERATE;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.LTEStatusType.LTE_SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.LTEStatusType.LTE_SIGNAL_STRENGTH_POOR;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.MICStatusType.MIC_OFF;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.MICStatusType.MIC_ON;


public class CameraRecordFragment extends Fragment {
    private static final String TAG = CameraRecordFragment.class.getSimpleName();
    private DashCam mMainCam;
    private OSDView osdView;
    private TelephonyManager mTelephonyManager;
    private UIElementStatusEnum.LTEStatusType lteLevel;

    private static final int REQUEST_VIDEO_PERMISSIONS = 1001;
    private static final int REQUEST_GPS_PERMISSIONS = 1002;
    private static final String[] VIDEO_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
    };
    private static final String[] GPS_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,

    };

    private BroadcastReceiver mSDMonitor = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) {
                Logg.d(TAG, "SD Card MEDIA_MOUNTED");
                if (mMainCam != null) {
                    try {
                        mMainCam.startVideoRecord();
                    } catch (IOException e) {
                        Logg.e(TAG, "Fail to start video recording with " + e.getMessage());
                    }
                }
            } else if (intent.getAction().equals(Intent.ACTION_MEDIA_BAD_REMOVAL)) {
                Logg.d(TAG, "SD Card MEDIA_BAD_REMOVAL");
                mMainCam.stopVideoRecord();
            }
        }
    };

    DashCam.StateCallback mDashCallback = new DashCam.StateCallback() {
        @Override
        public void onStarted() {
            Logg.d(TAG, "DashState: onStarted");
            LedMananger.getInstance().setLedRecStatus(true,true);
            EventUtil.sendEvent(new MessageEvent<>(Event.EventCode.EVENT_RECORDING,
                    UIElementStatusEnum.RecordingStatusType.RECORDING_CONTINUOUS));
        }

        @Override
        public void onStoped() {
            Logg.d(TAG, "DashState: onStoped");
            LedMananger.getInstance().setLedRecStatus(true,false);
            EventUtil.sendEvent(new MessageEvent<>(Event.EventCode.EVENT_RECORDING,
                    UIElementStatusEnum.RecordingStatusType.RECORDING_STOP));

        }

        @Override
        public void onError() {
            Logg.d(TAG, "DashState: onError");
            LedMananger.getInstance().setLedRecStatus(false,false);
            EventUtil.sendEvent(new MessageEvent<>(Event.EventCode.EVENT_RECORDING,
                    UIElementStatusEnum.RecordingStatusType.RECORDING_ERROR));

        }

        @Override
        public void onEventStateChanged(boolean on) {
            Logg.d(TAG, "DashState: onEventStateChanged " + on);
            EventUtil.sendEvent(new MessageEvent<>(Event.EventCode.EVENT_RECORDING,
                    on ? UIElementStatusEnum.RecordingStatusType.RECORDING_EVENT :
                         UIElementStatusEnum.RecordingStatusType.RECORDING_CONTINUOUS));


        }
    };

    public static CameraRecordFragment newInstance() {
        return new CameraRecordFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logg.d(TAG,"onCreate");
        mTelephonyManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        EventUtil.register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera_record, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState){
        requestVideoPermissions();
        requestGPSPermissions();
        GPSStatusManager.getInstance().recordLocation(true);
        osdView = (OSDView) view.findViewById(R.id.osd_view);
        osdView.init(1000);
        mMainCam = new DashCam(getActivity(), mDashCallback);

    }

    @Override
    public void onResume() {
        super.onResume();
        Logg.d(TAG,"onResume");
        onMessageEvent(new MessageEvent(Event.EventCode.EVENT_MIC));
        mTelephonyManager.listen(mListener,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        filter.addDataScheme("file");
        getActivity().registerReceiver(mSDMonitor, filter);

        try {
            mMainCam.startVideoRecord();
        } catch (IOException e) {
            Logg.e(TAG, "Fail to start video recording with " + e.getMessage());
        }
    }

    @Override
    public void onPause() {
        Logg.d(TAG,"onPause");
        mMainCam.stopVideoRecord();
        getActivity().unregisterReceiver(mSDMonitor);
        mTelephonyManager.listen(mListener, PhoneStateListener.LISTEN_NONE);
        LedMananger.getInstance().setLedMicStatus(false);
        LedMananger.getInstance().setLedRecStatus(true,false);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Logg.d(TAG,"onDestroy");
        osdView.unInit();
        EventUtil.unregister(this);
        GPSStatusManager.getInstance().recordLocation(false);
        super.onDestroy();
    }

    @Override
    public void onStop(){
        Logg.d(TAG,"onStop");
        super.onStop();
    }

    private void requestVideoPermissions() {
        if (!hasPermissionsGranted(VIDEO_PERMISSIONS)) {
            requestPermissions(VIDEO_PERMISSIONS, REQUEST_VIDEO_PERMISSIONS);
        }
    }
    private void requestGPSPermissions(){
        if(!hasPermissionsGranted(GPS_PERMISSIONS)){
            requestPermissions(GPS_PERMISSIONS,REQUEST_GPS_PERMISSIONS);
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
    public void onMessageEvent(MessageEvent messageEvent){
        Logg.d(TAG,"onMessageEvent messageEvent="+messageEvent.getData());
        if (messageEvent != null){
            handleMessageEvent(messageEvent);
        }
    }

    private void handleMessageEvent(MessageEvent messageEvent){
        if (messageEvent.getCode() == Event.EventCode.EVENT_RECORDING) {
            GlobalLogic.getInstance().setRecordingStatus((UIElementStatusEnum.RecordingStatusType)messageEvent.getData());
            osdView.startRecordingCountDown();
        } else if (messageEvent.getCode() == Event.EventCode.EVENT_RECORDING_FILE_LIMIT){
            GlobalLogic.getInstance().setEventRecordingLimitStatus((UIElementStatusEnum.EventRecordingLimitStatusType)messageEvent.getData());
        } else if (messageEvent.getCode() == Event.EventCode.EVENT_PARKING_RECODING_FILE_LIMIT){
            GlobalLogic.getInstance().setParkingRecordingLimitStatus((UIElementStatusEnum.ParkingRecordingLimitStatusType)messageEvent.getData());
        } else if (messageEvent.getCode() == Event.EventCode.EVENT_GPS){
            GlobalLogic.getInstance().setGPSStatus((UIElementStatusEnum.GPSStatusType)messageEvent.getData());
        } else if (messageEvent.getCode() == Event.EventCode.EVENT_SDCARD){
            GlobalLogic.getInstance().setSDCardInitStatus((UIElementStatusEnum.SDCardInitStatus)messageEvent.getData());
        } else if (messageEvent.getCode() == Event.EventCode.EVENT_MIC){
            GlobalLogic.getInstance().setMicStatus(GlobalLogic.getInstance().getInt("MIC") == 0 ? MIC_ON : MIC_OFF);
            LedMananger.getInstance().setLedMicStatus(GlobalLogic.getInstance().getInt("MIC") == 0 ? true : false);
        } else if(messageEvent.getCode() == Event.EventCode.EVENT_FOTA_UPDATE){
            GlobalLogic.getInstance().setFOTAFileStatus((UIElementStatusEnum.FOTAFileStatus)messageEvent.getData());
        }
        osdView.invalidateView();
    }

    private final PhoneStateListener mListener = new PhoneStateListener(){
        @Override
        public void onSignalStrengthsChanged(SignalStrength sStrength) {
            super.onSignalStrengthsChanged(sStrength);
            try {
                Method method;
                int strength = 0;
                int networkType = mTelephonyManager.getNetworkType();
                // if networkType is LTE, using custom dbm to distinguish signal strength level
                lteLevel = LTE_SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
                if(networkType == TelephonyManager.NETWORK_TYPE_LTE )
                {
                    method = sStrength.getClass().getDeclaredMethod("getLteDbm");
                    int lteRsrp = (int) method.invoke(sStrength);
                    if (lteRsrp > -44) {
                        lteLevel = LTE_SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
                    } else if (lteRsrp >= -97){
                        lteLevel =  LTE_SIGNAL_STRENGTH_GREAT;
                    } else if (lteRsrp >= -105){
                        lteLevel = LTE_SIGNAL_STRENGTH_GOOD;
                    } else if (lteRsrp >= -113){
                        lteLevel = LTE_SIGNAL_STRENGTH_MODERATE;
                    } else if (lteRsrp >= -120) {
                        lteLevel = LTE_SIGNAL_STRENGTH_POOR;
                    } else if (lteRsrp >= -140) {
                        lteLevel = LTE_SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
                    }
                    GlobalLogic.getInstance().setLTEStatus(lteLevel);
                    osdView.invalidateView();
                }
                Logg.i(TAG, "SignalStrengthLevel: " + Integer.toString(strength)+",lteStatusType="+lteLevel);
            }
            catch (Exception ignored)
            {
                Logg.e(TAG, "Exception: " + ignored.toString());
            }
        }
    };
}
