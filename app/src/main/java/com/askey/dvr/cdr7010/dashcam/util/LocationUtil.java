package com.askey.dvr.cdr7010.dashcam.util;

import android.location.Location;
import android.support.annotation.NonNull;

public class LocationUtil {

    public static String latitudeToDMS(double latitude){
        String direction = (latitude > 0) ? "N " :"S ";
        String strLatitude = Location.convert(latitude, Location.FORMAT_SECONDS);
        strLatitude = replaceDelimiters(strLatitude, 3);
        strLatitude = direction + strLatitude;
        return strLatitude;
    }

    public static String longitudeToDMS(double longitude){
        String direction = (longitude > 0) ? "W " : "E ";
        String strLongitude = Location.convert(longitude, Location.FORMAT_SECONDS);
        strLongitude = replaceDelimiters(strLongitude, 3);
        strLongitude = direction + strLongitude;
        return strLongitude;
    }

    @NonNull
    private static String replaceDelimiters(String str, int decimalPlace) {
        str = str.replaceFirst(":", "°");
        str = str.replaceFirst(":", "'");
        int pointIndex = str.indexOf(".");
        if (pointIndex > 4) {
            int endIndex = pointIndex + 1 + decimalPlace;
            if (endIndex < str.length()) {
                str = str.substring(0, endIndex);
            }
        }
        str = str + "\"";
        return str;
    }
}
