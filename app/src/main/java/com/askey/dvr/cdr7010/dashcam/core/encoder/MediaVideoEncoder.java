package com.askey.dvr.cdr7010.dashcam.core.encoder;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.view.Surface;

import com.askey.dvr.cdr7010.dashcam.util.Logg;

import java.io.IOException;

public class MediaVideoEncoder extends MediaEncoder {
	private static final String LOG_TAG = "MediaVideoEncoder";
	private static final String MIME_TYPE = "video/avc";
	// parameters for recording
    private static final int FRAME_RATE = 27;
	private static final float BPP = 0.25f;
    private final int mWidth;
    private final int mHeight;
    private Surface mSurface;

	public MediaVideoEncoder(final MediaMuxerWrapper muxer, final MediaEncoderListener listener, final int width, final int height) {
		super(muxer, listener);
		 Logg.i(LOG_TAG, "MediaVideoEncoder: width="+width + ", height="+height);
		mWidth = width;
		mHeight = height;
	}

	@Override
	protected void prepare() throws IOException {
		 Logg.i(LOG_TAG, "prepare: ");
        mMuxerStarted = mIsEOS = false;
        final MediaCodecInfo videoCodecInfo = selectVideoCodec(MIME_TYPE);
        if (videoCodecInfo == null) {
            Logg.e(LOG_TAG, "Unable to find an appropriate codec for " + MIME_TYPE);
            return;
        }
		Logg.i(LOG_TAG, "selected codec: " + videoCodecInfo.getName());

        final MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, mWidth, mHeight);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);	// API >= 18
        format.setInteger(MediaFormat.KEY_BIT_RATE, calcBitRate());
        format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
		Logg.i(LOG_TAG, "format: " + format);

        mMediaCodec = MediaCodec.createEncoderByType(MIME_TYPE);
        mMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        // get Surface for encoder input
        // this method only can call between #configure and #start
        mSurface = mMediaCodec.createInputSurface();	// API >= 18
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

	public Surface getInputSurface() {
		return mSurface;
	}

	@Override
    protected void release() {
		Logg.i(LOG_TAG, "release:");
		if (mSurface != null) {
			mSurface.release();
			mSurface = null;
		}
		super.release();
	}

	private int calcBitRate() {
		final int bitrate = (int)(BPP * FRAME_RATE * mWidth * mHeight);
		Logg.i(LOG_TAG, String.format("bitrate=%5.2f[Mbps]", bitrate / 1024f / 1024f));
		return bitrate;
	}

    /**
     * select the first codec that match a specific MIME type
     * @param mimeType
     * @return null if no codec matched
     */
    protected static final MediaCodecInfo selectVideoCodec(final String mimeType) {
    	Logg.v(LOG_TAG, "selectVideoCodec:");

    	// get the list of available codecs
		MediaCodecList codecList = new MediaCodecList(MediaCodecList.ALL_CODECS);
		MediaCodecInfo[] codecInfos = codecList.getCodecInfos();
		for (MediaCodecInfo codecInfo : codecInfos) {

            if (!codecInfo.isEncoder()) {	// skipp decoder
                continue;
            }
            // select first codec that match a specific MIME type and color format
            final String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                	Logg.i(LOG_TAG, "codec:" + codecInfo.getName() + ",MIME=" + types[j]);
            		final int format = selectColorFormat(codecInfo, mimeType);
                	if (format > 0) {
                		return codecInfo;
                	}
                }
            }
        }
        return null;
    }

    /**
     * select color format available on specific codec and we can use.
     * @return 0 if no colorFormat is matched
     */
    protected static final int selectColorFormat(final MediaCodecInfo codecInfo, final String mimeType) {
		Logg.i(LOG_TAG, "selectColorFormat: ");
    	int result = 0;
    	final MediaCodecInfo.CodecCapabilities caps;
    	try {
    		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
    		caps = codecInfo.getCapabilitiesForType(mimeType);
    	} finally {
    		Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
    	}
        int colorFormat;
        for (int i = 0; i < caps.colorFormats.length; i++) {
        	colorFormat = caps.colorFormats[i];
            if (isRecognizedViewoFormat(colorFormat)) {
            	if (result == 0)
            		result = colorFormat;
                break;
            }
        }
        if (result == 0)
        	Logg.e(LOG_TAG, "couldn't find a good color format for " + codecInfo.getName() + " / " + mimeType);
        return result;
    }

	/**
	 * color formats that we can use in this class
	 */
    protected static int[] recognizedFormats;
	static {
		recognizedFormats = new int[] {
//        	MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar,
//        	MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar,
//        	MediaCodecInfo.CodecCapabilities.COLOR_QCOM_FormatYUV420SemiPlanar,
        	MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface,
		};
	}

    private static final boolean isRecognizedViewoFormat(final int colorFormat) {
		Logg.i(LOG_TAG, "isRecognizedViewoFormat:colorFormat=" + colorFormat);
    	final int n = recognizedFormats != null ? recognizedFormats.length : 0;
    	for (int i = 0; i < n; i++) {
    		if (recognizedFormats[i] == colorFormat) {
    			return true;
    		}
    	}
    	return false;
    }

    @Override
    protected void signalEndOfInputStream() {
		Logg.d(LOG_TAG, "sending EOS to encoder");
		try {
			mMediaCodec.signalEndOfInputStream();	// API >= 18
		} catch (IllegalStateException e) {
			Logg.e(LOG_TAG, "IllegalStateException in MediaCodec.signalEndOfInputStream");
		}

		mIsEOS = true;
	}

	@Override
	protected int getSampleType() {
		return MediaMuxerWrapper.SAMPLE_TYPE_VIDEO;
	}

	@Override
	protected String getName() {
		return MediaVideoEncoder.class.getSimpleName();
	}

}
