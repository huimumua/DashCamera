package com.askey.dvr.cdr7010.dashcam.core;

public interface DashCamControl {
    void onStartVideoRecord() throws Exception ;
    void onStopVideoRecord();
    void onMuteAudio();
    void onDemuteAudio();
}
