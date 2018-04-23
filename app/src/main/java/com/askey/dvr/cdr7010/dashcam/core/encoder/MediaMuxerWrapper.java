package com.askey.dvr.cdr7010.dashcam.core.encoder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.IntDef;

import com.askey.dvr.cdr7010.dashcam.application.DashCamApplication;
import com.askey.dvr.cdr7010.dashcam.core.jni.MediaBuffer;
import com.askey.dvr.cdr7010.dashcam.util.Logg;
import com.askey.dvr.cdr7010.dashcam.util.SDCardUtils;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MediaMuxerWrapper {
	private static final String LOG_TAG = "MuxerWrapper";

	private Context mContext;
	private final HandlerThread mHandlerThread;
	private final Handler mHandler;
	private final String mOutputDir;
	private MediaFormat mVideoFormat;
	private MediaFormat mAudioFormat;
	private int mEncoderCount, mStatredCount;
	private boolean mIsStarted;
	private boolean mReasonInterruption = false;
	private MediaEncoder mVideoEncoder, mAudioEncoder;
	private long mFirstStarPtsUs = -1;
	private long mEventPtsUs = -1;
	private int mEventId = 0;
	private long mTotalDurationUs;
	private SegmentCallback mSegmentCallback;
	private long mSegmentDurationLimitedUs = 60 * 1000 * 1000L; // us
	private static final long SEGMENT_CREATE_THRESHOLD_US = 5 * 1000 * 1000L; // us
	private static final long EVENT_RECORD_DURATION_US = 5 * 1000 * 1000L; // us
	private final MediaBuffer mMediaBuffer;
	private final HandlerThread mMuxerThread;
	private final MuxerHandler mMuxerHandler;

	private ISegmentListener mSegmentListener;
	private StateCallback mStateCallback;

	public static final int SAMPLE_TYPE_VIDEO = 1;
	public static final int SAMPLE_TYPE_AUDIO = 2;

	@IntDef({SAMPLE_TYPE_VIDEO, SAMPLE_TYPE_AUDIO})
	public @interface SampleType {}

	public interface SegmentCallback {
		boolean segmentStartPrepareSync(int event, String path);
		void segmentStartAsync(int event, long startTimeMs);
		void segmentCompletedAsync(int event, long eventTimeMs, String path, long startTimeMs, long durationMs);
	}

	public interface StateCallback {
	    void onStartd();
	    void onStoped();
	    void onInterrupted();
    }

    private BroadcastReceiver mSDCardEjectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_MEDIA_EJECT)) {
                terminateRecording();
            }
        }
    };

	/**
	 * Constructor
	 * @throws IOException
	 */
	public MediaMuxerWrapper(Context context,
                             final SegmentCallback segmentCallback,
                             final StateCallback stateCallback) throws IOException {
	    mContext = context.getApplicationContext();
        mSegmentCallback = segmentCallback;
        mStateCallback = stateCallback;

	    registerReceiver();

		if (!SDCardUtils.isSDCardEnable()) {
			throw new IOException("SD Card unmounted");
		}

		mOutputDir = SDCardUtils.getSDCardPath() + "DVR/NORMAL";

		mEncoderCount = mStatredCount = 0;
		mIsStarted = false;

		mHandlerThread = new HandlerThread("MuxerWorker");
		mHandlerThread.start();
		mHandler = new Handler(mHandlerThread.getLooper());

		mMuxerThread = new HandlerThread("MuxerThread");
		mMuxerThread.start();
		mMuxerHandler = new MuxerHandler(mMuxerThread.getLooper(), this);

		File cache = DashCamApplication.getAppContext().getCacheDir();
        cleanDirectory(cache);

        File dir = new File(mOutputDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("create output directory error.");
        }

        File tmpDir = new File(Environment.getExternalStorageDirectory().getPath() + "/.tmp");
        if (!tmpDir.exists() && !tmpDir.mkdirs()) {
            throw new IOException("create tmp directory error.");
        }

		mMediaBuffer = new MediaBuffer(mMuxerHandler, cache.getAbsolutePath());
	}

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addDataScheme("file");
        mContext.registerReceiver(mSDCardEjectReceiver, filter);
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
        mReasonInterruption = true;
		mMediaBuffer.stop();
		mHandlerThread.quit();
		mMuxerHandler.terminate();
		mMuxerThread.quit();

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
			mMediaBuffer.reset();
			mMediaBuffer.start();
			mIsStarted = true;
			mFirstStarPtsUs = -1;
			mReasonInterruption = false;
			notifyAll();
			Logg.v(LOG_TAG,  "MediaMuxer started:");
			if (mStateCallback != null) {
			    mStateCallback.onStartd();
            }
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
			mMediaBuffer.stop();
			mMuxerHandler.stop();
			mMuxerThread.quitSafely();
			mHandlerThread.quitSafely();
			if (mStateCallback != null) {
			    if (mReasonInterruption) {
			        mStateCallback.onInterrupted();
                } else {
                    mStateCallback.onStoped();
                }
            }
		}
	}

	/*package*/ synchronized void addTrackWithType(final @SampleType int type,
													  final MediaFormat format) {
		if (type == SAMPLE_TYPE_AUDIO)
			mAudioFormat = format;
		else
			mVideoFormat = format;
	}

	/*package*/ synchronized void writeSampleDataWithType(final @SampleType int type,
														  final ByteBuffer byteBuf,
														  final MediaCodec.BufferInfo bufferInfo) {
		if (!mIsStarted) return;
		long time = System.currentTimeMillis();
		int eventId = 0;
		if (type == SAMPLE_TYPE_VIDEO) {
			if (mFirstStarPtsUs < 0) {
				mFirstStarPtsUs = bufferInfo.presentationTimeUs;
			}
			final long ptsUs = bufferInfo.presentationTimeUs;
			mTotalDurationUs = ptsUs - mFirstStarPtsUs;
		}
		mMediaBuffer.writeSampleData(type, eventId, time, byteBuf, bufferInfo);
	}

	private void closeMuxer(AndroidMuxer muxer) {

		final AndroidMuxer tmpMuxer = muxer;
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				final String srcPath = tmpMuxer.filePath();
				final long timeMs;
				if (tmpMuxer.event() == 0) {
					timeMs = tmpMuxer.startTimeMs();
				} else {
					timeMs = tmpMuxer.eventTimeMs();
				}
				final String dstPath = mOutputDir + "/" + getCaptureFileName(new Date(timeMs));
				try {
					tmpMuxer.stop();
				} catch (final Exception e) {
					Logg.e(LOG_TAG, "closeSegment - > failed stopping muxer", e);
				}

				if (!(new File(srcPath)).renameTo(new File(dstPath))) {
					Logg.e(LOG_TAG, "Fail to move " + srcPath + " --> " + dstPath);
					return;
				}

				if (!new File(dstPath).exists()) {
					Logg.e(LOG_TAG, "file no exist: " + dstPath);
				}

				long eventTimeOffsetMs = tmpMuxer.eventTimeMs() - tmpMuxer.startTimeMs();
				if (mSegmentCallback != null) {
					mSegmentCallback.segmentCompletedAsync(tmpMuxer.event(), eventTimeOffsetMs, dstPath, tmpMuxer.startTimeMs(), tmpMuxer.duration());
				}
			}
		});
	}


	static private class MuxerHandler extends Handler {

		private AndroidMuxer muxer;
		private WeakReference<MediaMuxerWrapper> weakParent;
		private boolean flagTerm = false;

		MuxerHandler(Looper looper, MediaMuxerWrapper parent) {
			super(looper);
			weakParent = new WeakReference<>(parent);
		}

		void terminate() {
			flagTerm = true;
			if (muxer != null) {
				muxer.stop();
				muxer = null;
			}
		}

		void stop() {
			flagTerm = true;
			if (muxer != null) {
				MediaMuxerWrapper parent = weakParent.get();
				if (parent != null) {
					parent.closeMuxer(muxer);
				}
				muxer = null;
			}
		}

		void muxSampleData(int type, long time,
						   final ByteBuffer byteBuf,
						   final MediaCodec.BufferInfo bufferInfo) {

			if ((type == SAMPLE_TYPE_VIDEO) && ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0)) {
				if (muxer != null && muxer.duration() >= muxer.maxDuration()) {
					MediaMuxerWrapper parent = weakParent.get();
					if (parent != null) {
						parent.closeMuxer(muxer);
					}
					muxer = null;
				}

				if (muxer == null) {
					try {
						muxer = new AndroidMuxer(getTempFile());
						final MediaMuxerWrapper parent = weakParent.get();
						if (parent.mSegmentCallback != null) {
							parent.mSegmentCallback.segmentStartPrepareSync(0, muxer.filePath());
						}
						muxer.addTrack(SAMPLE_TYPE_VIDEO, parent.mVideoFormat);
						muxer.addTrack(SAMPLE_TYPE_AUDIO, parent.mAudioFormat);
						muxer.setMaxDuration(parent.mSegmentDurationLimitedUs);
						muxer.start(time);
						final long startTimeMs = muxer.startTimeMs();
						parent.mHandler.post(new Runnable() {
							@Override
							public void run() {
								if (parent.mSegmentCallback != null) {
									parent.mSegmentCallback.segmentStartAsync(0, startTimeMs);
								}
							}
						});
					} catch (IOException e) {
						Logg.e(LOG_TAG, "Fail to create mp4 muxer");
						return;
					}
				}
			}

			if (muxer != null) {
				muxer.writeSampleData(type, byteBuf, bufferInfo);
			}
		}

		@Override
		public void handleMessage(Message msg) {
			String path = (String) msg.obj;
			File file = new File(path);
			RandomAccessFile fin = null;
			try {
				fin = new RandomAccessFile(file, "r");
				byte[] array = null;
				while (true) {
					if (flagTerm) break;
					int tag = fin.readInt();
					if (tag != 0x55AA55AA) {
						fin.seek(fin.getFilePointer() - 3);
						continue;
					}

					int length = fin.readInt();
					byte type = fin.readByte();
					byte event = fin.readByte();
					long time = fin.readLong();
					int size = fin.readInt();
					int flags = fin.readInt();
					long pts = fin.readLong();
					if (array == null || array.length < size) {
						array = new byte[size];
					}
					int len = fin.read(array, 0, size);
					if (len < 0) {
						Logg.d(LOG_TAG, "EOF");
						break;
					}
					MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
					bufferInfo.flags = flags;
					bufferInfo.presentationTimeUs = pts;
					bufferInfo.offset = 0;
					bufferInfo.size = len;
					ByteBuffer buffer = ByteBuffer.wrap(array);
					if (flagTerm) break;
					muxSampleData(type, time, buffer, bufferInfo);
				}
			} catch (FileNotFoundException e) {
				Logg.e(LOG_TAG, "open fail:  " + path);
				return;
			} catch (EOFException e) {
			} catch (IOException e) {
				Logg.e(LOG_TAG, "read file: " + path + e.getMessage());
			}

			try {
				fin.close();
			} catch (IOException e) {
				Logg.e(LOG_TAG, "close fail:  " + path);
			}
		}
	}

    public static void cleanDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            throw new IllegalArgumentException("directory not exist.");
        }

        for (File file: directory.listFiles()) {
            if (file.isDirectory())
                cleanDirectory(file);
            else
                file.delete();
        }
    }

    private static final SimpleDateFormat mDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US);
    private static final String CAPTURE_FILE_EXT_NAME = ".mp4";

    public static String getCaptureFileName(final Date date) {
        synchronized (mDateTimeFormat) {
            return mDateTimeFormat.format(date) + CAPTURE_FILE_EXT_NAME;
        }
    }

    public static String getTempFile() {
        String root = Environment.getExternalStorageDirectory().getPath();
        if (!root.isEmpty()) {
            return root + "/.tmp/" + String.valueOf(System.nanoTime());
        }
        return "";
    }
}
