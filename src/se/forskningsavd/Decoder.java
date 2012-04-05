package se.forskningsavd;

import android.graphics.Bitmap;

public class Decoder {
    public native boolean init();
    public native boolean decode(byte[] frame, Bitmap target);

    static {
        System.loadLibrary("decoder-jni");
    }
}
