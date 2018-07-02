package com.dw.gif;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by chenjinyi on 16-7-21.
 */
public class GifTimerPool {
    private static final int TIME_INTERVAL = 10;
    private DrawThread thread;
    private ArrayList<TimeListener> mTimeListeners;
    private static boolean isPause = false;
    private ExecutorService executorService = null;

    public GifTimerPool() {
        mTimeListeners = new ArrayList<TimeListener>();
        thread = new DrawThread();
        thread.start();
    }

    public void recycle() {
        if(thread != null) {
            thread.interrupt();
        }
        if(executorService != null) {
            executorService.shutdownNow();
            executorService = null;
        }
        clearTimeListener();
    }

    public void commitRunnable(Runnable runnable) {
        try {
            if (executorService == null) {
                executorService = Executors.newFixedThreadPool(4);
            }
            if (!executorService.isShutdown()) {
                executorService.submit(runnable);
            }
        } catch (Exception e) {

        }
    }

    public void addTimeListener(TimeListener listener) {
        if(listener == null) return;
        if(mTimeListeners == null) return;
        synchronized(mTimeListeners) {
            if(!mTimeListeners.contains(listener))
                mTimeListeners.add(listener);
        }
    }

    public void removeTimeListener(TimeListener listener) {
        if(listener == null) return;
        if(mTimeListeners == null) return;
        synchronized(mTimeListeners) {
            mTimeListeners.remove(listener);
        }
    }

    public void clearTimeListener() {
        if(mTimeListeners == null) return;
        synchronized(mTimeListeners) {
            mTimeListeners.clear();
        }
    }

    public void setIsPause(boolean isPause) {
        this.isPause = isPause;
    }

    private class DrawThread extends Thread{
        public void run() {
            while(true) {
                if(isInterrupted()) return;
                if(!isPause) {
                    synchronized (mTimeListeners) {
                        if(mTimeListeners == null) continue;
                        for (int i = 0; i < mTimeListeners.size(); ) {
                            if(isInterrupted()) break;
                            TimeListener listener = mTimeListeners.get(i);
                            if (listener != null /*&& listener.isViewShown()*/) {
                                listener.startLoad();
                                listener.onTimeChange(TIME_INTERVAL);
                                i++;
                            } else {
                                mTimeListeners.remove(listener);
                            }
                        }
                    }
                }
                try {
                    Thread.sleep(TIME_INTERVAL);
                }catch (InterruptedException e) {
                    return;
                }
            }
        }
    }

    public interface TimeListener {
        public void onTimeChange(int time);
        public boolean isViewShown();
        public void startLoad();
    }
}
