package com.askey.dvr.cdr7010.dashcam.core.jni;

import android.media.MediaCodec;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import java.nio.ByteBuffer;

public class MediaBuffer {

    private Handler mHandler;
    private long mNativeObject;

    static {
        System.loadLibrary("mediabuffer-jni");
        class_init_native();
    }

    public MediaBuffer(int size, Handler handler, @NonNull String workingDir) {
        mHandler = handler;
        mNativeObject = native_init(size, workingDir);
    }

    public void start() {
        native_start(mNativeObject);
    }

    public void stop() {
        native_stop(mNativeObject);
    }

    public void reset() {
        native_reset(mNativeObject);
    }

    public void release() {
        native_release(mNativeObject);
    }

    public void writeSampleData(int type, int event, long time, @NonNull ByteBuffer byteBuf, MediaCodec.BufferInfo info) {
        try {
            native_writeSampleData(mNativeObject, type, event, time, byteBuf, info.offset, info.size, info.presentationTimeUs, info.flags);
        } catch (Exception e) {
            Log.d("MediaBuffer", e.getMessage());
        }
    }

    // called from JNI
    private void reportCacheFile(String path) {
        if (mHandler != null) {
            mHandler.sendMessage(mHandler.obtainMessage(0, path));
        }
    }

    private native static void class_init_native();

    private native long native_init(int bufferSize, String workingDir);
    private native void native_start(long nativeObject);
    private native void native_stop(long nativeObject);
    private native void native_reset(long nativeObject);
    private native void native_release(long nativeObject);
    private native void native_writeSampleData(long nativeObject, int type, int event, long time, @NonNull ByteBuffer byteBuf,
                                              int offset, int size, long presentationTimeUs, int flags);

}
