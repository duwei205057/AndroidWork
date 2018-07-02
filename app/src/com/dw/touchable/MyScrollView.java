package com.dw.touchable;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.OverScroller;
import android.widget.RelativeLayout;

import com.dw.R;

import static android.view.ViewGroup.FOCUS_AFTER_DESCENDANTS;

/**
 * Created by dw on 18-1-16.
 */

public class MyScrollView extends ViewGroup {

    private int measureWidth;
    private int measureHeight;
    private int mBgColor;
    private OverScroller mScroller;
    private int mTouchSlop;
    private int mMinimumVelocity;
    private int mMaximumVelocity;

    private int mOverscrollDistance;
    private int mOverflingDistance;
    private boolean mIsBeingDragged = false;
    private String mTag;

    private Context mContext;

    private int mLastMotionX = -1;
    private int mLastMotionY = -1;

    private Paint mPaint;

    public MyScrollView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.scrollerStyle);
        measureWidth = typedArray.getDimensionPixelSize(R.styleable.scrollerStyle_width, 0);
        measureHeight = typedArray.getDimensionPixelSize(R.styleable.scrollerStyle_height, 0);
        mBgColor = typedArray.getColor(R.styleable.scrollerStyle_bgColor, 0xFFFFFFFF);
        mTag = typedArray.getString(R.styleable.scrollerStyle_tag);
        typedArray.recycle();
//        setBackground(context.getResources().getDrawable(R.drawable.bg));
        initView();
    }

    private void initView() {
        mScroller = new OverScroller(mContext);
        setFocusable(true);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        setWillNotDraw(false);
        final ViewConfiguration configuration = ViewConfiguration.get(mContext);
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        mOverscrollDistance = configuration.getScaledOverscrollDistance();
        mOverflingDistance = configuration.getScaledOverflingDistance();
        mPaint = new Paint();
        mPaint.setTextSize(50);
    }


    @Override
    protected int computeHorizontalScrollRange() {
        return super.computeHorizontalScrollRange();
    }

    @Override
    protected int computeVerticalScrollRange() {
        return measureHeight;
    }

    protected int computeVerticalScrollOffset() {
        return Math.max(0, super.computeVerticalScrollOffset());
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int left = 0;
        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            int w = child.getMeasuredWidth();
            int h = child.getMeasuredHeight();
            child.layout(left + 200, 300, left + 200 + w, 300 + h);
//            child.layout(left + l, t, r + left, b);
            left += child.getMeasuredWidth();
        }
    }

    public void onDraw(Canvas canvas) {
        canvas.drawColor(mBgColor);
        int start = 0;
        int step = 100;
        int num = measureHeight / step;
        for (int i = 0; i < num ;i ++){
            canvas.drawText(String.valueOf(i), step, start + step * i, mPaint);
        }

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        LOGD("dispatchTouchEvent  ev="+ev.getAction());
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        LOGD("onInterceptTouchEvent  ev=" + ev.getAction());
        if ((action == MotionEvent.ACTION_MOVE)/* && mIsBeingDragged*/) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        LOGD("onTouchEvent  ev=" + ev.getAction()+" ev.getY()="+ev.getY()+" mLastMotionY="+mLastMotionY);
        int action = ev.getAction() & MotionEvent.ACTION_MASK;
        int currentX = (int)ev.getX();
        int currentY = (int)ev.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionX = currentX;
                mLastMotionY = currentY;
                requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                int dx = mLastMotionY - currentY;
                if(canScrollVertically(dx) && mLastMotionY != -1)
                    scrollBy(0, dx);
                else
                    requestDisallowInterceptTouchEvent(false);
                mLastMotionX = currentX;
                mLastMotionY = currentY;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mLastMotionX = -1;
                mLastMotionY = -1;
                break;
        }
        return true;
    }

    private void LOGD(String message) {
        Log.d("xx", message+" mTag=" + mTag);
    }
}
