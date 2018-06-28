package com.askey.dvr.cdr7010.dashcam.core.recorder;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.support.annotation.NonNull;

import com.askey.dvr.cdr7010.dashcam.service.FileManager;
import com.askey.dvr.cdr7010.dashcam.util.Logg;
import com.askey.dvr.cdr7010.dashcam.util.NMEAUtils;

import net.sf.marineapi.nmea.util.Position;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class Snapshot {

    private static final String TAG = "Snapshot";
    private static final long SEEK_STEP = 3 * 1000 * 1000L;

    public interface OnPictureTakeListener {
        void onPictureTake(List<String> path);
    }

    public static void take3Pictures(@NonNull String videoFilePath,
                                     long timeStamp,
                                     long firstTime,
                                     @NonNull FileManager fileManager,
                                     OnPictureTakeListener listener) {
        if (!new File(videoFilePath).exists()) {
            Logg.e(TAG, "video file not exist.");
            if (listener != null) {
                listener.onPictureTake(new ArrayList<String>(0));
            }
            return;
        }
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(videoFilePath);
        long pts = timeStamp - 3000;
        long pos = firstTime;
        final List<String> fileNames = new ArrayList<>(3);
        for (int i = 0; i < 3; i++) {
            try {
                String image = fileManager.getFilePathForPicture(pts);
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(image));
                Bitmap bmp = retriever.getFrameAtTime(pos);
                if (bmp != null) {
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                    bmp.recycle();
                    bos.flush();
                    bos.close();
                    ExifHelper.build(image, pts);
                    fileNames.add(image);
                }
            } catch (Exception e) {

            }
            pts += 3000;
            pos += SEEK_STEP;
        }

        String nmeaPath = videoFilePath.replace("EVENT", "SYSTEM/NMEA/EVENT").replace("mp4", "nmea");
        try {
            NMEAUtils nmeaUtils = new NMEAUtils(nmeaPath);
            nmeaUtils.setOnFinishListener(list -> {
                for (String file:fileNames) {
                    String timeNeed = file.substring(file.lastIndexOf("/") + 1, file.indexOf("."));
                    Position location = NMEAUtils.getLocationFromSentences(list, timeNeed);
                    ExifHelper.build(file, location);
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (listener != null) {
            listener.onPictureTake(fileNames);
        }
    }
}

