/**
 * Copyright 2017 lchad
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dw.webp;


import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;

//import com.sohu.inputmethod.sogou.BuildConfig;

/**
 * Created by lchad on 2017/3/24.
 * Github : https://www.github.com/lchad
 */

public class Gifflen {

//    private final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "Gifflen";

    static {
        System.loadLibrary("framesequence");
    }

    private static final int DEFAULT_COLOR = 256;
    private static final int DEFAULT_QUALITY = 10;
    private static final int DEFAULT_WIDTH = 320;
    private static final int DEFAULT_HEIGHT = 320;
    private static final int DEFAULT_DELAY = 500;

    private int mColor;
    private int mQuality;
    private int mDelay;
    private final int DEFAULTDELAY = 100;
    private final int MINDELAY = 20;
    private int mWidth;
    private int mHeight;
    private String mPath;

    private long mNativeGifflen;

    private Handler mHandler;

    private OnEncodeFinishListener mOnEncodeFinishListener;

    private Gifflen(String path, int color, int quality, int delay, int width, int height, OnEncodeFinishListener onEncodeFinishListener) {
        this.mPath = path;
        this.mColor = color;
        this.mQuality = quality;
        this.mDelay = delay;
        this.mWidth = width;
        this.mHeight = height;
        this.mOnEncodeFinishListener = onEncodeFinishListener;
        mHandler = new Handler(Looper.getMainLooper());
    }

    private int getDelay() {
        return (mDelay >= MINDELAY) ? mDelay : DEFAULTDELAY;
    }

    /**
     * 返回一个Builder对象.
     *
     * @return
     */
    public Builder newBuilder() {
        return new Builder();
    }


    private native int addFrameNative(int[] pixels, int delay, long gifflen);
    private native int initNative(String path, int width, int height, int color, int quality, int loopNum);
    private native void closeNative(long gifflen);


    /**
     * Gifflen init
     *
     * @param path    Gif 图片的保存路径
     * @param width   Gif 图片的宽度.
     * @param height  Gif 图片的高度.
     * @param color   Gif 图片的色域.
     * @param quality 进行色彩量化时的quality参数.
     */
    private void init(String path, int width, int height, int color, int quality, int loopNum) {
        mNativeGifflen = initNative(path, width, height, color, quality, loopNum);
    }

    /**
     * Gifflen addFrame
     *
     * @param pixels pixels array from bitmap
     * @return 是否成功.
     */
    private int addFrame(int[] pixels, int delay) throws Exception {
        if (mNativeGifflen == 0) {
            throw new Exception("attempted to use incorrectly built Gifflen");
        }
        return addFrameNative(pixels, delay / 10, mNativeGifflen);
    }

    /**
     * * native层做一些释放资源的操作.
     */
    private void close() throws Exception {
        if (mNativeGifflen == 0) {
            throw new Exception("attempted to use incorrectly built Gifflen");
        }
        closeNative(mNativeGifflen);
    }

    private boolean encodeFrameSequence(InputStream inputStream) {
        if(inputStream == null)
            return false;
        return encodeFrameSequence(FrameSequence.decodeStream(inputStream));
    }
    /**
     * 开始进行Gif生成
     *
     */
    private boolean encodeFrameSequence(ByteBuffer byteBuffer) {
        if(byteBuffer == null)
            return false;
        return encodeFrameSequence(FrameSequence.decodeByteBuffer(byteBuffer));
    }

