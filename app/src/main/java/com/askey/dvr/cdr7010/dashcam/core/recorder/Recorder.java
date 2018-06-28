package com.askey.dvr.cdr7010.dashcam.core.recorder;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Surface;

import com.askey.dvr.cdr7010.dashcam.core.RecordConfig;
import com.askey.dvr.cdr7010.dashcam.core.encoder.IFrameListener;
import com.askey.dvr.cdr7010.dashcam.core.encoder.MediaAudioEncoder;
import com.askey.dvr.cdr7010.dashcam.core.encoder.MediaEncoder;
import com.askey.dvr.cdr7010.dashcam.core.encoder.MediaMuxerWrapper;
import com.askey.dvr.cdr7010.dashcam.core.encoder.MediaVideoEncoder;
import com.askey.dvr.cdr7010.dashcam.service.FileManager;
import com.askey.dvr.cdr7010.dashcam.util.Logg;

import java.io.IOException;
import java.util.List;

public class Recorder implements IFrameListener {
    private final static String TAG = "Recorder";

    private Context mContext;
    private RecordConfig mConfig;
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

        void onEventCompleted(int evevtId, long timestamp, List<String> pictures, String video);
    }

    public Recorder(@NonNull Context context,
                    @NonNull RecordConfig config,
                    @Nullable StateCallback callback) {
        mContext = context.getApplicationContext();
        mConfig = config;
        mStateCallback = callback;
    }

    public void prepare() throws IOException {

        try {
            mMuxer = new MediaMuxerWrapper(mContext, mSegmentCallback, mMuxerStateCallback);
        } catch (IOException e) {
            Logg.e(TAG, "Exception: " + e.getMessage());
            throw new IOException("create muxer error.");
        }
        mVideoEncoder = new MediaVideoEncoder(mMuxer,
                mMediaEncoderListener,
                mConfig.videoWidth(),
                mConfig.videoHeight(),
                mConfig.videoFPS(),
                mConfig.videoBitRate());
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
        mAudioEncoder.mute();
    }

    public void demute() {
        mAudioEncoder.demute();
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
            if (mStateCallback != null) {
                mStateCallback.onEventStateChanged(event != 0);
            }
            return true;
        }

        @Override
        public void segmentStartAsync(int event, long startTimeMs) {
            Logg.v(TAG, "segmentStartAsync: startTimeMs=" + startTimeMs + ",event=" + event);
        }

        @Override
        public void segmentCompletedAsync(int event, final long eventTimeMs, final String path, final long startTimeMs, long durationMs) {
            Logg.v(TAG, "segmentCompletedAsync: event=" + event + " eventTimeMs=" + eventTimeMs + " " + path);
            if (event != 0) {
                Snapshot.take3Pictures(path, startTimeMs, 7 * 1000 * 1000L, FileManager.getInstance(mContext), pictures -> {
                    if (pictures != null) {
                        for (String pic : pictures) {
                            Logg.d(TAG, pic);
                        }
                    }
                    if (mStateCallback != null) {
                        mStateCallback.onEventCompleted(event, startTimeMs, pictures, path);
                    }
                });
            }
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
