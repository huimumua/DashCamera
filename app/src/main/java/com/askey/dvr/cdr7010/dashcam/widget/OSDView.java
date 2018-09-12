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
import android.graphics.Typeface;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.askey.dvr.cdr7010.dashcam.R;
import com.askey.dvr.cdr7010.dashcam.provider.OSDProvider;
import com.askey.dvr.cdr7010.dashcam.service.GPSStatusManager;
import com.askey.dvr.cdr7010.dashcam.util.DisplayUtils;
import com.askey.dvr.cdr7010.dashcam.util.Logg;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.EventRecordingLimitStatusType.EVENT_RECORDING_REACH_LIMIT_CONDITION;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.FOTAFileStatus.FOTA_FILE_EXIST;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.GPSStatusType.GPS_STRENGTH_FIXES;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.GPSStatusType.GPS_STRENGTH_NONE;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.GPSStatusType.GPS_STRENGTH_NOT_FIXES;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.LTEStatusType.LTE_NONE;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.LTEStatusType.LTE_SIGNAL_STRENGTH_GOOD;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.LTEStatusType.LTE_SIGNAL_STRENGTH_GREAT;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.LTEStatusType.LTE_SIGNAL_STRENGTH_MODERATE;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.LTEStatusType.LTE_SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.LTEStatusType.LTE_SIGNAL_STRENGTH_POOR;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.MICStatusType.MIC_OFF;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.MICStatusType.MIC_ON;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.ParkingRecordingLimitStatusType.PARKING_RECORDING_REACH_LIMIT_CONDITION;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.RecordingStatusType.RECORDING_CONTINUOUS;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.RecordingStatusType.RECORDING_ERROR;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.RecordingStatusType.RECORDING_EVENT;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.RecordingStatusType.RECORDING_STOP;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.RecordingStatusType.RECORDING_UNKNOWN;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_ASKEY_NOT_SUPPORTED;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_EVENT_FILE_LIMIT;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_EVENT_PICTURE_LIMIT;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_INIT_FAIL;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_INIT_SUCCESS;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_MOUNTED;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_REMOVED;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_SUPPORTED;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_UNMOUNTED;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_UNRECOGNIZABLE;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SDcardStatusType.SDCARD_UNSUPPORTED;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SecondCameraStatusType.CONNECTED;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.SimCardStatus.SIM_STATE_READY;

public class OSDView extends View {
    private static final String TAG = "OSDView";

    private static final int REFRESH_PREVIEW_TIME_TIMER = 0x500;
    private Context mContext;
    private String timeString;
    private RectF timeRectF;
    private RectF recordingRectF;
    private RectF micRectF;
    private RectF lteRectF;
    private RectF volumeUpRectF;
    private RectF volumeDownRectF;
    private RectF menuRectF;
    private RectF countTimeRectF;
    private RectF parkingRecordingLimitRectF;
    private RectF eventRecordingLimitRectF;
    private RectF sdCardRectF;
    private RectF updateRectF;
    private RectF secondCameraRectF;
    private RectF gpsRectF;
    private RectF simCardRectF;
    private RectF userInfoRectF;
    private Paint timePaint;
    private Paint countDownPaint;
    private Paint drawPaint;
    private boolean threadExitFlag = false;
    private int timerInterval = 1000;
    private int scrollingSpeed;
    private Bitmap time_bg;
    private Bitmap continuous_recording;
    private Bitmap event_recording;
    private Bitmap stop_recording;
    private Bitmap mic_on;
    private Bitmap mic_off;
    private Bitmap lte_sinal_strength_great;
    private Bitmap lte_sinal_strength_good;
    private Bitmap lte_sinal_strength_moderate;
    private Bitmap lte_sinal_strength_poor;
    private Bitmap lte_signal_none;
    private Bitmap lte_none;
    private Bitmap volume_up;
    private Bitmap volume_down;
    private Bitmap menu;
    private Bitmap menuDisabled;
    private Bitmap parking_recording_limit;
    private Bitmap event_recording_limit;
    private Bitmap sdcard_error;
    private Bitmap sdcard_testing;
    private Bitmap sdcard_recording;
    private Bitmap sdcard_not_found;
    private Bitmap update;
    private Bitmap second_camera;
    private Bitmap gps_signal_strength_not_fixes;
    private Bitmap gps_signal_strength_fixes;
    private Bitmap gps_signal_strength_none;
    private Bitmap simcard_error;
    private OSDProvider osdProvider;
    private int startDrawX = 0;
    private int textWidth, textHeight;
    private boolean isOutSide;
    private int spacing;
    private boolean isJudge = true;
    private int baseY = 0;
    private int padding = 2;

