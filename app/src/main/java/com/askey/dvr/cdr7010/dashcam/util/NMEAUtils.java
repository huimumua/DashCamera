package com.askey.dvr.cdr7010.dashcam.util;

import android.support.annotation.NonNull;

import net.sf.marineapi.nmea.event.SentenceEvent;
import net.sf.marineapi.nmea.event.SentenceListener;
import net.sf.marineapi.nmea.io.SentenceReader;
import net.sf.marineapi.nmea.sentence.RMCSentence;
import net.sf.marineapi.nmea.util.Date;
import net.sf.marineapi.nmea.util.Position;
import net.sf.marineapi.nmea.util.Time;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * NMEA 工具类
 * <p>
 * Created by Navas.li on 2018/6/20
 */
public class NMEAUtils {
    private List<RMCSentence> sentences = new ArrayList<>();
    private SentenceReader sentenceReader;
    private OnReadFinishListener listener;

    public NMEAUtils(String filePath) throws FileNotFoundException {
        sentences.clear();
        sentenceReader = new SentenceReader(new FileInputStream(new File(filePath)));
        sentenceReader.addSentenceListener(new SentenceListener() {
            @Override
            public void readingPaused() {
                if (listener != null) {
                    listener.onReadFinish(sentences);
                }
            }

            @Override
            public void readingStarted() {

            }

            @Override
            public void readingStopped() {
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
        });
    }

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
