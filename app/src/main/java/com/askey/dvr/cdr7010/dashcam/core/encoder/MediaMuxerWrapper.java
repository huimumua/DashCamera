package com.askey.dvr.cdr7010.dashcam.core.encoder;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.support.annotation.IntDef;

import com.askey.dvr.cdr7010.dashcam.util.Logg;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MediaMuxerWrapper {
	private static final String LOG_TAG = "MuxerWrapper";

	private final String mOutputDir;
	private MediaFormat mVideoFormat;
	private MediaFormat mAudioFormat;
	private int mEncoderCount, mStatredCount;
	private boolean mIsStarted;
	private MediaEncoder mVideoEncoder, mAudioEncoder;
	private long mFirstStarPtsUs = -1;
    private long mEventPtsUs = -1;
	private long mTotalDurationUs;
	private SegmentCallback mSegmentCallback;
	private long mSegmentDurationLimitedUs = 15 * 1000 * 1000L; // us
	private static final long SEGMENT_CREATE_THRESHOLD_US = 5 * 1000 * 1000L; // us
	private static final long EVENT_RECORD_DURATION_US = 5 * 1000 * 1000L; // us


    private ISegmentListener mSegmentListener;

	public static final int SAMPLE_TYPE_VIDEO = 1;
	public static final int SAMPLE_TYPE_AUDIO = 2;
    public static final int SAMPLE_TYPE_NMEA = 3;

	@IntDef({SAMPLE_TYPE_VIDEO, SAMPLE_TYPE_AUDIO, SAMPLE_TYPE_NMEA})
	public @interface SampleType {}

	public interface SegmentCallback {
		boolean segmentStartPrepareSync(int event, String path);
		void segmentStartAsync(int event, long startTimeMs);
		void segmentCompletedAsync(int event, long eventTimeMs, String path, long startTimeMs, long durationMs);
	}

	/**
	 * Constructor
	 * @throws IOException
	 */
	public MediaMuxerWrapper(final String outputDir, final SegmentCallback callback) throws IOException {
		mOutputDir = outputDir;
		mSegmentCallback = callback;
		mEncoderCount = mStatredCount = 0;
		mIsStarted = false;


	}

	public void prepare() throws IOException {
		if (mVideoEncoder != null)
			mVideoEncoder.prepare();
		if (mAudioEncoder != null)
			mAudioEncoder.prepare();
	}

	public void startRecording() {
		if (mVideoEncoder != null)
			mVideoEncoder.startRecording();
		if (mAudioEncoder != null)
			mAudioEncoder.startRecording();
	}

	public void stopRecording() {
		if (mVideoEncoder != null)
			mVideoEncoder.stopRecording();
		mVideoEncoder = null;
		if (mAudioEncoder != null)
			mAudioEncoder.stopRecording();
		mAudioEncoder = null;
	}

	public void terminateRecording() {
        if (mVideoEncoder != null)
            mVideoEncoder.stopRecording();
        mVideoEncoder = null;
        if (mAudioEncoder != null)
            mAudioEncoder.stopRecording();
        mAudioEncoder = null;
        mIsStarted = false;

    }

	public synchronized boolean isStarted() {
		return mIsStarted;
	}

	public long getSegmentDuration() {
		return 0;
	}

	public long getTotalDuration() {
		return mTotalDurationUs / 1000L;
	}

	public void setSegmentDuration(long timeMs) {
		mSegmentDurationLimitedUs = timeMs * 1000L;
	}

	public long getSegmentDurationLimitedMs() {
		return mSegmentDurationLimitedUs / 1000L;
	}

	public void registerSegmentListener(ISegmentListener listener) {
        mSegmentListener = listener;
    }

    public void removeSegmentListener(ISegmentListener listener) {
        if (listener == mSegmentListener) {
            mSegmentListener = null;
        }
    }

//**********************************************************************
//**********************************************************************
	/**
	 * assign encoder to this calss. this is called from encoder.
	 * @param encoder instance of MediaVideoEncoder or MediaAudioEncoder
	 */
	/*package*/ void addEncoder(final MediaEncoder encoder) {
		if (encoder instanceof MediaVideoEncoder) {
			if (mVideoEncoder != null)
				throw new IllegalArgumentException("Video encoder already added.");
			mVideoEncoder = encoder;
		} else if (encoder instanceof MediaAudioEncoder) {
			if (mAudioEncoder != null)
				throw new IllegalArgumentException("Video encoder already added.");
			mAudioEncoder = encoder;
		} else {
            throw new IllegalArgumentException("unsupported encoder");
        }
		mEncoderCount = (mVideoEncoder != null ? 1 : 0) + (mAudioEncoder != null ? 1 : 0);
	}

	/**
	 * request start recording from encoder
	 * @return true when muxer is ready to write
	 */
	/*package*/ synchronized boolean start() {
		Logg.v(LOG_TAG,  "start:");
		mStatredCount++;
		if ((mEncoderCount > 0) && (mStatredCount == mEncoderCount)) {
			mTotalDurationUs = 0;
            mIsStarted = true;
            mFirstStarPtsUs = -1;
			notifyAll();
			Logg.v(LOG_TAG,  "MediaMuxer started:");
		}
		return mIsStarted;
	}

	/**
	 * request stop recording from encoder when encoder received EOS
	 */
	/*package*/ synchronized void stop() {
		Logg.v(LOG_TAG,  "stop:mStatredCount=" + mStatredCount);
		mStatredCount--;
		if ((mEncoderCount > 0) && (mStatredCount <= 0)) {
			mIsStarted = false;
        }
	}

	/*package*/ synchronized boolean addTrackWithType(final @SampleType int type,
													  final MediaFormat format) {
		if (type == SAMPLE_TYPE_AUDIO)
			mAudioFormat = format;
		else
			mVideoFormat = format;
		return true;
	}

	/*package*/ synchronized void writeSampleDataWithType(final @SampleType int type,
														  final ByteBuffer byteBuf,
														  final MediaCodec.BufferInfo bufferInfo) {

	}


}
