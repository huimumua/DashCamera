package com.askey.dvr.cdr7010.dashcam.core.recorder;

import android.media.ExifInterface;
import android.support.annotation.NonNull;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


import java.io.IOException;

public class ExifHelper {
    // TODO: all of parameters need to be confirmed
    public static void build(@NonNull String file, long time) {
        try {
            ExifInterface exif = new ExifInterface(file);
            exif.setAttribute(ExifInterface.TAG_MAKE, "JVC KENWOOD");
            exif.setAttribute(ExifInterface.TAG_MODEL, "CDR7010"); // TODO: MODEL NAME?
            exif.setAttribute(ExifInterface.TAG_ORIENTATION, "1"); //upper left
            exif.setAttribute(ExifInterface.TAG_X_RESOLUTION, "72");
            exif.setAttribute(ExifInterface.TAG_Y_RESOLUTION, "72");
            exif.setAttribute(ExifInterface.TAG_RESOLUTION_UNIT, "2"); //inch
            exif.setAttribute(ExifInterface.TAG_Y_CB_CR_POSITIONING, "1"); //center
            exif.setAttribute(ExifInterface.TAG_Y_CB_CR_SUB_SAMPLING, "2 1");
            exif.setAttribute(ExifInterface.TAG_IMAGE_WIDTH, "1920");
            exif.setAttribute(ExifInterface.TAG_IMAGE_LENGTH, "1080");

            SimpleDateFormat fm = new SimpleDateFormat("YYYY:MM:DD HH:MM:SS", Locale.US);
            String datetime = fm.format(new Date(time));
            exif.setAttribute(ExifInterface.TAG_DATETIME, datetime);
            exif.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, datetime);
            exif.setAttribute(ExifInterface.TAG_DATETIME_DIGITIZED, datetime);
            exif.setAttribute(ExifInterface.TAG_F_NUMBER, "2");
            exif.setAttribute(ExifInterface.TAG_EXPOSURE_PROGRAM, "2");
            exif.setAttribute(ExifInterface.TAG_COMPONENTS_CONFIGURATION, "YCbCr");
            exif.setAttribute(ExifInterface.TAG_APERTURE_VALUE, "2");
            exif.setAttribute(ExifInterface.TAG_MAX_APERTURE_VALUE, "2");
            exif.setAttribute(ExifInterface.TAG_METERING_MODE, "2");
            exif.setAttribute(ExifInterface.TAG_LIGHT_SOURCE, "0");
            exif.setAttribute(ExifInterface.TAG_FLASH, "0"); //no flash
            exif.setAttribute(ExifInterface.TAG_FOCAL_LENGTH, "2.4");
            exif.setAttribute(ExifInterface.TAG_COLOR_SPACE, "1");
            exif.setAttribute(ExifInterface.TAG_PIXEL_X_DIMENSION, "1920");
            exif.setAttribute(ExifInterface.TAG_PIXEL_Y_DIMENSION, "1080");
            exif.setAttribute(ExifInterface.TAG_SCENE_TYPE, "0");
            exif.setAttribute(ExifInterface.TAG_SCENE_CAPTURE_TYPE, "0");
            exif.setAttribute(ExifInterface.TAG_WHITE_BALANCE, "0");
            exif.setAttribute(ExifInterface.TAG_GAIN_CONTROL, "0");
            exif.setAttribute(ExifInterface.TAG_CONTRAST, "0");
            exif.setAttribute(ExifInterface.TAG_SATURATION, "0");
            exif.setAttribute(ExifInterface.TAG_SHARPNESS, "0");

            exif.setAttribute(ExifInterface.TAG_GPS_VERSION_ID, "220");
            exif.saveAttributes();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
