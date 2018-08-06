package com.askey.dvr.cdr7010.dashcam.util;

import android.content.Context;

import com.askey.dvr.cdr7010.dashcam.domain.Event;
import com.askey.dvr.cdr7010.dashcam.domain.MessageEvent;
import com.askey.dvr.cdr7010.dashcam.service.DialogManager;
import com.askey.dvr.cdr7010.dashcam.service.EventManager;
import com.askey.dvr.cdr7010.dashcam.service.GPSStatusManager;
import com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum;

import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.GPSStatusType.GPS_STRENGTH_FIXES;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.GPSStatusType.GPS_STRENGTH_NONE;
import static com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum.GPSStatusType.GPS_STRENGTH_NOT_FIXES;

public class GpsHelper{
    private static final String TAG = GpsHelper.class.getSimpleName();
    public static final int GPS_SIGNAL_STRENGTH_NONE =0;
    public static final int GPS_SIGNAL_STRENGTH_POOR = 1;
    public static final int GPS_SIGNAL_STRENGTH_GOOD =2;
    private static int curGpsSignalStrength = -1;
    private static int curEventType = -2;

    private static void handleGpsSignalStrength(int gpsSignalStrength,int eventType){
        if(DialogManager.getIntance().isDialogShowing(eventType)){
            return;
        }
        switch(gpsSignalStrength){
            case GPS_SIGNAL_STRENGTH_NONE:
                EventUtil.sendEvent(new MessageEvent<UIElementStatusEnum.GPSStatusType>(Event.EventCode.EVENT_GPS, GPS_STRENGTH_NONE));
                handoutLocationMessageError();
                GPSStatusManager.getInstance().setHaveGpsSignal(false);
                break;
            case GPS_SIGNAL_STRENGTH_POOR:
                EventUtil.sendEvent(new MessageEvent<UIElementStatusEnum.GPSStatusType>(Event.EventCode.EVENT_GPS, GPS_STRENGTH_NOT_FIXES));
                handoutLocationMessageError();
                GPSStatusManager.getInstance().setHaveGpsSignal(false);
                break;
            case GPS_SIGNAL_STRENGTH_GOOD:
                EventUtil.sendEvent(new MessageEvent<UIElementStatusEnum.GPSStatusType>(Event.EventCode.EVENT_GPS, GPS_STRENGTH_FIXES));
                handoutLocationMessage();
                GPSStatusManager.getInstance().setHaveGpsSignal(true);
                break;
            default:

        }
    }
    public static void processGpsSignal(Context context,int gpsSignalStrength,int eventType){
        if(AppUtils.isActivityTop(context,Const.ACTIVITY_CLASSNAME)){
            handleGpsSignalStrength(gpsSignalStrength,eventType);
            curGpsSignalStrength = -1;
            curEventType = -2;
        }else{
            curGpsSignalStrength  = gpsSignalStrength;
            curEventType = eventType;
        }
    }
    public static void checkGpsSignalStrength(){
        Logg.d(TAG,"checkGpsSignalStrength curGpsSignalStrength="+curGpsSignalStrength);
        if(curGpsSignalStrength == -1){
            curEventType = -2;
            return ;
        }else{
            handleGpsSignalStrength(curGpsSignalStrength,curEventType);
        }
        curGpsSignalStrength =  -1;
        curEventType = -2;
    }
    private static void handoutLocationMessage(){
        int eventType = 108; //defined to 108 in assets
        EventManager.getInstance().handOutEventInfo(eventType);
    }

    private static  void handoutLocationMessageError(){
        int eventType = 109; //defined to 109 in assets
        EventManager.getInstance().handOutEventInfo(eventType);
    }
}