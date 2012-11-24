package se.forskningsavd.automatonbrain;

import android.graphics.Bitmap;

class Decoder {
    public native boolean init();
    public native boolean decode(byte[] frame, Bitmap target);

    static {
        System.loadLibrary("decoder-jni");
    }
}
