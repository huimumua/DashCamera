package com.askey.dvr.cdr7010.dashcam.core.jni;

import android.media.MediaCodec;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import java.nio.ByteBuffer;

public class MediaBuffer {

    private Handler mHandler;

    static {
        System.loadLibrary("mediabuffer-jni");
        class_init_native();
    }

    public MediaBuffer(Handler handler, @NonNull String workingDir) {
        mHandler = handler;
        native_init(6000 * 1024 * 10 / 8, workingDir);
    }

    public void writeSampleData(int type, int event, long time, @NonNull ByteBuffer byteBuf, MediaCodec.BufferInfo info) {
        try {
            nativeWriteSampleData(type, event, time, byteBuf, info.offset, info.size, info.presentationTimeUs, info.flags);
        } catch (Exception e) {
            Log.d("MediaBuffer", e.getMessage());
        }
    }

    @Override
    protected void finalize() throws Throwable {
        release();
        super.finalize();
    }

    // called from JNI
    private void reportCacheFile(String path) {
        if (mHandler != null) {
            mHandler.sendMessage(mHandler.obtainMessage(0, path));
        }
    }

    private native static void class_init_native();
    public native void start();
    public native void stop();
    public native void reset();
    public native void release();

    private native void native_init(int bufferSize, String workingDir);
    private native void nativeWriteSampleData(int type, int event, long time, @NonNull ByteBuffer byteBuf,
                                              int offset, int size, long presentationTimeUs, int flags);

}
