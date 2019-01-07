package com.dw.webp;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.bitmap_recycle.ArrayPool;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.module.LibraryGlideModule;

import java.io.InputStream;
import java.nio.ByteBuffer;

@GlideModule
public class FsGlideLibraryModule extends LibraryGlideModule {
    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {

        final Resources resources = context.getResources();
        final BitmapPool bitmapPool = glide.getBitmapPool();
        final ArrayPool arrayPool = glide.getArrayPool();
        final ByteBufferFsDecoder bufferFsDecoder = new ByteBufferFsDecoder(registry.getImageHeaderParsers());
        final StreamFsDecoder streamFsDecoder = new StreamFsDecoder(registry.getImageHeaderParsers(), arrayPool);
        /* static webp decoders */
        WebpDownsampler webpDownsampler = new WebpDownsampler(registry.getImageHeaderParsers(),
                resources.getDisplayMetrics(), arrayPool, bitmapPool);
        final ByteBufferBitmapWebpDecoder byteBufferBitmapDecoder = new ByteBufferBitmapWebpDecoder(webpDownsampler);
        final StreamBitmapWebpDecoder streamBitmapDecoder = new StreamBitmapWebpDecoder(webpDownsampler, arrayPool);
        registry /* FrameSequences */
                .prepend(ByteBuffer.class, FrameSequence.class, bufferFsDecoder)
                .prepend(InputStream.class, FrameSequence.class, streamFsDecoder)
                /* FrameSequencesDrawables */
                .prepend(ByteBuffer.class, FrameSequenceDrawable.class,
                        new FsDrawableDecoder<>(bitmapPool, bufferFsDecoder))
                .prepend(InputStream.class, FrameSequenceDrawable.class,
                        new FsDrawableDecoder<>(bitmapPool, streamFsDecoder))
                /* Bitmaps for static lossless and transparent webp or animated webp images*/
                .append(InputStream.class, Bitmap.class, streamBitmapDecoder)
                .append(ByteBuffer.class, Bitmap.class, byteBufferBitmapDecoder)
                .append(InputStream.class, NinePatchBitmap.class, new NinePatchBitmapDecoder<InputStream>())
                .append(ByteBuffer.class, NinePatchBitmap.class, new NinePatchBitmapDecoder<ByteBuffer>())
                /* Transcodes */
                .register(FrameSequence.class, FrameSequenceDrawable.class, new FsDrawableTranscoder(bitmapPool))
                /*Encoder*/
                .register(FrameSequenceDrawable.class, new FsDrawableEncoder())
                .register(FrameSequence.class, new FrameSequenceEncoder());
    }
}
