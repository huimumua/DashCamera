package com.askey.dvr.cdr7010.dashcam.core.recorder;

import android.content.Context;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;

import com.askey.dvr.cdr7010.dashcam.core.RecordConfig;
import com.askey.dvr.cdr7010.dashcam.core.encoder.IFrameListener;
import com.askey.dvr.cdr7010.dashcam.core.encoder.MediaAudioEncoder;
import com.askey.dvr.cdr7010.dashcam.core.encoder.MediaEncoder;
import com.askey.dvr.cdr7010.dashcam.core.encoder.MediaMuxerWrapper;
import com.askey.dvr.cdr7010.dashcam.core.encoder.MediaVideoEncoder;
import com.askey.dvr.cdr7010.dashcam.core.nmea.NmeaRecorder;
import com.askey.dvr.cdr7010.dashcam.service.FileManager;
import com.askey.dvr.cdr7010.dashcam.util.AESCryptUtil;
import com.askey.dvr.cdr7010.dashcam.util.FileUtils;
import com.askey.dvr.cdr7010.dashcam.util.HashUtil;
import com.askey.dvr.cdr7010.dashcam.util.Logg;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class Recorder implements IFrameListener {
    private final static String TAG_BASE = Recorder.class.getSimpleName();
    private final String TAG;
    private static final String AES_KEY = "CaH5U?<5no_z3S,0Zx,8Ua<0Qo&5Ep/0";

    private Context mContext;
    private RecordConfig mConfig;
    private MediaVideoEncoder mVideoEncoder;
    private MediaAudioEncoder mAudioEncoder;
    private MediaMuxerWrapper mMuxer;
    private HashMap<String, NmeaRecorder> mNmeaMap;

    private final Object mSync = new Object();

    private StateCallback mStateCallback;

    public interface StateCallback {
        void onStarted();

        void onStoped();

        void onInterrupted();

        void onEventStateChanged(boolean on);

        void onEventCompleted(int eventId, long timestamp, List<String> pictures, String video);

        void onEventTerminated(int eventId, int reason);
    }

    public Recorder(@NonNull Context context,
                    @NonNull RecordConfig config,
                    @Nullable StateCallback callback) {
        mContext = context.getApplicationContext();
        mConfig = config;
        mStateCallback = callback;
        TAG = TAG_BASE + "-" + config.cameraId();
    }

    public void prepare() throws IOException {
        mMuxer = new MediaMuxerWrapper(mContext, mConfig, mSegmentCallback, mMuxerStateCallback);

        mVideoEncoder = new MediaVideoEncoder(mMuxer,
                mMediaEncoderListener,
                mConfig.videoWidth(),
                mConfig.videoHeight(),
                mConfig.videoFPS(),
                mConfig.videoBitRate());

        if (mConfig.audioRecordEnable()) {
            mAudioEncoder = new MediaAudioEncoder(mMuxer, mConfig.audioMute(), mMediaEncoderListener);
        }

        mMuxer.prepare();
        if (mConfig.nmeaRecordEnable()) {
            mNmeaMap = new LinkedHashMap<>();
            NmeaRecorder.init(mContext);
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
        mMuxer.release();
        mMuxer = null;
        mVideoEncoder = null;
        mAudioEncoder = null;
        if (mConfig.nmeaRecordEnable()) {
            NmeaRecorder.deinit(mContext);
        }
    }

    public void mute() {
        if (mAudioEncoder != null) {
            mAudioEncoder.mute();
        }
    }

    public void demute() {
        if (mAudioEncoder != null) {
            mAudioEncoder.demute();
        }
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
        public boolean segmentStartPrepareSync(int event, long startTime, String path) {

            Logg.v(TAG, "segmentStartPrepareSync: event=" + event + " startTime" + startTime + " " + path);
            // 注意：禁止在这里进行耗时操作
            if (mConfig.nmeaRecordEnable()) {
                try {
                    String nmeaPath;
                    if (event == 0) {
                        nmeaPath = FileManager.getInstance(mContext).getFilePathForNmeaNormal(mConfig.cameraId(), startTime);
                    } else {
                        nmeaPath = FileManager.getInstance(mContext).getFilePathForNmeaEvent(mConfig.cameraId(), startTime);
                    }
                    Logg.i(TAG, "nmea path = " + nmeaPath);
                    NmeaRecorder nmea = NmeaRecorder.create(nmeaPath);
                    if (nmea != null) {
                        if (event == 0) {
                            nmea.start(startTime, 60);
                        } else {
                            nmea.eventStart(startTime, event);
                        }
                        mNmeaMap.put(path, nmea);
                    }
                } catch (RemoteException e) {
                    Logg.e(TAG, "create NMEA file error. exception: " + e.getMessage());
                }
            }

            if (mStateCallback != null) {
                Logg.e("iamlbccc", "onEventStateChanged...");
                mStateCallback.onEventStateChanged(event != 0);
            }
            return true;
        }

        @Override
        public void segmentStartAsync(int event, long startTimeMs) {
            Logg.v(TAG, "segmentStartAsync: startTimeMs=" + startTimeMs + ",event=" + event);
        }

        @Override
        public void segmentCompletedSync(int event, String path) {
            // 注意：禁止在这里进行耗时操作
            if (mConfig.nmeaRecordEnable()) {
                NmeaRecorder nmea = mNmeaMap.remove(path);
                if (nmea != null && NmeaRecorder.RecorderState.STARTED == nmea.getState()) {
                    nmea.stop();
                }
            }
        }

        @Override
        public void segmentCompletedAsync(int event, final long eventTimeMs, final String path, final long startTimeMs, long durationMs) {
            Logg.v(TAG, "segmentCompletedAsync: event=" + event + " eventTimeMs=" + eventTimeMs + " startTimeMs=" + startTimeMs + " " + path);
            if (event != 0) {
                saveHash(path, startTimeMs, true);
                Snapshot.take3Pictures(mContext, path, mConfig.cameraId(), startTimeMs, 7 * 1000 * 1000L, FileManager.getInstance(mContext), pictures -> {
                    if (pictures != null) {
                        for (String pic : pictures) {
                            Logg.d(TAG, pic);
                        }
                    }
                    if (mStateCallback != null) {
                        mStateCallback.onEventCompleted(event, startTimeMs, pictures, path);
                    }
                });
            } else {
                saveHash(path, startTimeMs, false);
            }
        }

        @Override
        public void segmentTerminated() {
            Logg.d(TAG, "segmentTerminated");

            if (mConfig.nmeaRecordEnable()) {
                for (NmeaRecorder nmea : mNmeaMap.values()) {
                    Logg.d(TAG, "nmea loop");

                    if (nmea != null && NmeaRecorder.RecorderState.STARTED == nmea.getState()) {
                        Logg.d(TAG, "nmea.stop");
                        nmea.stop();
                    }
                }
                mNmeaMap.clear();
            }
        }

        @Override
        public void segmentTerminatedWithReason(int event, int reason) {
            if (mStateCallback != null) {
                mStateCallback.onEventTerminated(event, reason);
            }
        }
    };

    private void saveHash(String path, long time, boolean isEvent) {
        try {
            String desPath;
            String fileName = getFileNameFromPath(path);
            Log.d(TAG, "fileName==" + fileName);
            if (fileName != null) {
                if (isEvent) {
                    desPath = FileManager.getInstance(mContext).getFilePathForHashEvent(fileName);
                } else {
                    desPath = FileManager.getInstance(mContext).getFilePathForHashNormal(fileName);
                }
                Logg.d(TAG, "desPath==" + desPath + ",time==" + time);
                if (!TextUtils.isEmpty(desPath)) {
                    String sha256 = HashUtil.getSHA256(new File(path));
                    Logg.d(TAG, "sha256==" + sha256);
                    if (sha256 != null) {
                        String encrypt = AESCryptUtil.encrypt(AES_KEY, sha256);
                        Logg.d(TAG, "encrypt==" + encrypt);
                        FileUtils.writeFile(desPath, encrypt, false);
                    }
                }
            }
        } catch (Exception e) {
            Logg.d(TAG, "error happened when save hash");
        }
    }

    private String getFileNameFromPath(String path) {
        if (path == null || !path.contains("/") || !path.contains(".")) {
            return null;
        }
        int start = path.lastIndexOf("/");
        int end = path.lastIndexOf(".");
        return path.substring(start + 1, end);
    }

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
