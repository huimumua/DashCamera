package com.askey.dvr.cdr7010.dashcam.core.recorder;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.RemoteException;
import android.support.annotation.NonNull;

import com.askey.dvr.cdr7010.dashcam.service.FileManager;
import com.askey.dvr.cdr7010.dashcam.util.Logg;
import com.askey.dvr.cdr7010.dashcam.util.NMEAUtils;

import net.sf.marineapi.nmea.util.Position;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Snapshot {

    private static final String TAG = "Snapshot";
    private static final boolean VERBOSE = false;
    private static final int COLOR_FormatI420 = 1;
    private static final int COLOR_FormatNV21 = 2;
    private static final int DEFAULT_TIMEOUT_US = 10 * 1000;
    private static final int DECODE_COLOR_FORMAT = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible;
    private static final long SEEK_STEP = 3 * 1000 * 1000L;

    public interface OnPictureTakeListener {
        void onPictureTake(List<String> path);
    }

    public static void take3Pictures(@NonNull String videoFilePath,
                                     long timeStamp,
                                     long firstTime,
                                     @NonNull FileManager filemanager,
                                     OnPictureTakeListener listener) {
        try {
            File videoFile = new File(videoFilePath);
            MediaExtractor extractor = new MediaExtractor();
            extractor.setDataSource(videoFile.toString());
            int trackIndex = selectVideoTrack(extractor);
            if (trackIndex < 0) {
                Logg.e(TAG, "No video track found in " + videoFilePath);
                return;
            }
            extractor.selectTrack(trackIndex);
            MediaFormat mediaFormat = extractor.getTrackFormat(trackIndex);
            String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
            MediaCodec decoder = MediaCodec.createDecoderByType(mime);
            if (isColorFormatSupported(DECODE_COLOR_FORMAT, decoder.getCodecInfo().getCapabilitiesForType(mime))) {
                mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, DECODE_COLOR_FORMAT);
            }
            decodeFramesToImage(videoFilePath, decoder, extractor, mediaFormat, timeStamp, firstTime, filemanager, listener);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void decodeFramesToImage(String videoPath,
                                            MediaCodec decoder,
                                            MediaExtractor extractor,
                                            MediaFormat mediaFormat,
                                            long timeStamp,
                                            long time,
                                            FileManager fileManager,
                                            OnPictureTakeListener listener) {
        Logg.d(TAG, "videoPath==" + videoPath);
        String nmeaPath = videoPath.replace("EVENT", "SYSTEM/NMEA/EVENT").replace("mp4", "nmea");
        Logg.d(TAG, "nmeaPath==" + nmeaPath);
        List<String> fileNames = new ArrayList<>(3);
        try {
            NMEAUtils nmeaUtils = new NMEAUtils(nmeaPath);
            nmeaUtils.setOnFinishListener(list -> {
                Logg.d(TAG, "list..." + list.size());
                MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
                boolean sawInputEOS = false;
                boolean sawOutputEOS = false;
                decoder.configure(mediaFormat, null, null, 0);
                decoder.start();
                int sampleCount = 0;
                long seekPos = time;
                while (!sawOutputEOS) {
                    if (!sawInputEOS) {
                        int inputBufferId = decoder.dequeueInputBuffer(DEFAULT_TIMEOUT_US);
                        if (inputBufferId >= 0) {
                            ByteBuffer inputBuffer = decoder.getInputBuffer(inputBufferId);
                            if (inputBuffer == null) {
                                continue;
                            }
                            extractor.seekTo(seekPos, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
                            inputBuffer.clear();
                            int sampleSize = extractor.readSampleData(inputBuffer, 0);
                            if (sampleSize < 0 || sampleCount >= 3) {
                                decoder.queueInputBuffer(inputBufferId, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                                sawInputEOS = true;
                            } else {
                                //long presentationTimeUs = extractor.getSampleTime();
                                int flags = extractor.getSampleFlags();
                                long pts = 0;
                                if (sampleCount == 0) {
                                    pts = timeStamp - 3000;
                                } else if (sampleCount == 1) {
                                    pts = timeStamp;
                                } else if (sampleCount == 2) {
                                    pts = timeStamp + 3000;
                                }
                                decoder.queueInputBuffer(inputBufferId, 0, sampleSize, pts, flags);
                                sampleCount++;
                                seekPos += SEEK_STEP;
                            }
                        }
                    }
                    int outputBufferId = decoder.dequeueOutputBuffer(info, DEFAULT_TIMEOUT_US);
                    if (outputBufferId >= 0) {
                        if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            sawOutputEOS = true;
                        }
                        if (info.size > 0) {
                            Image image = decoder.getOutputImage(outputBufferId);
                            if (image == null) {
                                continue;
                            }
                            try {
                                String filePath = fileManager.getFilePathForPicture(info.presentationTimeUs);
                                Logg.d(TAG, "info.presentationTimeUs..." + info.presentationTimeUs);
                                compressToJpeg(filePath, image);
                                image.close();
                                String timeNeed = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.indexOf("."));
                                Logg.d(TAG, "timeNeed =" + timeNeed);
                                Position location = null;
                                try {
                                    location = NMEAUtils.getLocationFromSentences(list, timeNeed);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                ExifHelper.build(filePath, info.presentationTimeUs, location);
                                fileNames.add(filePath);
                                Logg.d(TAG, "save jpeg: " + filePath);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                                Logg.e(TAG, "fail to get file path from FileManager with error: "
                                        + e.getMessage());
                            }
                            decoder.releaseOutputBuffer(outputBufferId, true);
                        }
                    }
                }
                listener.onPictureTake(fileNames);
                decoder.stop();
                decoder.release();
                if (extractor != null) {
                    extractor.release();
                }
            });
            nmeaUtils.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int selectVideoTrack(MediaExtractor extractor) {
        int numTracks = extractor.getTrackCount();
        for (int i = 0; i < numTracks; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/")) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isColorFormatSupported(int colorFormat, MediaCodecInfo.CodecCapabilities caps) {
        for (int c : caps.colorFormats) {
            if (c == colorFormat) {
                return true;
            }
        }
        return false;
    }

    private static boolean isImageFormatSupported(Image image) {
        int format = image.getFormat();
        switch (format) {
            case ImageFormat.YUV_420_888:
            case ImageFormat.NV21:
            case ImageFormat.YV12:
                return true;
        }
        return false;
    }

    private static byte[] getDataFromImage(Image image, int colorFormat) {
        if (colorFormat != COLOR_FormatI420 && colorFormat != COLOR_FormatNV21) {
            throw new IllegalArgumentException("only support COLOR_FormatI420 " + "and COLOR_FormatNV21");
        }
        if (!isImageFormatSupported(image)) {
            throw new RuntimeException("can't convert Image to byte array, format " + image.getFormat());
        }
        Rect crop = image.getCropRect();
        int format = image.getFormat();
        int width = crop.width();
        int height = crop.height();
        Image.Plane[] planes = image.getPlanes();
        byte[] data = new byte[width * height * ImageFormat.getBitsPerPixel(format) / 8];
        byte[] rowData = new byte[planes[0].getRowStride()];
        if (VERBOSE) Logg.v(TAG, "get data from " + planes.length + " planes");
        int channelOffset = 0;
        int outputStride = 1;
        for (int i = 0; i < planes.length; i++) {
            switch (i) {
                case 0:
                    channelOffset = 0;
                    outputStride = 1;
                    break;
                case 1:
                    if (colorFormat == COLOR_FormatI420) {
                        channelOffset = width * height;
                        outputStride = 1;
                    } else if (colorFormat == COLOR_FormatNV21) {
                        channelOffset = width * height + 1;
                        outputStride = 2;
                    }
                    break;
                case 2:
                    if (colorFormat == COLOR_FormatI420) {
                        channelOffset = (int) (width * height * 1.25);
                        outputStride = 1;
                    } else if (colorFormat == COLOR_FormatNV21) {
                        channelOffset = width * height;
                        outputStride = 2;
                    }
                    break;
            }
            ByteBuffer buffer = planes[i].getBuffer();
            int rowStride = planes[i].getRowStride();
            int pixelStride = planes[i].getPixelStride();
            if (VERBOSE) {
                Logg.v(TAG, "pixelStride " + pixelStride);
                Logg.v(TAG, "rowStride " + rowStride);
                Logg.v(TAG, "width " + width);
                Logg.v(TAG, "height " + height);
                Logg.v(TAG, "buffer size " + buffer.remaining());
            }
            int shift = (i == 0) ? 0 : 1;
            int w = width >> shift;
            int h = height >> shift;
            buffer.position(rowStride * (crop.top >> shift) + pixelStride * (crop.left >> shift));
            for (int row = 0; row < h; row++) {
                int length;
                if (pixelStride == 1 && outputStride == 1) {
                    length = w;
                    buffer.get(data, channelOffset, length);
                    channelOffset += length;
                } else {
                    length = (w - 1) * pixelStride + 1;
                    buffer.get(rowData, 0, length);
                    for (int col = 0; col < w; col++) {
                        data[channelOffset] = rowData[col * pixelStride];
                        channelOffset += outputStride;
                    }
                }
                if (row < h - 1) {
                    buffer.position(buffer.position() + rowStride - length);
                }
            }
            if (VERBOSE) Logg.v(TAG, "Finished reading data from plane " + i);
        }
        return data;
    }

    private static void dumpFile(String fileName, byte[] data) {
        FileOutputStream outStream;
        try {
            outStream = new FileOutputStream(fileName);
        } catch (IOException ioe) {
            throw new RuntimeException("Unable to create output file " + fileName, ioe);
        }
        try {
            outStream.write(data);
            outStream.close();
        } catch (IOException ioe) {
            throw new RuntimeException("failed writing data to file " + fileName, ioe);
        }
    }

    private static void compressToJpeg(String fileName, Image image) {
        FileOutputStream outStream;
        try {
            outStream = new FileOutputStream(fileName);
        } catch (IOException ioe) {
            throw new RuntimeException("Unable to create output file " + fileName, ioe);
        }
        Rect rect = image.getCropRect();
        YuvImage yuvImage = new YuvImage(getDataFromImage(image, COLOR_FormatNV21), ImageFormat.NV21, rect.width(), rect.height(), null);
        yuvImage.compressToJpeg(rect, 100, outStream);
    }
}
