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
import android.support.annotation.NonNull;

import com.askey.dvr.cdr7010.dashcam.application.DashCamApplication;
import com.askey.dvr.cdr7010.dashcam.core.RecordConfig;
import com.askey.dvr.cdr7010.dashcam.core.camera2.CameraHelper;
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

    private static final String TAG_BASE = MediaMuxerWrapper.class.getSimpleName();
    private final String LOG_TAG;

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
    private MediaBuffer mMediaBuffer;
    private final HandlerThread mMuxerThread;
    private final MuxerHandler mMuxerHandler;
    private EventState mEventState;
    private final RecordConfig mConfig;

    private ISegmentListener mSegmentListener;
    private StateCallback mStateCallback;

    public static final int SAMPLE_TYPE_VIDEO = 1;
    public static final int SAMPLE_TYPE_AUDIO = 2;

    @IntDef({SAMPLE_TYPE_VIDEO, SAMPLE_TYPE_AUDIO})
    public @interface SampleType {
    }

    public interface SegmentCallback {
        boolean segmentStartPrepareSync(int event, long startTimeMs, String path);

        void segmentStartAsync(int event, long startTimeMs);

        void segmentCompletedSync(int event, String path);

        void segmentCompletedAsync(int event, long eventTimeMs, String path, long startTimeMs, long durationMs);

        void segmentTerminated();

        void segmentTerminatedWithReason(int event, int reason);
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
                Logg.d(LOG_TAG, "SD Card MEDIA_EJECT");
                terminateRecording();
            }
        }
    };

    /**
     * Constructor
     *
     * @throws IOException
     */
    public MediaMuxerWrapper(Context context,
                             @NonNull RecordConfig config,
                             final SegmentCallback segmentCallback,
                             final StateCallback stateCallback) throws IOException {
        LOG_TAG = TAG_BASE + "-" + config.cameraId();
        mContext = context.getApplicationContext();
        mConfig = config;
        mSegmentCallback = segmentCallback;
        mStateCallback = stateCallback;
        mEventState = new EventState(mContext);

        registerReceiver();

        mEncoderCount = mStatredCount = 0;
        mIsStarted = false;

        mHandlerThread = new HandlerThread("MuxerWorker-" + mConfig.cameraId());
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());

        mMuxerThread = new HandlerThread("MuxerThread-" + mConfig.cameraId());
        mMuxerThread.start();
        mMuxerHandler = new MuxerHandler(mMuxerThread.getLooper(), this);

        File cache = new File(DashCamApplication.getAppContext().getCacheDir().getAbsolutePath(), String.valueOf(mConfig.cameraId()));
        cleanDirectory(cache);
        cache.mkdirs();

        int bufferSize = (mConfig.cameraId() == CameraHelper.CAMERA_MAIN) ? (8 * 1024 * 1024 * 10 / 8) : (8 * 1024 * 1024 * 5 / 8);
        mMediaBuffer = new MediaBuffer(bufferSize, mMuxerHandler, cache.getAbsolutePath());
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.setPriority(1000); //[PUCDR-1412][PUCDR-1287] The highest priority
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addDataScheme("file");
        mContext.registerReceiver(mSDCardEjectReceiver, filter);
    }

    private void unregisterReceiver() {
        mContext.unregisterReceiver(mSDCardEjectReceiver);
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
        Logg.d(LOG_TAG, "terminateRecordeing");
        mReasonInterruption = true;
        if (mMediaBuffer != null) {
            mMediaBuffer.stop();
        }
        mMuxerHandler.removeMessages(0);
        mMuxerHandler.terminate();
        mMuxerThread.quit();
        mMuxerHandler.eventMuxer.terminate();
        if (mSegmentCallback != null) {
            Logg.d(LOG_TAG, "terminateRecordeing: segmentTerminated");
            mSegmentCallback.segmentTerminated();
        }
    }

    public void release() {
        if (mMediaBuffer != null) {
            mMediaBuffer.release();
            mMediaBuffer = null;
        }
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
     *
     * @return true when muxer is ready to write
     */
    /*package*/
    synchronized boolean start() {
        Logg.v(LOG_TAG, "start:");
        mStatredCount++;
        if ((mEncoderCount > 0) && (mStatredCount == mEncoderCount)) {
            mTotalDurationUs = 0;
            if (mMediaBuffer != null) {
                mMediaBuffer.reset();
                mMediaBuffer.start();
            }
            mIsStarted = true;
            mFirstStarPtsUs = -1;
            mReasonInterruption = false;
            notifyAll();
            Logg.v(LOG_TAG, "MediaMuxer started:");
            if (mStateCallback != null) {
                mStateCallback.onStartd();
            }
        }
        return mIsStarted;
    }

    /**
     * request stop recording from encoder when encoder received EOS
     */
    /*package*/
    synchronized void stop() {
        Logg.v(LOG_TAG, "stop:mStatredCount=" + mStatredCount);
        mStatredCount--;
        if ((mEncoderCount > 0) && (mStatredCount <= 0)) {
            mIsStarted = false;
            if (mMediaBuffer != null) {
                mMediaBuffer.stop();
            }
            mMuxerHandler.removeMessages(0);
            mMuxerHandler.stop();
            mMuxerThread.quitSafely();
            mMuxerHandler.eventMuxer.stop();

            unregisterReceiver();
            mEventState.release();

            if (mStateCallback != null) {
                if (mReasonInterruption) {
                    mStateCallback.onInterrupted();
                } else {
                    mStateCallback.onStoped();
                }
            }
        }
        mHandlerThread.quitSafely();
    }

    /*package*/
    synchronized void addTrackWithType(final @SampleType int type,
                                       final MediaFormat format) {
        if (type == SAMPLE_TYPE_AUDIO)
            mAudioFormat = format;
        else
            mVideoFormat = format;

        mMuxerHandler.eventMuxer.addTrack(type, format);
    }

    /*package*/
    synchronized void writeSampleDataWithType(final @SampleType int type,
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
                    Logg.e("iamlbccc", "Write event slice to cache file...");
                    Event event = mEventState.getEvent();
                    eventId = event.getId();
                    time = event.getTime();
                }
            }
        }
        if (mMediaBuffer != null) {
            mMediaBuffer.writeSampleData(type, eventId, time, byteBuf, bufferInfo);
        }
    }

    private void closeMuxer(AndroidMuxer muxer) {

        final AndroidMuxer tmpMuxer = muxer;
        if (mSegmentCallback != null) {
            mSegmentCallback.segmentCompletedSync(tmpMuxer.event(), tmpMuxer.filePath());
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    tmpMuxer.stop();
                } catch (final Exception e) {
                    Logg.e(LOG_TAG, "closeSegment - > failed stopping muxer", e);
                }

                if (mSegmentCallback != null) {
                    mSegmentCallback.segmentCompletedAsync(tmpMuxer.event(),
                            0,
                            tmpMuxer.filePath(),
                            tmpMuxer.startTimeMs(),
                            tmpMuxer.duration());
                }
            }
        });
    }


    private class MuxerHandler extends Handler {

        private AndroidMuxer muxer;
        private WeakReference<MediaMuxerWrapper> weakParent;
        private boolean flagTerm = false;
        private EventMuxer eventMuxer;
        private int slice_index = 0;
        private final Object syncObj = new Object();
        private boolean preperaEventRecording = false;

        MuxerHandler(Looper looper, MediaMuxerWrapper parent) {
            super(looper);
            weakParent = new WeakReference<>(parent);
            eventMuxer = new EventMuxer(parent.mContext, parent.mConfig, parent.mSegmentCallback, parent.mHandler);
        }

        void terminate() {
            flagTerm = true;
            synchronized (syncObj) {
                if (muxer != null) {
                    muxer.stop();
                    muxer = null;
                }

            }
        }

        void stop() {
            flagTerm = true;
            synchronized (syncObj) {
                if (muxer != null) {
                    MediaMuxerWrapper parent = weakParent.get();
                    if (parent != null) {
                        parent.closeMuxer(muxer);
                    }
                    muxer = null;
                }
            }
        }

        private void pauseContinuesRecording() {
            MediaMuxerWrapper parent = weakParent.get();

            synchronized (syncObj) {

                if (parent != null && muxer != null) {
                    preperaEventRecording = true;
                    parent.closeMuxer(muxer);
                    muxer = null;
                }
            }
        }

        void muxSampleData(int type, long time,
                           final ByteBuffer byteBuf,
                           final MediaCodec.BufferInfo bufferInfo) {

            if ((type == SAMPLE_TYPE_VIDEO) && ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0)) {
                final MediaMuxerWrapper parent = weakParent.get();
                if (parent == null) {
                    return;
                }

                synchronized (syncObj) {
                    if (muxer != null && muxer.duration() >= muxer.maxDuration()) {
                        parent.closeMuxer(muxer);
                        muxer = null;
                    }

                    if (muxer == null) {
                        try {
                            Logg.e("iamlbccc", "Starting of new normal, get file path. ");
                            String path = FileManager.getInstance(parent.mContext).getFilePathForNormal(parent.mConfig.cameraId(), time);
                            Logg.e("iamlbccc", "Starting of new normal, creating Muxer.");
                            muxer = new AndroidMuxer(path);
                            Logg.e("iamlbccc", "Starting of new normal, creating Muxer. Done");
                            if (parent.mSegmentCallback != null) {
                                Logg.e("iamlbccc", "Normal Start...  (sync)");
                                parent.mSegmentCallback.segmentStartPrepareSync(Event.ID_NONE, time, muxer.filePath());
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
                                        Logg.e("iamlbccc", "Normal Start...  (async)");
                                        parent.mSegmentCallback.segmentStartAsync(Event.ID_NONE, startTimeMs);
                                    }
                                }
                            });
                        } catch (RemoteException e) {
                            Logg.e(LOG_TAG, "fail to get file path from FileManager with error: "
                                    + e.getMessage());
                            return;
                        } catch (IOException e) {
                            Logg.e(LOG_TAG, "Fail to create mp4 muxer with exception: " + e.getMessage());
                            return;
                        }
                    }
                }
            }

            synchronized (syncObj) {
                if (muxer != null) {
                    muxer.writeSampleData(type, byteBuf, bufferInfo);
                }
            }
        }

        @Override
        public void handleMessage(Message msg) {
            String path = (String) msg.obj;
            Logg.e("iamlbccc", "handMessage -> " + msg.obj);
            File file = new File(path);
            RandomAccessFile fin = null;
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int eventId = Event.ID_NONE;
            long eventTime = 0;
            try {
                Logg.e("iamlbccc", "Normal Access cache file.");
                fin = new RandomAccessFile(file, "r");
                int index = fin.readInt();
                if (index - slice_index != 1) {
                    Logg.e(LOG_TAG, "slice index error: prev=" + slice_index + "  current=" + index);
                }
                slice_index = index;
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
                        preperaEventRecording = false;
                        break;
                    }

                    if (preperaEventRecording) {
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
                        Logg.e("iamlbccc", "Switch to event. pausing continues recording.");
                        // stop continues recording
                        eventId = event;
                        eventTime = time;
                        pauseContinuesRecording();
                        Logg.e("iamlbccc", "Switch to event. pausing continues recording. Done");
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

        for (File file : directory.listFiles()) {
            if (file.isDirectory())
                cleanDirectory(file);
            else
                file.delete();
        }
    }
}