    public OSDView(Context context) {
        super(context);
        mContext = context;
        osdProvider = new OSDProvider();
        initViews();
    }

    public OSDView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        osdProvider = new OSDProvider();
        initViews();
    }

    public void init(int normalInterval) {
        timerInterval = normalInterval;
        startClockTimer();
    }

    public void unInit() {
        threadExitFlag = true;
    }

    private void initViews() {
        timeRectF = new RectF(11, 214, 95, 240);
        timePaint = new Paint();
        timePaint.setTextSize(DisplayUtils.sp2px(mContext, 18));
        timePaint.setColor(Color.WHITE);
        timePaint.setAntiAlias(true);

        time_bg = decodeResource(getResources(), R.drawable.bg_time);

        recordingRectF = new RectF(11, 4, 82, 28);;
        continuous_recording = decodeResource(getResources(), R.drawable.continuous_recording);
        event_recording = decodeResource(getResources(), R.drawable.event_recording);
        stop_recording = decodeResource(getResources(), R.drawable.stop_recording);

        micRectF = new RectF(230, 212, 282, 240);
        mic_on = decodeResource(getResources(), R.drawable.mic_on);
        mic_off = decodeResource(getResources(), R.drawable.mic_off);

        lteRectF = new RectF(243, 8, 287, 24);
        lte_sinal_strength_great = decodeResource(getResources(), R.drawable.lte_sinal_strength_great);
        lte_sinal_strength_good = decodeResource(getResources(), R.drawable.lte_sinal_strength_good);
        lte_sinal_strength_moderate = decodeResource(getResources(), R.drawable.lte_sinal_strength_moderate);
        lte_sinal_strength_poor = decodeResource(getResources(), R.drawable.lte_sinal_strength_poor);
        lte_signal_none = decodeResource(getResources(), R.drawable.lte_sinal_none);
        lte_none = decodeResource(getResources(), R.drawable.lte_none);


        volumeUpRectF = new RectF(292, 22, 320, 74);
        volume_up = decodeResource(getResources(), R.drawable.volume_up);

        volumeDownRectF = new RectF(292, 167, 320, 219);
        volume_down = decodeResource(getResources(), R.drawable.volume_down);

        menuRectF = new RectF(292, 94, 320, 146);
        menu = decodeResource(getResources(), R.drawable.menu);
        menuDisabled = decodeResource(getResources(), R.drawable.menu_disabled);

        countTimeRectF = new RectF(13, 36, 28, 52);
        countDownPaint = new Paint();
        countDownPaint.setTextSize(DisplayUtils.sp2px(mContext, 18));
        countDownPaint.setColor(Color.WHITE);
        countDownPaint.setAntiAlias(true);
        Typeface font = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
        countDownPaint.setTypeface(font);

        parkingRecordingLimitRectF = new RectF(164, 156, 230, 192);
        parking_recording_limit = decodeResource(getResources(), R.drawable.parking_recording_limit);

        eventRecordingLimitRectF = new RectF(90, 156, 156, 192);
        event_recording_limit = decodeResource(getResources(), R.drawable.event_recording_limit);

        sdCardRectF = new RectF(11, 62, 53, 90);
        sdcard_error = decodeResource(getResources(), R.drawable.icon_sdcard_error);
        sdcard_not_found = decodeResource(getResources(), R.drawable.icon_sdcard_nofound);
        sdcard_recording = decodeResource(getResources(), R.drawable.icon_sdcard_recording);
        sdcard_testing = decodeResource(getResources(), R.drawable.icon_sdcard_testing);

        secondCameraRectF = new RectF(11, 148, 35, 164);
        second_camera = decodeResource(getResources(), R.drawable.icon_2nd_camera);

        updateRectF = new RectF(11, 105, 35, 121);
        update = decodeResource(getResources(), R.drawable.icon_update);

        simCardRectF = new RectF(11, 190, 35, 206);
        simcard_error = decodeResource(getResources(), R.drawable.icon_simcard_error);

        gpsRectF = new RectF(200, 8, 232, 22);
        gps_signal_strength_not_fixes = decodeResource(getResources(), R.drawable.icon_gps_strength);
        gps_signal_strength_none = decodeResource(getResources(), R.drawable.icon_gps_strength_none);
        gps_signal_strength_fixes = decodeResource(getResources(), R.drawable.icon_gps_strength_max);

        userInfoRectF = new RectF(85, 8, 192, 25);
        drawPaint = new Paint();
        drawPaint.setTextSize(DisplayUtils.sp2px(mContext, 12));
        drawPaint.setColor(Color.WHITE);
        drawPaint.setAntiAlias(true);
        startDrawX = (int) userInfoRectF.left;
        spacing = 30;
        scrollingSpeed = 500;
    }

    private Bitmap decodeResource(Resources resources, int id) {
        TypedValue value = new TypedValue();
        resources.openRawResource(id, value);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inTargetDensity = value.density;
        return BitmapFactory.decodeResource(resources, id, opts);
    }

    private String updateClock() {
        //更新时间
        int hour;
        int minute;
        Calendar calendar = new GregorianCalendar();
        Date trialTime = new Date();
        calendar.setTime(trialTime);
        String language = Locale.getDefault().getLanguage();
        if (language.equals("ja")) {
            hour = calendar.get(Calendar.HOUR_OF_DAY);
            minute = calendar.get(Calendar.MINUTE);
            timeString = (hour < 10 ? "0" + hour : hour) + ":" + (minute < 10 ? "0" + minute : minute);
        } else if (language.equals("us") || language.equals("en")) {
            hour = calendar.get(Calendar.HOUR);
            minute = calendar.get(Calendar.MINUTE);
            int am_pm = calendar.get(Calendar.AM_PM);
            timeString = (hour < 10 ? "0" + hour : hour) + ":" + (minute < 10 ? "0" + minute : minute) + (am_pm == 0 ? " AM" : " PM");
        }
        return timeString;
    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case REFRESH_PREVIEW_TIME_TIMER:
                    invalidateView();
                    break;
                default:
                    break;
            }
            return true;
        }
    });

    private void startClockTimer() {
        new Thread() {
            @Override
            public void run() {
                while (!threadExitFlag) {
                    try {
                        handler.sendEmptyMessage(REFRESH_PREVIEW_TIME_TIMER);
                        Thread.sleep(timerInterval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }.start();

    }

    private int countTime = -1;
    private Handler timeHandler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            countTime--;
            if (countTime >= 0) {
                timeHandler.postDelayed(this, 1000);
            }
            invalidate();
        }
    };

    public void startRecordingCountDown() {
        countTime = 6;
        timeHandler.removeCallbacks(runnable);
        timeHandler.post(runnable);
    }

    public void invalidateView() {
        invalidate();
    }

    private void drawRecordingCountDown(Canvas canvas, String countDownText, RectF dstRectF) {
        float x = dstRectF.left;
        float y = dstRectF.top;
        float width = dstRectF.width();
        float height = dstRectF.height();
        Rect strRect = new Rect();
        countDownPaint.getTextBounds(countDownText, 0, countDownText.length(), strRect);
        Paint.FontMetrics fontMetrics = countDownPaint.getFontMetrics();
        float baseline = (height - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top;
        canvas.drawText(countDownText, x, y + baseline, countDownPaint);
    }

    private void drawTime(Canvas canvas, RectF bgRectF, String timeText, RectF dstRectF) {
        float x = dstRectF.left;
        float y = dstRectF.top;
        float width = dstRectF.width();
        float height = dstRectF.height();
        if (bgRectF != null) {
            canvas.drawBitmap(time_bg, null, bgRectF, null);
        }
        Rect strRect = new Rect();
        timePaint.getTextBounds(timeText, 0, timeText.length(), strRect);
        Paint.FontMetrics fontMetrics = timePaint.getFontMetrics();
        float baseline = (height - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top;
        canvas.drawText(timeText, x, y + baseline, timePaint);
    }

    private void drawUserInfo(Canvas canvas, String userInfo) {
        RectF rcItem = null;
        canvas.save();
        canvas.clipRect(userInfoRectF);
        canvas.drawColor(Color.GRAY);
        Paint.FontMetrics fontMetrics = drawPaint.getFontMetrics();
        int txtMaxHeight = (int) Math.ceil(fontMetrics.bottom - fontMetrics.top);
        rcItem = new RectF(0, 0, userInfoRectF.width(), userInfoRectF.height());

        Rect rect = new Rect();
        drawPaint.getTextBounds(userInfo, 0, userInfo.length(), rect);
        textWidth = rect.width();
        textHeight = rect.height();
        int measureWidth = (int) rcItem.width();
        if (textWidth >= measureWidth) {
            isOutSide = true;
        } else {
            isOutSide = false;
        }
        if (isJudge) {
            float baseline = (rcItem.height() - rect.height() - txtMaxHeight) * 3 / 4;
            baseY = (int) (rcItem.bottom - fontMetrics.bottom - baseline);
        }
//        if (isOutSide) {
//            if (startDrawX < -textWidth) {
//                startDrawX = spacing;
//            }
//            int outSide = startDrawX;
//            if (outSide < -(textWidth - rcItem.width())) {
//                canvas.drawText(userInfo, textWidth + outSide + spacing, baseY, drawPaint);
//            }
//            canvas.drawText(userInfo, startDrawX, baseY, drawPaint);
//            startDrawX -= 5;
//        } else {
        canvas.drawText(userInfo, (int) userInfoRectF.left + padding, baseY, drawPaint);
//        }
        // postInvalidateDelayed(scrollingSpeed);
        canvas.restore();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        //调用父View的onDraw函数，因为View这个类帮我们实现了一些
        // 基本的而绘制功能，比如绘制背景颜色、背景图片等
        super.onDraw(canvas);
        drawTime(canvas, timeRectF, updateClock(), timeRectF);
        if (osdProvider.getRecordingStatus() == RECORDING_UNKNOWN) {
            canvas.drawBitmap(stop_recording, null, recordingRectF, null);
        } else if (osdProvider.getRecordingStatus() == RECORDING_CONTINUOUS) {
            canvas.drawBitmap(continuous_recording, null, recordingRectF, null);
        } else if (osdProvider.getRecordingStatus() == RECORDING_EVENT) {
            canvas.drawBitmap(event_recording, null, recordingRectF, null);
            if (countTime >= 0) {
                drawRecordingCountDown(canvas, "" + countTime, countTimeRectF);
            }
        } else if (osdProvider.getRecordingStatus() == RECORDING_STOP
                || osdProvider.getRecordingStatus() == RECORDING_ERROR) {
            canvas.drawBitmap(stop_recording, null, recordingRectF, null);
        }
        if (osdProvider.getMicStatus() == MIC_ON) {
            canvas.drawBitmap(mic_on, null, micRectF, null);
        } else if (osdProvider.getMicStatus() == MIC_OFF) {
            canvas.drawBitmap(mic_off, null, micRectF, null);
        }
        if (osdProvider.getLTEStatus() == LTE_SIGNAL_STRENGTH_NONE_OR_UNKNOWN) {
            canvas.drawBitmap(lte_signal_none, null, lteRectF, null);
        } else if (osdProvider.getLTEStatus() == LTE_SIGNAL_STRENGTH_GREAT) {
            canvas.drawBitmap(lte_sinal_strength_great, null, lteRectF, null);
        } else if (osdProvider.getLTEStatus() == LTE_SIGNAL_STRENGTH_GOOD) {
            canvas.drawBitmap(lte_sinal_strength_good, null, lteRectF, null);
        } else if (osdProvider.getLTEStatus() == LTE_SIGNAL_STRENGTH_MODERATE) {
            canvas.drawBitmap(lte_sinal_strength_moderate, null, lteRectF, null);
        } else if (osdProvider.getLTEStatus() == LTE_SIGNAL_STRENGTH_POOR) {
            canvas.drawBitmap(lte_sinal_strength_poor, null, lteRectF, null);
        } else if (osdProvider.getLTEStatus() == LTE_NONE) {
            canvas.drawBitmap(lte_none, null, lteRectF, null);
        }

//        if (osdProvider.getEventRecordingLimitStatus() == EVENT_RECORDING_REACH_LIMIT_CONDITION) {
//            canvas.drawBitmap(event_recording_limit, null, eventRecordingLimitRectF, null);
//        }
        if (osdProvider.getParkingRecordingLimitStatus() == PARKING_RECORDING_REACH_LIMIT_CONDITION) {
            canvas.drawBitmap(parking_recording_limit, null, parkingRecordingLimitRectF, null);
        }
        if (osdProvider.getSDCardStatus() == SDCARD_INIT_FAIL) {
            canvas.drawBitmap(sdcard_error, null, sdCardRectF, null);
        } else if ((osdProvider.getRecordingStatus() == RECORDING_CONTINUOUS
                || osdProvider.getRecordingStatus() == RECORDING_EVENT)
                && (osdProvider.getSDCardStatus() == SDCARD_INIT_SUCCESS
                || osdProvider.getSDCardStatus() == SDCARD_EVENT_FILE_LIMIT
                || osdProvider.getSDCardStatus() == SDCARD_EVENT_PICTURE_LIMIT)) {
            canvas.drawBitmap(sdcard_recording, null, sdCardRectF, null);
        } else if ((osdProvider.getRecordingStatus() == RECORDING_STOP || osdProvider.getRecordingStatus() == RECORDING_UNKNOWN)
                && (osdProvider.getSDCardStatus() == SDCARD_MOUNTED || osdProvider.getSDCardStatus() == SDCARD_SUPPORTED
                || osdProvider.getSDCardStatus() == SDCARD_INIT_SUCCESS || osdProvider.getSDCardStatus() == SDCARD_EVENT_FILE_LIMIT
                || osdProvider.getSDCardStatus() == SDCARD_EVENT_PICTURE_LIMIT)) {
            canvas.drawBitmap(sdcard_testing, null, sdCardRectF, null);
        } else if (osdProvider.getSDCardStatus() == SDCARD_INIT_FAIL
                || osdProvider.getSDCardStatus() == SDCARD_UNRECOGNIZABLE
                || osdProvider.getSDCardStatus() == SDCARD_UNSUPPORTED
                || osdProvider.getSDCardStatus() == SDCARD_ASKEY_NOT_SUPPORTED
                ) {
            canvas.drawBitmap(sdcard_error, null, sdCardRectF, null);
        } else if (osdProvider.getSDCardStatus() == SDCARD_REMOVED ||
                osdProvider.getSDCardStatus() == SDCARD_UNMOUNTED) {
            canvas.drawBitmap(sdcard_not_found, null, sdCardRectF, null);
        }
        if (osdProvider.getFotaFileStatus() == FOTA_FILE_EXIST) {
            canvas.drawBitmap(update, null, updateRectF, null);
        }
        if (osdProvider.getSecondCameraStatus() == CONNECTED) {
            canvas.drawBitmap(second_camera, null, secondCameraRectF, null);
        }
        if (osdProvider.getSimCardStatus() != SIM_STATE_READY) {
            canvas.drawBitmap(simcard_error, null, simCardRectF, null);
        }
        if (osdProvider.getGpsStatus() == GPS_STRENGTH_FIXES) {
            canvas.drawBitmap(gps_signal_strength_fixes, null, gpsRectF, null);
        } else if (osdProvider.getGpsStatus() == GPS_STRENGTH_NOT_FIXES) {
            canvas.drawBitmap(gps_signal_strength_not_fixes, null, gpsRectF, null);
        } else if (osdProvider.getGpsStatus() == GPS_STRENGTH_NONE) {
            canvas.drawBitmap(gps_signal_strength_none, null, gpsRectF, null);
        }


        canvas.drawBitmap(volume_up, null, volumeUpRectF, null);
        Location currentLocation = GPSStatusManager.getInstance().getCurrentLocation();
        if (currentLocation != null && currentLocation.getSpeed() > 0.0f) { //有GPS信号，且速度大于0
            canvas.drawBitmap(menuDisabled, null, menuRectF, null);
        } else {
            canvas.drawBitmap(menu, null, menuRectF, null);
        }
        canvas.drawBitmap(volume_down, null, volumeDownRectF, null);
        String userInfo = osdProvider.getUserInfo();
        if (!TextUtils.isEmpty(userInfo)) {
            drawUserInfo(canvas, userInfo);
        }
    }
}