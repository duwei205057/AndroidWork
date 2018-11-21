package com.dw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by dw on 17-12-1.
 */

public class MyView extends ViewGroup {

    int color;
    String tag;

    public void setItem(OnTouchListen item) {
        this.item = item;
    }

    OnTouchListen item;
    public MyView(Context context,int color,String tag) {
        super(context);
        this.color = color;
        this.tag = tag;
    }


    public void onDraw(Canvas canvas){
        canvas.drawARGB(0x55, Color.red(color),Color.green(color),Color.blue(color));
    }

    public boolean onTouchEvent(MotionEvent e){
        Log.d("xx"," tag =" +tag+" x="+e.getX()+" y="+e.getY()+" action="+e.getActionMasked());
        if(item != null) item.getTouch(e,this);
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    interface OnTouchListen{
        void getTouch(MotionEvent e,View v);
    }
}
