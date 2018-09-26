package com.askey.dvr.cdr7010.dashcam.core.renderer;

import android.content.Context;
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
import com.askey.dvr.cdr7010.dashcam.core.gles.OffscreenSurface;
import com.askey.dvr.cdr7010.dashcam.core.gles.WindowSurface;
import com.askey.dvr.cdr7010.dashcam.core.osd.GnssOSD;
import com.askey.dvr.cdr7010.dashcam.core.osd.GroupOSD;
import com.askey.dvr.cdr7010.dashcam.core.osd.TimestampOSD;
import com.askey.dvr.cdr7010.dashcam.util.Logg;

public class EGLRenderer implements OnFrameAvailableListener {

    private final static String TAG = "EGLRenderer";

    private final static int MSG_INIT = 0;
    private final static int MSG_DEINIT = 1;
    private final static int MSG_DSLP_SURFACE = 2;
    private final static int MSG_ENC_SURFACE_CREATE = 3;
    private final static int MSG_ENC_SURFACE_DESTROY = 4;
    private final static int MSG_DSLP_CLEAR = 5;
    private final static int MSG_UPDATE_FRAME = 6;

    private Context mContext;
    private final int mWidth;
    private final int mHeight;
    private boolean mVideoStamp;
    private RenderHandler mRenderHandler;
    private HandlerThread mRenderThread;
    private IFrameListener mFrameListener;

    public interface OnSurfaceTextureListener {
        void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height);

        void onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture);
    }

    public EGLRenderer(Context context, int width, int height, boolean videoStamp,
                       OnSurfaceTextureListener listener) {
        mContext = context.getApplicationContext();
        mWidth = width;
        mHeight = height;
        mVideoStamp = videoStamp;
        mRenderThread = new HandlerThread("EGLRenderThread");
        mRenderThread.start();
        mRenderHandler = new RenderHandler(mRenderThread.getLooper());
        mRenderHandler.mSurfaceTextureListener = listener;
    }

    public void start() {
        mRenderHandler.sendEmptyMessage(MSG_INIT);
    }

    public void stop() {
        mRenderHandler.sendEmptyMessage(MSG_DEINIT);
    }

    @Override
    public void finalize() {
        if (mRenderThread != null)
            mRenderThread.quit();
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
        private VideoTextureController mTextureController;
        private SurfaceTexture mInputSurface;
        private final float[] mTmpMatrix = new float[16];
        private int mViewportWidth, mViewportHeight;
        private final Object mDispSync = new Object();
        private final Object mEncSync = new Object();
        private OnSurfaceTextureListener mSurfaceTextureListener;
        private GroupOSD mGroupOsd;

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
            Logg.d(TAG, "init");
            mEglCore = new EglCore(null, EglCore.FLAG_RECORDABLE);
            OffscreenSurface dummySurface = new OffscreenSurface(mEglCore, 1, 1);
            dummySurface.makeCurrent();

            mTextureController = new VideoTextureController();
            mTextureController.prepare();

            if (mVideoStamp) {
                mGroupOsd = new GroupOSD();
                mGroupOsd.addOSD(new TimestampOSD());
                mGroupOsd.addOSD(new GnssOSD(mContext));
            }

            mInputSurface = new SurfaceTexture(mTextureController.getTexture());
            mInputSurface.setDefaultBufferSize(mWidth, mHeight);
            mInputSurface.setOnFrameAvailableListener(EGLRenderer.this);

            if (mSurfaceTextureListener != null) {
                mSurfaceTextureListener.onSurfaceTextureAvailable(mInputSurface, mWidth, mHeight);
            }
        }

        private void deinit() {
            Logg.d(TAG, "deinit");
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
            if (mGroupOsd != null) {
                mGroupOsd.release();
                mGroupOsd = null;
            }
            if (mInputSurface != null) {
                if (mSurfaceTextureListener != null) {
                    mSurfaceTextureListener.onSurfaceTextureDestroyed(mInputSurface);
                }
                mInputSurface.release();
                mInputSurface = null;
            }
            if (mTextureController != null) {
                mTextureController.release();
                mTextureController = null;
            }
            if (mEglCore != null) {
                mEglCore.release();
                mEglCore = null;
            }
            mRenderThread.quit();
            mRenderThread = null;
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
            //Logg.d(TAG, "drawFrame");
            if (mEglCore == null) {
                return;
            }

            // Latch the next frame from the camera.
            mInputSurface.updateTexImage();
            mInputSurface.getTransformMatrix(mTmpMatrix);
            mTextureController.setMatrix(mTmpMatrix);

            synchronized (mDispSync) {
                if (mDisplaySurface != null) {
                    mDisplaySurface.makeCurrent();
                    GLES20.glViewport(0, 0, mViewportWidth, mViewportHeight);
                    mTextureController.draw();
                    mDisplaySurface.swapBuffers();
                }
            }

            synchronized (mEncSync) {
                if (mEncoderSurface != null) {
                    mEncoderSurface.makeCurrent();
                    GLES20.glViewport(0, 0, mWidth, mHeight);
                    mTextureController.draw();
                    mEncoderSurface.setPresentationTime(mInputSurface.getTimestamp());
                    if (mGroupOsd != null) {
                        mGroupOsd.draw();
                    }
                    if (mSnapshotCallback != null) {
                        final int width = mEncoderSurface.getWidth();
                        final int height = mEncoderSurface.getHeight();
                        final byte[] data = mEncoderSurface.snapshot().array();
                        mSnapshotCallback.onSnapshotAvailable(data, width, height, System.currentTimeMillis());
                        mSnapshotCallback = null;
                    }
                    if (mFrameListener != null) {
                        mFrameListener.frameAvailableSoon();
                    }
                    mEncoderSurface.swapBuffers();
                }
            }
        }
    }

    private SnapshotCallback mSnapshotCallback = null;

    public void takeDisplaySnapshot(SnapshotCallback callback) {
        if (mSnapshotCallback == null) {
            mSnapshotCallback = callback;
        }
    }

    public interface SnapshotCallback {
        void onSnapshotAvailable(byte[] data, int width, int height, long timeStamp);
    }
}
