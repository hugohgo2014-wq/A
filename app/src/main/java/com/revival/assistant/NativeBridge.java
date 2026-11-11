package com.revival.assistant;

public final class NativeBridge {
    static { System.loadLibrary("assistant_native"); }
    private NativeBridge() {}
    public static native boolean loadModel(String absolutePath);
    public static native String generate(String prompt);
    public static native boolean isLoaded();
    public static native void glInit();
    public static native void glResize(int width, int height);
    public static native void glDraw(float timeSeconds);
}
