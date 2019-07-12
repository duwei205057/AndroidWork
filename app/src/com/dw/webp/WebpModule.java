package com.dw.webp;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.engine.bitmap_recycle.ArrayPool;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.engine.cache.DiskLruCacheWrapper;
import com.bumptech.glide.module.AppGlideModule;

import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;

//uncomment this to enable module
@GlideModule
public class WebpModule extends AppGlideModule {

    private static DiskCache globalDiskCache = DiskLruCacheWrapper.get(new File("/sdcard/dwcache/"), 50000000);;

    public static DiskCache getGlobalDiskCache() {
        return globalDiskCache;
    }

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
        registry /* FrameSequencesDrawables */
                .prepend(ByteBuffer.class, FrameSequenceDrawable.class,
                        new FsDrawableDecoder<>(bitmapPool, bufferFsDecoder))
                .prepend(InputStream.class, FrameSequenceDrawable.class,
                        new FsDrawableDecoder<>(bitmapPool, streamFsDecoder))
                /* Bitmaps for static lossless and transparent webp or animated webp images*/
                .append(InputStream.class, Bitmap.class, streamBitmapDecoder)
                .append(ByteBuffer.class, Bitmap.class, byteBufferBitmapDecoder)
                .append(InputStream.class, NinePatchBitmap.class, new NinePatchBitmapDecoder<InputStream>())
                .append(ByteBuffer.class, NinePatchBitmap.class, new NinePatchBitmapDecoder<ByteBuffer>())
                .append(TransformUrl.class, InputStream.class, new TransformUrlLoader.Factory())
                /*Encoder*/
                .register(FrameSequenceDrawable.class, new FsDrawableEncoder());
    }

    @Override
    public boolean isManifestParsingEnabled() {
        return true;
    }

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        builder.setDiskCache(new DiskCache.Factory() {
            @Nullable
            @Override
            public DiskCache build() {
                return globalDiskCache;
            }
        });
    }
}
