package com.askey.dvr.cdr7010.dashcam.service;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;

import com.askey.dvr.cdr7010.dashcam.application.DashCamApplication;
import com.askey.dvr.cdr7010.dashcam.domain.GpsSvInfo;
import com.askey.dvr.cdr7010.dashcam.util.Logg;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

/**
 * 项目名称：settings
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2018/4/25 13:27
 * 修改人：skysoft
 * 修改时间：2018/4/25 13:27
 * 修改备注：
 */
public class GPSStatusManager {
    private static final String TAG = "GPSStatusManager";
    private Context mContext;
    private Listener mListener;
    private android.location.LocationManager mLocationManager;
    private boolean mRecordLocation;
    private static GPSStatusManager mnLocationManager;
    private int mGpsUsedInFix;
    private ArrayList<GpsSvInfo> gpsStatusList;

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(android.location.LocationManager.GPS_PROVIDER)/*,
            new LocationListener(android.location.LocationManager.NETWORK_PROVIDER)*/
    };

    public interface Listener {
        void showGpsOnScreenIndicator(boolean hasSignal);

        void hideGpsOnScreenIndicator();
    }

    public GPSStatusManager(Context context, Listener listener) {
        mContext = context;
        mListener = listener;
    }

    public static GPSStatusManager getInstance(Context context) {
        if (mnLocationManager == null)
            mnLocationManager = new GPSStatusManager(context, null);
        return mnLocationManager;
    }

    public static GPSStatusManager getInstance() {
        if (mnLocationManager == null)
            mnLocationManager = new GPSStatusManager(DashCamApplication.getAppContext(), null);
        return mnLocationManager;
    }

    public ArrayList<GpsSvInfo> getGpsStatusList() {
        return gpsStatusList;
    }

    public void setGpsStatusList(ArrayList<GpsSvInfo> gpsStatusList) {
        this.gpsStatusList = gpsStatusList;
    }

    /**
     * 强制帮用户打开GPS
     * @param context
     */
    public void openGPS(Context context) {
//        获取GPS现在的状态（打开或是关闭状态）

        PackageManager pm = context.getPackageManager();
        boolean permission = (PackageManager.PERMISSION_GRANTED ==
                pm.checkPermission("android.permission.WRITE_SECURE_SETTINGS", context.getPackageName()));
        if (permission) {
            boolean result = Settings.Secure.putInt(context.getContentResolver(),
                    Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_HIGH_ACCURACY);
            Logg.i(TAG, "openGPS: over" + result);
        } else {
            Logg.showToast(context, "没有打开GPS的权限");
        }

    }

    //关闭gps
    public void closeGPS(Context context) {

        PackageManager pm = context.getPackageManager();
        boolean permission = (PackageManager.PERMISSION_GRANTED ==
                pm.checkPermission("android.permission.WRITE_SECURE_SETTINGS", context.getPackageName()));
        if (permission) {
            boolean result = Settings.Secure.putInt(context.getContentResolver(),
                    Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF);
            Logg.i(TAG, "closeGPS over" + result);
        } else {
            Logg.showToast(context, "没有关闭GPS的权限");
        }

    }

    public Location getCurrentLocation() {
        if (!mRecordLocation) return null;
        int length = mLocationListeners.length;
        if (length > 0) {
            // go in best to worst order
            for (int i = 0; i < length; i++) {
                Location l = mLocationListeners[i].current();
                if (l != null) return l;
            }
        }
//        Logg.d(TAG, "No location received yet.");
        return null;
    }

    /**
     * 已定位卫星数
     * */
    public int getGpsUsedInFix() {
//        Logg.d(TAG, "getGpsUsedInFix: ");
        if (getCurrentLocation() == null) return 0;
        return mGpsUsedInFix;
    }

    public void recordLocation(boolean recordLocation) {
        if (mRecordLocation != recordLocation) {
            mRecordLocation = recordLocation;
            if (recordLocation) {
                startReceivingLocationUpdates();
            } else {
                stopReceivingLocationUpdates();
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void startReceivingLocationUpdates() {
        Logg.i(TAG, " startReceivingLocationUpdates ");
        if (mLocationManager == null) {
            mLocationManager = (android.location.LocationManager)
                    mContext.getSystemService(Context.LOCATION_SERVICE);
        }
        if (mLocationManager != null) {
            Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            if (year < 2017) {
                mLocationManager.sendExtraCommand("gps", "delete_aiding_data", null);
            }
            boolean gpsRe = mLocationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
            if (!gpsRe) {
                Logg.i(TAG, "getLocationByGPS:请打开GPS定位功能!");
                openGPS(mContext);
                //打开gps
            }

            try {
                mLocationManager.requestLocationUpdates(
                        android.location.LocationManager.GPS_PROVIDER,
                        1000,
                        0F,
                        mLocationListeners[0]);
                mLocationManager.addGpsStatusListener(gpsStatusListener);
                if (mListener != null) mListener.showGpsOnScreenIndicator(false);
            } catch (SecurityException ex) {
                Logg.e(TAG, "fail to request location update, ignore", ex);
            } catch (IllegalArgumentException ex) {
                Logg.d(TAG, "provider does not exist " + ex.getMessage());
            }
        }
        mGpsUsedInFix = 0;
    }

    @SuppressWarnings("deprecation")
    private void stopReceivingLocationUpdates() {
        if (mLocationManager != null) {
            mLocationManager.removeGpsStatusListener(gpsStatusListener);
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (SecurityException ex) {
                    Logg.e(TAG, "fail to request location update, ignore", ex);
                } catch (IllegalArgumentException ex) {
                    Logg.d(TAG, "provider does not exist " + ex.getMessage());
                }
            }
            Logg.d(TAG, "stopReceivingLocationUpdates");
        }
        if (mListener != null) mListener.hideGpsOnScreenIndicator();
     //   closeGPS(DashCamApplication.getAppContext());
    }

    private GpsStatus.Listener gpsStatusListener = new GpsStatus.Listener() {
        public void onGpsStatusChanged(int event) {
//            Logg.d(TAG, "GpsStatus: onGpsStatusChanged: event=" + event);
            switch (event) {
                // 第一次定位
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    Logg.i(TAG, " gpsStatusListener GPS_EVENT_FIRST_FIX.");
                    break;
                // 卫星状态改变
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    Logg.i(TAG, " gpsStatusListener GPS_EVENT_SATELLITE_STATUS.");
                    if (ActivityCompat.checkSelfPermission(DashCamApplication.getAppContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    ArrayList<GpsSvInfo> gpsStatusList = new ArrayList<GpsSvInfo>();
                    GpsStatus gpsStauts = mLocationManager.getGpsStatus(null); // 取当前状态
                    int maxSatellites = gpsStauts.getMaxSatellites(); //获取卫星颗数的默认最大值
                    Iterator<GpsSatellite> it = gpsStauts.getSatellites().iterator();//创建一个迭代器保存所有卫星
                    int gpsCount = 0;
                    while (it.hasNext() && gpsCount <= maxSatellites) {
                        GpsSatellite gpsSatellite = it.next();
                        //可见卫星数量
                       float azimuth = gpsSatellite.getAzimuth();  // 方位角 0-360度
                       float elevation = gpsSatellite.getElevation(); //*返回卫星的高程，以度为单位。海拔可以在0到90之间变化
                       int prn = gpsSatellite.getPrn();//返回卫星的prn（伪随机数）。
                       float snr = gpsSatellite.getSnr();//返回卫星的信号与噪声比。
                        gpsSatellite.hasAlmanac();//如果gps引擎有卫星的年鉴数据，则返回真。
                        gpsSatellite.hasEphemeris();//如果gps引擎有卫星的星历数据，则返回为真
                        gpsSatellite.usedInFix(); //已定卫星数
                        if(gpsSatellite.usedInFix()){
                            //已定位卫星数量
                            gpsCount++;
                            GpsSvInfo gpsSvInfo =new GpsSvInfo();
                            gpsSvInfo.setAzimuth(azimuth);
                            gpsSvInfo.setElevation(elevation);
                            gpsSvInfo.setPrn(prn);
                            gpsSvInfo.setSnr(snr);
                            gpsStatusList.add(gpsSvInfo);
                        }
                    }
                    Logg.d(TAG, "GpsStatus: onGpsStatusChanged: gpsCount=" + gpsCount);
                    mLocationListeners[0].setHaveSatelUsedInFix(gpsCount > 3);
                    mGpsUsedInFix = gpsCount;
                    setGpsStatusList(gpsStatusList);
                    break;
                // 定位启动
                case GpsStatus.GPS_EVENT_STARTED:
                    Logg.i(TAG, " gpsStatusListener GPS_EVENT_STARTED.");
                    break;
                // 定位结束
                case GpsStatus.GPS_EVENT_STOPPED:
                    Logg.i(TAG, " gpsStatusListener GPS_EVENT_STOPPED.");
                    break;
            }
        }
    };

    private class LocationListener
            implements android.location.LocationListener {
        Location mLastLocation;
        private boolean mValid;
        private boolean haveSatelUsedInFix;
        String mProvider;
        Location mLastLocationCash;
        long mLastLocationTime;

        public LocationListener(String provider) {
            mProvider = provider;
            mLastLocation = new Location(mProvider);
            mLastLocationCash = new Location(mProvider);
        }

        @Override
        public void onLocationChanged(Location newLocation) {
            if (newLocation.getLatitude() == 0.0
                    && newLocation.getLongitude() == 0.0) {
                // Hack to filter out 0.0,0.0 locations
                return;
            }
            // If GPS is available before start camera, we won't get status
            // update so update GPS indicator when we receive data.
            if (mListener != null && mRecordLocation &&
                    android.location.LocationManager.GPS_PROVIDER.equals(mProvider)) {
                mListener.showGpsOnScreenIndicator(true);
            }
            if (!mValid) {
                Logg.d(TAG, "Got first location.");
            }
            mLastLocation.set(newLocation);

//            if(mProvider.equals(android.location.LocationManager.NETWORK_PROVIDER)){
//                boolean netAvailable = NetWorkUtils.isNetworkAvailable(DvrApplication.getAppContext());
//                Logg.d(TAG, "LocationListener: onLocationChanged: provider=" + mProvider + ", netAvailable=" + netAvailable);
//                mValid = netAvailable;
//            }else {
            mValid = true;
//            }
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
            mValid = false;
        }

        @Override
        public void onStatusChanged(
                String provider, int status, Bundle extras) {
            Logg.d(TAG, "LocationListener: onStatusChanged: provider=" + mProvider + ", " + provider + ", status=" + status);
            switch(status) {
                case LocationProvider.OUT_OF_SERVICE:
                case LocationProvider.TEMPORARILY_UNAVAILABLE: {
                    mValid = false;
                    if (mListener != null && mRecordLocation &&
                            android.location.LocationManager.GPS_PROVIDER.equals(provider)) {
                        mListener.showGpsOnScreenIndicator(false);
                    }
                    break;

                }
                default:
                    break;
            }
        }

        public Location current() {
            //3秒后若车速相同且大于0，且经纬度位置不变则判定为GPS信号异常；
            // 为解决进隧道时GpsStatus.Listener的卫星状态改变时usedInFix函数数十秒内返回true，而导致进隧道后长时间不显示*号
            if(SystemClock.elapsedRealtime() - mLastLocationTime > 3000){
                mLastLocationTime = SystemClock.elapsedRealtime();
                int cashSpeed = (int)mLastLocationCash.getSpeed();
                int curSpeed = (int)mLastLocation.getSpeed();
                String cashLocationStr = String.format("%.4f",mLastLocationCash.getLatitude()) + ", " + String.format("%.4f",mLastLocationCash.getLongitude());
                String curLocationStr = String.format("%.4f",mLastLocation.getLatitude()) + ", " + String.format("%.4f",mLastLocation.getLongitude());
//                Logg.d(TAG, "current: curSpeed=" + curSpeed + ", " + cashLocationStr + "::" + curLocationStr);
                //车速不为0且经纬度位置不变
                if(cashSpeed == curSpeed && curSpeed > 0 && cashLocationStr.equals(curLocationStr))
                    mValid = false;
                mLastLocationCash.set(mLastLocation);
            }

            return (mValid && haveSatelUsedInFix) ? mLastLocation : null;
        }

        public void setHaveSatelUsedInFix(boolean haveSatelUsedInFix) {
            this.haveSatelUsedInFix = haveSatelUsedInFix;
        }

    }


}
