package com.askey.dvr.cdr7010.dashcam.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import net.sf.marineapi.nmea.event.SentenceEvent;
import net.sf.marineapi.nmea.event.SentenceListener;
import net.sf.marineapi.nmea.io.SentenceReader;
import net.sf.marineapi.nmea.sentence.RMCSentence;
import net.sf.marineapi.nmea.sentence.SentenceId;
import net.sf.marineapi.nmea.util.Date;
import net.sf.marineapi.nmea.util.Position;
import net.sf.marineapi.nmea.util.Time;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * NMEA 工具类
 * <p>
 * Created by Navas.li on 2018/6/20
 */
public class NMEAUtils {
    private static final String TAG = "NMEAUtils";
    private List<RMCSentence> sentences = new ArrayList<>();
    private SentenceReader sentenceReader;
    private OnReadFinishListener listener;
    Context context;

    public NMEAUtils(Context context, String filePath) throws FileNotFoundException {
        this.context = context;
        sentences.clear();
        FileInputStream fileInputStream = new FileInputStream(new File(filePath));
        sentenceReader = new SentenceReader(fileInputStream);
        sentenceReader.addSentenceListener(new SentenceListener() {
            @Override
            public void readingPaused() {
                Logg.d(TAG,"readingPaused");
                sentenceReader.stop();
            }

            @Override
            public void readingStarted() {
                Logg.d(TAG,"readingStarted");
            }

            @Override
            public void readingStopped() {
                Logg.d(TAG,"readingStopped");
                try {
                    fileInputStream.close();
                    Logg.d(TAG,"fileInputStream.close");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                context.unregisterReceiver(mSdBadRemovalListener);
                if (listener != null) {
                    listener.onReadFinish(sentences);
                }
            }

            @Override
            public void sentenceRead(SentenceEvent sentenceEvent) {
                if (sentenceEvent.getSentence() instanceof RMCSentence) {
                    sentences.add((RMCSentence) sentenceEvent.getSentence());
                }
            }
        }, SentenceId.RMC);
        sentenceReader.setExceptionListener(e -> {
            //do nothing
        });
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addDataScheme("file");
        context.registerReceiver(mSdBadRemovalListener, filter);
    }

    private BroadcastReceiver mSdBadRemovalListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logg.d(TAG,"mSdBadRemovalListener...");
            if (Intent.ACTION_MEDIA_EJECT.equals(intent.getAction())) {
                Logg.d(TAG,"mSdBadRemovalListener...ACTION_MEDIA_EJECT");
                try {
                    sentenceReader.stop();
                    Logg.d(TAG,"sentenceReader.stop..");
                } catch (Exception e) {
                    //
                }
            }
        }
    };

    public void start() {
        sentenceReader.start();
    }

    public interface OnReadFinishListener {
        void onReadFinish(List<RMCSentence> list);
    }

    public void setOnFinishListener(OnReadFinishListener listener) {
        this.listener = listener;
    }

    public static Position getLocationFromSentences(List<RMCSentence> sentences, String timeStamp) {
        String dateUse = timeStamp.substring(0, 6);
        String timeUse = timeStamp.substring(6, timeStamp.length());
        for (RMCSentence rmcSentence : sentences) {
            Date date = rmcSentence.getDate();
            String dateFormatted = formatDay(date);
            Time time = rmcSentence.getTime();
            String timeFormatted = formatTime(time);
            if ((dateFormatted.equals(dateUse) || dateFormatted.substring(2, dateFormatted.length()).equals(dateUse)) && timeFormatted.equals(timeUse)) {
                return rmcSentence.getPosition();
            }
        }
        return null;
    }

    private static String formatTime(Time time) {
        int hour = time.getHour();
        int minutes = time.getMinutes();
        double seconds = time.getSeconds();
        return add0(hour) + add0(minutes) + add0((int) seconds);
    }

    private static String formatDay(Date date) {
        int day = date.getDay();
        int month = date.getMonth();
        int year = date.getYear();
        return year + add0(month) + add0(day);
    }

    private static String add0(int dayMonth) {
        if (dayMonth < 10) {
            return "0" + dayMonth;
        }
        return String.valueOf(dayMonth);
    }
}
