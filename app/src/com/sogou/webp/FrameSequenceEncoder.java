package com.sogou.webp;

import android.util.Log;

import com.bumptech.glide.load.EncodeStrategy;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceEncoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.util.ByteBufferUtil;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by dw on 18-12-11.
 */

public class FrameSequenceEncoder implements ResourceEncoder<FrameSequence> {
    private static final String TAG = "FrameSequenceEncoder";
    @Override
    public EncodeStrategy getEncodeStrategy(Options options) {
        return EncodeStrategy.SOURCE;
    }

    @Override
    public boolean encode(Resource<FrameSequence> data, File file, Options options) {
        FrameSequence fs = data.get();
        boolean success = false;
        try {
            byte[] byteData = fs.getByteData();
            ByteBufferUtil.toFile(ByteBuffer.wrap(byteData), file);
            success = true;
        } catch (IOException e) {
            if (Log.isLoggable(TAG, Log.WARN)) {
                Log.w(TAG, "Failed to encode FrameSequence", e);
            }
        }
        return success;
    }
}
