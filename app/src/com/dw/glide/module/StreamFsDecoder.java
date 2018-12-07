package com.dw.glide.module;

import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.model.ImageVideoWrapper;
import com.sogou.webp.FrameSequence;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author liuchun
 */
public class StreamFsDecoder implements ResourceDecoder<ImageVideoWrapper, FrameSequence> {


    public StreamFsDecoder() {
    }

    @Override
    public Resource<FrameSequence> decode(ImageVideoWrapper source, int width, int height) throws IOException {
        FrameSequence fs = FrameSequence.decodeStream(source.getStream());
        if (fs == null) {
            return null;
        }

        return new FrameSequenceResource(fs);
    }

    @Override
    public String getId() {
        return "";
    }
}
