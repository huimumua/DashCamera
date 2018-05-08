package com.askey.dvr.cdr7010.dashcam.ui.utils;
public class UIElementStatusEnum{
    public enum RecordingStatusType{
        RECORDING_UNKNOWN(0),
        RECORDING_CONTINUOUS(1),
        RECORDING_EVENT(2),
        RECORDING_STOP(3),
        RECORDING_ERROR(4);

        public final int value;
        RecordingStatusType(int value){
            this.value = value;
        }
        @Override
        public String toString(){
            return String.valueOf(value);
        }

    }
    public enum MICStatusType{
        MIC_OFF(0),
        MIC_ON(1);
        public final int value;
        MICStatusType(int value){
            this.value = value;
        }
        @Override
        public String toString(){
            return String.valueOf(value);
        }

    }
    public enum LTEStatusType{
        LTE_SIGNAL_STRENGTH_NONE_OR_UNKNOWN(0),
        LTE_SIGNAL_STRENGTH_POOR(1),
        LTE_SIGNAL_STRENGTH_MODERATE(2),
        LTE_SIGNAL_STRENGTH_GOOD(3),
        LTE_SIGNAL_STRENGTH_GREAT(4);

        public final int value;
        LTEStatusType(int value){
            this.value = value;
        }
        @Override
        public String toString(){
            return String.valueOf(value);
        }
    }
    public enum EventRecordingLimitStatusType{
        EVENT_RECORDING_REACH_LIMIT_CONDITION(0),
        EVENT_RECORDING_UNREACHABLE_LIMIT_CONDITION(1);
        public final int value;
        EventRecordingLimitStatusType(int value){
            this.value = value;
        }
        @Override
        public String toString(){
            return String.valueOf(value);
        }
    }
    public enum ParkingRecordingLimitStatusType{
        PARKING_RECORDING_REACH_LIMIT_CONDITION(0),
        PARKING_RECORDING_UNREACHABLE_LIMIT_CONDITION(1);
        public final int value;
        ParkingRecordingLimitStatusType(int value){
            this.value = value;
        }
        @Override
        public String toString(){
            return String.valueOf(value);
        }
    }
    public enum SDcardStatusType{
        MEDIA_MOUNTED(0),// 此时SD是可读写的
        MEDIA_MOUNTED_READ_ONLY(1),//SD卡存在且为只读状态
        MEDIA_REMOVED(2),//SD不存在
        MEDIA_SHARED(3),//SD卡存在，正与PC等相连接
        MEDIA_BAD_REMOVAL(4),//SD卡在挂载状态下被错误取出
        MEDIA_CHECKING(5),//正在检查SD卡
        MEDIA_NOFS(6),//SD卡存在，但其文件系统不被支持
        MEDIA_UNMOUNTABLE(7),//SD卡存在，无法被挂载
        MEDIA_UNMOUNTED(8),//SD卡存在，未被挂载
        MEDIA_UNKNOWN(9);//SD卡不能使用其他原因

        public final int value;
        SDcardStatusType(int value){
            this.value = value;
        }
        @Override
        public String toString(){
            return String.valueOf(value);
        }
    }
    public enum SecondCameraStatusType{
        CONNECTED(0),
        DISCONNECTED(1);

        public final int value;
        SecondCameraStatusType(int value){
            this.value = value;
        }
        @Override
        public String toString(){
            return String.valueOf(value);
        }
    }
    public enum GPSStatusType{
        GPS_STRENGTH_NONE(0),
        GPS_STRENGTH_NOT_FIXES(1),
        GPS_STRENGTH_FIXES(2);


        public final int value;
        GPSStatusType(int value){
            this.value = value;
        }
        @Override
        public String toString(){
            return String.valueOf(value);
        }
    }
    public enum SDCardInitStatus{
        INIT_SUCCESS(0),
        INIT_FAIL(1);
        public final int value;
        SDCardInitStatus(int value){
            this.value = value;
        }
        @Override
        public String toString(){
            return String.valueOf(value);
        }
    }
    public enum FOTAFileStatus{
        FOTA_FILE_EXIST(0),
        FOTA_FILE_NOT_EXIST(1);
        public final int value;
        FOTAFileStatus(int value){
            this.value = value;
        }
        @Override
        public String toString(){
            return String.valueOf(value);
        }
    }
    public enum SimCardStatus{
        SIM_STATE_ABSENT(0),
        SIM_STATE_NETWORK_LOCKED(1),
        SIM_STATE_PIN_REQUIRED(2),
        SIM_STATE_PUK_REQUIRED(3),
        SIM_STATE_UNKNOWN(4),
        SIM_STATE_READY(5);

        public final int value;
        SimCardStatus(int value){
            this.value = value;
        }
        @Override
        public String toString(){
            return String.valueOf(value);
        }

    }
}