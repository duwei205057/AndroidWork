package com.dw;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * Created by dw on 17-11-26.
 */

public class MoveView extends View {

    int lastX = -1;
    int lastY = -1;
    Scroller mScroller;
    Paint mPaint;
    Bitmap dark;
    Bitmap flx;
    int curMode = -1;
    Path mPath;
    PorterDuff.Mode[] modes = {PorterDuff.Mode.CLEAR,/** [0, 0] */
            /** [Sa, Sc] */
            PorterDuff.Mode.SRC,
            /** [Da, Dc] */
            PorterDuff.Mode.DST,
            /** [Sa + (1 - Sa)*Da, Rc = Sc + (1 - Sa)*Dc] */
            PorterDuff.Mode.SRC_OVER,
            /** [Sa + (1 - Sa)*Da, Rc = Dc + (1 - Da)*Sc] */
            PorterDuff.Mode.DST_OVER,
            /** [Sa * Da, Sc * Da] */
            PorterDuff.Mode.SRC_IN,
            /** [Sa * Da, Sa * Dc] */
            PorterDuff.Mode.DST_IN,
            /** [Sa * (1 - Da), Sc * (1 - Da)] */
            PorterDuff.Mode.SRC_OUT,
            /** [Da * (1 - Sa), Dc * (1 - Sa)] */
            PorterDuff.Mode.DST_OUT,
            /** [Da, Sc * Da + (1 - Sa) * Dc] */
            PorterDuff.Mode.SRC_ATOP,
            /** [Sa, Sa * Dc + Sc * (1 - Da)] */
            PorterDuff.Mode.DST_ATOP,
            /** [Sa + Da - 2 * Sa * Da, Sc * (1 - Da) + (1 - Sa) * Dc] */
            PorterDuff.Mode.XOR,
            /** [Sa + Da - Sa*Da,
            Sc*(1 - Da) + Dc*(1 - Sa) + min(Sc, Dc)] */
            PorterDuff.Mode.DARKEN,
            /** [Sa + Da - Sa*Da,
            Sc*(1 - Da) + Dc*(1 - Sa) + max(Sc, Dc)] */
            PorterDuff.Mode.LIGHTEN,
            /** [Sa * Da, Sc * Dc] */
            PorterDuff.Mode.MULTIPLY,
            /** [Sa + Da - Sa * Da, Sc + Dc - Sc * Dc] */
            PorterDuff.Mode.SCREEN,
            /** Saturate(S + D) */
            PorterDuff.Mode.ADD,
            PorterDuff.Mode.OVERLAY};

    public MoveView(Context context) {
        super(context);
    }

    public MoveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Interpolator itp = new Interpolator() {
            @Override
            public float getInterpolation(float input) {
                return input * input;
            }
        };
        mScroller = new Scroller(context,itp);
        mPaint = new Paint();
//        mPaint.setColor(0xFF00FF00);
//        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        dark = BitmapFactory.decodeResource(context.getResources(),R.drawable.porterduff_darkmode);
        flx = BitmapFactory.decodeResource(context.getResources(),R.drawable.porterduff_fanlingxi);

        mPath = new Path();
        mPath.addCircle(200,200,80, Path.Direction.CW);
        mPaint = new Paint();

        Path path = new Path();
        path.addCircle(200,200,50, Path.Direction.CW);
        mPath.op(path, Path.Op.DIFFERENCE);
        mPath.addArc(200,50,300,150,0,90);
    }

    public void onDraw(Canvas canvas){
        Log.d("xx", "MoveView  onDraw: ");
//        canvas.drawColor(0xFFFF0000);
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();
        //将绘制操作保存到新的图层，因为图像合成是很昂贵的操作，将用到硬件加速，这里将图像合成的处理放到离屏缓存中进行
        int layerId = canvas.saveLayer(0, 0, canvasWidth, canvasHeight, null, Canvas.ALL_SAVE_FLAG);
        mPaint.setColor(0xFF00FF00);
        canvas.drawCircle(50,50,20,mPaint);
//        Rect dst = new Rect(20,20,100,100);
//        canvas.drawBitmap(dark,null,dst,mPaint);
//        mPaint.setColor(0xFF0000FF);
//        curMode = (curMode + 1) % modes.length;
        curMode = 7;
        mPaint.setXfermode(new PorterDuffXfermode(modes[curMode]));
//        Log.d("xx","modes[curMode]=="+modes[curMode].ordinal()+" layerId="+layerId);
        mPaint.setColor(0xFF0000FF);
        canvas.drawRect(50,50,100,100,mPaint);
        canvas.saveLayerAlpha(0, 0, canvasWidth, canvasHeight, 0x55, Canvas.ALL_SAVE_FLAG);
        mPaint.setColor(Color.YELLOW);
        canvas.drawCircle(70,50,20,mPaint);
//        canvas.drawBitmap(flx,null,dst,mPaint);
        mPaint.setXfermode(null);
        canvas.restoreToCount(layerId);

        mPaint.setColor(0x55FF0000);
        canvas.drawRect(200,200,300,550,mPaint);
        mPaint.setColor(0xFFFF0000);
//        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawArc(200,200,300,550,0,90,true,mPaint);
        mPaint.setColor(0xFF000000);
        mPath.setFillType(Path.FillType.INVERSE_WINDING);
        canvas.drawPath(mPath, mPaint);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        int action = event.getActionMasked();
        int rawX = (int)event.getRawX();
        int rawY = (int)event.getRawY();
        switch(action){
            case MotionEvent.ACTION_DOWN:
                lastX = rawX;
                lastY = rawY;
                break;
            case MotionEvent.ACTION_MOVE:
                int offsetX = rawX - lastX;
                int offsetY = rawY - lastY;
//                        layout(getLeft() + offsetX,getTop() + offsetY,getRight() + offsetX,getBottom() + offsetY);
                ((View)getParent()).scrollBy(-offsetX,-offsetY);
                lastX = rawX;
                lastY = rawY;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                int scrollX = ((View)getParent()).getScrollX();
                int scrollY = ((View)getParent()).getScrollY();
                mScroller.startScroll(scrollX,scrollY,-scrollX,-scrollY,2000);
                lastX = -1;
                lastY = -1;
                invalidate();
                break;

        }
        return true;
    }

    @Override
    public void computeScroll(){
        super.computeScroll();
        if(mScroller.computeScrollOffset()){
            ((View)getParent()).scrollTo(mScroller.getCurrX(),mScroller.getCurrY());
            invalidate();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.d("xx", "MoveView  onSizeChanged: w="+w+" h="+h+" oldw="+oldw+" oldh="+oldh);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        Log.d("xx", "MoveView  onSizeChanged: left="+left+" top="+top+" right="+right+" bottom="+bottom);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.d("xx", "MoveView  onMeasure: ");
    }
}
