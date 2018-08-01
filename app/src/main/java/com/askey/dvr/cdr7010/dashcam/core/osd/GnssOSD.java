package com.askey.dvr.cdr7010.dashcam.core.osd;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Handler;
import android.os.Message;

import com.askey.dvr.cdr7010.dashcam.service.GPSStatusManager;
import com.askey.dvr.cdr7010.dashcam.util.LocationUtil;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class GnssOSD extends BaseOSD {
    private static int WIDTH = 225;
    private static int HEIGHT = 32;
    private static int FONT_SIZE = 14;
    private static int TEXT_WIDTH = 225;
    private static int TEXT_HEIGHT = 32;

    private Context mContext;
    private Bitmap mBitmap;
    private final Canvas mCanvas;
    private final Paint mPaint;
    private double mLatitude;
    private double mLongitude;
    private StringBuilder mStringBuilder = new StringBuilder(10);
    private TimerTask mTask;
    private final Timer mTimer = new Timer();
    private GnssRenderHandler gnssRenderHandler;

    static class GnssRenderHandler extends Handler {
        WeakReference<GnssOSD> weakParent;

        GnssRenderHandler(GnssOSD parent) {
            weakParent = new WeakReference<>(parent);
        }

        @Override
        public void handleMessage(Message msg) {
            GnssOSD gnss = weakParent.get();
            if (gnss != null) {
                gnss.setGpsLocation();
                gnss.update();
            }
            super.handleMessage(msg);
        }
    }

    public GnssOSD(Context context) {
        mContext = context.getApplicationContext();
        mBitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
        mBitmap.eraseColor(Color.TRANSPARENT);
        mCanvas = new Canvas(mBitmap);
        mPaint = new Paint();
        mPaint.setTextSize(FONT_SIZE);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        mPaint.setTypeface(Typeface.create("DroidSans", Typeface.BOLD));
        mPaint.setTextAlign(Paint.Align.LEFT);
        mPaint.setColor(Color.WHITE);
        setPosition(new float[]{-0.98f, -1.0f, -0.1f, -1.0f, -0.98f, -0.8f, -0.1f, -0.8f});
        init();
    }

    private void init() {
        gnssRenderHandler = new GnssRenderHandler(this);
        mTask = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 1;
                gnssRenderHandler.sendMessage(message);
            }
        };
        mTimer.schedule(mTask, 100, 500);
    }

    @Override
    protected Bitmap getBitmap() {
        return mBitmap;
    }

    private void update() {
        mStringBuilder.setLength(0);
        mStringBuilder.append(LocationUtil.latitudeToDMS(mLatitude));
        mStringBuilder.append("  ");
        mStringBuilder.append(LocationUtil.longitudeToDMS(mLongitude));
        mBitmap.eraseColor(Color.TRANSPARENT);
        mCanvas.drawText(mStringBuilder.toString(), 2, TEXT_HEIGHT - 3, mPaint);
    }
    private void setGpsLocation(){
        Location currentLocation = GPSStatusManager.getInstance().getCurrentLocation();
        if (currentLocation != null) {
            mLatitude = currentLocation.getLatitude();
            mLongitude = currentLocation.getLongitude();
        }else{
            mLatitude = 0.0;
            mLongitude = 0.0;
        }
    }
    @Override
    public void release() {
        mTask.cancel();
        mTimer.cancel();
        super.release();
    }
}
