package com.askey.dvr.cdr7010.dashcam.core.recorder;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.media.ExifInterface;

import net.sf.marineapi.nmea.util.Position;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ExifHelper {

    public static void build(@NonNull String file, long time) {
        build(file, time, null);
    }

    public static void build(@NonNull String file, long time, Position location) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault());
        String timeStamp = format.format(new Date(time));
        try {
            ExifInterface exif = new ExifInterface(file);
            //主图像信息
            exif.setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION, "");
            exif.setAttribute(ExifInterface.TAG_MAKE, Build.BRAND);
            exif.setAttribute(ExifInterface.TAG_MODEL, Build.MODEL);
            exif.setAttribute(ExifInterface.TAG_ORIENTATION, "1");//正常左上
            exif.setAttribute(ExifInterface.TAG_X_RESOLUTION, "72/1");//水平分辨率
            exif.setAttribute(ExifInterface.TAG_Y_RESOLUTION, "72/1");//竖直分辨率
            exif.setAttribute(ExifInterface.TAG_RESOLUTION_UNIT, "2");
            exif.setAttribute(ExifInterface.TAG_DATETIME, timeStamp);
            exif.setAttribute(ExifInterface.TAG_Y_CB_CR_POSITIONING, "1");
            //拍摄参数
            exif.setAttribute(ExifInterface.TAG_F_NUMBER, "2");
            exif.setAttribute(ExifInterface.TAG_EXPOSURE_PROGRAM, "2");
            exif.setAttribute(ExifInterface.TAG_EXIF_VERSION, "0230");
            exif.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, timeStamp);
            exif.setAttribute(ExifInterface.TAG_DATETIME_DIGITIZED, timeStamp);
            exif.setAttribute(ExifInterface.TAG_COMPONENTS_CONFIGURATION, "YCbCr");
            exif.setAttribute(ExifInterface.TAG_APERTURE_VALUE, "2");
            exif.setAttribute(ExifInterface.TAG_MAX_APERTURE_VALUE, "2");
            exif.setAttribute(ExifInterface.TAG_METERING_MODE, "2");
            exif.setAttribute(ExifInterface.TAG_LIGHT_SOURCE, "0");
            exif.setAttribute(ExifInterface.TAG_FLASH, "32");
            exif.setAttribute(ExifInterface.TAG_FOCAL_LENGTH, "2.36");
            exif.setAttribute(ExifInterface.TAG_FLASHPIX_VERSION, "Ver.1.0");
            exif.setAttribute(ExifInterface.TAG_COLOR_SPACE, "1");
            exif.setAttribute(ExifInterface.TAG_PIXEL_X_DIMENSION, "1920");
            exif.setAttribute(ExifInterface.TAG_PIXEL_Y_DIMENSION, "1080");
            exif.setAttribute(ExifInterface.TAG_SCENE_TYPE, "00");
            exif.setAttribute(ExifInterface.TAG_WHITE_BALANCE, "0");
            exif.setAttribute(ExifInterface.TAG_SCENE_CAPTURE_TYPE, "0");
            exif.setAttribute(ExifInterface.TAG_GAIN_CONTROL, "0");
            exif.setAttribute(ExifInterface.TAG_CONTRAST, "0");
            exif.setAttribute(ExifInterface.TAG_SATURATION, "0");
            exif.setAttribute(ExifInterface.TAG_SHARPNESS, "0");
            //GPS信息
            exif.setAttribute(ExifInterface.TAG_GPS_VERSION_ID, "2200");
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                // 写入纬度信息
                // 写入经度信息
                exif.setLatLong(latitude, longitude);
            }
            //other
            exif.saveAttributes();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void build(@NonNull String file, Position location) {
        try {
            ExifInterface exif = new ExifInterface(file);

            //GPS信息
            exif.setAttribute(ExifInterface.TAG_GPS_VERSION_ID, "2200");
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                // 写入纬度信息
                // 写入经度信息
                exif.setLatLong(latitude, longitude);
            }
            //other
            exif.saveAttributes();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
