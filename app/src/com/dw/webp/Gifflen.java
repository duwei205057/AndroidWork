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


import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by lchad on 2017/3/24.
 * Github : https://www.github.com/lchad
 */

public class Gifflen {

    private static final String TAG = "Gifflen";

    static {
        System.loadLibrary("framesequence");
    }

    private static final int DEFAULT_COLOR = 256;
    private static final int DEFAULT_QUALITY = 100;
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

    private String mTargetPath;

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
    private native int initNative(String path, int width, int height, int color, int quality);
    private native void closeNative(long gifflen);


    /**
     * Gifflen init
     *
     * @param path    Gif 图片的保存路径
     * @param width   Gif 图片的宽度.
     * @param height  Gif 图片的高度.
     * @param color   Gif 图片的色域.
     * @param quality 进行色彩量化时的quality参数.
     * @return 如果返回值是0, 就代表着执行失败.
     */
    private void init(String path, int width, int height, int color, int quality) {
        mNativeGifflen = initNative(path, width, height, color, quality);
    }

    /**
     * Gifflen addFrame
     *
     * @param pixels pixels array from bitmap
     * @return 是否成功.
     */
    private int addFrame(int[] pixels, int delay) {
        if (mNativeGifflen == 0) {
            throw new IllegalStateException("attempted to use incorrectly built Gifflen");
        }
        return addFrameNative(pixels, delay / 10, mNativeGifflen);
    }

    /**
     * * native层做一些释放资源的操作.
     */
    private void close() {
        if (mNativeGifflen == 0) {
            throw new IllegalStateException("attempted to use incorrectly built Gifflen");
        }
        closeNative(mNativeGifflen);
    }

    /**
     * 开始进行Gif生成
     *
     */
    private boolean encodeFrameSequence(ByteBuffer byteBuffer) {
        check(mWidth, mHeight, mPath);
        if(byteBuffer == null)
            return false;
        File file = new File(mPath);
        FrameSequence fs = null;
        FrameSequence.State fsState = null;
        try {
//                long startTime = System.currentTimeMillis();
            fs = FrameSequence.decodeByteBuffer(byteBuffer);
            if (fs == null) return false;
            int realWidth = mWidth <= 0 ? fs.getWidth() : Math.min(mWidth, fs.getWidth());
            int realHeight = mHeight <= 0 ? fs.getHeight() : Math.min(mHeight, fs.getHeight());
            int[] pixels = new int[realWidth * realHeight];
            Bitmap frameBitmap = Bitmap.createBitmap(fs.getWidth(), fs.getHeight(), Bitmap.Config.ARGB_8888);
            fsState = fs.createState();
            init(mPath, realWidth, realHeight, mColor, mQuality);
            int t = 0;
            for (int i = 0; i < fs.getFrameCount(); i++) {
                int curDelay = (int)fsState.getFrame(i, frameBitmap, i - 2);
                Bitmap bitmap = frameBitmap;
                if (realWidth < frameBitmap.getWidth() || realHeight < frameBitmap.getHeight()) {
                    bitmap = Bitmap.createScaledBitmap(frameBitmap, realWidth, realHeight, true);
                }
                if (i > fs.getFrameCount() / 3) {
                    if (t <= 2) {
                        OutputStream os = new FileOutputStream("/sdcard/frame"+t++);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                        os.close();
                    }
                }

                bitmap.getPixels(pixels, 0, realWidth, 0, 0, realWidth, realHeight);
                int result = addFrame(pixels, curDelay);
                if (bitmap != frameBitmap)
                    bitmap.recycle();
            }
            frameBitmap.recycle();
            close();
//                Log.d("xx"," turn to gif cost "+(System.currentTimeMillis() - startTime));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if(fsState != null)
                fsState.destroy();
        }
        return true;
    }

    /**
     * 开始进行Gif生成
     *
     * @param files  传入的每一帧图片的File对象
     * @return 是否成功
     */
    public boolean encodeFiles(List<File> files) {
        check(mWidth, mHeight, mPath);
        int state;
        int[] pixels = new int[mWidth * mHeight];

        init(mPath, mWidth, mHeight, mColor, mQuality);
        int delay = getDelay();
        for (File aFileList : files) {
            Bitmap bitmap;
            try {
                bitmap = BitmapFactory.decodeStream(new FileInputStream(aFileList));
            } catch (FileNotFoundException e) {
                return false;
            }

            if (mWidth < bitmap.getWidth() || mHeight < bitmap.getHeight()) {
                bitmap = Bitmap.createScaledBitmap(bitmap, mWidth, mHeight, true);
            }
            bitmap.getPixels(pixels, 0, mWidth, 0, 0, mWidth, mHeight);
            addFrame(pixels, delay);
            bitmap.recycle();
        }

        close();

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

        public void encodeFiles(List<File> files) {
            build().encodeFiles(files);
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

            if (this.width <= 0) {
                throw new IllegalStateException("the width value is invalid!!");
            }
            if (this.height <= 0) {
                throw new IllegalStateException("the height value is invalid!!");
            }

            return new Gifflen(this.path, this.color, this.quality, this.delay, width, height, onEncodeFinishListener);
        }
    }

    private void check(final int width, final int height, String targetPath) {
        if (targetPath != null && targetPath.length() > 0) {
            mTargetPath = targetPath;
        } else {
            throw new IllegalStateException("the target path is invalid!!");
        }
        if (width <= 0 || height <= 0) {
            throw new IllegalStateException("the width or height value is invalid!!");
        }
    }

    public void onEncodeFinish() {
        if (mOnEncodeFinishListener != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mOnEncodeFinishListener.onEncodeFinish(mTargetPath);
                }
            });
        }
    }

    public interface OnEncodeFinishListener {
        void onEncodeFinish(String path);
    }

}
