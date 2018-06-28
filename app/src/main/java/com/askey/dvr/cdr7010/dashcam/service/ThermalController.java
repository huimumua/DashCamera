package com.askey.dvr.cdr7010.dashcam.service;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.askey.dvr.cdr7010.dashcam.domain.Event;
import com.askey.dvr.cdr7010.dashcam.util.EventUtil;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class ThermalController{
    private static final String TAG = ThermalController.class.getSimpleName();
    private static final String LCD_THERMAL_ZONE_PATH ="/sys/class/thermal/thermal_zone15/temp";
    private static final String CPU_THERMAL_ZONE_PATH ="/sys/class/thermal/thermal_zone1/temp";
    private static final int CPU_HIGH_TEMP_THRESHOLD = 85;
    private static final int CPU_HIGH_TEMP_CRITICAL_POINT = 70;
    private static final int LCD_HIGH_TEMP_THRESHOLD = 69;
    private HandlerThread mThermalMonitorThread;
    private Handler mThermalMonitorHandler;
    private ThermalListener thermalListener;
    private boolean isCloseLcd;
    public interface ThermalListener{
        void startRecording();
        void closeRecording();
        void closeLcdPanel();
    }
    public ThermalController(ThermalListener thermalListener){
        this.thermalListener = thermalListener;
        EventUtil.register(this);
    }
    public void startThermalMonitor(){
        mThermalMonitorThread = new HandlerThread("ThermalMonitor");
        mThermalMonitorThread.start();
        mThermalMonitorHandler = new Handler(mThermalMonitorThread.getLooper());
        mThermalMonitorHandler.postDelayed(new ThermalMonitorRunnable(),0);
    }
    public void stopThermalMonitor(){
        mThermalMonitorThread.quit();
        mThermalMonitorHandler = null;
        EventUtil.unregister(this);
    }
    public void setLcdCloseStatus(boolean isCloseLcd){
        this.isCloseLcd = isCloseLcd;
    }
    private boolean isCloseLcd(){
        return isCloseLcd;
    }

    private  class ThermalMonitorRunnable implements Runnable {
       @Override
       public void run(){
           int cpu_temp = getSensorTemp(CPU_THERMAL_ZONE_PATH);
           int lcd_temp = getSensorTemp(LCD_THERMAL_ZONE_PATH)/10;
           if(thermalListener != null){
               if(cpu_temp > CPU_HIGH_TEMP_THRESHOLD){
                   EventUtil.sendEvent(Integer.valueOf(CPU_HIGH_TEMP_THRESHOLD));
               }else if(0 < cpu_temp &&  cpu_temp <= CPU_HIGH_TEMP_CRITICAL_POINT){
                   EventUtil.sendEvent(Integer.valueOf(CPU_HIGH_TEMP_CRITICAL_POINT));
               }
               if(lcd_temp > LCD_HIGH_TEMP_THRESHOLD){
                   if(!isCloseLcd()) {
                       EventUtil.sendEvent(Integer.valueOf(LCD_HIGH_TEMP_THRESHOLD));
                   }
               }
           }
           mThermalMonitorHandler.postDelayed(this,1*60*1000);
       }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHandleHighTempEvent(Integer eventType){
        if(eventType.intValue() == Event.HIGH_TEMPERATURE_THRESHOLD_LV1){
            if(thermalListener != null){
                thermalListener.closeLcdPanel();
            }
        } else if(eventType.intValue() == CPU_HIGH_TEMP_THRESHOLD){
            EventManager.getInstance().handOutEventInfo(Event.HIGH_TEMPERATURE_THRESHOLD_LV2);
            thermalListener.closeRecording();
        }else if(eventType.intValue() == CPU_HIGH_TEMP_CRITICAL_POINT){
            thermalListener.startRecording();
        }else if(eventType.intValue() == LCD_HIGH_TEMP_THRESHOLD){
            EventManager.getInstance().handOutEventInfo(Event.HIGH_TEMPERATURE_THRESHOLD_LV1);
            setLcdCloseStatus(true);
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