package com.dw.gif;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;

public class MultiGifImageView extends BaseGifImageView {
    
    private static final String TAG = "MultiGifImageView";
    private int mRadius;
    private Rect mBounds = new Rect();
    private RectF mBoundsF = new RectF();
    private Path mPath = new Path();

    private GifTimerPool.TimeListener mTimeListener = null;
    private GifTimerPool mGifTimerPool = null;

    private GifImage mGifImage;

    private int mTimer = 0;
    private int mSpace = 0;
    private boolean mHasDrawnGif = false;

    public MultiGifImageView(Context context) {
        super(context);
        init();
    }

    public MultiGifImageView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        init();
    }

    public MultiGifImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @Override
    protected void init() {
        super.init();
        mDrawLoadingBitmap = false;
        mTimeListener = new GifTimerPool.TimeListener() {
            @Override
            public void onTimeChange(int time) {
                loop(time);
            }
            @Override
            public boolean isViewShown() {
                return isShown();
            }

            @Override
            public void startLoad() {
                if(isShown() && mGifImage != null)
                    mGifImage.startDecoder(mGifTimerPool);
            }
        };
    }

    @Override
    public void setGifImage(int resId) {
        setImageDrawable(null);
        if (mGifDecoder != null) {
            mGifDecoder.free();
        }
        mGifImage = new GifImage(getContext(), resId);
        if (mGifTimerPool != null) {
            mGifTimerPool.addTimeListener(mTimeListener);
        }
        invalidate();
    }

    public void setGifImage(GifImage gifImage) {
        setImageDrawable(null);
        if (mGifImage != null) {
            mGifImage.recycle();
        }
        if(gifImage != null) {
            mGifImage = gifImage;
            mIsGifImage = true;
            if(mGifTimerPool != null)
                mGifTimerPool.addTimeListener(mTimeListener);
        } else {
            mIsGifImage = false;
        }
        invalidate();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        recycle();
        super.setImageDrawable(drawable);
    }

    public void setGifTimerPool(GifTimerPool pool) {
        mGifTimerPool = pool;
    }

    public void setRoundCorner(int r) {
        mRadius = r;
    }

    protected void onDraw(Canvas canvas) {
        if(mRadius > 0) {
            canvas.getClipBounds(mBounds);
            mBoundsF.set(mBounds.left, mBounds.top, mBounds.right, mBounds.bottom);
            mPath.reset();
            mPath.addRoundRect(mBoundsF, mRadius, mRadius, Path.Direction.CCW);
            canvas.clipPath(mPath);
        }
        if (!mIsGifImage) {
            super.onDraw(canvas);
            return;
        }
        canvas.setDrawFilter(mPaintFlagsDrawFilter);
        if (mGifImage == null || !mGifImage.ismLoadingComplate()) {
            return;
        }
        drawGifImage(canvas, mGifImage.mWidth, mGifImage.mHeight);
        mHasDrawnGif = true;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private Handler redrawHandler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BaseGifImageView.MSG_REDRAW:
                    if(mSpace > mTimer) return;
                    if(mGifImage == null) return;
                    GifFrame frame = mGifImage.next();
                    if (frame != null && mGifImage.ismLoadingComplate() && mHasDrawnGif) {
                        mCurrentImage = frame.image;
                        mCurrentDrawable = null;
                        mSpace = frame.delay;
                        mTimer = 0;
                        mHasDrawnGif = false;
                        invalidate();
                    }
                    break;
            }
        }
    };

    public void recycle() {
        super.recycle();
        if(mGifTimerPool != null)
            mGifTimerPool.removeTimeListener(mTimeListener);
        mGifImage = null;
        mSpace = 0;
        mTimer = 0;
        mHasDrawnGif = true;
    }

    private void loop(int time) {
        mTimer += time;
        if (redrawHandler != null) {
            Message msg = redrawHandler.obtainMessage(BaseGifImageView.MSG_REDRAW);
            redrawHandler.removeMessages(BaseGifImageView.MSG_REDRAW);
            redrawHandler.sendMessage(msg);
        } else {
            return;
        }
    }
}