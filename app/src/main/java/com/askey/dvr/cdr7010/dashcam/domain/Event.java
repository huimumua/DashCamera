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
   public static final int PEDESTRIAN_COLLISION_WARNING =28;
   public static final int ABRUPT_HANDLE_LEFT = 29;
   public static final int ABRUPT_HANDLE_RIGHT = 30;

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
   public static final int SDCARD_UNSUPPORTED = 111;
   public static final int SDCARD_UNFORMATTED = 112;
   public static final int SDCARD_ERROR = 113;
   public static final int RECORDING_STOP = 114;
   public static final int RECORDING_EVENT_FAILED =115;
   public static final int SDCARD_SPACE_INSUFFICIENT = 116;
   public static final int HIGH_TEMPERATURE_THRESHOLD_LV1 = 117;
   public static final int HIGH_TEMPERATURE_THRESHOLD_LV2 = 118;
   public static final int HIGH_TEMPERATURE_THRESHOLD_LV3 = 119;
   public static final int EMERGENCY_CALL_USER = 120;
   public static final int USER_SWITCH_BY_USER = 121;
   public static final int CHANGE_SETTINGS_BY_USER = 122;
   public static final int EVENT_RECORDING_START = 123;
   public static final int EVENT_RECORDING_END =124;
   public static final int EVENT_DOWNLOAD_RESULT = 125;
   public static final int EVENT_SIMCARD_ERROR =126;
   public static final int USER_SWITCH_BY_SERVER =127;
   public static final int CHANGE_SETTINGS_BY_SERVER =128;
   public static final int RECORDING_PIC_FAILED =129;

   public static final class EventCode {
       public static final int EVENT_RECORDING = 0x1;
       public static final int EVENT_GPS = 0x2;
       public static final int EVENT_RECORDING_FILE_LIMIT = 0x4;
       public static final int EVENT_PARKING_RECODING_FILE_LIMIT = 0x8;
       public static final int EVENT_SDCARD = 0x16;
       public static final int EVENT_MIC = 0x32;
       public static final int EVENT_FOTA_UPDATE = 0x64;
       public static final int EVENT_SIMCARD = 0x128;
       public static final int EVENT_SWITCH_USER = 0x256;
       public static final int EVENT_CHECK_SDCARD_AND_SIMCARD = 0x512;
    // other more
   }
   public static final int[] detectEvent ={ACCIDENT_DETECTION,LONG_RUNNING,ABRUPT_HANDLE,
           WOBBLE, FRONT_COLLISION_WARNING,RIGHT_LANE_DEPARTURE_WARNING,LEFT_LANE_DEPARTURE_WARNING,
           START_DELAY,EQUIPMENT_FAILURE,RAPID_ACCELERATION,RAPID_DECELERATION,REVERSE_RUN,
           ZONE_30,DRIVING_OUTSIDE_THE_DESIGNATED_AREA,ACCIDENT_FREQUENT_PLACE,VIOLATION_OF_SIGNS,
           BEWARE_OF_ANIMALS_DEER,BEWARE_OF_ANIMALS_RACOON,BEWARE_OF_ANIMALS_HARE,
           BEWARE_OF_ANIMALS_YAMBARU_QUEENA,BEWARE_OF_ANIMALS_YAMANEKO,BEWARE_OF_ANIMALS,
           BEWARE_OF_ANIMALS_RARE,TYPHOON_WARNING,TYPHOON_ALERT,WEATHER_ALERT_SPECIAL,WEATHER_ALERT,
           EMERGENCY_CALL_BUTTON,PEDESTRIAN_COLLISION_WARNING,ABRUPT_HANDLE_LEFT,ABRUPT_HANDLE_RIGHT,
           EQUIPMENT_FAILURE
   };
   public static final int[] nomalEvent ={ACCIDENT_DETECTION,LONG_RUNNING,ABRUPT_HANDLE,
           WOBBLE,FRONT_COLLISION_WARNING,RIGHT_LANE_DEPARTURE_WARNING,LEFT_LANE_DEPARTURE_WARNING,
           START_DELAY,EQUIPMENT_FAILURE,RAPID_ACCELERATION,RAPID_DECELERATION,REVERSE_RUN, ZONE_30,
           DRIVING_OUTSIDE_THE_DESIGNATED_AREA,ACCIDENT_FREQUENT_PLACE,VIOLATION_OF_SIGNS, BEWARE_OF_ANIMALS_DEER,
           BEWARE_OF_ANIMALS_RACOON,BEWARE_OF_ANIMALS_HARE, BEWARE_OF_ANIMALS_YAMBARU_QUEENA,
           BEWARE_OF_ANIMALS_YAMANEKO,BEWARE_OF_ANIMALS, BEWARE_OF_ANIMALS_RARE,TYPHOON_WARNING,
           TYPHOON_ALERT,WEATHER_ALERT_SPECIAL,WEATHER_ALERT,PEDESTRIAN_COLLISION_WARNING,ABRUPT_HANDLE_LEFT,
           ABRUPT_HANDLE_RIGHT, GPS_LOCATION_INFORMATION,GPS_LOCATION_INFORMATION_ERROR,EMERGENCY_CALL_BUTTON,
           EMERGENCY_CALL_USER, USER_SWITCH_BY_SERVER,EQUIPMENT_FAILURE};
   public static final int[] sdCardUnMountedEvent ={SDCARD_UNMOUNTED};
   public static final int[] sdCardAbnormalEvent ={SDCARD_UNFORMATTED,SDCARD_UNSUPPORTED,SDCARD_ERROR,
           SDCARD_SPACE_INSUFFICIENT};
   public static final int[] highTemperatureLv3Event ={HIGH_TEMPERATURE_THRESHOLD_LV3};
   public static final int[] highTemperatureLv2Event ={HIGH_TEMPERATURE_THRESHOLD_LV2};
   public static final int[] highTemperatureLv1Event ={HIGH_TEMPERATURE_THRESHOLD_LV1};
   public static final int[] abnormalStopRecordingEvent ={RECORDING_STOP};
   public static final int[] noticeEvent ={NOTICE_START,DRIVING_REPORT,MONTHLY_DRIVING_REPORT,
           AdDVICE_BEFORE_DRIVING};
   public static final int[] simCardErroeEvent ={EVENT_SIMCARD_ERROR};
   public static final int[] limitRecordingFileEvent ={RECORDING_EVENT_FAILED,RECORDING_PIC_FAILED};
   public static final int[] weatherWarning ={0x0313,0x0314,0x0315,0x0316,0x0317,0x0318,0x0319,0x031A,0x031B,0x031C,
   0x031D,0x031E,0x031F,0x0320,0x0321,0x0322,0x0323,0x0324,0x0325,0x0326,0x0327,0x0328,0x0329,0x032A,0x032B,0x032C,
   0x032D,0x030E,0x032F,0x0330,0x0331,0x0332,0x0333,0x0334,0x0336,0x0337,0x0338,0x0339,0x033A,0x033B,0x033D};
   public static final int[] specialWeatherWarning={0x0301,0x0302,0x0303,0x0304,0x0305,0x0306,0x0307,0x0308,0x0309,
   0x030A,0x030B,0x030C,0x030D,0x030F,0x0310,0x0311,0x0312};
   public static boolean contains(int[] eventArr,int eventType){
      for(int element:eventArr){
         if(eventType == element)
            return true;
      }
      return false;
   }
}