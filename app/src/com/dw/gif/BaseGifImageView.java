package com.dw.gif;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by tianmiao on 17-5-18.
 */
public class BaseGifImageView extends ImageView {
    private static final String TAG = "BaseGifImageView";
    private static final boolean DEBUG = false;

    public static final int GIF_SCALE_TYPE_CENTER = 1;
    public static final int GIF_SCALE_TYPE_FIT_CENTER = 2;
    public static final int GIF_SCALE_TYPE_FITXY = 3;
    public static final int GIF_SCALE_TYPE_FIT_WIDTH = 4;
    public static final int GIF_SCALE_TYPE_FIT_HEIGHT = 5;

    protected static final int MSG_INVALIDATE_VIEW = 1233;
    protected static final int MSG_REDRAW = 1234;
    protected static final int MSG_GIF_STOP_ACTION = 1235;

    protected boolean mIsGifImage = false;
    protected int mGifLoopTimes = -1;/* -1: loop ; 1: single time; else > 0: play times */
    protected int mGifScaleType = 2; // 1: GIF_SCALE_TYPE_CENTER; 2: GIF_SCALE_TYPE_FIT_CENTER; 3: GIF_SCALE_TYPE_FITXY
    protected boolean mDrawLoadingBitmap = true; //show loading bitmap or not
    protected boolean mCheckDarkMode = false;
    protected boolean mDarkModeOn = false;
    protected boolean mRecyclePrevious = false;
    protected long mGifLoopInterval = 0;//每次循环间隔
    /*
     * true: show immediately when the first frame decoded,
     * used for large gif, recycle previous frames to save memory
     * gif delay time is not exact the time gif file declared when time to decode single frame is longer then delay time
     *
     * false: start play after all frames decoded
     * used for short gif, all frames will stay in memory until gif view is destroyed
     * */
    protected BaseGifDecoder mGifDecoder;
    // called when gif stopped
    protected GifStopListener mGifStopListener;
    protected GifStartListener mGifStartListener;

    protected static final int LOADING_TIME_INTERVAL = 150;
    protected static final int LOADING_START_TIME_INTERVAL = 300;

    protected GifImageType animationType = GifImageType.WAIT_FINISH;
    protected Bitmap mCurrentImage = null;
    protected Drawable mCurrentDrawable = null;
    protected RoundedBitmapDrawable mRoundDrawable = null;
    protected boolean mUpdateDrawable = false;

    protected boolean isRun = true;
    protected boolean pause = false;
    protected boolean decodeFinish = false;

    public final static String DRAWTHREAD_TAG = "DRAWTHREAD_TAG";
    protected DrawThread drawThread = null;
    protected LoadingDrawThread mLoadingDrawThread = null;
    protected Rect mGifRect = null;

    protected boolean mGifLoading = true;
    protected Drawable mGifLoadingDrawable = null;
    protected int mLoadingDegree = 0;
    protected PaintFlagsDrawFilter mPaintFlagsDrawFilter;

    private boolean mIsRoundImage = false;
    private float mRoundedCircle = 0f;

    public enum GifImageType{
        WAIT_FINISH (0),
        SYNC_DECODER (1),
        COVER (2),
        LARGE_MODE_ONCE(3),
        LARGE_MODE_LOOP(4);
        GifImageType(int i){
            nativeInt = i;
        }
        final int nativeInt;
    }

    public interface GifStopListener {
        void onGifStop(boolean status);
    }

    public interface GifStartListener {
        void onGifStart(boolean status);
    }

    public BaseGifImageView(Context context) {
        super(context);
        init();
    }

