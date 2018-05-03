package com.askey.dvr.cdr7010.dashcam.core.osd;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.support.annotation.NonNull;

import com.askey.dvr.cdr7010.dashcam.core.gles.GlUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public abstract class BaseOSD {

    private static String VERTEX_SHADER_CODE =
            "attribute vec4 a_position;\n" +
            "attribute vec2 a1_texcoord;\n" +
            "varying vec2 v_texcoord;\n" +
            "void main() {\n" +
            "  gl_Position = a_position;\n" +
            "  v_texcoord = a1_texcoord;\n" +
            "}\n";

    private static String FRAGMENT_SHADER_CODE =
            "precision mediump float;\n" +
            "uniform sampler2D tex_sampler;\n" +
            "varying vec2 v_texcoord;\n" +
            "void main() {\n" +
            "  gl_FragColor = texture2D(tex_sampler, v_texcoord);\n" +
            "}\n";

    private static float[] TEX_VERTICES = new float[]{
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f};
    private float[] POS_VERTICES = new float[]{
            -0.1f, -1.0f,
            0.98f, -1.0f,
            -0.1f, -0.8f,
            0.98f, -0.8f};

    private int mProgram;
    private int mTexSamplerHandle;
    private int mTexCoordHandle;
    private int mPosCoordHandle;
    private FloatBuffer mTexVertices;
    private FloatBuffer mPosVertices;
    private int mTextureId;

    public BaseOSD() {
        initBuffer();
        initTex();
    }

    private void initBuffer() {
        mTexVertices = ByteBuffer.allocateDirect(TEX_VERTICES.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mTexVertices.put(TEX_VERTICES).position(0);

        mPosVertices = ByteBuffer.allocateDirect(POS_VERTICES.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mPosVertices.put(POS_VERTICES).position(0);
    }

    private void initTex() {
        mProgram = GlUtil.createProgram(VERTEX_SHADER_CODE, FRAGMENT_SHADER_CODE);
        mTexSamplerHandle = GLES20.glGetUniformLocation(mProgram,"tex_sampler");
        mTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "a1_texcoord");
        mPosCoordHandle = GLES20.glGetAttribLocation(mProgram, "a_position");

        final int[] tex = new int[1];
        GLES20.glGenTextures(1, tex, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        mTextureId = tex[0];
    }

    public final void setPosition(@NonNull float[] pos) {
        POS_VERTICES = pos;
        mPosVertices.put(POS_VERTICES).position(0);
    }

    public final void draw() {
        onDraw();
    }

    protected void onDraw() {
        //将纹理坐标传递给着色器程序并使能属性数组
        GLES20.glVertexAttribPointer(mTexCoordHandle, 2, GLES20.GL_FLOAT, false, 0, mTexVertices);
        GLES20.glEnableVertexAttribArray(mTexCoordHandle);
        //将顶点坐标传递给着色器程序并使能属性数组
        GLES20.glVertexAttribPointer(mPosCoordHandle, 2, GLES20.GL_FLOAT, false, 0, mPosVertices);
        GLES20.glEnableVertexAttribArray(mPosCoordHandle);

        //opengles2.0 背景透明(参考：http://blog.csdn.net/renai2008/article/details/7956988 )
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        GLES20.glUseProgram(mProgram);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, getBitmap(), 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glUseProgram(0);
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
    private void deleteTex() {
        final int[] tex = new int[] {mTextureId};
        GLES20.glDeleteTextures(1, tex, 0);
    }

    protected abstract Bitmap getBitmap();
}
