package com.askey.dvr.cdr7010.dashcam.core.nmea;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.util.Pools.SynchronizedPool;
import android.util.Log;

import com.askey.dvr.cdr7010.dashcam.util.Logg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class NmeaRecorder {

    private static final String LOG_TAG = NmeaRecorder.class.getSimpleName();
    private static final SynchronizedPool<NmeaRecorder> nmeaRecorderPool = new SynchronizedPool<NmeaRecorder>(5);
    private static ScheduledThreadPoolExecutor sensorThreadPool = new ScheduledThreadPoolExecutor(10);
    private static ScheduledThreadPoolExecutor gpsThreadPool = new ScheduledThreadPoolExecutor(10);
    private static List<NmeaRecorder> nmeaRecorderListener = new ArrayList<>();
    private static List<NmeaRecorder> removeNmeaRecorders = new ArrayList<>();

    private static LinkedHashMap<Long, NmeaNodeSet> histroyPool = new LinkedHashMap<>();
    private static LinkedHashMap<Integer, NmeaNode> nmeaNodePool = new LinkedHashMap<Integer, NmeaNode>();
    private static LinkedHashMap<Integer, String> sensorNodePool = new LinkedHashMap<Integer, String>();

    private static String[] currentDatas = new String[5];
    private static float[] currentGsensors = new float[3];

    private RecorderState mState;
    private FileOutputStream mOutputStream;
    private long mRecordTime, mStartTime, mEndTime;
    private static final Object mSync = new Object();
    private static final String newLineSymbol = "\r\n";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmssZ");

    public enum RecorderState {
        CREATED,
        STARTED,
        STOPPED
    }

    private NmeaRecorder() {

    }

    public static void init(Context context) {
        initNmeaSet();
        initLocation(context);
        initSensorManager(context);
        initNmeaDirToSDcard();
        initScheduleRate();
    }

    public static NmeaRecorder create(String filePath) {
        NmeaRecorder acquire = nmeaRecorderPool.acquire();
        if (acquire == null) {
            acquire = new NmeaRecorder();
            // Logg.i(LOG_TAG, "new acquire = " + acquire);
        }

        acquire.mState = RecorderState.CREATED;

        File file = new File(filePath); //TODO: Need to follow fileManager process
        try {
            acquire.mOutputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            return null;
        }
        Logg.i(LOG_TAG, "Create file done");
        return acquire;
    }

    public boolean eventStart(final long startTimeStamp) {
        if (mState != RecorderState.CREATED)
            throw new RuntimeException("The NmeaRecorder state is not CREATED.");
        Log.i(LOG_TAG, "Event NMEARecord start");

        mState = RecorderState.STARTED;
        mStartTime = startTimeStamp / 1000;
        mRecordTime = mStartTime - 10;
        mEndTime = mStartTime + 4;
        // Log.i(LOG_TAG, "Event startTime = " + mStartTime + ", recordTime" + mRecordTime + ", endTime" + mEndTime);
        try {
            mOutputStream.write("$GTRIP,ABCD1234,2".getBytes()); //TODO: Change JVC format
            mOutputStream.write(newLineSymbol.getBytes());
            while (mRecordTime < mStartTime) {
                // Logg.i(LOG_TAG,"recordTime = " + mRecordTime);
                writeToFile(mRecordTime);
                mRecordTime++;
            }
            nmeaRecorderListener.add(this);
        } catch (IOException e) {
            // throw new RuntimeException(e);
            return false;
        }
        return true;
    }


    public boolean start(final long startTimeStamp, final long durationInSec) {
        if (mState != RecorderState.CREATED)
            throw new RuntimeException("The NmeaRecorder state is not CREATED.");
        Log.i(LOG_TAG, "NMEARecord start");
        mState = RecorderState.STARTED;
        mStartTime = startTimeStamp / 1000;
        mRecordTime = mStartTime;
        mEndTime = mStartTime + durationInSec;
        // Log.i(LOG_TAG, "startTime = " + mStartTime + ", recordTime" + mRecordTime + ", endTime" + mEndTime);
        try {
            mOutputStream.write("$GTRIP,ABCD1234,2".getBytes()); //TODO: Change JVC format
            mOutputStream.write(newLineSymbol.getBytes());
            nmeaRecorderListener.add(this);
        } catch (IOException e) {
            // throw new RuntimeException(e);
            return false;
        }
        return true;
    }

    public boolean stop() {
        if (mState != RecorderState.STARTED)
            throw new RuntimeException("The NmeaRecorder state is not STARTED.");
        mState = RecorderState.STOPPED;

        try {
            // Log.i(LOG_TAG, "stop: recordTime = " + mRecordTime);
            Log.i(LOG_TAG, "Stop record");
            mOutputStream.flush();
            mOutputStream.close();
            removeNmeaRecorders.add(this);
            nmeaRecorderPool.release(this);
        } catch (IOException e) {
            // throw new RuntimeException(e);
            return false;
        }
        return true;
    }

    public RecorderState getState() {
        return mState;
    }

    private void writeToFile(long recodingTime) {
        try {
            NmeaNodeSet nmeaNodeSet = histroyPool.get(recodingTime);
            if (nmeaNodeSet == null) {
                Log.i(LOG_TAG, "Don't find nmeaNodeSet = " + recodingTime);
                return;
            }
            // String tempString = "number" + nmeaNodeSet.keyNumber;
            // mOutputStream.write(tempString.getBytes());
            // mOutputStream.write(newLineSymbol.getBytes());
            for (int nodeNumber = 0; nodeNumber < nmeaNodeSet.nmeaNodes.length; nodeNumber++) {
                for (int dataNumber = 0; dataNumber < nmeaNodeSet.nmeaNodes[nodeNumber].dataArray.length; dataNumber++) {
                    if (mState == RecorderState.STOPPED) {
                        return;
                    }
                    if (!nmeaNodeSet.nmeaNodes[nodeNumber].dataArray[dataNumber].isEmpty()) {
                        mOutputStream.write(nmeaNodeSet.nmeaNodes[nodeNumber].dataArray[dataNumber].getBytes());
                    }
                }
            }
        } catch (IOException e) {
            // throw new RuntimeException(e);
        }
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
            for (int number = 0; number < 30; number++) {
                NmeaNode nmeaNode = new NmeaNode();
                nmeaNode.nodeNumber = number;
                nmeaNodePool.put(number, nmeaNode);
            }
        }

        if (sensorNodePool.size() > 0) {
            sensorNodePool.clear();
        }

        for (int number = 0; number < 5; number++) {
            String sensorNode = "$GSENS,999.999,999.999,999.999" + newLineSymbol;
            sensorNodePool.put(number, sensorNode);
        }

        for (int number = 0; number < 5; number++) {
            currentDatas[number] = "";
        }
    }

    @SuppressLint("MissingPermission")
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

        // if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        //         && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        //     // TODO: Consider calling
        //     //    ActivityCompat#requestPermissions
        //     // here to request the missing permissions, and then overriding
        //     //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
        //     //                                          int[] grantResults)
        //     // to handle the case where the user grants the permission. See the documentation
        //     // for ActivityCompat#requestPermissions for more details.
        //     return;
        // }

        Location location = locationManager.getLastKnownLocation(locationProvider);
        if (location != null) {
            mLatitude = location.getLatitude();
            mLongitude = location.getLongitude();
            // Log.i(LOG_TAG, "init: Latitude = " + mLatitude + ", Longitude = " + mLongitude);
        }

        locationManager.requestLocationUpdates(locationProvider, 500, 0, locationListener);
        locationManager.addNmeaListener(nmeaListener);
    }

    private static LocationListener locationListener = new LocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle arg2) {
            // Log.d("GPS-NMEA", provider + "");
            switch (status) {
                case LocationProvider.OUT_OF_SERVICE:
                    // Log.d("GPS-NMEA", "OUT_OF_SERVICE");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    // Log.d("GPS-NMEA", " TEMPORARILY_UNAVAILABLE");
                    break;
                case LocationProvider.AVAILABLE:
                    // Log.d("GPS-NMEA", "" + provider + "");
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
            // Log.i(LOG_TAG,"nmea show = " + nmea);
            String[] datas = nmea.split(",");
            switch (datas[0]) {
                case "$GNRMC":
                    // nmea.replaceFirst("GNRMC", "GPRMC"); //Change to GPRMC
                    currentDatas[0] = nmea.replaceFirst("GNRMC", "GPRMC");
                    break;
                case "$GPRMC":
                    currentDatas[0] = nmea;
                    break;
                case "$GNGGA":
                    // nmea.replaceFirst("GNGGA", "GPGGA"); //Change to GPGGA
                    currentDatas[1] = nmea.replaceFirst("GNGGA", "GPGGA");
                    break;
                case "$GPGGA":
                    currentDatas[1] = nmea;
                    break;
                case "$GPGSV":
                    if (Integer.valueOf(datas[2]) == 1) {
                        currentDatas[2] = "";
                        currentDatas[2] = nmea;
                    } else {
                        currentDatas[2] = currentDatas[2] + nmea;
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
            System.arraycopy(event.values, 0, currentGsensors, 0, currentGsensors.length);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private static void initSensorManager(Context context) {
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
    }

    private static Runnable sensorRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (mSync) {
                int keyNumber = sensorNodePool.entrySet().iterator().next().getKey();
                sensorNodePool.remove(keyNumber);
                String sensorData;
                if (currentGsensors[0] == 0.0 && currentGsensors[1] == 0.0 && currentGsensors[2] == 0.0) {
                    sensorData = "$GSENS,999.999,999.999,999.999" + newLineSymbol;
                } else {
                    sensorData = String.format("$GSENS,%.3f,%.3f,%.3f", currentGsensors[0], currentGsensors[1], currentGsensors[2]) + newLineSymbol;
                }
                sensorNodePool.put(keyNumber, sensorData);
            }
        }
    };

    private static Runnable gpsRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                NmeaNode nmeaNode = nmeaNodePool.remove(nmeaNodePool.entrySet().iterator().next().getKey());
                currentDatasUpdate();
                if (nmeaNode != null) {
                    nmeaNodeUpdate(nmeaNode);
                } else {
                    Log.i(LOG_TAG, "nnmeaNode not get node");
                }
            } catch (Exception e) {
                // Log.i(LOG_TAG, "error = " + e);
            }
        }
    };

    private static void currentDatasUpdate() {
        if (!currentDatas[0].contains("$GPRMC")) {
            currentDatas[0] = "$GPRMC,,V,,,,,,,,,,N*AA";
            currentDatas[0] = currentDatas[0] + newLineSymbol;
        }
        currentDatas[3] = "";
        synchronized (mSync) {
            for (Map.Entry<Integer, String> entry : sensorNodePool.entrySet()) {
                currentDatas[3] = currentDatas[3] + entry.getValue();
            }
        }
        currentDatas[4] = "$JKDSA,,,,,,,,,,," + newLineSymbol; //TODO: Change JVC format
    }

    private static void nmeaNodeUpdate(NmeaNode nmeaNode) {
        long currentTime = System.currentTimeMillis() / 1000;

        System.arraycopy(currentDatas, 0, nmeaNode.dataArray, 0, nmeaNode.dataArray.length);
        if (nmeaNode.nodeNumber % 2 == 0) {
            nmeaNodePool.put(nmeaNode.nodeNumber, nmeaNode);
        } else if (nmeaNode.nodeNumber % 2 == 1) {

            NmeaNodeSet nmeaNodeSet = histroyPool.remove(histroyPool.entrySet().iterator().next().getKey());
            if (nmeaNodeSet == null) {
                Log.i(LOG_TAG, "nmeaNodeSet = null, " + currentTime);
            } else {
                nmeaNodeSetUpdate(nmeaNodeSet, nmeaNode, currentTime);
            }

        } else {
            throw new RuntimeException("The NmeaRecorder state is not CREATED.");
        }
    }

    private static void nmeaNodeSetUpdate(NmeaNodeSet nmeaNodeSet, NmeaNode nmeaNode, long currentTime) {
        NmeaNode nmeaNodeFront = nmeaNodePool.get(nmeaNode.nodeNumber - 1);
        nmeaNodeSet.keyNumber = currentTime;
        nmeaNodeSet.nmeaNodes[0] = nmeaNodeFront;
        nmeaNodeSet.nmeaNodes[1] = nmeaNode;
        histroyPool.put(nmeaNodeSet.keyNumber, nmeaNodeSet);
        nmeaNodePool.put(nmeaNode.nodeNumber, nmeaNode);
        // Log.i(LOG_TAG, "nmeaNodeSet keyNumber" + nmeaNodeSet.keyNumber);

        for (NmeaRecorder nmeaRecorder : nmeaRecorderListener) {
            if (nmeaRecorder.getState() == RecorderState.STARTED) {
                // Log.i(LOG_TAG, "nmeaRecorder = " + nmeaRecorder.state + ", write = " + nmeaNodeSet.keyNumber);
                while (nmeaRecorder.mRecordTime < nmeaNodeSet.keyNumber) {  //Supplement RecordTime to now
                    nmeaRecorder.writeToFile(nmeaRecorder.mRecordTime);
                    nmeaRecorder.mRecordTime++;
                }
                nmeaRecorder.writeToFile(nmeaNodeSet.keyNumber);
                nmeaRecorder.mRecordTime++;
            }
            if (nmeaRecorder.mEndTime == nmeaNodeSet.keyNumber) {
                nmeaRecorder.stop();
            }
        }
        for (NmeaRecorder nmeaRecorder : removeNmeaRecorders) {
            nmeaRecorderListener.remove(nmeaRecorder);
        }
        removeNmeaRecorders.clear();
    }


    private static void initScheduleRate() {
        if (sensorThreadPool.getPoolSize() == 0 && gpsThreadPool.getPoolSize() == 0) {
            sensorThreadPool.scheduleAtFixedRate(sensorRunnable, 0, 100, TimeUnit.MILLISECONDS);
            gpsThreadPool.scheduleAtFixedRate(gpsRunnable, 0, 500, TimeUnit.MILLISECONDS);
        }
    }

    private static String setNmeaJkopt(long currentTime) {
        Date date = new Date(currentTime);
        char[] timeSplit = dateFormat.format(date).toCharArray();
        String jkpot = "$JKOPT,0x" + timeSplit[0] + timeSplit[1] + ",0x"
                + timeSplit[2] + timeSplit[3] + ",0x" + timeSplit[4] + timeSplit[5] + ",0x"
                + timeSplit[6] + timeSplit[7] + ",0x" + timeSplit[8] + timeSplit[9] + ",0x"
                + timeSplit[10] + timeSplit[11] + ",,,,,,0x0000,0" + newLineSymbol;
//        Log.i(LOG_TAG,"currentTime = " + currentTime + "show string = " + jkpot);

        return jkpot;
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

        // dir = new File(path + "/SYSTEM/NMEA/PARKING");
        // if (!dir.exists()) {
        //     Log.d(LOG_TAG, "mkdir = " + dir.getPath());
        //     dir.mkdir();
        // } else {
        //     Log.i(LOG_TAG, "mkdir = " + dir.getPath());
        // }
        //
        // dir = new File(path + "/SYSTEM/NMEA/MANUAL");
        // if (!dir.exists()) {
        //     Log.d(LOG_TAG, "mkdir = " + dir.getPath());
        //     dir.mkdir();
        // } else {
        //     Log.i(LOG_TAG, "mkdir = " + dir.getPath());
        // }
    }
}
