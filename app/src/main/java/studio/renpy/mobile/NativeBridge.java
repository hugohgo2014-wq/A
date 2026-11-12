package studio.renpy.mobile;
public final class NativeBridge {
 static { System.loadLibrary("renpycore"); }
 private NativeBridge() {}
 public static native String compileStory(String encodedEvents);
}
