package com.askey.dvr.cdr7010.dashcam.core.osd;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import com.askey.dvr.cdr7010.dashcam.util.LocationUtil;

import java.util.List;

public class GnssOSD extends BaseOSD {
    private static int WIDTH = 225;
    private static int HEIGHT = 32;
    private static int FONT_SIZE = 14;
    private static int TEXT_WIDTH = 225;
    private static int TEXT_HEIGHT = 32;

    private Context mContext;
    private Bitmap mBitmap;
    private final Canvas mCanvas;
    private final Paint mPaint;
    private LocationManager mLocationManager;
    private double mLatitude;
    private double mLongitude;
    private StringBuilder mStringBuilder = new StringBuilder(10);

    public GnssOSD(Context context) {
        mContext = context.getApplicationContext();
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
        setPosition(new float[]{-0.98f, -1.0f, -0.1f, -1.0f, -0.98f, -0.8f, -0.1f, -0.8f});
        initLocation();
    }

    private void initLocation() {
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        String locationProvider;
        if (providers.contains(LocationManager.GPS_PROVIDER)) {
            locationProvider = LocationManager.GPS_PROVIDER;
        } else if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            locationProvider = LocationManager.NETWORK_PROVIDER;
        } else {
            return;
        }

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //return;
        }

        Location location = mLocationManager.getLastKnownLocation(locationProvider);
        if (location != null) {
            mLatitude = location.getLatitude();
            mLongitude = location.getLongitude();
        }
        update();

        mLocationManager.requestLocationUpdates(locationProvider, 1000, 10, locationListener);
    }

    @Override
    protected Bitmap getBitmap() {
        return mBitmap;
    }

    private void update() {
        mStringBuilder.setLength(0);
        mStringBuilder.append(LocationUtil.latitudeToDMS(mLatitude));
        mStringBuilder.append("  ");
        mStringBuilder.append(LocationUtil.longitudeToDMS(mLongitude));
        mBitmap.eraseColor(Color.TRANSPARENT);
        mCanvas.drawText(mStringBuilder.toString(), 2, TEXT_HEIGHT - 3, mPaint);
    }

    private LocationListener locationListener =  new LocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle arg2) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onLocationChanged(Location location) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            double diffLati = latitude - mLatitude;
            double diffLongi = longitude - mLongitude;
            boolean refresh = false;
            if (diffLati < 0.001 || diffLati > -0.001) {
                mLatitude = latitude;
                refresh = true;
            }
            if (diffLongi < 0.001 || diffLongi > -0.001) {
                mLongitude = longitude;
                refresh = true;
            }

            if (refresh) {
                update();
            }
        }
    };
}
