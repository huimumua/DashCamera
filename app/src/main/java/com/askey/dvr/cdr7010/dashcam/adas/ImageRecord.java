package com.askey.dvr.cdr7010.dashcam.adas;

import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v4.util.Pools;

import static com.askey.dvr.cdr7010.dashcam.adas.AdasController.BUFFER_NUM;

class ImageRecord {
    private static final Pools.SynchronizedPool<ImageRecord> sPool;
    private long mTimeStamp;
    private Image mImage;

    static {
        // BUFFER_NUM is enough because it define the maximum image buffer
        sPool = new Pools.SynchronizedPool<>(BUFFER_NUM);
        for (int i = 0; i < BUFFER_NUM; i++) {
            sPool.release(new ImageRecord());
        }
    }
    public static ImageRecord obtain(long timestamp, @NonNull Image image) {
        ImageRecord instance = sPool.acquire();
        if (instance == null) {
            throw new NullPointerException("Impossible!");
        }
        instance.mTimeStamp = timestamp;
        instance.mImage = image;
        return instance;
    }

    private ImageRecord() {

    }

    public Image getImage() {
        return mImage;
    }
    public long getTimestamp() {
        return mTimeStamp;
    }

    public void close() {
        recycle();
    }

    public void recycle() {
        mImage.close();
        sPool.release(this);
    }

    @Override
    public String toString() {
        return "ImageRecord{" +
                "mTimeStamp=" + mTimeStamp +
                ", mImage=" + mImage +
                '}';
    }
}
