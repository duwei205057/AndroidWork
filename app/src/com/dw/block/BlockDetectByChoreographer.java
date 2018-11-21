package com.dw.block;

import android.util.Log;
import android.view.Choreographer;

import java.util.concurrent.TimeUnit;

/**
 * Created by dw on 18-1-19.
 */

public class BlockDetectByChoreographer {
    public static void start() {
        Choreographer.getInstance().postFrameCallback(new Choreographer.FrameCallback() {
            long lastFrameTimeNanos = 0;
            long currentFrameTimeNanos = 0;
            LogMonitor lm = new LogMonitor("Choreographer");

            @Override
            public void doFrame(long frameTimeNanos) {
                if (lastFrameTimeNanos == 0) {
                    lastFrameTimeNanos = frameTimeNanos;
                }
                currentFrameTimeNanos = frameTimeNanos;
                long diffMs = TimeUnit.MILLISECONDS.convert(currentFrameTimeNanos - lastFrameTimeNanos, TimeUnit.NANOSECONDS);
                if (diffMs > 16.6f) {
                    int droppedCount = (int) (diffMs / 16.6);
                }
                lm.removeMonitor();
                lm.startMonitor();
                Log.d("xx", "postFrameCallback-----------");
                Choreographer.getInstance().postFrameCallback(this);
            }
        });
    }
}
