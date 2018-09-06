package com.askey.dvr.cdr7010.dashcam.core.recorder;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.support.annotation.NonNull;

import com.askey.dvr.cdr7010.dashcam.service.FileManager;
import com.askey.dvr.cdr7010.dashcam.util.Logg;
import com.askey.dvr.cdr7010.dashcam.util.NMEAUtils;

import net.sf.marineapi.nmea.util.Position;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Snapshot {

    private static final String TAG = "Snapshot";
    private static final long SEEK_STEP = 3 * 1000 * 1000L;

    public interface OnPictureTakeListener {
        void onPictureTake(List<String> path);
    }

    public static void take3Pictures(@NonNull String videoFilePath,
                                     int cameraId,
                                     long timeStamp,
                                     long firstTime,
                                     @NonNull FileManager fileManager,
                                     OnPictureTakeListener listener) {
        if (!new File(videoFilePath).exists()) {
            Logg.e(TAG, "video file not exist.");
            if (listener != null) {
                listener.onPictureTake(new ArrayList<>(0));
            }
            return;
        }
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(videoFilePath);
        } catch (Exception e) {
            Logg.e(TAG, "setDataSource exception: " + e.getMessage());
            if (listener != null) {
                listener.onPictureTake(new ArrayList<>(0));
            }
            return;
        }
        long pts = timeStamp - 3000;
        long pos = firstTime;
        final List<String> fileNames = new ArrayList<>(3);
        for (int i = 0; i < 3; i++) {
            String image;
            try {
                image = fileManager.getFilePathForPicture(cameraId, pts);
            } catch (Exception e) {
                pts += 3000;
                pos += SEEK_STEP;
                continue;
            }
            Bitmap bmp = retriever.getFrameAtTime(pos);
            saveBitmap(pts, fileNames, image, bmp);
            pts += 3000;
            pos += SEEK_STEP;
        }
        retriever.release();
        String nmeaPath = videoFilePath.replace("EVENT", "SYSTEM/NMEA/EVENT").replace("mp4", "nmea");
        saveNmea(fileNames, nmeaPath);
        if (listener != null) {
            listener.onPictureTake(fileNames);
        }
    }

    private static void saveBitmap(long pts, List<String> fileNames, String image, Bitmap bmp) {
        if (bmp != null) {
            FileOutputStream bos = null;
            try {
                bos = new FileOutputStream(image);
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                bos.flush();
                bmp.recycle();
                ExifHelper.build(image, pts);
                fileNames.add(image);
            } catch (IOException e) {
                //ignore
            } finally {
                if (bos != null) {
                    try {
                        bos.close();
                    } catch (IOException e) {
                        //ignore
                    }
                }
            }
        }
    }

    private static void saveNmea(List<String> fileNames, String nmeaPath) {
        try {
            NMEAUtils nmeaUtils = new NMEAUtils(nmeaPath);
            nmeaUtils.setOnFinishListener(list -> {
                for (String file : fileNames) {
                    String timeNeed = file.substring(file.lastIndexOf("/") + 1, file.indexOf("."));
                    Position location = NMEAUtils.getLocationFromSentences(list, timeNeed);
                    ExifHelper.build(file, location);
                }
            });
            nmeaUtils.start();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}

