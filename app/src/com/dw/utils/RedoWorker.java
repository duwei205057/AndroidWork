package com.dw.utils;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by dw on 18-5-22.
 */

public class RedoWorker {

    private Handler mHandler;

    private volatile boolean mIsWorking;

    public RedoWorker(Looper looper) {
        mHandler = new Handler(looper);
    }

    public boolean ismIsWorking() {
        return mIsWorking;
    }

    public void start(final Runnable job, final int... millisecondsGap){
        if (job == null || millisecondsGap == null || millisecondsGap.length == 0) return;
        final int totalCount = millisecondsGap.length;
        mIsWorking = true;
        mHandler.post(new Runnable() {
            private int mCurrentCount = 0;
            @Override
            public void run() {
                if (!mIsWorking) return;
                if (mCurrentCount > 0)job.run();
                if (mIsWorking && mCurrentCount < totalCount){
                    int delay = millisecondsGap[mCurrentCount++ ];
                    mHandler.postDelayed(this, delay);
                }

            }
        });
    }

    public void startLooper(final Runnable job, final int millisecondsGap){
        if (job == null) return;
        mIsWorking = true;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!mIsWorking) return;
                job.run();
                mHandler.postDelayed(this, millisecondsGap);
            }
        });
    }

    public void done(){
        mIsWorking = false;
        mHandler.removeCallbacksAndMessages(null);
    }

}