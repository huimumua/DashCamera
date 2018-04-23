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
}