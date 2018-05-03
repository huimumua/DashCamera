package com.askey.dvr.cdr7010.dashcam.core.osd;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class GroupOSD extends BaseOSD {

    private List<BaseOSD> mElements = new ArrayList<>();
    public void addOSD(@NonNull BaseOSD osd) {
        mElements.add(osd);
    }

    @Override
    protected Bitmap getBitmap() {
        return null;
    }

    @Override
    protected void onDraw() {
        for (BaseOSD e : mElements) {
            e.draw();
        }
    }

    @Override
    public void release() {
        for (BaseOSD e : mElements) {
            e.release();
        }
        super.release();
    }
}
