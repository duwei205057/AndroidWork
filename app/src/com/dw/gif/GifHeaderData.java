package com.dw.gif;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by tianmiao on 17-5-25.
 */
public class GifHeaderData {
    public int width;
    public int height;
    public boolean gctFlag;
    public int gctSize;
    public int bgIndex;
    public int pixelAspect;
    public int frameCount = -1;
    public int[] gct;

    private Object lock = new Byte[0];

    public GifFrameData currentFrame;
    private List<GifFrameData> frames = new ArrayList<>();

    public List<GifFrameData> getFrames() {
        synchronized (lock) {
            return frames;
        }
    }

    public void recycle() {
        width = 0;
        height = 0;
        gctFlag = false;
        gctSize = 0;
        bgIndex = 0;
        pixelAspect = 0;
        frameCount = 0;
        synchronized (lock) {
            if (frames != null) {
                for (GifFrameData frame : frames) {
                    if (frame != null) {
                        frame.recycle();
                        frame = null;
                    }
                }
                frames.clear();
                frames = null;
            }
        }
    }

    public void reset() {
        width = 0;
        height = 0;
        gctFlag = false;
        gctSize = 0;
        bgIndex = 0;
        pixelAspect = 0;
        frameCount = 0;
        gct = null;

        synchronized (lock) {
            if (frames != null) {
                for (GifFrameData frame : frames) {
                    if (frame != null) {
                        frame.recycle();
                        frame = null;
                    }
                }
                frames.clear();
            }
        }
    }
}