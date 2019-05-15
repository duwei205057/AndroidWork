package com.dw.webp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class NinePatchBitmapDecoder<DataType> implements ResourceDecoder<DataType, NinePatchBitmap> {

    @Override
    public boolean handles(DataType dataType, Options options) throws IOException {
        return true;
    }

    @Nullable
    @Override
    public Resource<NinePatchBitmap> decode(DataType dataType, int width, int height, Options options) throws IOException {

        try {
            Bitmap bt = null;
            if (dataType instanceof InputStream)
                bt = BitmapFactory.decodeStream((InputStream)dataType);
            else if (dataType instanceof ByteBuffer) {
                byte data[] = ((ByteBuffer)dataType).array();
                bt = BitmapFactory.decodeByteArray(data, 0, data.length);
            } else {
                return null;
            }
            NinePatchBitmap npb = new NinePatchBitmap(bt);
            return new NinePatchBitmapResource(npb);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
