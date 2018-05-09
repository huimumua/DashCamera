package com.askey.dvr.cdr7010.dashcam.core.recorder;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Surface;

import com.askey.dvr.cdr7010.dashcam.core.encoder.IFrameListener;
import com.askey.dvr.cdr7010.dashcam.core.encoder.MediaAudioEncoder;
import com.askey.dvr.cdr7010.dashcam.core.encoder.MediaEncoder;
import com.askey.dvr.cdr7010.dashcam.core.encoder.MediaMuxerWrapper;
import com.askey.dvr.cdr7010.dashcam.core.encoder.MediaVideoEncoder;
import com.askey.dvr.cdr7010.dashcam.util.Logg;

import java.io.IOException;

public class Recorder implements IFrameListener {
    private final static String TAG = "Recorder";

    private Context mContext;
    private MediaVideoEncoder mVideoEncoder;
    private MediaAudioEncoder mAudioEncoder;
    private MediaMuxerWrapper mMuxer;

    private final Object mSync = new Object();

    private StateCallback mStateCallback;

    public interface StateCallback {
        void onStarted();
        void onStoped();
        void onInterrupted();
        void onEventStateChanged(boolean on);
    }

    public Recorder(@NonNull Context context, @Nullable StateCallback callback) {
        mContext = context.getApplicationContext();
        mStateCallback = callback;
    }

    public void prepare() throws IOException {

        try {
            mMuxer = new MediaMuxerWrapper(mContext, mSegmentCallback, mMuxerStateCallback);
        } catch (IOException e) {
            Logg.e(TAG, "Exception: " + e.getMessage());
            throw new IOException("create muxer error.");
        }
        mVideoEncoder = new MediaVideoEncoder(mMuxer, mMediaEncoderListener, 1920, 1080);
        mAudioEncoder = new MediaAudioEncoder(mMuxer, mMediaEncoderListener);

        try {
            mMuxer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Surface getInputSurface() {
        return mVideoEncoder.getInputSurface();
    }

    public void startRecording() {
        mMuxer.startRecording();
    }

    public void stopRecording() {
        if (mMuxer != null) {
            mMuxer.stopRecording();
        }
    }

    public void release() {
        mMuxer = null;
        mVideoEncoder = null;
        mAudioEncoder = null;
    }

    public void mute() {
        mAudioEncoder.pause();
    }

    public void demute() {
        mAudioEncoder.resume();
    }

    @Override
    public void frameAvailableSoon() {
        if (mVideoEncoder != null) {
            mVideoEncoder.frameAvailableSoon();
        }
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
            if (mStateCallback != null) {
                mStateCallback.onEventStateChanged(event != 0);
            }
        }

        @Override
        public void segmentCompletedAsync(int event, final long eventTimeMs, final String path, final long startTimeMs, long durationMs) {
            Logg.v(TAG, "segmentCompletedAsync: event=" + event + " eventTimeMs=" + eventTimeMs + " " + path);
        }
    };

    private MediaMuxerWrapper.StateCallback mMuxerStateCallback = new MediaMuxerWrapper.StateCallback() {
        @Override
        public void onStartd() {
            Logg.v(TAG, "mStateCallback onStartd");
            if (mStateCallback != null) {
                mStateCallback.onStarted();
            }
        }

        @Override
        public void onStoped() {
            Logg.v(TAG, "mStateCallback onStoped");
            if (mStateCallback != null) {
                mStateCallback.onStoped();
            }
        }

        @Override
        public void onInterrupted() {
            synchronized (mSync) {
                Logg.v(TAG, "mStateCallback onInterrupted");
                if (mStateCallback != null) {
                    mStateCallback.onInterrupted();
                }
            }
        }
    };
}
