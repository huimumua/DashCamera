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
import com.askey.dvr.cdr7010.dashcam.provider.OSDProvider;
import com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum;

import java.util.Calendar;

import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.EventRecordingLimitStatusType.EVENT_RECORDING_REACH_LIMIT_CONDITION;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.LTEStatusType.LTE_SIGNAL_STRENGTH_GOOD;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.LTEStatusType.LTE_SIGNAL_STRENGTH_GREAT;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.LTEStatusType.LTE_SIGNAL_STRENGTH_MODERATE;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.LTEStatusType.LTE_SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.LTEStatusType.LTE_SIGNAL_STRENGTH_POOR;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.MICStatusType.MIC_OFF;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.MICStatusType.MIC_ON;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.ParkingRecordingLimitStatusType.PARKING_RECORDING_REACH_LIMIT_CONDITION;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.RecordingStatusType.RECORDING_CONTINUOUS;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.RecordingStatusType.RECORDING_EVENT;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.RecordingStatusType.RECORDING_PARKING;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.MEDIA_BAD_REMOVAL;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.MEDIA_CHECKING;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.MEDIA_MOUNTED;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.MEDIA_MOUNTED_READ_ONLY;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.MEDIA_NOFS;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.MEDIA_REMOVED;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.MEDIA_SHARED;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.MEDIA_UNKNOWN;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.MEDIA_UNMOUNTABLE;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.MEDIA_UNMOUNTED;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SecondCameraStatusType.CONNECTED;

public class OSDView extends View {
    private static final int REFRESH_PREVIEW_TIME_TIMER = 0x500;
    private Context mContext;
    private String timeString;
    private RectF  timeRectF;
    private RectF  recordingRectF;
    private RectF  micRectF;
    private RectF  lteRectF;
    private RectF  volumeUpRectF;
    private RectF  volumeDownRectF;
    private RectF  menuRectF;
    private RectF  countTimeRectF;
    private RectF  parkingRecordingLimitRectF;
    private RectF  eventRecordingLimitRectF;
    private RectF  sdCardRectF;
    private RectF  updateRectF;
    private RectF  secondCameraRectF;
    private Paint  timePaint;
    private boolean threadExitFlag = false;
    private int timerInterval = 1000;
    private Bitmap time_bg;
    private Bitmap continuous_recording ;
    private Bitmap event_recording;
    private Bitmap parking_recording;
    private Bitmap mic_on;
    private Bitmap mic_off;
    private Bitmap lte_sinal_strength_great;
    private Bitmap volume_up;
    private Bitmap volume_down;
    private Bitmap menu;
    private Bitmap parking_recording_limit;
    private Bitmap event_recording_limit;
    private Bitmap sdcard_error;
    private Bitmap sdcard_testing;
    private Bitmap sdcard_recording;
    private Bitmap sdcard_not_found;
    private Bitmap update;
    private Bitmap second_camera;
    private OSDProvider osdProvider;

