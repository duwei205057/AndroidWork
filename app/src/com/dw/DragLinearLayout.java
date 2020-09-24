package com.dw;

import android.content.Context;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * Created by @author dw
 * Date : 20-5-20
 */
public class DragLinearLayout extends LinearLayout {


    private ViewDragHelper mDragger;


    public DragLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDragger = ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback() {
            @Override
            public int getViewHorizontalDragRange(View child) {
                //返回可拖动的子视图的水平运动范围(以像素为单位)的大小。
                //对于不能垂直移动的视图，此方法应该返回0。
                return getMeasuredWidth() - child.getMeasuredWidth();
            }

            @Override
            public int getViewVerticalDragRange(View child) {
                //返回可拖动的子视图的竖直运动范围(以像素为单位)的大小。
                //对于不能垂直移动的视图，此方法应该返回0。
                return getMeasuredHeight() - child.getMeasuredHeight();
            }

            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                //返回true表view示捕获当前touch到的
                return true;
            }

            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                if (left > getWidth() - child.getMeasuredWidth()){
                    //超出左侧边界处理
                    left = getWidth() - child.getMeasuredWidth();
                } else if (left < 0) {
                    //超出右侧边界处理
                    left = 0;
                }
                return left;
            }

            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                if (top > getHeight() - child.getMeasuredHeight()){
                    //超出下边界处理
                    top = getHeight() - child.getMeasuredHeight();
                } else if (top < 0) {
                    //超出上边界处理
                    top = 0;
                }
                return top;
            }

        });
        mDragger.setEdgeTrackingEnabled(ViewDragHelper.EDGE_LEFT);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        //允许拖拽时,放开下面的注释
//        getParent().requestDisallowInterceptTouchEvent(true);
        return mDragger.shouldInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDragger.processTouchEvent(event);
        return true;
    }

    @Override
    public void computeScroll() {
        if (mDragger.continueSettling(true)) {
            invalidate();
        }
    }
}
