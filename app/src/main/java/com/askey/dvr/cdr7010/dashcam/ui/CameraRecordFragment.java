package com.askey.dvr.cdr7010.dashcam.ui;

import android.Manifest;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.askey.dvr.cdr7010.dashcam.R;
import com.askey.dvr.cdr7010.dashcam.core.DashCam;

public class CameraRecordFragment extends Fragment {

    private DashCam mMainCam;

    private static final int REQUEST_VIDEO_PERMISSIONS = 1001;
    private static final String[] VIDEO_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
    };

    public static CameraRecordFragment newInstance() {
        return new CameraRecordFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera_record, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState){
        requestVideoPermissions();
        mMainCam = new DashCam(getActivity());
        mMainCam.prepare();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMainCam.startVideoRecord();
    }

    @Override
    public void onPause() {
        super.onPause();
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
}