    public OSDView(Context context){
        super(context);
        mContext = context;
        osdProvider = new OSDProvider();
        initViews();
    }
    public OSDView(Context context, AttributeSet attrs){
        super(context, attrs);
        mContext = context;
        osdProvider = new OSDProvider();
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
        timeRectF = new RectF(20,180,40,210);
        timePaint = new Paint();
        timePaint.setColor(Color.RED);
        timePaint.setAntiAlias(true);

        time_bg = decodeResource(getResources(), R.drawable.time_bg);

        recordingRectF = new RectF(20,0,80,30);
        continuous_recording = decodeResource(getResources(), R.drawable.continuous_recording);
        event_recording = decodeResource(getResources(), R.drawable.event_recording);
        parking_recording = decodeResource(getResources(), R.drawable.parking_recording);

        micRectF = new RectF(220,140,270,200);
        mic_on = decodeResource(getResources(), R.drawable.mic_on);
        mic_off = decodeResource(getResources(), R.drawable.mic_off);

        lteRectF = new RectF(220,0,260,20);
        lte_sinal_strength_great = decodeResource(getResources(), R.drawable.lte_sinal_strength_great);

        volumeUpRectF = new RectF(290,30,320,60);
        volume_up = decodeResource(getResources(), R.drawable.volume_up);

        volumeDownRectF = new RectF(290,170,320,200);
        volume_down = decodeResource(getResources(), R.drawable.volume_down);

        menuRectF = new RectF(290,100,320,130);
        menu = decodeResource(getResources(), R.drawable.menu);

        countTimeRectF = new RectF(90,0,120,30);

        parkingRecordingLimitRectF = new RectF(180,150,230,170);
        parking_recording_limit = decodeResource(getResources(), R.drawable.parking_recording_limit);

        eventRecordingLimitRectF = new RectF(120,150,170,170);
        event_recording_limit = decodeResource(getResources(), R.drawable.event_recording_limit);

        sdCardRectF = new RectF(20,70,44,86);
        sdcard_error = decodeResource(getResources(), R.drawable.icon_sdcard_error);
        sdcard_not_found = decodeResource(getResources(), R.drawable.icon_sdcard_nofound);
        sdcard_recording = decodeResource(getResources(), R.drawable.icon_sdcard_recording);
        sdcard_testing = decodeResource(getResources(), R.drawable.icon_sdcard_testing);

        secondCameraRectF = new RectF(20,141,44,157);
        second_camera = decodeResource(getResources(), R.drawable.icon_2nd_camera);

        updateRectF = new RectF(20,104,44,120);
        update = decodeResource(getResources(), R.drawable.icon_update);
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
    private int countTime = -1 ;
    private Handler timeHandler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            countTime--;
            if(countTime >= 0) {
                handler.postDelayed(this, 1000);
            }
        }
    };
    public void startRecordingCountDown(){
        countTime = 6;
        timeHandler.post(runnable);
    }
    public void invalidateView(){
        invalidate();
    }
    private void drawTime(Canvas canvas,RectF bgRectF,String timeText,RectF dstRectF){
        float x = dstRectF.left;
        float y = dstRectF.top;
        float width = dstRectF.width();
        float height = dstRectF.height();
        if(bgRectF != null) {
            canvas.drawBitmap(time_bg, null, bgRectF, null);
        }
        Rect strRect = new Rect();
        timePaint.getTextBounds(timeText,0,timeText.length(),strRect);
        Paint.FontMetrics fontMetrics = timePaint.getFontMetrics();
        float baseline = (height - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top;
        canvas.drawText(timeText,x + width / 2 - strRect.width() / 2, y + baseline, timePaint);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        //调用父View的onDraw函数，因为View这个类帮我们实现了一些
        // 基本的而绘制功能，比如绘制背景颜色、背景图片等
        super.onDraw(canvas);
        drawTime(canvas,timeRectF,updateClock(),timeRectF);
        if(osdProvider.getRecordingStatus() == RECORDING_CONTINUOUS){
            canvas.drawBitmap(continuous_recording,null, recordingRectF, null);
        }else if(osdProvider.getRecordingStatus() == RECORDING_EVENT){
            canvas.drawBitmap(event_recording,null, recordingRectF, null);
            if(countTime>=0) {
                drawTime(canvas, null,"0" + countTime, countTimeRectF);
            }
        }else if(osdProvider.getRecordingStatus() == RECORDING_PARKING){
            canvas.drawBitmap(parking_recording,null, recordingRectF, null);
            if(countTime>=0) {
                drawTime(canvas, null,"0" + countTime, countTimeRectF);
            }
        }
        if(osdProvider.getMicStatus() == MIC_ON){
            canvas.drawBitmap(mic_on,null, micRectF, null);
        }else if(osdProvider.getMicStatus() == MIC_OFF){
            canvas.drawBitmap(mic_off,null, micRectF, null);
        }
        if(osdProvider.getLTEStatus() == LTE_SIGNAL_STRENGTH_NONE_OR_UNKNOWN){
            canvas.drawBitmap(lte_sinal_strength_great,null, lteRectF, null);
        }else if(osdProvider.getLTEStatus() == LTE_SIGNAL_STRENGTH_GREAT){
            canvas.drawBitmap(lte_sinal_strength_great,null, lteRectF, null);
        }else if(osdProvider.getLTEStatus() == LTE_SIGNAL_STRENGTH_GOOD){
            canvas.drawBitmap(lte_sinal_strength_great,null, lteRectF, null);
        }else if(osdProvider.getLTEStatus() == LTE_SIGNAL_STRENGTH_MODERATE){
            canvas.drawBitmap(lte_sinal_strength_great,null, lteRectF, null);
        }else if(osdProvider.getLTEStatus() == LTE_SIGNAL_STRENGTH_POOR){
            canvas.drawBitmap(lte_sinal_strength_great,null, lteRectF, null);
        }

        if(osdProvider.getEventRecordingLimitStatus() == EVENT_RECORDING_REACH_LIMIT_CONDITION){
            canvas.drawBitmap(event_recording_limit,null, eventRecordingLimitRectF, null);
        }
        if(osdProvider.getParkingRecordingLimitStatus() == PARKING_RECORDING_REACH_LIMIT_CONDITION){
            canvas.drawBitmap(parking_recording_limit,null, parkingRecordingLimitRectF, null);
        }
        if(osdProvider.getRecordingStatus() == RECORDING_CONTINUOUS && osdProvider.getSDcardStatusType() == MEDIA_MOUNTED){
            canvas.drawBitmap(sdcard_recording,null,sdCardRectF,null);
        }else if((osdProvider.getRecordingStatus() == RECORDING_PARKING && osdProvider.getSDcardStatusType() == MEDIA_MOUNTED)
                || osdProvider.getSDcardStatusType() == MEDIA_CHECKING){
            canvas.drawBitmap(sdcard_testing,null,sdCardRectF,null);
        }else if(osdProvider.getSDcardStatusType() == MEDIA_MOUNTED_READ_ONLY
                ||osdProvider.getSDcardStatusType() == MEDIA_SHARED
                ||osdProvider.getSDcardStatusType() == MEDIA_BAD_REMOVAL
                ||osdProvider.getSDcardStatusType() == MEDIA_NOFS
                ||osdProvider.getSDcardStatusType() == MEDIA_UNMOUNTABLE
                ||osdProvider.getSDcardStatusType() == MEDIA_UNMOUNTED
                ||osdProvider.getSDcardStatusType() == MEDIA_UNKNOWN
                ){
            canvas.drawBitmap(sdcard_error,null,sdCardRectF,null);
        } else if(osdProvider.getSDcardStatusType() == MEDIA_REMOVED ){
            canvas.drawBitmap(sdcard_not_found,null,sdCardRectF,null);
        }
        canvas.drawBitmap(update,null,updateRectF,null);
        if(osdProvider.getSecondCameraStatus() == CONNECTED){
            canvas.drawBitmap(second_camera,null,secondCameraRectF,null);
        }


        canvas.drawBitmap(volume_up,null, volumeUpRectF, null);
        canvas.drawBitmap(menu,null, menuRectF, null);
        canvas.drawBitmap(volume_down,null, volumeDownRectF, null);
    }
}