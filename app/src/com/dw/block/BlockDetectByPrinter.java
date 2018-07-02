package com.dw.block;

import android.os.Looper;
import android.util.Log;
import android.util.Printer;

/**
 * Created by dw on 18-1-19.
 */

public class BlockDetectByPrinter {
    public static void start() {
        Looper.getMainLooper().setMessageLogging(new Printer() {
            private static final String START = ">>>>> Dispatching";
            private static final String END = "<<<<< Finished";
            LogMonitor lm = new LogMonitor("Looper");
            @Override
            public void println(String x) {
                if (x.startsWith(START)) {
                    lm.startMonitor();
                    Log.d("xx", "before handle-----------");
                }
                if (x.startsWith(END)) {
                    lm.removeMonitor();
                    Log.d("xx", "after handle-----------");
                }
            }
        });
    }
}
