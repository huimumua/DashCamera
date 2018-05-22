package com.askey.dvr.cdr7010.dashcam.domain;
public final class Event{
   public static final int CONTINUOUS_RECORDING_START = 104;
   public static final int CONTINUOUS_RECORDING_END =105;
   public static final int AUDIO_RECORDING_ON = 106;
   public static final int AUDIO_RECORDING_OFF = 107;
   public static final int SDCARD_UNMOUNTED = 110;
   public static final int SDCARD_UNFORMATTED = 111;
   public static final int SDCARD_UNSUPPORTED = 112;
   public static final int SDCARD_ERROR = 113;
   public static final int RECORDING_STOP = 114;
    public static final int HIGH_TEMPERATURE_THRESHOLD = 118;
   public static final int EVENT_RECORDING_START = 123;
   public static final int EVENT_RECORDING_END =124;


   public static final class EventCode {
       public static final int EVENT_RECORDING = 0x1;
       public static final int EVENT_GPS = 0x2;
       public static final int EVENT_RECORDING_FILE_LIMIT = 0x4;
       public static final int EVENT_PARKING_RECODING_FILE_LIMIT = 0x8;
       public static final int EVENT_SDCARD = 0x16;
       public static final int EVENT_MIC = 0x32;
       public static final int EVENT_FOTA_UPDATE = 0x64;
       public static final int EVENT_SIMCARD = 0x128;
    // other more
   }
}