package com.askey.dvr.cdr7010.dashcam.ui;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
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
import com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum;
import com.askey.dvr.cdr7010.dashcam.util.EventUtil;
import com.askey.dvr.cdr7010.dashcam.util.Logg;
import com.askey.dvr.cdr7010.dashcam.widget.OSDView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Method;

import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.LTEStatusType.LTE_SIGNAL_STRENGTH_GOOD;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.LTEStatusType.LTE_SIGNAL_STRENGTH_GREAT;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.LTEStatusType.LTE_SIGNAL_STRENGTH_MODERATE;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.LTEStatusType.LTE_SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.LTEStatusType.LTE_SIGNAL_STRENGTH_POOR;


public class CameraRecordFragment extends Fragment {
    private static final String TAG = CameraRecordFragment.class.getSimpleName();
    private DashCam mMainCam;
    private OSDView osdView;
    private TelephonyManager mTelephonyManager;
    private UIElementStatusEnum.LTEStatusType lteLevel;

    private static final int REQUEST_VIDEO_PERMISSIONS = 1001;
    private static final String[] VIDEO_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
    };

    public static CameraRecordFragment newInstance() {
        return new CameraRecordFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        osdView = view.findViewById(R.id.osd_view);
        mMainCam = new DashCam(getActivity());
        mMainCam.prepare();
    }

    @Override
    public void onResume() {
        super.onResume();
        osdView.init(1000);
        mTelephonyManager.listen(mListener,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        mMainCam.startVideoRecord();
    }

    @Override
    public void onPause() {
        super.onPause();
        mTelephonyManager.listen(mListener, PhoneStateListener.LISTEN_NONE);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        osdView.unInit();
        EventUtil.unregister(this);
    }

    private void requestVideoPermissions() {
        if (!hasPermissionsGranted(VIDEO_PERMISSIONS)) {
            requestPermissions(VIDEO_PERMISSIONS, REQUEST_VIDEO_PERMISSIONS);
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
        if(messageEvent != null){
            handleMessageEvent(messageEvent);
        }
    }
    private void handleMessageEvent(MessageEvent messageEvent){
        if(messageEvent.getCode() == Event.EventCode.EVENT_RECORDING){
            GlobalLogic.getInstance().setRecordingStatus((UIElementStatusEnum.RecordingStatusType)messageEvent.getData());
            osdView.startRecordingCountDown();
        }else if(messageEvent.getCode() == Event.EventCode.EVENT_RECORDING_FILE_LIMIT){
            GlobalLogic.getInstance().setEventRecordingLimitStatus((UIElementStatusEnum.EventRecordingLimitStatusType)messageEvent.getData());
        }else if(messageEvent.getCode() == Event.EventCode.EVENT_PARKING_RECODING_FILE_LIMIT){
            GlobalLogic.getInstance().setParkingRecordingLimitStatus((UIElementStatusEnum.ParkingRecordingLimitStatusType)messageEvent.getData());
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
