package com.dw.gif;

/**
 * Created by tianmiao on 17-5-25.
 */
public class GifFrameData {
    public int ix;
    public int iy;
    public int iw;
    public int ih;
    public boolean lctFlag;
    public boolean interlace;
    public int lctSize;
    public int[] lct;
    public int bufferFrameStart;
    // last graphic control extension info
    public int dispose;
    public boolean transparency; // use transparent color
    public int delay; // delay in milliseconds
    public int transIndex; // transparent color index

    public void recycle() {
        lct = null;
    }
}
