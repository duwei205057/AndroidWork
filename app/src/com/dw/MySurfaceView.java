package com.dw;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by dw on 17-11-26.
 */

public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback{

    SurfaceHolder sh;
    final int BALLWIDTH = 50;
    Paint mPaint;
    int offSetX;
    int offSetY;
    boolean isRunning;

    public MySurfaceView(Context context) {
        super(context);
    }

    public MySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        sh = getHolder();
        sh.addCallback(this);
        mPaint = new Paint();
        mPaint.setColor(0xFFFF0000);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("xx","surfaceCreated!!!!!");
        isRunning = true;
//        Canvas canvas = sh.lockCanvas();
//        canvas.drawColor(Color.GRAY);
//        sh.unlockCanvasAndPost(canvas);
//        canvas = sh.lockCanvas(new Rect(0,0,0,0));
//        sh.unlockCanvasAndPost(canvas);
//        canvas = sh.lockCanvas(new Rect(0,0,0,0));
//        sh.unlockCanvasAndPost(canvas);
        post(drawRun);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("xx","surfaceDestroyed!!!!!");
        isRunning = false;
    }

    Runnable drawRun = new Runnable() {
        @Override
        public void run() {
            PropertyValuesHolder pvhX = PropertyValuesHolder.ofInt("x",0,800);
            PropertyValuesHolder pvhY = PropertyValuesHolder.ofInt("y",0,800);
            ValueAnimator va = ValueAnimator.ofPropertyValuesHolder(pvhX,pvhY);
            va.setDuration(2000);
            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if(!isRunning) animation.cancel();
                    int x = (int)animation.getAnimatedValue("x");
                    int y = (int)animation.getAnimatedValue("y");
//                    Log.d("xx","x="+x+" y="+y);
                    Canvas canvas = sh.lockCanvas(/*new Rect(x + offSetX - BALLWIDTH,y + offSetY - BALLWIDTH,x + offSetX + BALLWIDTH,y + offSetY + BALLWIDTH)*/);
                    if(canvas != null){
                        canvas.drawColor(Color.GRAY);
//                    canvas.drawOval(x,y,x + BALLWIDTH ,y + BALLWIDTH ,mPaint);
                        canvas.drawCircle(x + offSetX,y + offSetY,BALLWIDTH,mPaint);
                        sh.unlockCanvasAndPost(canvas);
                    }
                }
            });

            va.start();
//            if(isRunning) postDelayed(drawRun,2500);
        }
    };

    public boolean onTouchEvent(MotionEvent event){
        if(event.getActionMasked() == MotionEvent.ACTION_DOWN){
            offSetX = (int)event.getX();
            offSetY = (int)event.getY();
            post(drawRun);
        }
        return true;
    }


}
