package com.dw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by dw on 18-1-12.
 */

public class GestureView extends View {

    GestureDetector mGestureDetector;

    public GestureView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                Log.d("xx","onSingleTapUp  e="+e);
                return super.onSingleTapUp(e);
            }

            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
                Log.d("xx","onLongPress  e="+e);
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                Log.d("xx","onScroll  e1="+e1+" e2="+e2+" distanceX="+distanceX+" distanceY="+distanceY);
                return super.onScroll(e1, e2, distanceX, distanceY);
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                Log.d("xx","onFling  e1="+e1+" e2="+e2+" distanceX="+velocityX+" distanceY="+velocityY);
                return super.onFling(e1, e2, velocityX, velocityY);
            }

            @Override
            public void onShowPress(MotionEvent e) {
                super.onShowPress(e);
                Log.d("xx","onShowPress  e="+e);
            }

            @Override
            public boolean onDown(MotionEvent e) {
                Log.d("xx","onDown  e="+e);
                return super.onDown(e);
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                Log.d("xx","onDoubleTap  e="+e);
                return super.onDoubleTap(e);
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                Log.d("xx","onDoubleTapEvent  e="+e);
                return super.onDoubleTapEvent(e);
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                Log.d("xx","onSingleTapConfirmed  e="+e);
                return super.onSingleTapConfirmed(e);
            }

            @Override
            public boolean onContextClick(MotionEvent e) {
                Log.d("xx","onContextClick  e="+e);
                return super.onContextClick(e);
            }
        });
        setOverScrollMode(View.OVER_SCROLL_ALWAYS);

    }

    public void onDraw(Canvas canvas){
        canvas.drawColor(0x55888888);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(ev);
    }

    public boolean onTouchEvent(MotionEvent event){
        Log.d("xx","onTouchEvent =="+event.getAction());
        mGestureDetector.onTouchEvent(event);
        return true;
    }
}
