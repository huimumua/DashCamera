package com.askey.dvr.cdr7010.dashcam.core.glutils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BitmapDrawer {
    private static final String TAG = BitmapDrawer.class.getSimpleName();
    private int mProgram;
    private int mTexSamplerHandle;
    private int mTexCoordHandle;
    private int mPosCoordHandle;
    private FloatBuffer mTexVertices;
    private FloatBuffer mPosVertices;
    private int mTextureHandle;

    public BitmapDrawer() {
        mTextureHandle = initTex()[0];
    }

    /**
     * create external texture
     * @return texture ID
     */
    public int[] initTex() {
        //着色器程序
        String VERTEX_SHADER_CODE =
                "attribute vec4 a_position;\n" +
                        "attribute vec2 a1_texcoord;\n" +
                        "varying vec2 v_texcoord;\n" +
                        "void main() {\n" +
                        "  gl_Position = a_position;\n" +
                        "  v_texcoord = a1_texcoord;\n" +
                        "}\n";
        String FRAGMENT_SHADER_CODE =
                "precision mediump float;\n" +
                        "uniform sampler2D tex_sampler;\n" +
                        "varying vec2 v_texcoord;\n" +
                        "void main() {\n" +
                        "  gl_FragColor = texture2D(tex_sampler, v_texcoord);\n" +
                        "}\n";
        mProgram = GLShaderToolbox.createProgram(VERTEX_SHADER_CODE, FRAGMENT_SHADER_CODE);
        mTexSamplerHandle = GLES20.glGetUniformLocation(mProgram,"tex_sampler");
        mTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "a1_texcoord");
        mPosCoordHandle = GLES20.glGetAttribLocation(mProgram, "a_position");
        //创建纹理并将图片贴入纹理
        final int[] tex = new int[1];
        GLES20.glGenTextures(1, tex, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex[0]);
        GLShaderToolbox.initTextureNeedParams();

        return tex;
    }

    public void drawBitmap(Bitmap bitmap, float[] POS_VERTICES) {
        //纹理坐标屏幕左上角为原点(左下，右下，左上，右上)
        float[] TEX_VERTICES = { 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f };
        mTexVertices = ByteBuffer.allocateDirect(TEX_VERTICES.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTexVertices.put(TEX_VERTICES).position(0);
        //顶点坐标屏幕中心点为原点(左下，右下，左上，右上)
//        float[] POS_VERTICES = {-1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f };
        if(POS_VERTICES == null)
            POS_VERTICES = new float[]{-0.1f, -1.0f, 0.98f, -1.0f, -0.1f, -0.8f, 0.98f, -0.8f };
        mPosVertices = ByteBuffer.allocateDirect(POS_VERTICES.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mPosVertices.put(POS_VERTICES).position(0);

        //将纹理坐标传递给着色器程序并使能属性数组
        GLES20.glVertexAttribPointer(mTexCoordHandle, 2, GLES20.GL_FLOAT, false, 0, mTexVertices);
        GLES20.glEnableVertexAttribArray(mTexCoordHandle);
//        GLShaderToolbox.checkGlError("vertex attribute setup");
        //将顶点坐标传递给着色器程序并使能属性数组
        GLES20.glVertexAttribPointer(mPosCoordHandle, 2, GLES20.GL_FLOAT, false, 0, mPosVertices);
        GLES20.glEnableVertexAttribArray(mPosCoordHandle);
//        GLShaderToolbox.checkGlError("vertex attribute setup");

        //opengles2.0 背景透明(参考：http://blog.csdn.net/renai2008/article/details/7956988 )
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        GLES20.glUseProgram(mProgram);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureHandle);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glUseProgram(0);
//        bitmap.recycle();
    }

    /**
     * terminatinng, this should be called in GL context
     */
    public void release() {
        deleteTex();

        if (mProgram >= 0)
            GLES20.glDeleteProgram(mProgram);
        mProgram = -1;
    }

    /**
     * delete specific texture
     */
    public void deleteTex() {
        final int[] tex = new int[] {mTextureHandle};
        GLES20.glDeleteTextures(1, tex, 0);
    }

    public Bitmap getBitmapByStr(String string, int color, int textSize, int width, int height, Paint.Style style, Bitmap mBitmap) {
        mBitmap.eraseColor(Color.TRANSPARENT);
        Canvas canvas = new Canvas(mBitmap);
        Paint paint = new Paint();
        paint.setTextSize(textSize);
        paint.setStyle(style);
        paint.setAntiAlias(true);
//        mPaint.setTypeface(Typeface.create("宋体", Typeface.BOLD));
        paint.setTypeface(Typeface.create("DroidSans", Typeface.BOLD));
        //paint.setARGB(0xff, 0x00, 0x00, 0xff);
        paint.setTextAlign(Paint.Align.LEFT);

        if(string == null){
            string = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss a", Locale.US).format(new Date(System.currentTimeMillis()));
        }
        paint.setColor(color);
        canvas.drawText(string, 2, height - 3, paint);
        return mBitmap;
    }

    public Bitmap getPointBitmap(float radius, int color, Bitmap mBitmap) {
        mBitmap.eraseColor(Color.TRANSPARENT);
        Canvas canvas = new Canvas(mBitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
//        mPaint.setTypeface(Typeface.create("宋体", Typeface.BOLD));
        paint.setTypeface(Typeface.create("DroidSans", Typeface.BOLD));
        //paint.setARGB(0xff, 0x00, 0x00, 0xff);
        paint.setColor(color);
        canvas.drawCircle(radius, radius, radius, paint);
        return mBitmap;
    }
}