    public BaseGifImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init();
    }

    public BaseGifImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    protected void init() {
        mPaintFlagsDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    }

    public void setPause(boolean isPause) {
        pause = isPause;
    }

    public void setGifImageType(GifImageType type) {
        animationType = type;
    }

    /**
     * params: isGif true: play gif; false: use as normal imageView
     * */
    public void setIsGifImage(boolean isGif) {
        mIsGifImage = isGif;
    }

    /** 设置gif是否圆角*/
    public void setImageRound(boolean isRound) {
        mIsRoundImage = isRound;
    }
    public void setRoundedCircle(float circle) {
        mRoundedCircle = circle;
    }

    public void showLoadingNow() {
        if (mDrawLoadingBitmap) {
            stopDrawLoading();
            startDrawLoading();
        }
    }

    public void setLoopTimes(int times) {
        mGifLoopTimes = times;
    }

    public void setLoopInterval(long interval) {
        mGifLoopInterval = interval;
    }

    public void setShowLoading(boolean showLoading) {
        mDrawLoadingBitmap = showLoading;
    }

    public void setLoadingDrawable(Drawable loadingDrawable) {
        mGifLoadingDrawable = loadingDrawable;
    }

    public void setGifScaleType(int scaleType) {
        mGifScaleType = scaleType;
    }

    public void setAnimationType(GifImageType type) {
        animationType = type;
        if (type == GifImageType.LARGE_MODE_ONCE) {
            mGifLoopTimes = 1;
            setRecyclePrevious(true);
        }
    }

    public void setRecyclePrevious(boolean recycle) {
        mRecyclePrevious = recycle;
    }

    public void setCheckDarkMode(boolean checkDarkMode, boolean darkModeOn) {
        mCheckDarkMode = checkDarkMode;
        mDarkModeOn = darkModeOn;
    }

    public void setImageRequireSize(int requireWidth, int requireHeight) {
        if (mGifDecoder != null)
            mGifDecoder.setImageRequireSize(requireWidth, requireHeight);
    }

    public void setGifStopListener(GifStopListener listener) {
        mGifStopListener = listener;
    }

    public void setGifStartListener(GifStartListener listener) {
        mGifStartListener = listener;
    }

    public void setGifImage(int resId){
        setImageDrawable(null);
        Resources r = getContext().getResources();
        InputStream is = r.openRawResource(resId);
        setGifDecoderImage(is);
        setIsGifImage(true);
    }

    public void setGifImage(String gifFilePath, boolean showNow) {
        LOGD("gifFilePath = " + gifFilePath);
        if (gifFilePath == null) return;
        try {
            File file = new File(gifFilePath);
            InputStream is = new FileInputStream(file);
            setGifDecoderImage(is);
            setIsGifImage(true);
        } catch (Exception e) {
        }
    }

    private void setGifDecoderImage(InputStream is) {
        try {
            if (mGifDecoder != null) {
                mGifDecoder.interrupt();
                mGifDecoder.free();
                mGifDecoder = null;
            }
        } catch (Exception e) {}

        try {
            if (drawThread != null) {
                drawThread.interrupt();
                drawThread = null;
            }
        } catch (Exception e) {}

        if (is != null) {
            mCurrentDrawable = null;
            mRoundDrawable = null;
            mCurrentImage = null;
            isRun = true;
            pause = false;
            decodeFinish = false;
        }
        try {
            if (animationType == GifImageType.LARGE_MODE_ONCE || animationType == GifImageType.LARGE_MODE_LOOP) {
                byte[] data = getByteArrayFromStream(is);
                mGifDecoder = new BaseGifDecoder(data, mGifAction, true);
            } else {
                mGifDecoder = new BaseGifDecoder(is, mGifAction);
            }
            mGifDecoder.setLoopTimes(mGifLoopTimes);
            mGifDecoder.start();
        } catch (Exception e) {
        }
        if (animationType == GifImageType.WAIT_FINISH) {
            if (mDrawLoadingBitmap) {
                stopDrawLoading();
                startDrawLoading();
            }
        }
    }

    private byte[] getByteArrayFromStream(InputStream is) throws IOException {
        byte[] buffer;
        ByteArrayOutputStream os = new ByteArrayOutputStream(1000);
        byte[] b = new byte[1000];
        int n;
        while ((n = is.read(b)) != -1) {
            os.write(b, 0, n);
        }
        is.close();
        os.close();
        buffer = os.toByteArray();
        return buffer;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!mIsGifImage) {
            super.onDraw(canvas);
            return;
        }
        //画布抗锯齿
        canvas.setDrawFilter(mPaintFlagsDrawFilter);
        int widthWithoutPadding = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        int heightWithoutPadding = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
        if (animationType == GifImageType.WAIT_FINISH && mGifLoading) {
            super.onDraw(canvas);
            if (mDrawLoadingBitmap && mGifLoadingDrawable != null) {
                LOGD("===========================draw loading image :"+ mLoadingDegree);
                int loadingDrawableWidth = mGifLoadingDrawable.getIntrinsicWidth();
                int loadingDrawbleHeight = mGifLoadingDrawable.getIntrinsicHeight();
                if (loadingDrawableWidth > widthWithoutPadding) {
                    loadingDrawableWidth = widthWithoutPadding;
                }
                if (loadingDrawbleHeight > heightWithoutPadding) {
                    loadingDrawbleHeight = heightWithoutPadding;
                }
                mGifLoadingDrawable.setBounds(
                        getPaddingLeft() + (widthWithoutPadding - loadingDrawableWidth) / 2,
                        getPaddingTop() + (heightWithoutPadding - loadingDrawbleHeight) / 2,
                        getPaddingLeft() + (widthWithoutPadding + loadingDrawableWidth) / 2,
                        getPaddingTop() + (heightWithoutPadding + loadingDrawbleHeight) / 2);
                canvas.save();
                canvas.rotate(mLoadingDegree, getPaddingLeft() + widthWithoutPadding / 2, getPaddingTop() + heightWithoutPadding / 2);
                mGifLoadingDrawable.draw(canvas);
                canvas.restore();
            }
            return;
        }
        if (mGifDecoder == null || mGifDecoder.mWidth <= 0 || mGifDecoder.mHeight <= 0) {
            return;
        }
        drawGifImage(canvas, mGifDecoder.mWidth, mGifDecoder.mHeight);
    }

    protected void drawGifImage(Canvas canvas, int gifWidth, int gifHeight) {
        if(mCurrentImage == null || mCurrentImage.isRecycled()){
            return;
        }
        if (gifWidth <= 0 || gifHeight <= 0) {
            return;
        }
        int saveCount = canvas.getSaveCount();
        canvas.save();
        canvas.translate(getPaddingLeft(), getPaddingTop());
        if (mGifRect == null) {
            mGifRect = new Rect();
        }
        int realWidth = 0, realHeight = 0;
        int widthWithoutPadding = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        int heightWithoutPadding = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
        switch (mGifScaleType) {
            case GIF_SCALE_TYPE_CENTER:
                realWidth = gifWidth;
                realHeight = gifHeight;
                if (gifWidth > widthWithoutPadding) {
                    realWidth = widthWithoutPadding;
                    realHeight = gifHeight * widthWithoutPadding / gifWidth;
                }
                if (realHeight > heightWithoutPadding) {
                    realHeight = heightWithoutPadding;
                    realWidth = gifWidth * heightWithoutPadding / gifHeight;
                }
                break;
            case GIF_SCALE_TYPE_FIT_CENTER:
                if (gifWidth > gifHeight) {
                    realWidth = widthWithoutPadding;
                    realHeight = gifHeight * widthWithoutPadding / gifWidth;
                } else if (gifWidth < gifHeight) {
                    realWidth = gifWidth * heightWithoutPadding / gifHeight;
                    realHeight = heightWithoutPadding;
                } else {
                    if (widthWithoutPadding > heightWithoutPadding) {
                        realWidth = realHeight = heightWithoutPadding;
                    } else {
                        realWidth = realHeight = widthWithoutPadding;
                    }
                }
                break;
            case GIF_SCALE_TYPE_FITXY:
                realWidth = widthWithoutPadding;
                realHeight = heightWithoutPadding;
                break;
            case GIF_SCALE_TYPE_FIT_WIDTH:
                realWidth = widthWithoutPadding;
                realHeight = gifHeight * widthWithoutPadding / gifWidth;
                break;
            case GIF_SCALE_TYPE_FIT_HEIGHT:
                realWidth = gifWidth * heightWithoutPadding / gifHeight;
                realHeight = heightWithoutPadding;
                break;
        }
        mGifRect.left = (widthWithoutPadding - realWidth) / 2;
        mGifRect.top = (heightWithoutPadding - realHeight) / 2;
        mGifRect.right = mGifRect.left + realWidth;
        mGifRect.bottom = mGifRect.top + realHeight;
        if (!mIsRoundImage) {
            if (mCurrentDrawable == null || mUpdateDrawable)
                mCurrentDrawable = new BitmapDrawable(getContext().getResources(), mCurrentImage);
            if (mCheckDarkMode) {
                mCurrentDrawable = GifCommonUtil.checkDarkMode(mCurrentDrawable, mDarkModeOn);
            }
            mCurrentDrawable.setBounds(mGifRect);
            mCurrentDrawable.draw(canvas);
        } else {
            if (mRoundDrawable == null || mUpdateDrawable)
                mRoundDrawable = RoundedBitmapDrawableFactory.create(getResources(), mCurrentImage);
            mRoundDrawable.setCornerRadius(mRoundedCircle);
            if (mCheckDarkMode) {
                mRoundDrawable = (RoundedBitmapDrawable) GifCommonUtil.checkDarkMode(mRoundDrawable, mDarkModeOn);
            }
            mRoundDrawable.setBounds(mGifRect);
            mRoundDrawable.draw(canvas);

        }
        canvas.restoreToCount(saveCount);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //TODO
    }

    private GifAction mGifAction = new GifAction() {
        @Override
        public void parseOk(boolean parseStatus, int frameIndex) {
            LOGD("===================== parse OK +++++++++ parseStatus: "+ parseStatus + " frameIndex: "+frameIndex);
            if (!parseStatus || (mGifDecoder == null)) {
                stopDrawLoading();
                Message msg = redrawHandler.obtainMessage();
                msg.what = MSG_GIF_STOP_ACTION;
                msg.obj = false;
                redrawHandler.sendMessage(msg);
                isRun = false;
                return;
            }
            switch (animationType) {
                case LARGE_MODE_ONCE:
                case LARGE_MODE_LOOP:
                    stopDrawLoading();
                    if (mGifStartListener != null)
                        mGifStartListener.onGifStart(true);
                    if (frameIndex > 0) {
                        if (drawThread == null) {
                            drawThread = new DrawThread();
                            drawThread.setName(DRAWTHREAD_TAG);
                            drawThread.start();
                        }
                    } else {
                        decodeFinish = true;
                    }
                    break;
                case WAIT_FINISH:
                    if (frameIndex == -1) {
                        stopDrawLoading();
                        decodeFinish = true;
                        if (mGifDecoder != null && mGifDecoder.getFrameCount() > 1) {
                            if (drawThread == null) {
                                drawThread = new DrawThread();
                                drawThread.setName(DRAWTHREAD_TAG);
                                drawThread.start();
                            }
                        } else {
                            reDraw();
                        }
                    }
                    break;
                case COVER:
                    if (frameIndex == 1) {
                        stopDrawLoading();
                        mCurrentImage = mGifDecoder.getImage();
                        mUpdateDrawable = true;
                        reDraw();
                    } else if (frameIndex == -1) {
                        decodeFinish = true;
                        if (mGifDecoder.getFrameCount() > 1) {
                            if (drawThread == null) {
                                drawThread = new DrawThread();
                                drawThread.setName(DRAWTHREAD_TAG);
                                drawThread.start();
                            }
                        } else {
                            reDraw();
                        }
                    }
                    break;
                case SYNC_DECODER:
                    if (frameIndex == 1) {
                        stopDrawLoading();
                        mCurrentImage = mGifDecoder.getImage();
                        mUpdateDrawable = true;
                        reDraw();
                    } else if (frameIndex == -1) {
                        decodeFinish = true;
                        reDraw();
                    } else {
                        if (drawThread == null) {
                            drawThread = new DrawThread();
                            drawThread.setName(DRAWTHREAD_TAG);
                            drawThread.start();
                        }
                    }
                    break;
            }
        }
    };

    private void reDraw(){
        if(redrawHandler != null){
            Message msg = redrawHandler.obtainMessage();
            msg.what = MSG_INVALIDATE_VIEW;
            redrawHandler.sendMessage(msg);
        }
    }

    private void startDrawLoading() {
        LOGD("+++++++++++++++++++startDrawLoading++++++++");
        mLoadingDegree = 0;
        mGifLoading = true;
        if (mLoadingDrawThread == null) {
            mLoadingDrawThread = new LoadingDrawThread();
            mLoadingDrawThread.start();
        }
    }

    private void stopDrawLoading() {
        LOGD("+++++++++++++++++++stopDrawLoading++++++++");
        mLoadingDegree = 0;
        mGifLoading = false;
        if (mLoadingDrawThread != null) {
            mLoadingDrawThread.interrupt();
            mLoadingDrawThread = null;
        }
    }

    private Handler redrawHandler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_INVALIDATE_VIEW:
                    invalidate();
                    break;
                case MSG_GIF_STOP_ACTION:
                    boolean status = (boolean)msg.obj;
                    recycle();
                    if (mGifStopListener != null) {
                        mGifStopListener.onGifStop(status);
                    }
                    break;
            }
        }
    };


    private class LoadingDrawThread extends Thread {
        public void run() {
            try {
                Thread.sleep(LOADING_START_TIME_INTERVAL);
                mGifLoading = true;
                while(redrawHandler != null && mGifLoading){
                    LOGD("+++++++++++++++++++LoadingDrawThread++++++++"+ mLoadingDegree);
                    mLoadingDegree = (mLoadingDegree + 30) % 360 ;
                    Message msg = redrawHandler.obtainMessage();
                    msg.what = MSG_INVALIDATE_VIEW;
                    redrawHandler.sendMessage(msg);
                    Thread.sleep(LOADING_TIME_INTERVAL);
                }
            } catch (Exception e) {
                // TODO: handle exception
            }

        }
    }

    private class DrawThread extends Thread{
        public void run() {
            try {
                int frameIndex = 0;
                int loopedTimes = 0;
                while(isRun) {
                    if (mGifDecoder == null) return;
                    if(!pause){
                        GifFrame frame = mGifDecoder.next();
                        if (frame != null) {
                            mCurrentImage = frame.image;
                            frameIndex++;
                            mUpdateDrawable = true;
                            long sp = frame.delay;
                            if(redrawHandler != null){
                                Message msg = redrawHandler.obtainMessage();
                                msg.what = MSG_INVALIDATE_VIEW;
                                redrawHandler.sendMessage(msg);
                                if (mRecyclePrevious) {
                                    int freeIndex = frameIndex - 1;
                                    if (freeIndex >= 0)
                                        recycleFrameIndex(freeIndex);
                                }
                                if (sp > 0)
                                    Thread.sleep(sp);
                                //重新下一轮播放，先播放下一轮的第一帧，然后暂停一段时间
                                if (mGifLoopTimes > 1 && mGifLoopInterval > 0) {
                                    int getLoopedTimes = mGifDecoder.getLoopedTimes();
                                    if (getLoopedTimes > loopedTimes) {
                                        loopedTimes = getLoopedTimes;
                                        Thread.sleep(mGifLoopInterval);
                                    }
                                }
                            } else {
                                break;
                            }
                        } else {
                            if (!isRun) {
                                Message msg = redrawHandler.obtainMessage();
                                msg.what = MSG_GIF_STOP_ACTION;
                                msg.obj = false;
                                redrawHandler.sendMessage(msg);
                                break;
                            } else {
                                if (decodeFinish && mGifLoopTimes > 0 && mGifDecoder.getLoopedTimes() >= mGifLoopTimes) {
                                    Message msg = redrawHandler.obtainMessage();
                                    msg.what = MSG_GIF_STOP_ACTION;
                                    msg.obj = true;
                                    redrawHandler.sendMessage(msg);
                                    isRun = false;
                                    break;
                                } else if (decodeFinish && (mGifLoopTimes < 0 || mGifDecoder.getLoopedTimes() < mGifLoopTimes)) {
                                    decodeFinish = false;
                                    frameIndex = 0;
                                }
                            }
                            Thread.sleep(10);
                        }
                    } else {
                        Thread.sleep(10);
                    }
                }
                Message msg = redrawHandler.obtainMessage();
                msg.what = MSG_GIF_STOP_ACTION;
                msg.obj = false;
                redrawHandler.sendMessage(msg);
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

    public void recycleFrameIndex(int frameIndex) {
        if (mGifDecoder != null) {
            mGifDecoder.recycleBitmap(frameIndex);
        }
    }

    public void recycle() {
        mIsGifImage = false;
        reDraw();
        if (mGifDecoder != null) {
            mGifDecoder.mStopped = true;
            try {
                mGifDecoder.interrupt();
                mGifDecoder.free();
                mGifDecoder = null;
            } catch (Exception e) {}

        }
        if (drawThread != null) {
            try {
                drawThread.interrupt();
                drawThread = null;
            } catch (Exception e){}
        }
        if (mLoadingDrawThread != null) {
            try {
                mLoadingDrawThread.interrupt();
                mLoadingDrawThread = null;
            } catch (Exception e) {}
        }
        mCurrentImage = null;
        mCurrentDrawable = null;
        mRoundDrawable = null;
    }

    private void LOGD(String str) {
        if (DEBUG) {
            Log.d(TAG, str);
        }
    }
}
