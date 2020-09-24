package com.dw.voice;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RotateDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;

import com.aop.DebugTrace;
import com.dw.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by dw on 18-2-1.
 */

public class VoiceView extends View {

    private SparseArray<Rect> mRectHashMap = new SparseArray<Rect>();
    private int                 mContentWidth;
    private int                 mContentHeight;

    private int mSelectedItemIndex = -1;
    private static final int INDEX_BACKSPACE = 0;
    private static final int INDEX_VOICE_AREA = 1;

    private int                 mBackgroundColor;

    private Drawable mUpShadow;
    private Drawable mDrawableCircle;
    private Drawable mDrawableMic;
    private Drawable mDrawableRecognizing;

    private Rect mCenterRect = new Rect();

    private volatile  int       mState               = STATE_DEFAULT;

    public static final int     STATE_DEFAULT         = 0;
    public static final int     STATE_SPEAKING        = 1;
    public static final int     STATE_RECOGNIZING     = 2;
    public static final int     STATE_TIP             = 3;

    Paint mAlphaPaint;
    Transformation mTransformation;
    Animation mAm1;
    Animation mAm2;
    Animation mAm3;
    Context mContext;

    private int                 mLevelCircle          = 0;

    //TODO remove later
    public final static int[] KEY_STATE_PRESSED = {
            android.R.attr.state_pressed
    };

    public final static int[] KEY_STATE_NORMAL = {
            android.R.attr.state_enabled
    };

    public VoiceView(Context context) {
        this(context, null);
    }

