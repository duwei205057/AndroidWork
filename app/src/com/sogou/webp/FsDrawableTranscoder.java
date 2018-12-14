package com.sogou.webp;

import android.graphics.Bitmap;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder;

public class FsDrawableTranscoder implements ResourceTranscoder<FrameSequence, FrameSequenceDrawable> {

    private final FrameSequenceDrawable.BitmapProvider bitmapProvider;

    public FsDrawableTranscoder(final BitmapPool bitmapPool) {
        bitmapProvider = new FrameSequenceDrawable.BitmapProvider() {
            @Override
            public Bitmap acquireBitmap(int minWidth, int minHeight) {
                return bitmapPool.get(minWidth, minHeight, Bitmap.Config.ARGB_8888);
            }

            @Override
            public void releaseBitmap(Bitmap bitmap) {
                bitmapPool.put(bitmap);
            }
        };
    }


    @Override
    public Resource<FrameSequenceDrawable> transcode(Resource<FrameSequence> toTranscode) {
        FrameSequenceDrawable fsDrawable = new FrameSequenceDrawable(toTranscode.get(), bitmapProvider);
        return new FsDrawableResource(fsDrawable);
    }
}
