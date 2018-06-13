package com.askey.dvr.cdr7010.dashcam.core.osd;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;

import com.askey.dvr.cdr7010.dashcam.BuildConfig;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class TimestampOSD extends BaseOSD {
    private static int WIDTH = 225;
    private static int HEIGHT = 32;
    private static int FONT_SIZE = 16;
    private static int TEXT_WIDTH = 225;
    private static int TEXT_HEIGHT = 32;

    private Bitmap mBitmap;
    private final Canvas mCanvas;
    private final Paint mPaint;
    private static DateFormat FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
    private final Date mDate = new Date();
    private TimerTask mTask;
    private final Timer mTimer = new Timer();
    private final MyHandler mHandler;
    private boolean mRefresh = false;
    private static Date sBuildTime = BuildConfig.buildTime;
    private String mText;

    static class MyHandler extends Handler {
        WeakReference<TimestampOSD> weakParent;

        MyHandler(TimestampOSD parent) {
            weakParent = new WeakReference<>(parent);
        }

        @Override
        public void handleMessage(Message msg) {
            TimestampOSD timestamp = weakParent.get();
            if (timestamp != null) {
                timestamp.update();
            }
            super.handleMessage(msg);
        }
    }

    public TimestampOSD() {
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

        mHandler = new MyHandler(this);
        mTask = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 1;
                mHandler.sendMessage(message);
            }
        };
        mTimer.schedule(mTask, 100, 100);
    }

    @Override
    protected Bitmap getBitmap() {
        return mBitmap;
    }

    @Override
    protected void onDraw() {
        if (!mRefresh) {
            update();
        }
        super.onDraw();
        mRefresh = false;
    }

    @Override
    public void release() {
        mTask.cancel();
        mTimer.cancel();
        super.release();
    }

    private void update() {
        mDate.setTime(System.currentTimeMillis());
        if (mDate.before(sBuildTime)) {
            mText = "--/--/--　--:--:--";
        } else {
            mText = FORMAT.format(mDate);
        }
        mBitmap.eraseColor(Color.TRANSPARENT);
        mCanvas.drawText(mText, 12, TEXT_HEIGHT - 3, mPaint);
        mRefresh = true;
    }
}
