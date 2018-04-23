package com.askey.dvr.cdr7010.dashcam.ui.utils;
public class UIElementStatusEnum{
    public enum RecordingStatusType{
        RECORDING_UNKNOWN(0),
        RECORDING_CONTINUOUS(1),
        RECORDING_EVENT(2),
        RECORDING_PARKING(3);

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
        MIC_ON(0),
        MIC_OFF(1);
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
}