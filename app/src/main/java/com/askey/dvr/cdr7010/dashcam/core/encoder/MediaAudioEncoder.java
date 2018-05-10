package com.askey.dvr.cdr7010.dashcam.core.encoder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.MediaRecorder;

import com.askey.dvr.cdr7010.dashcam.util.Logg;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MediaAudioEncoder extends MediaEncoder {
    private static final String LOG_TAG = "AudioEncoder";
    private static final String MIME_TYPE = "audio/mp4a-latm";
    private static final int SAMPLE_RATE = 44100;   // 44.1[KHz] is only setting guaranteed to be available on all devices.
    private static final int BIT_RATE = 64000;
    private static final int SAMPLES_PER_FRAME = 1024;   // AAC, bytes/frame/channel
    private static final int FRAMES_PER_BUFFER = 25;     // AAC, frame/buffer/sec
    private AudioThread mAudioThread = null;
    private volatile boolean mIsSampling = false;

    public MediaAudioEncoder(final MediaMuxerWrapper muxer, final MediaEncoderListener listener) {
        super(muxer, listener);
    }

    @Override
    protected void prepare() throws IOException {
        Logg.v(LOG_TAG, "prepare:");
        mMuxerStarted = mIsEOS = false;
        // prepare MediaCodec for AAC encoding of audio data from inernal mic.
        final MediaCodecInfo audioCodecInfo = selectAudioCodec(MIME_TYPE);
        if (audioCodecInfo == null) {
            Logg.e(LOG_TAG, "Unable to find an appropriate codec for " + MIME_TYPE);
            return;
        }
        Logg.i(LOG_TAG, "selected codec: " + audioCodecInfo.getName());

        final MediaFormat audioFormat = MediaFormat.createAudioFormat(MIME_TYPE, SAMPLE_RATE, 1);
        audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        audioFormat.setInteger(MediaFormat.KEY_CHANNEL_MASK, AudioFormat.CHANNEL_IN_MONO);
        audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);
        audioFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);
//      audioFormat.setLong(MediaFormat.KEY_MAX_INPUT_SIZE, inputFile.length());
//      audioFormat.setLong(MediaFormat.KEY_DURATION, (long)durationInMs );
        Logg.i(LOG_TAG, "format: " + audioFormat);
        mMediaCodec = MediaCodec.createEncoderByType(MIME_TYPE);
        mMediaCodec.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mMediaCodec.start();
        Logg.i(LOG_TAG, "prepare finishing");
        if (mListener != null) {
            try {
                mListener.onPrepared(this);
            } catch (final Exception e) {
                Logg.e(LOG_TAG, "prepare:", e);
            }
        }
    }

    @Override
    protected void startRecording() {
        super.startRecording();
        // create and execute audio capturing thread using internal mic
        resume();
    }

    public synchronized void resume() {
        if (mAudioThread == null && !mIsSampling) {
            mIsSampling = true;
            mAudioThread = new AudioThread();
            mAudioThread.start();
        }
    }

    public synchronized void pause() {
        if (mAudioThread != null && mIsSampling) {
            mIsSampling = false;
            try {
                mAudioThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mAudioThread = null;
        }
    }

    @Override
    public boolean isSilent() {
        return !mIsSampling;
    }

    @Override
    protected void release() {
        mAudioThread = null;
        super.release();
    }

    private static final int[] AUDIO_SOURCES = new int[] {
        MediaRecorder.AudioSource.MIC,
        MediaRecorder.AudioSource.DEFAULT,
        MediaRecorder.AudioSource.CAMCORDER,
        MediaRecorder.AudioSource.VOICE_COMMUNICATION,
        MediaRecorder.AudioSource.VOICE_RECOGNITION,
    };

    /**
     * Thread to capture audio data from internal mic as uncompressed 16bit PCM data
     * and write them to the MediaCodec encoder
     */
    private class AudioThread extends Thread {
        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
            try {
                final int min_buffer_size = AudioRecord.getMinBufferSize(
                    SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
                int buffer_size = SAMPLES_PER_FRAME * FRAMES_PER_BUFFER;
                if (buffer_size < min_buffer_size)
                    buffer_size = ((min_buffer_size / SAMPLES_PER_FRAME) + 1) * SAMPLES_PER_FRAME * 2;

                AudioRecord audioRecord = null;
                for (final int source : AUDIO_SOURCES) {
                    try {
                        audioRecord = new AudioRecord(
                            source, SAMPLE_RATE,
                            AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, buffer_size);
                        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED)
                            audioRecord = null;
                    } catch (final Exception e) {
                        audioRecord = null;
                    }
                    if (audioRecord != null) break;
                }
                if (audioRecord != null) {
                    try {
                        if (mIsCapturing) {
                             Logg.v(LOG_TAG, "AudioThread:start audio recording");
                            final ByteBuffer buf = ByteBuffer.allocateDirect(SAMPLES_PER_FRAME);
                            int readBytes;
                            audioRecord.startRecording();
                            try {
                                for (;  mIsSampling && mIsCapturing && !mRequestStop && !mIsEOS ;) {
                                    // read audio data from internal mic
                                    buf.clear();
                                    readBytes = audioRecord.read(buf, SAMPLES_PER_FRAME);
                                    if (readBytes > 0) {
                                        // set audio data to encoder
                                        buf.position(readBytes);
                                        buf.flip();
                                        encode(buf, readBytes, getPTSUs());
                                        frameAvailableSoon();
                                    }
                                }
                                frameAvailableSoon();
                            } finally {
                                audioRecord.stop();
                            }
                        }
                    } finally {
                        audioRecord.release();
                        mIsSampling = false;
                    }
                } else {
                    Logg.e(LOG_TAG, "failed to initialize AudioRecord");
                }
            } catch (final Exception e) {
                Logg.e(LOG_TAG, "AudioThread#run", e);
            }
            Logg.v(LOG_TAG, "AudioThread:finished");
        }
    }

    /**
     * select the first codec that match a specific MIME type
     * @param mimeType
     * @return
     */
    private static final MediaCodecInfo selectAudioCodec(final String mimeType) {
        Logg.v(LOG_TAG, "selectAudioCodec:");

        MediaCodecInfo result = null;
        // get the list of available codecs
        MediaCodecList codecList = new MediaCodecList(MediaCodecList.ALL_CODECS);
        MediaCodecInfo[] codecInfos = codecList.getCodecInfos();
LOOP:   for (MediaCodecInfo codecInfo : codecInfos) {
            if (!codecInfo.isEncoder()) {   // skipp decoder
                continue;
            }
            final String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                Logg.i(LOG_TAG, "supportedType:" + codecInfo.getName() + ",MIME=" + types[j]);
                if (types[j].equalsIgnoreCase(mimeType)) {
                    if (result == null) {
                        result = codecInfo;
                        break LOOP;
                    }
                }
            }
        }
        return result;
    }

    @Override
    protected int getSampleType() {
        return MediaMuxerWrapper.SAMPLE_TYPE_AUDIO;
    }

    @Override
    protected String getName() {
        return MediaAudioEncoder.class.getSimpleName();
    }
}
