package com.askey.dvr.cdr7010.dashcam.core.encoder;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.askey.dvr.cdr7010.dashcam.core.encoder.MediaMuxerWrapper.SegmentCallback;
import com.askey.dvr.cdr7010.dashcam.core.event.Event;
import com.askey.dvr.cdr7010.dashcam.service.FileManager;
import com.askey.dvr.cdr7010.dashcam.util.Logg;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;

import static com.askey.dvr.cdr7010.dashcam.core.encoder.MediaMuxerWrapper.SAMPLE_TYPE_AUDIO;
import static com.askey.dvr.cdr7010.dashcam.core.encoder.MediaMuxerWrapper.SAMPLE_TYPE_VIDEO;

public class EventMuxer implements Runnable{

    private final static String LOG_TAG = "EventMuxer";

    private final Thread mThread;
    private ArrayBlockingQueue<Slice> mInputQueue = new ArrayBlockingQueue<>(100);
    private MediaFormat mVideoFormat;
    private MediaFormat mAudioFormat;
    private AndroidMuxer mMuxer;
    private boolean mFlagTerm = false;
    private boolean mFlagStop = false;
    private Context mContext;
    private final SegmentCallback mCallback;
    private final Handler mHandler;

    @Override
    public void run() {
        Logg.d(LOG_TAG, "run");
        int sliceCount = 0;
        int eventId = Event.ID_NONE;
        long eventTime = 0;
        LinkedList<Slice> eventSlices = new LinkedList<>();

        mInputQueue.clear();

        while (true) {
            if (mFlagTerm) {
                break;
            }

            if ((eventId == Event.ID_NONE)) {
                try {
                    while (eventSlices.size() > 10) {
                        Slice s1 = eventSlices.poll();
                        new File(s1.file).delete();
                        if (mFlagTerm || mFlagStop) {
                            break;
                        }
                    }

                    Slice s = mInputQueue.take();
                    if (mFlagTerm || mFlagStop) {
                        break;
                    }
                    if (s.eventId != Event.ID_NONE) { // event rise
                        eventId = s.eventId;
                        eventTime = s.eventTime;
                        sliceCount = 0;
                    }
                    if (s.file != null) {
                        eventSlices.add(new Slice(s.eventId, s.eventTime, s.file));
                    }
                } catch (InterruptedException e) {
                }
            } else if (eventSlices.size() < 15) {
                try {
                    Slice s = mInputQueue.take();
                    if (mFlagTerm) {
                        break;
                    }
                    eventSlices.add(new Slice(s.eventId, s.eventTime, s.file));
                } catch (InterruptedException e) {
                }
            }

            if (eventId != Event.ID_NONE && sliceCount < 15) {
                Slice s = eventSlices.get(sliceCount++);
                if (s != null) {
                    if (mMuxer == null) {
                        try {
                            String path = FileManager.getInstance(mContext).getFilePathForEvent(eventTime);
                            mMuxer = createMuxer(path, eventId, eventTime);
                        } catch (RemoteException e) {
                            Logg.e(LOG_TAG, "fail to get file path from FileManager with error: "
                                    + e.getMessage());
                        }
                    }

                    if (mFlagTerm) {
                        break;
                    }

                    if (mMuxer != null) {
                        if (s.file != null) {
                            writeSampleDataFromSlice(s.file);
                        }

                        if (sliceCount >= 15 || mFlagStop) {
                            final AndroidMuxer muxer = mMuxer;
                            final int event = eventId;
                            final long time = eventTime;
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    muxer.stop();
                                    if (mCallback != null) {
                                        mCallback.segmentCompletedAsync(event, time, muxer.filePath(), muxer.startTimeMs(), muxer.duration()/1000L);
                                    }
                                }
                            });

                            sliceCount = 0;
                            mMuxer = null;
                            eventId = Event.ID_NONE;
                        }
                    }
                }
            }
        }

        if (mMuxer != null) {
            mMuxer.stop();
            mMuxer = null;
        }
        mInputQueue.clear();
        Logg.d(LOG_TAG, "EventMuxer thread exit");
    }

    EventMuxer(@NonNull Context context,
               @Nullable final SegmentCallback callback,
               Handler handler) {
        mContext = context.getApplicationContext();
        mCallback = callback;
        mHandler = handler;

        mThread = new Thread(this, "EvtRecThread");
        mThread.start();
    }

    void addTrack(int type, MediaFormat format) {
        if (type == SAMPLE_TYPE_AUDIO)
            mAudioFormat = format;
        else
            mVideoFormat = format;
    }

    void stop() {
        Logg.d(LOG_TAG, "stop");
        mFlagStop = true;
        mInputQueue.add(new Slice(0, 0, null));
    }

    void terminate() {
        Logg.d(LOG_TAG, "terminate");
        mFlagTerm = true;
        mInputQueue.add(new Slice(0, 0, null));
    }

    void release() {
    }

    void feed(int eventId, long time, String file) {
        mInputQueue.add(new Slice(eventId, time, file));
    }

    boolean isRecording() {
        return mMuxer != null;
    }

    AndroidMuxer createMuxer(final String path, final int eventId, final long eventTime) {
        AndroidMuxer muxer = null;
        try {
            if (mCallback != null) {
                mCallback.segmentStartPrepareSync(eventId, path);
            }
            muxer = new AndroidMuxer(path);
            muxer.addTrack(SAMPLE_TYPE_VIDEO, mVideoFormat);
            muxer.addTrack(SAMPLE_TYPE_AUDIO, mAudioFormat);
            muxer.setMaxDuration(15 * 1000L);
            muxer.start(eventTime);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mCallback != null) {
                        mCallback.segmentStartAsync(eventId, eventTime);
                    }
                }
            });
        } catch (IOException e) {
            Logg.e(LOG_TAG, "Fail to create muxer");
        }
        return muxer;
    }

    private void writeSampleDataFromSlice(String slice) {
        File file = new File(slice);
        RandomAccessFile fin = null;
        try {
            fin = new RandomAccessFile(file, "r");
            byte[] array = null;
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            while (true) {
                if (mFlagTerm) break;
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
                bufferInfo.flags = flags;
                bufferInfo.presentationTimeUs = pts;
                bufferInfo.offset = 0;
                bufferInfo.size = len;
                ByteBuffer buffer = ByteBuffer.wrap(array);
                mMuxer.writeSampleData(type, buffer, bufferInfo);
            }
        } catch (FileNotFoundException e) {
            Logg.e(LOG_TAG, "open fail:  " + slice);
            return;
        } catch (EOFException e) {
        } catch (IOException e) {
            Logg.e(LOG_TAG, "read file: " + slice + e.getMessage());
        }

        try {
            fin.close();
        } catch (IOException e) {
            Logg.e(LOG_TAG, "close fail:  " + slice);
        }
    }

    private static class Slice {
        Slice(int eventId, long time, String file) {
            this.eventId = eventId;
            this.eventTime = time;
            this.file = file;
        }
        int eventId;
        long eventTime;
        String file;
    }
}

