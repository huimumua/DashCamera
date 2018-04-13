package com.askey.dvr.cdr7010.dashcam.core.recorder;

import android.graphics.SurfaceTexture;
import android.view.Surface;

import com.askey.dvr.cdr7010.dashcam.core.encoder.IFrameListener;
import com.askey.dvr.cdr7010.dashcam.core.encoder.MediaAudioEncoder;
import com.askey.dvr.cdr7010.dashcam.core.encoder.MediaEncoder;
import com.askey.dvr.cdr7010.dashcam.core.encoder.MediaMuxerWrapper;
import com.askey.dvr.cdr7010.dashcam.core.encoder.MediaVideoEncoder;
import com.askey.dvr.cdr7010.dashcam.core.renderer.EGLRenderer;
import com.askey.dvr.cdr7010.dashcam.util.Logg;

import java.io.IOException;

public class Recorder {
    private final static String TAG = "Recorder";

    private EGLRenderer mRenderer;
    private MediaVideoEncoder mVideoEncoder;
    private MediaAudioEncoder mAudioEncoder;
    private MediaMuxerWrapper mMuxer;
    private Surface mSurface;

    private final Object mWait = new Object();

    public void prepare() {
        mRenderer = new EGLRenderer();
        mRenderer.setSurfaceTextureListener(new EGLRenderer.OnSurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                Logg.d(TAG, "onSurfaceTextureAvailable");
                synchronized (mWait) {
                    mSurface = new Surface(surfaceTexture);
                    mWait.notifyAll();
                }
            }

            @Override
            public void onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                Logg.d(TAG, "onSurfaceTextureDestroyed");
                mSurface.release();
                mSurface = null;
            }
        });
        mRenderer.start();

        try {
            mMuxer = new MediaMuxerWrapper("/sdcard/dvr", null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mVideoEncoder = new MediaVideoEncoder(mMuxer, mMediaEncoderListener, 1920, 1080);
        mAudioEncoder = new MediaAudioEncoder(mMuxer, mMediaEncoderListener);

        try {
            mMuxer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mRenderer.createEncoderSurface(mVideoEncoder.getInputSurface(), new IFrameListener() {
            @Override
            public void frameAvailableSoon() {
                if (mVideoEncoder != null) {
                    mVideoEncoder.frameAvailableSoon();
                }
            }
        });

        synchronized (mWait) {
            Logg.d(TAG, "prepare wait");
            if (mSurface == null) {
                try {
                    mWait.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Logg.d(TAG, "prepare exit");
        }
    }

    public Surface getInputSurface() {
        return mSurface;
    }

    public void startRecording() {
        mMuxer.startRecording();
    }

    public void stopRecording() {
        mMuxer.stopRecording();
    }

    private final MediaEncoder.MediaEncoderListener mMediaEncoderListener = new MediaEncoder.MediaEncoderListener() {
        @Override
        public void onPrepared(final MediaEncoder encoder) {
            Logg.v(TAG, "onPrepared: ");

        }

        @Override
        public void onStopped(final MediaEncoder encoder) {
            Logg.v(TAG, "onStopped: ");
            if (encoder instanceof MediaVideoEncoder) {

            }
        }
    };
}
