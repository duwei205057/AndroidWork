package com.dw.gif;

import android.content.Context;
import android.content.res.Resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class GifImage {
    private BaseGifDecoder mGifDecoder = null;
    private boolean mLoading = true;
    public int mWidth = 0;
    public int mHeight = 0;
    private boolean startDecode = false;

    public GifImage(Context context, int resId) {
        Resources r = context.getResources();
        InputStream is = r.openRawResource(resId);
        setGifDecoderImage(is);
    }

    public GifImage(InputStream is) {
        setGifDecoderImage(is);
    }

    public GifImage(String path) throws FileNotFoundException{
        if (path == null) return ;
        File file = new File(path);
        FileInputStream is = new FileInputStream(file);
        setGifDecoderImage(is);
    }

    private void setGifDecoderImage(InputStream is){
        if(mGifDecoder != null){
            mGifDecoder.interrupt();
            mGifDecoder.free();
            mGifDecoder= null;
        }
        try {
            mGifDecoder = new BaseGifDecoder(is,mGifAction);
        } catch (Exception e) {
        }
    }

    public void setImageRequireSize(int requireWidth, int requireHeight) {
        mGifDecoder.setImageRequireSize(requireWidth, requireHeight);
    }

    public void startDecoder(GifTimerPool pool) {
        if(startDecode || ismLoadingComplate()) return;
        if(pool != null) {
            pool.commitRunnable(new Runnable() {
                @Override
                public void run() {
                    if(mGifDecoder != null)
                        mGifDecoder.run();
                }
            });
            startDecode = true;
        }
    }

    private GifAction mGifAction = new GifAction() {
        @Override
        public void parseOk(boolean parseStatus, int frameIndex) {
            if (!parseStatus) return;
            if(frameIndex == -1){
                mLoading = false;
                if(mGifDecoder != null) {
                    mWidth = mGifDecoder.mWidth;
                    mHeight = mGifDecoder.mHeight;
                }
            }
        }
    };

    public GifFrame next() {
        if(mGifDecoder == null)
            return null;
        return mGifDecoder.next();
    }

    public void reset() {
        mGifDecoder.reset();
    }

    public boolean ismLoadingComplate() {
        return !mLoading;
    }

    public void recycle() {
        if (mGifDecoder != null) {
            mGifDecoder.interrupt();
            mGifDecoder.free();
            mGifDecoder = null;
        }
    }
}
