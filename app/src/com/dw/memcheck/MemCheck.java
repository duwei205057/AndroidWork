package com.dw.memcheck;

/**
 * Created by dw.
 */

public class MemCheck {

    static {
        System.loadLibrary("memcheck");
    }

    public static boolean checkMemory(int memorySize) {
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int totalMemory = (int) (Runtime.getRuntime().totalMemory() / 1024);
        int freeMemory = (maxMemory - totalMemory) * 2 / 3;
        return memorySize < freeMemory;
    }

    public static boolean checkFullMemory(int memorySize) {
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int totalMemory = (int) (Runtime.getRuntime().totalMemory() / 1024);
        int freeMemory = (maxMemory - totalMemory);
        return memorySize < freeMemory;
    }

    public native int initModule();
    public native int memMalloc();
}