    private boolean encodeFrameSequence(FrameSequence fs) {
        File file = new File(mPath);
        FrameSequence.State fsState = null;
        if(!file.exists()) {
            try {
                long startTime = System.currentTimeMillis();
                if (fs == null) return false;
                if (fs.getWidth() <= 0 || fs.getHeight() <= 0) return false;
                //TODO 压缩尺寸过小,会有颜色丢失https://isparta.github.io/compare-webp/image/gif_webp/webp/2.webp 200
                float wScale = (mWidth <= 0 || mWidth > fs.getWidth()) ? 1 : (float) mWidth / fs.getWidth();
                float hScale = (mHeight <= 0 || mHeight > fs.getHeight()) ? 1 : (float) mHeight / fs.getHeight();
                float scale = Math.min(wScale, hScale);
                int realWidth = (int)(scale * fs.getWidth());
                int realHeight = (int)(scale * fs.getHeight());

                int[] pixels = new int[realWidth * realHeight];
                Bitmap frameBitmap = Bitmap.createBitmap(fs.getWidth(), fs.getHeight(), Bitmap.Config.ARGB_8888);
                fsState = fs.createState();
                init(mPath, realWidth, realHeight, mColor, mQuality, fs.getDefaultLoopCount());
                for (int i = 0; i < fs.getFrameCount(); i++) {
                    int curDelay = (int)fsState.getFrame(i, frameBitmap, i - 2);
                    Bitmap bitmap = frameBitmap;
                    if (realWidth < frameBitmap.getWidth() || realHeight < frameBitmap.getHeight()) {
                        bitmap = Bitmap.createScaledBitmap(frameBitmap, realWidth, realHeight, true);
                    }
                    bitmap.getPixels(pixels, 0, realWidth, 0, 0, realWidth, realHeight);
                    addFrame(pixels, curDelay);
                    if (bitmap != frameBitmap)
                        bitmap.recycle();
                }
                frameBitmap.recycle();
                close();
                LOGD(" turn to gif cost "+(System.currentTimeMillis() - startTime) +" total"+fs.getFrameCount()+"frame,"+fs.getWidth()+"x"+fs.getHeight()+",bytes:"+(fs.getFrameCount() * fs.getWidth() * fs.getHeight()));
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            } finally {
                if(fsState != null)
                    fsState.destroy();
            }
        }
        return true;
    }


    public static final class Builder {

        public Builder() {
            color = DEFAULT_COLOR;
            quality = DEFAULT_QUALITY;
            delay = DEFAULT_DELAY;
            width = DEFAULT_WIDTH;
            height = DEFAULT_HEIGHT;
        }

        private int color;
        private int quality;
        private int delay;
        private int width;
        private int height;
        private String path;

        private OnEncodeFinishListener onEncodeFinishListener;

        public Builder color(int color) {
            this.color = color;
            return this;
        }

        public Builder quality(int quality) {
            this.quality = quality;
            return this;
        }

        public Builder delay(int delay) {
            this.delay = delay;
            return this;
        }

        public Builder width(int wdith) {
            this.width = wdith;
            return this;
        }

        public Builder height(int height) {
            this.height = height;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder listener(OnEncodeFinishListener onEncodeFinishListener) {
            this.onEncodeFinishListener = onEncodeFinishListener;
            return this;
        }

        public void encodeFrameSequence(ByteBuffer byteBuffer) {
            build().encodeFrameSequence(byteBuffer);
        }

        public void encodeFrameSequence(InputStream inputStream) {
            build().encodeFrameSequence(inputStream);
        }

        private Gifflen build() {
            if (TextUtils.isEmpty(path)) {
                throw new IllegalStateException("the path value is invalid!!");
            }
            File p = new File(path).getParentFile();
            if (!p.exists()) {
                throw new IllegalStateException("the path value does not exists");
            }
            if (this.color < 2 || this.color > 256) {
                this.color = DEFAULT_COLOR;
            }
            if (this.quality <= 0 || this.quality > 100) {
                quality = DEFAULT_QUALITY;
            }

            return new Gifflen(this.path, this.color, this.quality, this.delay, width, height, onEncodeFinishListener);
        }
    }

    /**
     * called from native
     */
    public void onEncodeFinish() {
        if (mOnEncodeFinishListener != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mOnEncodeFinishListener.onEncodeFinish(mPath);
                }
            });
        }
    }

    public interface OnEncodeFinishListener {
        void onEncodeFinish(String path);
    }

    private void LOGD(String message) {
//        if (DEBUG)
//            Log.d("xx",message);
    }

}
