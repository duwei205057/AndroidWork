package com.dw.gif;

/**
 * Created by tianmiao on 17-5-25.
 */
public interface GifAction {
    /**
     *
     * @param parseStatus gif parse status, true if success
     * @param frameIndex the index of frame that has been parse, return -1 if all frame has been parsed.
     */
    public void parseOk(boolean parseStatus, int frameIndex);
}