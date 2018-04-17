package com.askey.dvr.cdr7010.dashcam.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.askey.dvr.cdr7010.dashcam.R;

import java.util.Calendar;

public class OSDView extends View {
    private static final int REFRESH_PREVIEW_TIME_TIMER = 0x500;
    private Context mContext;
    private String timeString;
    private RectF  timeRectF;
    private Paint  timePaint;
    private boolean threadExitFlag = false;
    private int timerInterval = 1000;
    private Bitmap time_bg;

    public OSDView(Context context){
        super(context);
        mContext = context;
        initViews();
    }
    public OSDView(Context context, AttributeSet attrs){
        super(context, attrs);
        mContext = context;
        initViews();
    }
    public void init(int normalInterval){
        timerInterval = normalInterval;
        startClockTimer();
    }
    public void unInit(){
        threadExitFlag = true;
    }

    private void initViews(){
        timeRectF = new RectF(0,0,60,60);
        timePaint = new Paint();
        timePaint.setColor(Color.RED);
        timePaint.setAntiAlias(true);

        time_bg = decodeResource(getResources(), R.drawable.time_bg);
    }
    private Bitmap decodeResource(Resources resources, int id){
        TypedValue value = new TypedValue();
        resources.openRawResource(id,value);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inTargetDensity = value.density;
        return BitmapFactory.decodeResource(resources,id,opts);
    }
    private String updateClock() {
        //更新时间
        Calendar calendar= Calendar.getInstance();
        int hour=calendar.get(Calendar.HOUR_OF_DAY);
        int minute=calendar.get(Calendar.MINUTE);
        timeString=hour+":"+minute;
        return timeString;
    }
    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            switch(msg.what){
                case REFRESH_PREVIEW_TIME_TIMER:
                    invalidateView();
                    break;
                default:
                    break;
            }
        }
    };
    private void startClockTimer(){
        new Thread(){
            @Override
            public void run(){
                while(!threadExitFlag){
                    try {
                        handler.sendEmptyMessage(REFRESH_PREVIEW_TIME_TIMER);
                        Thread.sleep(timerInterval);
                    }catch(InterruptedException e){
                        e.printStackTrace();
                    }

                }
            }
        }.start();

    }
    public void invalidateView(){
        invalidate();
    }
        private void drawTime(Canvas canvas,String timeText,float width,float height){
        canvas.drawBitmap(time_bg,null, timeRectF, null);
        Rect strRect = new Rect();
        timePaint.getTextBounds(timeText,0,timeText.length(),strRect);
        Paint.FontMetrics fontMetrics = timePaint.getFontMetrics();
        float baseline = (height - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top;
        canvas.drawText(timeText,width / 2 - strRect.width() / 2, baseline, timePaint);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        //调用父View的onDraw函数，因为View这个类帮我们实现了一些
        // 基本的而绘制功能，比如绘制背景颜色、背景图片等
        super.onDraw(canvas);
        drawTime(canvas,updateClock(),timeRectF.width(),timeRectF.height());
    }
}