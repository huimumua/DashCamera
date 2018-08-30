package com.askey.dvr.cdr7010.dashcam.core.encoder;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.support.annotation.NonNull;

import com.askey.dvr.cdr7010.dashcam.util.Logg;

import java.io.IOException;
import java.nio.ByteBuffer;

public class AndroidMuxer {
    private final String LOG_TAG = "AndroidMuxer";
    private static byte[] AAC_MONO_SILENCE_FRAME_WITH_SIZE = new byte[]{
            0x0A, 0x00, 0x01, 0x40, 0x20, 0x06, 0x4F, (byte) 0xDE, 0x02, 0x70, 0x0C, 0x1C};

    private MediaMuxer mMuxer;
    private int mVidIdx, mAudIdx;
    private String mPath;
    private boolean mStarted = false;
    private boolean mHasAudioData = false;
    private long mLastVideoPTSUs;
    private boolean mHasKeyFrame = false;
    private long mDurationUs;
    private long mStartPtsUs;
    private long mStartTimeMs;
    private long mEventTimeMs;
    private int mEventId = 0;
    private long mMaxDurationMs;
    private int mFrameCount = 0;

    AndroidMuxer(@NonNull final String path) throws IOException {
        mPath = path;
        Logg.e("iamlbccc", "\t+ New MediaMuxer for " + path);
        mMuxer = new MediaMuxer(mPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        Logg.e("iamlbccc", "\t- New MediaMuxer for " + path);
    }

    String filePath() {
        return mPath;
    }

    long duration() {
        return mDurationUs;
    }

    void setMaxDuration(long timerMs) {
        mMaxDurationMs = timerMs;
    }

    long maxDuration() {
        return mMaxDurationMs;
    }

    long startPtsUs() {
        return mStartPtsUs;
    }

    long startTimeMs() {
        return mStartTimeMs;
    }

    void riseEvent(int event, long time) {
        mEventId = event;
        mEventTimeMs = time;
    }

    long eventTimeMs() {
        return mEventTimeMs;
    }

    int event() {
        return mEventId;
    }

    void addTrack(int type, MediaFormat format) {
        if (format == null)
            return;

        try {
            if (type == MediaMuxerWrapper.SAMPLE_TYPE_AUDIO)
                mAudIdx = mMuxer.addTrack(format);
            else
                mVidIdx = mMuxer.addTrack(format);
        } catch (Exception e) {
        }
    }

    void writeSampleData(int type, ByteBuffer byteBuf, MediaCodec.BufferInfo bufferInfo) {
        if (!mStarted)
            return;

        if (type == MediaMuxerWrapper.SAMPLE_TYPE_AUDIO) {
            mHasAudioData = true;
        } else if (type == MediaMuxerWrapper.SAMPLE_TYPE_VIDEO) {
            if (!mHasKeyFrame) {
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0) {
                    mHasKeyFrame = true;
                    mStartPtsUs = bufferInfo.presentationTimeUs;
                } else {
                    return;
                }
            }
            long diff = bufferInfo.presentationTimeUs - mLastVideoPTSUs;
            if ((mLastVideoPTSUs != 0) && (diff > 500 * 1000)) {
                Logg.e(LOG_TAG, "frame interval(" + diff + ") > 500ms, maybe lose frames");
            }
            mDurationUs = bufferInfo.presentationTimeUs - mStartPtsUs;
            mLastVideoPTSUs = bufferInfo.presentationTimeUs;
            mFrameCount++;
        }
        int idx = (type == MediaMuxerWrapper.SAMPLE_TYPE_AUDIO) ? mAudIdx : mVidIdx;
        try {
            mMuxer.writeSampleData(idx, byteBuf, bufferInfo);
        } catch (Exception e) {
        }
    }

    private void writeAudioSilenceFrame(long ptsUs) {
        ByteBuffer buffer = ByteBuffer.wrap(AAC_MONO_SILENCE_FRAME_WITH_SIZE);
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        bufferInfo.set(0, AAC_MONO_SILENCE_FRAME_WITH_SIZE.length, ptsUs, 0);
        try {
            mMuxer.writeSampleData(mAudIdx, buffer, bufferInfo);
        } catch (Exception e) {
        }
    }

    void start(long time) {
        try {
            if (!mStarted) {
                mMuxer.start();
                mStartTimeMs = time;
                mStarted = true;
            }
        } catch (Exception e) {
            Logg.e(LOG_TAG, "fail with exception: " + e.getMessage());
        }
    }

    void stop() {
        if (mStarted && !mHasAudioData) {
            writeAudioSilenceFrame(mLastVideoPTSUs);
        }
        release();
        float fps = mFrameCount * 1000f * 1000f / mDurationUs;
        Logg.d(LOG_TAG, "frames: " + mFrameCount + "  duration(us): " + mDurationUs + "  fps: " + fps);
    }

    private void release() {
        try {
            mMuxer.release();
        } catch (Exception e) {
            Logg.e(LOG_TAG, "Fail to release MediaMuxer with: " + e.getMessage());
        } finally {
            mMuxer = null;
        }
    }
}
