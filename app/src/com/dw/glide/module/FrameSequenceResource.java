package com.dw.glide.module;


import com.bumptech.glide.load.engine.Resource;
import com.sogou.webp.FrameSequence;

/**
 * @author liuchun
 */
public class FrameSequenceResource implements Resource<FrameSequence> {
    private final FrameSequence frameSequence;


    public FrameSequenceResource(FrameSequence fs) {
        frameSequence = fs;
    }

    @Override
    public FrameSequence get() {
        return frameSequence;
    }

    @Override
    public int getSize() {
        return frameSequence.getWidth() * frameSequence.getHeight() * 4;
    }

    @Override
    public void recycle() {

    }
}
