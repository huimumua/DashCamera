package com.askey.dvr.cdr7010.dashcam.core.nmea;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.util.Pools.SynchronizedPool;
import android.util.Log;

import com.askey.dvr.cdr7010.dashcam.util.Logg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class NmeaRecorder {

    private static final String LOG_TAG = NmeaRecorder.class.getSimpleName();
    private static final SynchronizedPool<NmeaRecorder> nmeaRecorderPool = new SynchronizedPool<NmeaRecorder>(5);
    private static ScheduledThreadPoolExecutor sensorThreadPool = new ScheduledThreadPoolExecutor(10);
    private static ScheduledThreadPoolExecutor GPSThreadPool = new ScheduledThreadPoolExecutor(10);
    private static List<NmeaRecorder> nmeaRecorderListener = new ArrayList<NmeaRecorder>();

    private static LinkedHashMap<Long, NmeaNodeSet> histroyPool = new LinkedHashMap<Long, NmeaNodeSet>();
    private static LinkedHashMap<Integer, NmeaNode> nmeaNodePool = new LinkedHashMap<Integer, NmeaNode>();
    private static LinkedHashMap<Integer, String> sensorNodePool = new LinkedHashMap<Integer, String>();

    private static String[] tempDataArray = new String[6];
    private static float[] tempSensorArray = new float[3];

    public RecorderState state;
    private FileOutputStream outputStream;
    private long recordTime, startTime, endTime;
    private static final Object mSync = new Object();
    private static final String newLineSymbol = "\r\n";

    public enum RecorderState {
        CREATED,
        STARTED,
        STOPPED
    }

    private NmeaRecorder() {

    }

    public static boolean init(Context context) {
        initNmeaSet();
        initLocation(context);
        initSensorManager(context);
        initNmeaDirToSDcard();
        initScheduleRate();
        Logg.i(LOG_TAG, "init");
        return true;
    }

    public static NmeaRecorder create(String filePath) {
        NmeaRecorder acquire = nmeaRecorderPool.acquire();
        if (acquire == null) {
            acquire = new NmeaRecorder();
            Logg.i(LOG_TAG, "new acquire = " + acquire);
        }

        acquire.state = RecorderState.CREATED;
        File file = new File(filePath); //TODO: Need to follow fileManager process
        try {
            acquire.outputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            Logg.i(LOG_TAG, "create error = " + e);
            throw new RuntimeException(e);
        }
        Logg.i(LOG_TAG, "create file done = " + acquire);
        return acquire;
    }

    public boolean eventStart(final long startTimeStamp) {
        this.state = RecorderState.STARTED;
        startTime = startTimeStamp / 1000;
        recordTime = startTime - 10;
        endTime = startTime + 5;
        Log.i(LOG_TAG, "startTime = " + startTime + ", recordTime" + recordTime + ", endTime" + endTime);
        try {
            this.outputStream.write("$GTRIP,ABCD1234,2".getBytes()); //TODO: Change JVC format
            this.outputStream.write(newLineSymbol.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (recordTime < startTime) {
            writeToFile(recordTime);
            recordTime++;
        }
        nmeaRecorderListener.add(this);

        return true;
    }


    public boolean start(final long startTimeStamp, final long timeLenght) {
        this.state = RecorderState.STARTED;
        startTime = startTimeStamp / 1000;
        recordTime = startTime;
        endTime = startTime + timeLenght;
        Log.i(LOG_TAG, "startTime = " + startTime + ", recordTime" + recordTime + ", endTime" + endTime);
        try {
            this.outputStream.write("$GTRIP,ABCD1234,2".getBytes()); //TODO: Change JVC format
            this.outputStream.write(newLineSymbol.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        nmeaRecorderListener.add(this);
        return true;
    }

    public boolean stop() {
        if (this.state != RecorderState.STARTED)
            throw new RuntimeException("The NmeaRecorder state is not STARTED.");
        this.state = RecorderState.STOPPED;

        try {
            Log.i(LOG_TAG, "stop: recordTime = " + recordTime);
            this.outputStream.flush();
            this.outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        nmeaRecorderPool.release(this);
        nmeaRecorderListener.remove(this);

        return true;
    }

    private boolean writeToFile(long recodingTime) {
        try {
            NmeaNodeSet nmeaNodeSet = histroyPool.get(recodingTime);
            if (nmeaNodeSet != null) {
//                String tempString = "number" + nmeaNodeSet.keyNumber;
//                outputStream.write(tempString.getBytes());
//                outputStream.write(newLineSymbol.getBytes());
                for (int nodeNumber = 0; nodeNumber < nmeaNodeSet.nmeaNodes.length; nodeNumber++) {
                    //outputStream.write(("nodeNumber" + nodeNumber).getBytes());
                    //outputStream.write(newLineSymbol.getBytes());
                    for (int dataNumber = 0; dataNumber < nmeaNodeSet.nmeaNodes[nodeNumber].dataArray.length; dataNumber++) {
                        if (this.state == RecorderState.STOPPED) {
                            return true;
                        }
                        if (!nmeaNodeSet.nmeaNodes[nodeNumber].dataArray[dataNumber].isEmpty()) {
                            outputStream.write(nmeaNodeSet.nmeaNodes[nodeNumber].dataArray[dataNumber].getBytes());
                            if (dataNumber > 3)
                                outputStream.write(newLineSymbol.getBytes());
                        }
                    }
                }
            } else {
                Log.i(LOG_TAG, "no nmeaNodeSet = " + recodingTime);
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static boolean deinit(Context context) {
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensorManager.unregisterListener(sensorEventListener);
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(locationListener);
        locationManager.removeNmeaListener(nmeaListener);
        return true;
    }

    private static void initNmeaSet() {
        if (histroyPool.size() == 0) {
            for (long timestamp = 0; timestamp < 15; timestamp++) {
                NmeaNodeSet nmeaNodeSet = new NmeaNodeSet();
                nmeaNodeSet.keyNumber = timestamp;
                histroyPool.put(timestamp, nmeaNodeSet);
            }
        }

        if (nmeaNodePool.size() == 0) {
            for (int number = 0; number < 20; number++) {
                NmeaNode nmeaNode = new NmeaNode();
                nmeaNode.nodeNumber = number;
                nmeaNodePool.put(number, nmeaNode);
            }
        }

        if (sensorNodePool.size() == 0) {
            for (int number = 0; number < 5; number++) {
                String sensorNode = "$GSENS,999.999,999.999,999.999" + newLineSymbol;
                sensorNodePool.put(number, sensorNode);
            }
        }

        for (int number = 0; number < 6; number++) {
            tempDataArray[number] = "";
        }
    }

    private static void initLocation(Context context) {
        double mLatitude;
        double mLongitude;
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        String locationProvider;
        if (providers.contains(LocationManager.GPS_PROVIDER)) {
            locationProvider = LocationManager.GPS_PROVIDER;
        } else {
            locationProvider = LocationManager.NETWORK_PROVIDER;
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        Location location = locationManager.getLastKnownLocation(locationProvider);
        if (location != null) {
            mLatitude = location.getLatitude();
            mLongitude = location.getLongitude();
//            Log.i(LOG_TAG, "init: Latitude = " + mLatitude + ", Longitude = " + mLongitude);
        }

        locationManager.requestLocationUpdates(locationProvider, 500, 0, locationListener);
        locationManager.addNmeaListener(nmeaListener);
    }

    private static LocationListener locationListener = new LocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle arg2) {
//            Log.d("GPS-NMEA", provider + "");
            switch (status) {
                case LocationProvider.OUT_OF_SERVICE:
//                    Log.d("GPS-NMEA", "OUT_OF_SERVICE");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
//                    Log.d("GPS-NMEA", " TEMPORARILY_UNAVAILABLE");
                    break;
                case LocationProvider.AVAILABLE:
//                    Log.d("GPS-NMEA", "" + provider + "");
                    break;
            }
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onLocationChanged(Location location) {

        }
    };

    private static GpsStatus.NmeaListener nmeaListener = new GpsStatus.NmeaListener() {
        public void onNmeaReceived(long timestamp, String nmea) {
//            Log.i(LOG_TAG,"nmea show = " + nmea);
            String[] datas = nmea.split(",");
            switch (datas[0]) {
                case "$GNRMC":
                    nmea.replaceFirst("GNRMC","GPRMC"); //Change to GPRMC
                    tempDataArray[0] = nmea;
                    break;
                case "$GPRMC":
                    tempDataArray[0] = nmea;
                    break;
                case "$GNGGA":
                    nmea.replaceFirst("GNGGA","GPGGA"); //Change to GPGGA
                    tempDataArray[1] = nmea;
                    break;
                case "$GPGGA":
                    tempDataArray[1] = nmea;
                    break;
                case "$GPGSV":
                    if (Integer.valueOf(datas[2]) == 1) {
                        tempDataArray[2] = "";
                        tempDataArray[2] = nmea;
                    } else {
                        tempDataArray[2] = tempDataArray[2] + nmea;
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private static SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            //Log.i(LOG_TAG, "value = " + "$GSENS," + showXYZ);
            System.arraycopy(event.values, 0, tempSensorArray, 0, tempSensorArray.length);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private static void initSensorManager(Context context) {
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
    }

    private static void initScheduleRate() {
        if (sensorThreadPool.getPoolSize() == 0 && GPSThreadPool.getPoolSize() == 0) {
            sensorThreadPool.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
//                Log.i(LOG_TAG, "SensorPool");
                    int keyNumber = sensorNodePool.entrySet().iterator().next().getKey();
                    String sensorData = sensorNodePool.remove(keyNumber);
                    if (tempSensorArray[0] == 0.0 && tempSensorArray[1] == 0.0 && tempSensorArray[2] ==0.0){
                        sensorData = "$GSENS,999.999,999.999,999.999" + newLineSymbol;
                    } else {
                        sensorData = "$GSENS," + String.format("%.3f", tempSensorArray[0]) + String.format(",%.3f", tempSensorArray[1]) + String.format(",%.3f", tempSensorArray[2]) + newLineSymbol;
                    }
                    sensorNodePool.put(keyNumber, sensorData);
                }
            }, 0, 100, TimeUnit.MILLISECONDS);

            GPSThreadPool.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    long errorTime;
                    try {
                        synchronized (mSync) {
                            if (!tempDataArray[0].contains("$GPRMC")) {
                                tempDataArray[0] = "$GPRMC,,V,,,,,,,,,,N*AA";
                            }
                            tempDataArray[3] = "";
                            for (Map.Entry<Integer, String> entry : sensorNodePool.entrySet()) {
                                tempDataArray[3] = tempDataArray[3] + entry.getValue();
                            }


                            NmeaNode nmeaNode = nmeaNodePool.remove(nmeaNodePool.entrySet().iterator().next().getKey());
                            if (nmeaNode != null) {
                                nmeaNode.dataArray[0] = tempDataArray[0];
                                nmeaNode.dataArray[1] = tempDataArray[1];
                                nmeaNode.dataArray[2] = tempDataArray[2];
                                nmeaNode.dataArray[3] = tempDataArray[3];
                                nmeaNode.dataArray[4] = "$JKOPT,0x10,0x07,0x0f,0x08,0x36,0x0c,0x01f58a56,0x07a7ee6b,0x01,0x01,0x006d,0x0000,0"; //TODO: Chnage JVC format
                                nmeaNode.dataArray[5] = "$JKDSA,,,,,,,,,,,"; //TODO: Change JVC format
//                    Log.i(LOG_TAG,"nmeaNode nodeNumber = " + nmeaNode.nodeNumber);
                                if (nmeaNode.nodeNumber % 2 == 0) {
                                    nmeaNodePool.put(nmeaNode.nodeNumber, nmeaNode);
                                } else if (nmeaNode.nodeNumber % 2 == 1) {
                                    NmeaNode nmeaNodeFront = nmeaNodePool.get(nmeaNode.nodeNumber - 1);

                                    NmeaNodeSet nmeaNodeSet = histroyPool.remove(histroyPool.entrySet().iterator().next().getKey());
                                    nmeaNodeSet.keyNumber = System.currentTimeMillis() / 1000;
                                    nmeaNodeSet.nmeaNodes[0] = nmeaNodeFront;
                                    nmeaNodeSet.nmeaNodes[1] = nmeaNode;
                                    histroyPool.put(nmeaNodeSet.keyNumber, nmeaNodeSet);
                                    nmeaNodePool.put(nmeaNode.nodeNumber, nmeaNode);
//                                    Log.i(LOG_TAG, "nmeaNodeSet keyNumber" + nmeaNodeSet.keyNumber);
                                    List<NmeaRecorder> removeNmeaRecorders = new ArrayList<NmeaRecorder>();
                        /*for (NmeaRecorder nmeaRecorder : removeNmeaRecorders) {
                            nmeaRecorderListener.remove(nmeaRecorder);
                        }*/

                                    for (NmeaRecorder nmeaRecorder : nmeaRecorderListener) {
//                                        Log.i(LOG_TAG, "nmeaRecorder = " + nmeaRecorder.state + ", write = " + nmeaNodeSet.keyNumber);
                                        while (nmeaRecorder.recordTime < nmeaNodeSet.keyNumber) {
                                            nmeaRecorder.writeToFile(nmeaRecorder.recordTime);
                                            nmeaRecorder.recordTime++;
                                        }
                                        if (nmeaRecorder.state == RecorderState.STARTED) {
                                            nmeaRecorder.writeToFile(nmeaNodeSet.keyNumber);
                                            nmeaRecorder.recordTime++;
                                        }
//                                        Log.i(LOG_TAG, "nmeaRecorder endTime = " + nmeaRecorder.endTime);
                            if (nmeaNodeSet.keyNumber == nmeaRecorder.endTime) {
                                Log.i(LOG_TAG,"call stop()");
                                nmeaRecorder.stop();
                                removeNmeaRecorders.add(nmeaRecorder);
                            }
//                            Log.i(LOG_TAG, "nmeaRecorder startTime = "+ nmeaRecorder.startTime);
                                    }
                                } else {
                                    throw new RuntimeException("The NmeaRecorder state is not CREATED.");
                                }
                            } else {
                                Log.i(LOG_TAG, "nnmeaNode not get node");
                            }
                        }

                    } catch (Throwable throwable) {
                        Log.i(LOG_TAG, "error = " + throwable);
                    }
                }
            }, 0, 500, TimeUnit.MILLISECONDS);
        }
    }

    private static void initNmeaDirToSDcard() {  //TODO: Change to FileManager
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            Log.d(LOG_TAG, "RW");
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            Log.d(LOG_TAG, "onlyR");
        } else {
            Log.d(LOG_TAG, "noRW");
        }
        String path = Environment.getExternalStorageDirectory().getPath();
        File dir = new File(path + "/SYSTEM/NMEA");
        if (!dir.exists()) {
            Log.d(LOG_TAG, "mkdir = " + dir.getPath());
            dir.mkdir();
        } else {
            Log.i(LOG_TAG, "mkdir = " + dir.getPath());
        }
        dir = new File(path + "/SYSTEM/NMEA/EVENT");
        if (!dir.exists()) {
            Log.d(LOG_TAG, "mkdir = " + dir.getPath());
            dir.mkdir();
        } else {
            Log.i(LOG_TAG, "mkdir = " + dir.getPath());
        }

        dir = new File(path + "/SYSTEM/NMEA/NORMAL");
        if (!dir.exists()) {
            Log.d(LOG_TAG, "mkdir = " + dir.getPath());
            dir.mkdir();
        } else {
            Log.i(LOG_TAG, "mkdir = " + dir.getPath());
        }

        dir = new File(path + "/SYSTEM/NMEA/PARKING");
        if (!dir.exists()) {
            Log.d(LOG_TAG, "mkdir = " + dir.getPath());
            dir.mkdir();
        } else {
            Log.i(LOG_TAG, "mkdir = " + dir.getPath());
        }

        dir = new File(path + "/SYSTEM/NMEA/MANUAL");
        if (!dir.exists()) {
            Log.d(LOG_TAG, "mkdir = " + dir.getPath());
            dir.mkdir();
        } else {
            Log.i(LOG_TAG, "mkdir = " + dir.getPath());
        }
    }
}
