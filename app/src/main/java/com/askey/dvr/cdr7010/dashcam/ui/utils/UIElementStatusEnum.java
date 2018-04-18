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
}