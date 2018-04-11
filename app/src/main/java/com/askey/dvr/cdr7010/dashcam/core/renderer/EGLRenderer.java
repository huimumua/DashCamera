package com.askey.dvr.cdr7010.dashcam.core.renderer;

import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.Surface;

import com.askey.dvr.cdr7010.dashcam.core.encoder.IFrameListener;
import com.askey.dvr.cdr7010.dashcam.core.gles.EglCore;
import com.askey.dvr.cdr7010.dashcam.core.gles.GLDrawer2D;
import com.askey.dvr.cdr7010.dashcam.core.gles.OffscreenSurface;
import com.askey.dvr.cdr7010.dashcam.core.gles.WindowSurface;

public class EGLRenderer implements OnFrameAvailableListener {

    private final static int MSG_INIT = 0;
    private final static int MSG_DEINIT = 1;
    private final static int MSG_DSLP_SURFACE = 2;
    private final static int MSG_ENC_SURFACE_CREATE = 3;
    private final static int MSG_ENC_SURFACE_DESTROY = 4;
    private final static int MSG_DSLP_CLEAR = 5;
    private final static int MSG_UPDATE_FRAME = 6;

    private RenderHandler mRenderHandler;
    private HandlerThread mRenderThread;
    private SnapshotCallback mSnapshotCallback = null;
    private IFrameListener mFrameListener;

    public interface SnapshotCallback {
        void onSnapshotAvailable(byte[] data, int width, int height, long timeStamp);
    }

    public EGLRenderer() {

    }

    public void start() {
        mRenderThread = new HandlerThread("EGLRenderThread");
        mRenderThread.start();
        mRenderHandler = new RenderHandler(mRenderThread.getLooper());
        init();
    }

    public void stop() {
        deinit();
        mRenderThread.quitSafely();
        try {
            mRenderThread.join();
            mRenderThread = null;
            mRenderHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void init() {
        mRenderHandler.sendEmptyMessage(MSG_INIT);
    }

    private void deinit() {
        mRenderHandler.sendEmptyMessage(MSG_DEINIT);
    }

    public void setDisplaySurface(Surface surface, int width, int height) {
        mRenderHandler.setDisplaySurface(surface, width, height);
    }

    public void createEncoderSurface(Surface surface, IFrameListener listener) {
        mFrameListener = listener;
        mRenderHandler.createEncoderSurface(surface);
    }

    public void destroyEncoderSurface() {
        mRenderHandler.destroyEncoderSurface();
        mFrameListener = null;
    }

    public SurfaceTexture getInputSurfaceTexture() {
        return mRenderHandler.mInputSurface;
    }

    public void takeDisplaySnapshot(SnapshotCallback callback) {
        if (mSnapshotCallback == null) {
            mSnapshotCallback = callback;
        }
    }

    public void clear() {
        mRenderHandler.sendEmptyMessage(MSG_DSLP_CLEAR);
    }

    @Override //OnFrameAvailableListener
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        mRenderHandler.sendEmptyMessage(MSG_UPDATE_FRAME);
    }

    private class RenderHandler extends Handler {
        private EglCore mEglCore;
        private WindowSurface mDisplaySurface;
        private WindowSurface mEncoderSurface;
        private SurfaceTexture mInputSurface;
        private GLDrawer2D mDrawer;
//        private OverlayDrawer mOverlay;
        private int mInputTexture;
        private final float[] mTmpMatrix = new float[16];
        private int mViewportWidth, mViewportHeight;
        private final Object mDispSync = new Object();
        private final Object mEncSync = new Object();

        public RenderHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case MSG_INIT:
                    init();
                    break;
                case MSG_DEINIT:
                    deinit();
                    break;
                case MSG_DSLP_CLEAR:
                    this.cleanDisplay();
                    break;
                case MSG_UPDATE_FRAME:
                    drawFrame();
                    break;
            }
        }

