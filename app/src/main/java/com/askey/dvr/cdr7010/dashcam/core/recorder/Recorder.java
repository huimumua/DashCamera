package com.askey.dvr.cdr7010.dashcam.core.recorder;

import android.content.Context;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
    private final static String TAG = "Recorder";
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
            mMuxer = new MediaMuxerWrapper(mContext, mConfig, mSegmentCallback, mMuxerStateCallback);
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

        if (mConfig.audioRecordEnable()) {
            mAudioEncoder = new MediaAudioEncoder(mMuxer, mConfig.audioMute(), mMediaEncoderListener);
        }

        try {
            mMuxer.prepare();
            if (mConfig.nmeaRecordEnable()) {
                mNmeaMap = new LinkedHashMap<>();
                NmeaRecorder.init(mContext);
            }
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
            Logg.v(TAG, "segmentStartPrepareSync: event=" + event + " " + path);
            // 注意：禁止在这里进行耗时操作
            if (mConfig.nmeaRecordEnable()) {
                try {
                    String nmeaPath;
                    if (event == 0) {
                        nmeaPath = FileManager.getInstance(mContext).getFilePathForNmeaNormal(startTime);
                    } else {
                        nmeaPath = FileManager.getInstance(mContext).getFilePathForNmeaEvent(startTime);
                    }
                    Logg.i(TAG, "nmea path = " + nmeaPath);
                    NmeaRecorder nmea = NmeaRecorder.create(nmeaPath);
                    if (nmea != null) {
                        if (event == 0) {
                            nmea.start(startTime, 60);
                        } else {
                            nmea.eventStart(startTime);
                        }
                        mNmeaMap.put(path, nmea);
                    }
                } catch (RemoteException e) {
                    Logg.e(TAG, "create NMEA file error. exception: " + e.getMessage());
                }
            }

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
            Logg.v(TAG, "segmentCompletedAsync: event=" + event + " eventTimeMs=" + eventTimeMs + " " + path);
            if (event != 0) {
                saveHash(path, startTimeMs, true);
                Snapshot.take3Pictures(path, mConfig.cameraId(), startTimeMs, 7 * 1000 * 1000L, FileManager.getInstance(mContext), pictures -> {
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
            if (mConfig.nmeaRecordEnable()) {
                for (NmeaRecorder nmea : mNmeaMap.values()) {
                    if (nmea != null && NmeaRecorder.RecorderState.STARTED == nmea.getState()) {
                        nmea.stop();
                    }
                }
                mNmeaMap.clear();
            }
        }
    };

    private void saveHash(String path, long time, boolean isEvent) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                String sha256 = HashUtil.getSHA256(new File(path));
                Logg.d(TAG, "sha256==" + sha256);
//        String des = (String) SPUtils.get(mContext, SPUtils.STR_ENCODE, "");
//        Logg.d(TAG, "des==" + des);
//        if (!TextUtils.isEmpty(des)) {
                try {
//            String aesKey = KeyStoreUtils.getInstance().decryptByPrivateKey(des);
//            Logg.d(TAG, "aesKey==" + aesKey);
                    String encrypt = AESCryptUtil.encrypt(AES_KEY, sha256);
                    Logg.d(TAG, "encrypt==" + encrypt);
                    String desPath;
                    if (isEvent) {
                        desPath = FileManager.getInstance(mContext).getFilePathForHashEvent(time);
                    } else {
                        desPath = FileManager.getInstance(mContext).getFilePathForHashNormal(time);
                    }
                    Logg.d(TAG, "desPath==" + desPath);
                    if (desPath != null) {
                        FileUtils.writeFile(desPath, encrypt, false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
//        }
            }
        }.start();
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
