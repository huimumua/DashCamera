package com.askey.dvr.cdr7010.dashcam.core.encoder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.IntDef;

import com.askey.dvr.cdr7010.dashcam.application.DashCamApplication;
import com.askey.dvr.cdr7010.dashcam.core.event.Event;
import com.askey.dvr.cdr7010.dashcam.core.event.EventState;
import com.askey.dvr.cdr7010.dashcam.core.jni.MediaBuffer;
import com.askey.dvr.cdr7010.dashcam.service.FileManager;
import com.askey.dvr.cdr7010.dashcam.util.Logg;
import com.askey.dvr.cdr7010.dashcam.util.SDCardUtils;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

public class MediaMuxerWrapper {
    private static final String LOG_TAG = "MuxerWrapper";

    private Context mContext;
    private final HandlerThread mHandlerThread;
    private final Handler mHandler;
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
    private EventState mEventState = new EventState();

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
        mMuxerHandler.eventMuxer.terminate();

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
            mMuxerHandler.eventMuxer.stop();

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

        mMuxerHandler.eventMuxer.addTrack(type, format);
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

            if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0) {
                if (mEventState.isNeedProcess()) {
                    mEventState.doProcess();
                    Event event = mEventState.getEvent();
                    eventId = event.getId();
                }
            }
        }
        mMediaBuffer.writeSampleData(type, eventId, time, byteBuf, bufferInfo);
    }

    private void closeMuxer(AndroidMuxer muxer) {

        final AndroidMuxer tmpMuxer = muxer;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    tmpMuxer.stop();
                } catch (final Exception e) {
                    Logg.e(LOG_TAG, "closeSegment - > failed stopping muxer", e);
                }

                long eventTimeOffsetMs = tmpMuxer.eventTimeMs() - tmpMuxer.startTimeMs();
                if (mSegmentCallback != null) {
                    mSegmentCallback.segmentCompletedAsync(tmpMuxer.event(),
                            eventTimeOffsetMs,
                            tmpMuxer.filePath(),
                            tmpMuxer.startTimeMs(),
                            tmpMuxer.duration());
                }
            }
        });
    }


    static private class MuxerHandler extends Handler {

        private AndroidMuxer muxer;
        private WeakReference<MediaMuxerWrapper> weakParent;
        private boolean flagTerm = false;
        private EventMuxer eventMuxer;

        MuxerHandler(Looper looper, MediaMuxerWrapper parent) {
            super(looper);
            weakParent = new WeakReference<>(parent);
            eventMuxer = new EventMuxer(parent.mContext, parent.mSegmentCallback, parent.mHandler);
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

        void pauseContinuesRecording() {
            MediaMuxerWrapper parent = weakParent.get();
            if (parent != null && muxer != null) {
                parent.closeMuxer(muxer);
                muxer = null;
            }
        }

        void muxSampleData(int type, long time,
                           final ByteBuffer byteBuf,
                           final MediaCodec.BufferInfo bufferInfo) {

            if ((type == SAMPLE_TYPE_VIDEO) && ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0)) {
                final MediaMuxerWrapper parent = weakParent.get();
                if (muxer != null && muxer.duration() >= muxer.maxDuration()) {
                    if (parent != null) {
                        parent.closeMuxer(muxer);
                        muxer = null;
                    }
                }

                if (muxer == null) {
                    try {
                        String path = FileManager.getInstance(parent.mContext).getFilePathForNormal(time);
                        Logg.d(LOG_TAG, "NORMAL PATH: " + path);
                        muxer = new AndroidMuxer(path);
                        if (parent.mSegmentCallback != null) {
                            parent.mSegmentCallback.segmentStartPrepareSync(Event.ID_NONE, muxer.filePath());
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
                                    parent.mSegmentCallback.segmentStartAsync(Event.ID_NONE, startTimeMs);
                                }
                            }
                        });
                    } catch (IOException | RemoteException e) {
                        Logg.e(LOG_TAG, "Fail to create mp4 muxer with exception: " + e.getMessage());
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
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int eventId = Event.ID_NONE;
            long eventTime = 0;
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

                    if (eventMuxer.isRecording()) {
                        eventId = event;
                        eventTime = time;
                        break;
                    }

                    if (array == null || array.length < size) {
                        array = new byte[size];
                    }
                    int len = fin.read(array, 0, size);
                    if (len < 0) {
                        Logg.d(LOG_TAG, "EOF");
                        break;
                    }

                    bufferInfo.flags = flags;
                    bufferInfo.presentationTimeUs = pts;
                    bufferInfo.offset = 0;
                    bufferInfo.size = len;
                    ByteBuffer buffer = ByteBuffer.wrap(array);

                    if (flagTerm) {
                        break;
                    }

                    if (event != Event.ID_NONE) {
                        // stop continues recording
                        eventId = event;
                        eventTime = time;
                        pauseContinuesRecording();
                        break;
                    } else {
                        muxSampleData(type, time, buffer, bufferInfo);
                    }
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
            eventMuxer.feed(eventId, eventTime, path);
        }
    }

    private static void cleanDirectory(File directory) {
        if (!directory.exists()) {
            return;
        }

        for (File file: directory.listFiles()) {
            if (file.isDirectory())
                cleanDirectory(file);
            else
                file.delete();
        }
    }
}
