package com.askey.dvr.cdr7010.dashcam.ui;

import android.app.Fragment;
import android.graphics.ImageFormat;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.util.Size;

import com.askey.dvr.cdr7010.dashcam.R;

/**
 * Created by lly on 18-4-9.
 */

public class Camera2PreviewFragment extends Fragment {
    private static final String TAG = "Camera2PreviewFragment";

    private static final int STATE_TIMEOUT_MS = 5000;
    private static final int SESSION_WAIT_TIMEOUT_MS = 2500;

    private static final Size DESIRED_IMAGE_READER_SIZE = new Size(1920, 1440);
    private static final int IMAGE_READER_BUFFER_SIZE = 16;

    private AutoFitSurfaceView mSurfaceView;
    private Surface mSurface;

    public static Camera2PreviewFragment newInstance() {
        Camera2PreviewFragment fragment = new Camera2PreviewFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera2_preview, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        mSurfaceView = (AutoFitSurfaceView) view.findViewById(R.id.surface_view);
        mSurfaceView.setAspectRatio(DESIRED_IMAGE_READER_SIZE.getWidth(),
                DESIRED_IMAGE_READER_SIZE.getHeight());
        // This must be called here, before the initial buffer creation.
        // Putting this inside surfaceCreated() is insufficient.
        mSurfaceView.getHolder().setFormat(ImageFormat.YV12);
    }
}
