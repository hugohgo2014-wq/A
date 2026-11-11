package com.revival.assistant;

import android.content.Context;
import android.opengl.GLSurfaceView;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GlSurface extends GLSurfaceView implements GLSurfaceView.Renderer {
    private long start;
    public GlSurface(Context context) {
        super(context);
        setEGLContextClientVersion(3);
        setRenderer(this);
        setRenderMode(RENDERMODE_CONTINUOUSLY);
    }
    @Override public void onSurfaceCreated(GL10 gl, EGLConfig config) { start = System.nanoTime(); NativeBridge.glInit(); }
    @Override public void onSurfaceChanged(GL10 gl, int width, int height) { NativeBridge.glResize(width, height); }
    @Override public void onDrawFrame(GL10 gl) { NativeBridge.glDraw((System.nanoTime() - start) / 1_000_000_000.0f); }
}