        private void init() {
            mEglCore = new EglCore(null, EglCore.FLAG_RECORDABLE);
            OffscreenSurface dummySurface = new OffscreenSurface(mEglCore, 1, 1);
            dummySurface.makeCurrent();

            mInputTexture = GLDrawer2D.initTex();
            mInputSurface = new SurfaceTexture(mInputTexture);
            mInputSurface.setOnFrameAvailableListener(EGLRenderer.this);

            mDrawer = new GLDrawer2D();
//            mOverlay = new OverlayDrawer();
        }

        private void deinit() {
            if (mInputSurface != null) {
                mInputSurface.release();
                mInputSurface = null;
            }
            GLDrawer2D.deleteTex(mInputTexture);
            synchronized (mDispSync) {
                if (mDisplaySurface != null) {
                    mDisplaySurface.release();
                    mDisplaySurface = null;
                }
            }
            synchronized (mEncSync) {
                if (mEncoderSurface != null) {
                    mEncoderSurface.release();
                    mEncoderSurface = null;
                }
            }
            if (mEglCore != null) {
                mEglCore.release();
                mEglCore = null;
            }
            if (mDrawer != null) {
                mDrawer.release();
                mDrawer = null;
            }
//            if (mOverlay != null) {
//                mOverlay.release();
//                mOverlay = null;
//            }
        }

        private void setDisplaySurface(Surface surface, int width, int height) {
            synchronized (mDispSync) {
                if (surface != null) {
                    if (mDisplaySurface != null) {
                        mDisplaySurface.release();
                        mDisplaySurface = null;
                    }
                    mDisplaySurface = new WindowSurface(mEglCore, surface, false);
                    mViewportWidth = width;
                    mViewportHeight = height;
                } else if (mDisplaySurface != null) {
                    mDisplaySurface.release();
                    mDisplaySurface = null;
                }
            }
        }

        private void createEncoderSurface(Surface surface) {
            synchronized (mEncSync) {
                mEncoderSurface = new WindowSurface(mEglCore, surface, true);
            }
        }

        private void destroyEncoderSurface() {
            synchronized (mEncSync) {
                if (mEncoderSurface != null) {
                    mEncoderSurface.release();
                    mEncoderSurface = null;
                }
            }
        }

        private void cleanDisplay() {
            synchronized (mDispSync) {
                if (mDisplaySurface != null) {
                    mDisplaySurface.makeCurrent();
                    GLES20.glViewport(0, 0, mViewportWidth, mViewportHeight);
                    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
                    mDisplaySurface.swapBuffers();
                }
            }
        }

        private void drawFrame() {
            if (mEglCore == null) {
                return;
            }

            // Latch the next frame from the camera.
            mInputSurface.updateTexImage();
            mInputSurface.getTransformMatrix(mTmpMatrix);

            synchronized (mDispSync) {
                if (mDisplaySurface != null) {
                    mDisplaySurface.makeCurrent();
                    GLES20.glViewport(0, 0, mViewportWidth, mViewportHeight);
                    mDrawer.draw(mInputTexture, mTmpMatrix);
//                    mOverlay.drawPreview(mElapsed, mEncoderSurface == null);
                    mDisplaySurface.swapBuffers();
                }
            }

            synchronized (mEncSync) {
                if (mEncoderSurface != null) {
                    mEncoderSurface.makeCurrent();
                    GLES20.glViewport(0, 0, 1920, 1080);
                    mDrawer.draw(mInputTexture, mTmpMatrix);
//                    mOverlay.drawRecord(true);
                    mEncoderSurface.setPresentationTime(mInputSurface.getTimestamp());
                    takeSnapshotIfNecessary(mEncoderSurface);
                    if (mFrameListener != null) {
                        mFrameListener.frameAvailableSoon();
                    }
                    mEncoderSurface.swapBuffers();
                }
            }
        }

        private void takeSnapshotIfNecessary(WindowSurface surface) {
            if (mSnapshotCallback != null) {
                final SnapshotCallback callback = mSnapshotCallback;
                mSnapshotCallback = null;
                final int width = surface.getWidth();
                final int height = surface.getHeight();
                final byte[] data = surface.snapshot().array();
                callback.onSnapshotAvailable(data, width, height, System.currentTimeMillis());
            }
        }
    }

}
