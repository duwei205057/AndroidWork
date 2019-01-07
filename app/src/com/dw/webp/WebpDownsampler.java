package com.dw.webp;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;

import com.bumptech.glide.load.ImageHeaderParser;
import com.bumptech.glide.load.Option;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.ArrayPool;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;
import com.bumptech.glide.load.resource.gif.GifBitmapProvider;
import com.bumptech.glide.util.Preconditions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Decode the animated webp image and obtain the first frame bitmap or Decode static lossless and transparent webp
 *
 */
public class WebpDownsampler {

    private static final String TAG = "WebpDownsampler";
    public static final Option<Boolean> DISABLE_BITMAP = Option.memory(
            "com.bumptech.glide.integration.webp.decoder.AnimatedWebpBitmapDecoder.DisableBitmap", false);
    public static final Option<Boolean> DISABLE_DECODER = Option.memory(
            "com.bumptech.glide.integration.webp.decoder.WebpDownsampler.DisableDecoder", false);


    private final ArrayPool mArrayPool;
    private final BitmapPool mBitmapPool;
    private final GifBitmapProvider mProvider;
    private final DisplayMetrics displayMetrics;
    private final List<ImageHeaderParser> parsers;

    public WebpDownsampler(List<ImageHeaderParser> parsers, DisplayMetrics displayMetrics, ArrayPool byteArrayPool, BitmapPool bitmapPool) {
        this.parsers = parsers;
        this.displayMetrics = Preconditions.checkNotNull(displayMetrics);
        mArrayPool = byteArrayPool;
        mBitmapPool = bitmapPool;
        mProvider = new GifBitmapProvider(bitmapPool, byteArrayPool);
    }

    public boolean handles(InputStream is, Options options) throws IOException{

        WebpHeaderParser.WebpImageType webpType = WebpHeaderParser.getType(is, mArrayPool);
        // handle lossless and transparent webp
        return shouldDecode(webpType, options);
    }

    public boolean handles(ByteBuffer byteBuffer, Options options) throws IOException{

        WebpHeaderParser.WebpImageType webpType = WebpHeaderParser.getType(byteBuffer);
        // handle lossless and transparent webp
        return shouldDecode(webpType, options);
    }

    private boolean  shouldDecode(WebpHeaderParser.WebpImageType webpType, Options options) {
        if (WebpHeaderParser.isStaticWebpType(webpType) && webpType != WebpHeaderParser.WebpImageType.WEBP_SIMPLE) {// handle lossless and transparent webp
            return (options.get(DISABLE_DECODER) || WebpHeaderParser.sIsExtendedWebpSupported) ? false : true;
        }else if (WebpHeaderParser.isAnimatedWebpType(webpType)) {// handle animated webp
            return !options.get(DISABLE_BITMAP);
        } else {
            return false;
        }
    }

    public Resource<Bitmap> decode(InputStream source, int width, int height,
                                   Options options) throws IOException {
        byte[] data = inputStreamToBytes(source);
        if (data == null) {
            return null;
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        return decode(byteBuffer, width, height, options);
    }

    public Resource<Bitmap> decode(ByteBuffer source, int width, int height,
                                   Options options) throws IOException {
        int length = source.remaining();
        byte[] data = new byte[length];
        source.get(data, 0, length);
        FrameSequence fs = FrameSequence.decodeByteArray(data);
        FrameSequence.State state = fs.createState();
        if (fs == null) return null;
        try {
            Bitmap bitmap = mProvider.obtain(fs.getWidth(), fs.getHeight(), Bitmap.Config.ARGB_8888);
            state.getFrame(0, bitmap, -1);
            return BitmapResource.obtain(bitmap, mBitmapPool);
        } finally {
            // release the resources
            state.destroy();
        }
    }

    static byte[] inputStreamToBytes(InputStream is) {
        final int bufferSize = 16384;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(bufferSize);
        try {
            int nRead;
            byte[] data = new byte[bufferSize];
            while ((nRead = is.read(data)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
        } catch (IOException e) {
            if (Log.isLoggable(TAG, Log.WARN)) {
                Log.w(TAG, "Error reading data from stream", e);
            }
            return null;
        }
        return buffer.toByteArray();
    }
}
