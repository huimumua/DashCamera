package com.askey.dvr.cdr7010.dashcam.service;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.askey.dvr.cdr7010.dashcam.domain.Event;
import com.askey.dvr.cdr7010.dashcam.util.EventUtil;
import com.askey.dvr.cdr7010.dashcam.util.Logg;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class ThermalController{
    private static final String TAG = ThermalController.class.getSimpleName();
    private static final String LCD_THERMAL_ZONE_PATH ="/sys/class/thermal/thermal_zone2/temp";
    private static final String CPU_THERMAL_ZONE_PATH ="/sys/class/thermal/thermal_zone1/temp";
    private static final String LCD_POWER_STATUS_PATH ="/sys/class/graphics/fb0/askey_lcd_power_on_state";
    private static final String LCD_THERMAL_STATUS_PATH="/sys/class/graphics/fb0/askey_lcd_thermal_protection_state";
    private static final int CPU_POWER_OFF_HIGH_TEMP_THRESHOLD = 100;
    private static final int CPU_HIGH_TEMP_THRESHOLD = 85;
    private static final int CPU_HIGH_TEMP_CRITICAL_POINT = 70;
    private static final int LCD_HIGH_TEMP_THRESHOLD = 69;
    private static final int LCD_NORMAL_TEMP_THRESHOLD = 60;
    private static final int LCD_HIGH_TEMP_NOTIFY = 71;
    private static final int LCD_ON = 1;
    private static final int LCD_OFF =0;
    private HandlerThread mThermalMonitorThread;
    private Handler mThermalMonitorHandler;
    private ThermalListener thermalListener;
    private boolean isFirstStart;
    private int last_temp;
    public interface ThermalListener{
        void startRecording();
        void closeRecording();
        void closeLcdPanel();
        void startLcdPanel();
    }
    public ThermalController(ThermalListener thermalListener){
        this.thermalListener = thermalListener;
        EventUtil.register(this);
    }
    public void startThermalMonitor(){
        if(!isFirstStart) {
            mThermalMonitorThread = new HandlerThread("ThermalMonitor");
            mThermalMonitorThread.start();
            mThermalMonitorHandler = new Handler(mThermalMonitorThread.getLooper());
            mThermalMonitorHandler.postDelayed(new ThermalMonitorRunnable(), 0);
        }
        isFirstStart = true;
    }
    public void stopThermalMonitor(){
        mThermalMonitorThread.quit();
        mThermalMonitorHandler = null;
        EventUtil.unregister(this);
    }

    private  class ThermalMonitorRunnable implements Runnable {
        @Override
        public void run(){
            int cpu_temp = getSensorTemp(CPU_THERMAL_ZONE_PATH);
            int lcd_temp = getSensorTemp(LCD_THERMAL_ZONE_PATH) ;
            if(last_temp != cpu_temp ){
                Logg.d(TAG,"lcd_temp="+lcd_temp+",cpu_temp="+cpu_temp);
                last_temp = cpu_temp;
            }
            if(thermalListener != null){
                if(cpu_temp >= CPU_POWER_OFF_HIGH_TEMP_THRESHOLD){
                    EventUtil.sendEvent(Integer.valueOf(CPU_POWER_OFF_HIGH_TEMP_THRESHOLD));
                } else if((cpu_temp >= CPU_HIGH_TEMP_THRESHOLD) && (cpu_temp < CPU_POWER_OFF_HIGH_TEMP_THRESHOLD) ){
                    EventUtil.sendEvent(Integer.valueOf(CPU_HIGH_TEMP_THRESHOLD));
                }else if(0 < cpu_temp &&  cpu_temp <= CPU_HIGH_TEMP_CRITICAL_POINT){
                    EventUtil.sendEvent(Integer.valueOf(CPU_HIGH_TEMP_CRITICAL_POINT));
                }
                if(lcd_temp >= LCD_HIGH_TEMP_THRESHOLD){
                    if(LcdManager.getInstance().getLcdOnOffStatus() == LCD_ON) {
                        EventUtil.sendEvent(Integer.valueOf(LCD_HIGH_TEMP_NOTIFY));
                    }
                }
                if(lcd_temp <= LCD_NORMAL_TEMP_THRESHOLD){
                    if(LcdManager.getInstance().getLcdOnOffStatus() == LCD_OFF){
                        EventUtil.sendEvent(Integer.valueOf(LCD_NORMAL_TEMP_THRESHOLD));
                    }
                }
            }
            mThermalMonitorHandler.postDelayed(this,1*7*1000);
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHandleHighTempEvent(Integer eventType){
        if(eventType.intValue() == Event.HIGH_TEMPERATURE_THRESHOLD_LV1){
            if(thermalListener != null){
                Logg.d(TAG,"lcd_temp closeLcdPanel");
                thermalListener.closeLcdPanel();
            }
        } else if(eventType.intValue() == CPU_POWER_OFF_HIGH_TEMP_THRESHOLD){
            if(DialogManager.getIntance().isDialogShowing(Event.HIGH_TEMPERATURE_THRESHOLD_LV3)){
                return;
            }
            EventManager.getInstance().handOutEventInfo(Event.HIGH_TEMPERATURE_THRESHOLD_LV3);
        }else if(eventType.intValue() == CPU_HIGH_TEMP_THRESHOLD){
            if(DialogManager.getIntance().isDialogShowing(Event.HIGH_TEMPERATURE_THRESHOLD_LV2)){
                return;
            }
            EventManager.getInstance().handOutEventInfo(Event.HIGH_TEMPERATURE_THRESHOLD_LV2);
            thermalListener.closeRecording();
        }else if(eventType.intValue() == CPU_HIGH_TEMP_CRITICAL_POINT){
            thermalListener.startRecording();
        }else if(eventType.intValue() == LCD_HIGH_TEMP_NOTIFY){
            if(DialogManager.getIntance().isDialogShowing(Event.HIGH_TEMPERATURE_THRESHOLD_LV1)){
                return;
            }
            EventManager.getInstance().handOutEventInfo(Event.HIGH_TEMPERATURE_THRESHOLD_LV1);
        }else if(eventType.intValue() == LCD_NORMAL_TEMP_THRESHOLD){
            thermalListener.startLcdPanel();
        }

    }
    private int getSensorTemp(String sensor){
        int temp = 0;
        File fileDir = new File(sensor);
        if ((fileDir).exists()) {
            try {
                InputStreamReader isr = new InputStreamReader(new FileInputStream(fileDir), "UTF-8");
                BufferedReader br = new BufferedReader(isr);
                String str = br.readLine();
                br.close();
                isr.close();
                if(!TextUtils.isEmpty(str)) {
                    temp = Integer.parseInt(str);
                }
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
        return temp;
    }
}