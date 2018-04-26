package com.askey.dvr.cdr7010.dashcam.core.recorder;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

    private Context mContext;
    private EGLRenderer mRenderer;
    private MediaVideoEncoder mVideoEncoder;
    private MediaAudioEncoder mAudioEncoder;
    private MediaMuxerWrapper mMuxer;
    private Surface mSurface;

    private final Object mWait = new Object();
    private final Object mSync = new Object();

    private InterruptedCallback mIntrCallback;

    public interface InterruptedCallback {
        void onInterrupted();
    }

    public Recorder(@NonNull Context context, @Nullable InterruptedCallback callback) {
        mContext = context.getApplicationContext();
        mIntrCallback = callback;
    }

    public void prepare() throws IOException {
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
            mMuxer = new MediaMuxerWrapper(mContext, mSegmentCallback, mStateCallback);
        } catch (IOException e) {
            Logg.e(TAG, "Exception: " + e.getMessage());
            mRenderer.stop();
            mRenderer = null;
            throw new IOException("create muxer error.");
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
        if(mRenderer != null) {
            mRenderer.stop();
        }
        if(mMuxer != null) {
            mMuxer.stopRecording();
        }
    }

    public void release() {
        mRenderer = null;
        mMuxer = null;
        mVideoEncoder = null;
        mAudioEncoder = null;
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

    private MediaMuxerWrapper.SegmentCallback mSegmentCallback = new MediaMuxerWrapper.SegmentCallback() {
        @Override
        public boolean segmentStartPrepareSync(int event, String path) {
            Logg.v(TAG, "segmentStartPrepareSync: event=" + event + " " + path);
            // 注意：禁止在这里进行耗时操作
            return true;
        }

        @Override
        public void segmentStartAsync(int event, long startTimeMs) {
            Logg.v(TAG, "segmentStartAsync: startTimeMs=" + startTimeMs +",event="+event);
        }

        @Override
        public void segmentCompletedAsync(int event, final long eventTimeMs, final String path, final long startTimeMs, long durationMs) {
            Logg.v(TAG, "segmentCompletedAsync: event=" + event + " eventTimeMs=" + eventTimeMs + " " + path);
        }
    };

    private MediaMuxerWrapper.StateCallback mStateCallback = new MediaMuxerWrapper.StateCallback() {
        @Override
        public void onStartd() {
            Logg.v(TAG, "mStateCallback onStartd");
        }

        @Override
        public void onStoped() {
            Logg.v(TAG, "mStateCallback onStoped");
            synchronized (mSync) {
                if (mRenderer != null) {
                    mRenderer.stop();
                }
            }
        }

        @Override
        public void onInterrupted() {
            synchronized (mSync) {
                Logg.v(TAG, "mStateCallback onInterrupted");
                if (mRenderer != null) {
                    mRenderer.stop();
                }

                if (mIntrCallback != null) {
                    mIntrCallback.onInterrupted();
                }
            }
        }
    };
}
