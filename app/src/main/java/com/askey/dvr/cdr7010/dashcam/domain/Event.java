package com.askey.dvr.cdr7010.dashcam.domain;
public final class Event{
   public static final int ACCIDENT_DETECTION = 0;
   public static final int LONG_RUNNING = 1;
   public static final int ABRUPT_HANDLE = 2;
   public static final int WOBBLE = 3;
   public static final int FRONT_COLLISION_WARNING = 4;
   public static final int RIGHT_LANE_DEPARTURE_WARNING = 5;
   public static final int LEFT_LANE_DEPARTURE_WARNING = 6;
   public static final int START_DELAY = 7;
   public static final int EQUIPMENT_FAILURE = 8;
   public static final int RAPID_ACCELERATION = 9;
   public static final int RAPID_DECELERATION = 10;
   public static final int REVERSE_RUN = 11;
   public static final int ZONE_30 = 12;
   public static final int DRIVING_OUTSIDE_THE_DESIGNATED_AREA = 13;
   public static final int ACCIDENT_FREQUENT_PLACE = 14;
   public static final int VIOLATION_OF_SIGNS = 15;
   public static final int BEWARE_OF_ANIMALS_DEER = 16;
   public static final int BEWARE_OF_ANIMALS_RACOON = 17;
   public static final int BEWARE_OF_ANIMALS_HARE = 18;
   public static final int BEWARE_OF_ANIMALS_YAMBARU_QUEENA = 19;
   public static final int BEWARE_OF_ANIMALS_YAMANEKO = 20;
   public static final int BEWARE_OF_ANIMALS = 21;
   public static final int BEWARE_OF_ANIMALS_RARE = 22;
   public static final int TYPHOON_WARNING = 23;
   public static final int TYPHOON_ALERT = 24;
   public static final int WEATHER_ALERT_SPECIAL = 25;
   public static final int WEATHER_ALERT = 26;
   public static final int EMERGENCY_CALL_BUTTON = 27;
   public static final int NOTICE_START = 100;
   public static final int DRIVING_REPORT =101;
   public static final int MONTHLY_DRIVING_REPORT =102;
   public static final int AdDVICE_BEFORE_DRIVING =103;
   public static final int CONTINUOUS_RECORDING_START = 104;
   public static final int CONTINUOUS_RECORDING_END =105;
   public static final int AUDIO_RECORDING_ON = 106;
   public static final int AUDIO_RECORDING_OFF = 107;
   public static final int GPS_LOCATION_INFORMATION = 108;
   public static final int GPS_LOCATION_INFORMATION_ERROR = 109;
   public static final int SDCARD_UNMOUNTED = 110;
   public static final int SDCARD_UNFORMATTED = 111;
   public static final int SDCARD_UNSUPPORTED = 112;
   public static final int SDCARD_ERROR = 113;
   public static final int RECORDING_STOP = 114;
   public static final int RECORDING_FAILED =115;
   public static final int SDCARD_SPACE_INSUFFICIENT = 116;
   public static final int HIGH_TEMPERATURE_THRESHOLD_LV1 = 117;
   public static final int HIGH_TEMPERATURE_THRESHOLD_LV2 = 118;
   public static final int HIGH_TEMPERATURE_THRESHOLD_LV3 = 119;
   public static final int EMERGENCY_CALL_USER = 120;
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
   public static final int[] nomalEvent ={ACCIDENT_DETECTION,LONG_RUNNING,ABRUPT_HANDLE,
           WOBBLE,FRONT_COLLISION_WARNING,RIGHT_LANE_DEPARTURE_WARNING,LEFT_LANE_DEPARTURE_WARNING,
           START_DELAY,EQUIPMENT_FAILURE,RAPID_ACCELERATION,RAPID_DECELERATION,REVERSE_RUN,
           ZONE_30,DRIVING_OUTSIDE_THE_DESIGNATED_AREA,ACCIDENT_FREQUENT_PLACE,VIOLATION_OF_SIGNS,
           BEWARE_OF_ANIMALS_DEER,BEWARE_OF_ANIMALS_RACOON,BEWARE_OF_ANIMALS_HARE,
           BEWARE_OF_ANIMALS_YAMBARU_QUEENA,BEWARE_OF_ANIMALS_YAMANEKO,BEWARE_OF_ANIMALS,
           BEWARE_OF_ANIMALS_RARE,TYPHOON_WARNING,TYPHOON_ALERT,WEATHER_ALERT_SPECIAL,WEATHER_ALERT,
           GPS_LOCATION_INFORMATION,GPS_LOCATION_INFORMATION_ERROR,EMERGENCY_CALL_BUTTON,EMERGENCY_CALL_USER};
   public static final int[] sdCardUnMountedEvent ={SDCARD_UNMOUNTED};
   public static final int[] sdCardAbnormalEvent ={SDCARD_UNFORMATTED,SDCARD_UNSUPPORTED,SDCARD_ERROR,
           SDCARD_SPACE_INSUFFICIENT};
   public static final int[] highTemperatureLv3Event ={HIGH_TEMPERATURE_THRESHOLD_LV3};
   public static final int[] highTemperatureLv2Event ={HIGH_TEMPERATURE_THRESHOLD_LV2};
   public static final int[] highTemperatureLv1Event ={HIGH_TEMPERATURE_THRESHOLD_LV1};
   public static final int[] abnormalStopRecordingEvent ={RECORDING_STOP};
   public static final int[] noticeEvent ={NOTICE_START,MONTHLY_DRIVING_REPORT,MONTHLY_DRIVING_REPORT,
           AdDVICE_BEFORE_DRIVING};
   public static final int[] limitRecordingEvent ={RECORDING_FAILED};
   public static boolean contains(int[] eventArr,int eventType){
      for(int element:eventArr){
         if(eventType == element)
            return true;
      }
      return false;
   }
}