package com.askey.dvr.cdr7010.dashcam.core;

import android.os.Build;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class RecordConfig {

    public static Builder builder() {
        return new AutoValue_RecordConfig.Builder();
    }

    public abstract int cameraId();
    public abstract int videoWidth();
    public abstract int videoHeight();
    public abstract int videoFPS();
    public abstract int videoBitRate();
    public abstract boolean videoStampEnable();
    public abstract boolean audioRecordEnable();

    @AutoValue.Builder
    public static abstract class Builder {
        public abstract RecordConfig build();
        public abstract Builder cameraId(int cameraId);
        public abstract Builder videoWidth(int videoWidth);
        public abstract Builder videoHeight(int videoHeight);
        public abstract Builder videoFPS(int videoFPS);
        public abstract Builder videoBitRate(int videoBitRate);
        public abstract Builder videoStampEnable(boolean videoStampEnable);
        public abstract Builder audioRecordEnable(boolean audioRecordEnable);
    }

}