    public VoiceView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setClickable(true);
        initView();
    }

    private void initView(){
        mAm1 = AnimationUtils.loadAnimation(mContext, R.anim.voiceinput_speaking_anim);
        mAm1.setFillAfter(true);
        mAm2 = AnimationUtils.loadAnimation(mContext,R.anim.voiceinput_speaking_anim);
        mAm3 = AnimationUtils.loadAnimation(mContext,R.anim.voiceinput_speaking_anim);
        mDrawableCircle = mContext.getResources().getDrawable(R.drawable.voiceinput_circle);
        mDrawableRecognizing = mContext.getResources().getDrawable(R.drawable.voiceinput_recognizing);
        mDrawableMic = mContext.getResources().getDrawable(R.drawable.voiceinput_mic);
//        mCircleIntrinsicWidth = mDrawableCircle.getIntrinsicWidth();
//        mCircleIntrinsicHeight = mDrawableCircle.getIntrinsicHeight();
        mTransformation = new Transformation();
        mAlphaPaint = new Paint();
    }

    @Override
    public void onDraw(Canvas canvas){
        drawBackground(canvas);
        drawArea(canvas);
        mAlphaPaint.setColor(Color.RED);
        canvas.drawLine(0,mCenterRect.top,mContentWidth,mCenterRect.top,mAlphaPaint);
        canvas.drawLine(mCenterRect.left,0,mCenterRect.left,mContentHeight,mAlphaPaint);
        canvas.drawLine(0,mCenterRect.top + 152,mContentWidth,mCenterRect.top + 152,mAlphaPaint);
    }

    private void drawArea(Canvas canvas){
        /*public static final int     STATE_DEFAULT         = 0;
        public static final int     STATE_SPEAKING        = 1;
        public static final int     STATE_RECOGNIZING     = 2;
        public static final int     STATE_TIP             = 3;
        *//** finish sliding up, user can choose speech type, or use offline recognition *//*
        public static final int     STATE_FUNC            = 4;
        public static final int     STATE_SEAMLESS        = 5;
        public static final int     STATE_OFFLINE_INITIAL = 6;
        public static final int     STATE_OFFLINE_SPEAKING = 7;
        public static final int     STATE_OFFLINE_RECOGNIZING = 8;
        public static final int     STATE_SAVEING_VOICE_FILE = 9;*/
        switch(mState){
            case STATE_DEFAULT:
                drawDrawable(canvas,mDrawableCircle,getBgState(0),mCenterRect);
                drawDrawable(canvas,mDrawableMic,getBgState(0),mCenterRect,false);
                break;
            case STATE_SPEAKING:
                drawDrawable(canvas,mDrawableCircle,getBgState(0),mCenterRect);
                drawSpeakingAnimation(canvas,mDrawableCircle);
                break;
            case STATE_RECOGNIZING:
                drawDrawable(canvas,mDrawableMic,getBgState(0),mCenterRect,false);
                drawDrawable(canvas,mDrawableRecognizing,getBgState(0),mCenterRect);
                break;
            case STATE_TIP:
                drawDrawable(canvas,mDrawableCircle,getBgState(0),mCenterRect);
                break;
        }
    }

    public void drawBackground(Canvas canvas) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(mBackgroundColor);

        int left = 0;
        int top = 0;
        int right = left + mContentWidth;
        int bottom = top + mContentHeight;
        canvas.drawRect(left, top, right, bottom, paint);
        drawFaddingShadow(canvas,left,top,right,top);
    }

    private void drawFaddingShadow(Canvas canvas, int drawLeft, int drawTop,
                                   int drawRight, int drawBottom) {
//        LOGD("drawFaddingShadow");
        if (mUpShadow == null)
            return;
        mUpShadow.setBounds(drawLeft, drawTop, drawRight, (drawTop + mUpShadow.getIntrinsicHeight()));
        //TODO mUpShadow = DarkModeUtils.checkWallpaperAndDarkMode(mUpShadow);
        mUpShadow.draw(canvas);
    }

    private void drawDrawable(Canvas canvas, Drawable drawable, int[] state, Rect rect) {
        drawDrawable(canvas,drawable,state,rect,true);
    }

    private void drawDrawable(Canvas canvas, Drawable drawable, int[] state, Rect rect, boolean fill) {
//        LOGD("drawDrawable");
        if (drawable != null) {
            drawable.setState(state);
            if(fill){
                drawable.setBounds(rect);
            }else{
                //TODO SogouWindowManager.getInstance().getCommonSizeScale() 1
                int drawableWidth = (int) (drawable.getIntrinsicWidth() * 1);
                int drawableHeight = (int) (drawable.getIntrinsicHeight() * 1);
                int left = rect.left + (rect.width() - drawableWidth) / 2;
                int right = rect.left + (rect.width() + drawableWidth) / 2;
                int top = rect.top + (rect.height() - drawableHeight) / 2;
                int bottom = rect.top + (rect.height() + drawableHeight) / 2;
                drawable.setBounds(left,top,right,bottom);
            }
            //TODO drawable = DarkModeUtils.checkDarkMode(drawable); 夜间模式
            drawable.draw(canvas);
        }
    }

    private void drawSpeakingAnimation(Canvas canvas,Drawable drawable){
        boolean more = drawAnimation(canvas,mAm1,drawable);
        more |= drawAnimation(canvas,mAm2,drawable);
        more |= drawAnimation(canvas,mAm3,drawable);
//        Log.d("xx","getAlpha()=="+transformation.getAlpha());
//        Log.d("xx","getMatrix()=="+transformation.getMatrix());
//        Log.d("xx","mCircleBitmap.getByteCount()="+mCircleBitmap.getByteCount());
//        Log.d("xx","mCircleBitmap.getAllocationByteCount()="+mCircleBitmap.getAllocationByteCount());
        if(more) invalidate();
    }

    private boolean drawAnimation(Canvas canvas , Animation animation, Drawable drawable){
        long current = AnimationUtils.currentAnimationTimeMillis();
        mTransformation.clear();
        boolean more = animation.getTransformation(current, mTransformation);
        drawable.setAlpha((int) (mTransformation.getAlpha() * 255));
        canvas.save();
        canvas.translate(mCenterRect.left,mCenterRect.top);
        canvas.concat(mTransformation.getMatrix());
        Rect dst = new Rect(mCenterRect);
        dst.offsetTo(0, 0);
        drawDrawable(canvas,drawable,getBgState(0),dst);
        canvas.restore();
        drawable.setAlpha(0xFF);
        return more;
    }

    private int[] getBgState(int index) {
//        LOGD("getBgState");
        //TODO add ThemeUtils.KeyState.KEY_STATE_NORMAL
        int[] state = KEY_STATE_NORMAL;
        if (1 == index){
            state = KEY_STATE_PRESSED;
        }
        return state;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        updateBoundRect(right - top, bottom - top);
    }

    public void updateBoundRect(int width , int height)
    {
        mContentWidth = width;
        mContentHeight = height;
        calculateRects(mContentWidth,mContentHeight);
        requestLayout();
    }

    private void calculateRects(int width, int height){
        int h2 = (int) (width * 0.36f);
        int top = (int) (width * 0.22f);
        int bottom = top + h2;

        int left = (int) ((width - h2) / 2f);
        int right = left + h2;
        mCenterRect.set(left, top, right, bottom);
        mRectHashMap.put(INDEX_VOICE_AREA,mCenterRect);
        initAnimation(mAm1);
        initAnimation(mAm2);
        initAnimation(mAm3);
    }

    private void initAnimation(Animation anim){
        if(anim != null){
            anim.reset();
            anim.setStartTime(Animation.START_ON_FIRST_FRAME);
            if (!anim.isInitialized()) {
                anim.initialize(mCenterRect.width(), mCenterRect.height(), mCenterRect.width(), mCenterRect.height());
//                anim.initialize(mCenterRect.width(), mCenterRect.height(), getWidth(), getHeight());
            }
        }
    }

    private void cancelAnimation(){
        if(mAm1 != null) mAm1.cancel();
        if(mAm2 != null) mAm2.cancel();
        if(mAm3 != null) mAm3.cancel();
    }

    private Handler mUiHandler = new Handler() ;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        int touchedItem = getTouchedItem(x, y);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (touchedItem != -1) {
                    mSelectedItemIndex = touchedItem;
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mSelectedItemIndex != -1 &&
                        mSelectedItemIndex != touchedItem) {
                    mSelectedItemIndex = -1;
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mSelectedItemIndex != -1 &&
                        mSelectedItemIndex == touchedItem) {
                    onRectPressed(mSelectedItemIndex);
                    mSelectedItemIndex = -1;
                    invalidate();
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    private int getTouchedItem(float x, float y) {
        LOGD("getTouchedItem");
        int item = -1;
        if (mRectHashMap == null) return item;
        final int size = mRectHashMap.size();
        for (int i = 0; i < size; i++) {
            Rect r = (Rect) mRectHashMap.valueAt(i);
            if (r.left <= x && r.right >= x && r.top <= y && r.bottom >= y) {
                return mRectHashMap.keyAt(i);
            }
        }
        return item;
    }

    private void onRectPressed(int index){
        /*private static final int INDEX_BACKSPACE = 0;
        private static final int INDEX_VOICE_AREA = 1;*/

        index = INDEX_VOICE_AREA;
        switch(index){
            case INDEX_VOICE_AREA:
                handleVoiceArea();
                break;
            case INDEX_BACKSPACE:
                break;
        }
    }

    private void handleVoiceArea(){
        /*public static final int     STATE_DEFAULT         = 0;
        public static final int     STATE_SPEAKING        = 1;
        public static final int     STATE_RECOGNIZING     = 2;
        public static final int     STATE_TIP             = 3;
        public static final int     STATE_FUNC            = 4;
        public static final int     STATE_SEAMLESS        = 5;
        public static final int     STATE_OFFLINE_INITIAL = 6;
        public static final int     STATE_OFFLINE_SPEAKING = 7;
        public static final int     STATE_OFFLINE_RECOGNIZING = 8;
        public static final int     STATE_SAVEING_VOICE_FILE = 9;*/
        LOGD("=====handleVoiceArea=======mState="+mState);
        switch (mState) {
            case STATE_DEFAULT:
                showSpeaking();
                break;
            case STATE_SPEAKING:
                stopSpeaking();
                cancelAnimation();
                break;
            case STATE_RECOGNIZING:
                mState = STATE_DEFAULT;
                if (mUiHandler != null) mUiHandler.removeCallbacksAndMessages(null);
                break;
            /*case STATE_CLEAR:
                StatisticsData.getInstance(mContext).pingbackB[StatisticsData.gamepadVoiceClearClickTimes]++;
                cancelClearWaitAnimtor();
                mHandler.removeMessages(MSG_VOICEINPUT_COMMIT_RESULT);
                if (mContainer != null && mContainer.mService != null) {
                    mContainer.mService.clearVoiceInGamepad();
                }
                cancel();
                mCurrentState = STATE_NORMAL;
                break;*/
        }
    }

    public void showDefault() {
//        LOGD("showDefault");
        mState = STATE_DEFAULT;
        invalidate();
    }

    public void showSpeaking() {
        mState = STATE_SPEAKING;
        mUiHandler.postDelayed(mUpdateVolumeRunnable, 50);
    }

    public void showRecognizing() {
        mState = STATE_RECOGNIZING;
        if (mUiHandler == null)
            return;
        mUiHandler.removeCallbacksAndMessages(null);
        mUiHandler.post(new Runnable() {
            public void run() {
                invalidate();
            }
        });

        mUiHandler.postDelayed(mRecognizingRunnable, 0);
    }

    public void stopSpeaking() {
        mState = STATE_RECOGNIZING;
        showRecognizing();
    }

    private Runnable mUpdateVolumeRunnable = new Runnable() {
        public void run() {
            int animGap = 500;
            int index = 5;
            final int level = Math.min(Math.max(0, index), 6);
//            final Animation am1 = AnimationUtils.loadAnimation(mContext,R.anim.voiceinput_speaking_anim);
//            final Animation am2 = AnimationUtils.loadAnimation(mContext,R.anim.voiceinput_speaking_anim);
//            final Animation am3 = AnimationUtils.loadAnimation(mContext,R.anim.voiceinput_speaking_anim);
            animGap -= level * 200;
            if (animGap <= 200 ) animGap = 200;
            mAm1.setDuration(animGap * 2);
            mAm2.setDuration(animGap * 2);
            mAm3.setDuration(animGap * 2);
            if (mUiHandler == null)
                return;
            if (!isShown()){
                mUiHandler.removeCallbacksAndMessages(null);
                return;
            }
//            Log.d("xx","mParent.isShown()="+mParent.isShown()+"  mParent.getVisibility()="+mParent.getVisibility());
            mUiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    initAnimation(mAm1);
                    invalidate(mCenterRect);
                }
            }, 0);

            mUiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    initAnimation(mAm2);
                    invalidate(mCenterRect);
                }
            }, animGap);

            mUiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    initAnimation(mAm3);
                    invalidate(mCenterRect);
                }
            }, animGap * 2);
            mUiHandler.postDelayed(mUpdateVolumeRunnable, animGap * 5);
        }
    };

    private Runnable mRecognizingRunnable  = new Runnable() {
        public void run() {
            Drawable tmp = (RotateDrawable) mDrawableRecognizing;
            if (tmp == null || !isShown()) {
                if (mUiHandler != null) mUiHandler.removeCallbacksAndMessages(null);
                return;
            }
            tmp.setLevel(mLevelCircle);
            invalidate(mCenterRect);
            mLevelCircle += 500;
            if (mLevelCircle > 10000)
                mLevelCircle = 0;
            if (mUiHandler != null)
                mUiHandler.postDelayed(mRecognizingRunnable, 75);
        }
    };

    public void showTip(final int errorType) {
        mState = STATE_TIP;
    }

    private void resetToNormalState(){
        mState = STATE_DEFAULT;
        invalidate();
    }

    private void LOGD(String message){
        Log.d("xx",message);
    }
}
