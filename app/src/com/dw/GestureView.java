package com.dw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

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

    List<Path> mPathList = new ArrayList<>();
    Path mPath = new Path();
    Paint mPaint = new Paint();
    {
        mPaint.setColor(0x33FF0000);
        mPaint.setStrokeWidth(20);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        setLayerType(LAYER_TYPE_SOFTWARE, null);//// 使用PorterDuff.Mode,最好禁止硬件加速，硬件加速会有一些问题，这里禁用掉
    }

    public void onDraw(Canvas canvas){
        canvas.drawColor(0x55888888);
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();
//        int layerId = canvas.saveLayer(0, 0, canvasWidth, canvasHeight, null, Canvas.ALL_SAVE_FLAG);
//        mPathList.clear();
//        Path pa = new Path();
//        pa.addRect(100, 100, 300, 300, Path.Direction.CW);
//        mPathList.add(pa);
//        pa = new Path();
//        pa.addRect(200, 200, 400, 400, Path.Direction.CW);
//        mPathList.add(pa);
        for(Path p : mPathList) {
//
            canvas.drawPath(p, mPaint);
        }
//        canvas.restoreToCount(layerId);

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(ev);
    }


    public boolean onTouchEvent(MotionEvent event) {
        Log.d("xx", "onTouchEvent ==" + event.getAction());
        mGestureDetector.onTouchEvent(event);
        float x = event.getX();
        float y = event.getY();
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mPath = new Path();
                mPathList.add(mPath);
                mPath.moveTo(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                mPath.lineTo(x, y);
                mPath.close();
                mPath = new Path();
                mPathList.add(mPath);
                mPath.moveTo(x, y);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mPath.lineTo(x, y);
                mPath.close();
                break;
        }
        invalidate();
        return true;
    }
}
