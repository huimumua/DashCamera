package com.askey.dvr.cdr7010.dashcam.domain;
public final class Event{
   public static final class EventCode {
       public static final int EVENT_RECORDING = 0x1;
       public static final int EVENT_GPS = 0x2;
       public static final int EVENT_RECORDING_FILE_LIMIT = 0x4;
       public static final int EVENT_PARKING_RECODING_FILE_LIMIT = 0x8;
    // other more
   }
}