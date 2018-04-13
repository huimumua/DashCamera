package com.askey.dvr.cdr7010.dashcam.core.renderer;

import android.support.annotation.NonNull;

import com.askey.dvr.cdr7010.dashcam.BuildConfig;
import com.askey.dvr.cdr7010.dashcam.core.gles.GLDrawer2D;

public class VideoTextureController {
    private int mTexture = -1;
    private GLDrawer2D mDrawer;
    private float[] mMatrix;

    public VideoTextureController() {

    }

    public void prepare() {
        mTexture = GLDrawer2D.initTex();
        mDrawer = new GLDrawer2D();
    }

    public void release() {
        mDrawer.release();
        mDrawer = null;
        GLDrawer2D.deleteTex(mTexture);
        mTexture = -1;
    }

    public int getTexture() {
        return mTexture;
    }

    public void setMatrix(@NonNull float[] matrix) {
        mMatrix = matrix;
    }

    public void draw() {
        if (BuildConfig.DEBUG && mTexture == -1) {
            throw new AssertionError("texture invalid.");
        }
        if (BuildConfig.DEBUG && mDrawer == null) {
            throw new AssertionError("null drawer.");
        }

        mDrawer.draw(mTexture, mMatrix);
    }
}
