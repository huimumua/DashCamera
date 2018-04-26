package com.askey.dvr.cdr7010.dashcam.core.event;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Event {

    private @EventID int id;
    private long time;

    public static final int ID_NONE = 0;
    public static final int ID_ADAS = 1;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef ({ID_NONE, ID_ADAS})
    public @interface EventID {}

    public Event(@EventID int id, long time) {
        this.id = id;
        this.time = time;
    }

    @EventID
    public int getId() {
        return id;
    }

    public long getTime() {
        return time;
    }
}
