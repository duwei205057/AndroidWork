package com.sogou.nativecrashcollector;

/**
 * @author duwei
 * @date 18-10-16
 */

public class NativeInterface {
    private static volatile NativeInterface mInstance;
    public static final int CRASH_SWITCH = 1;
    public static final int ANR_SWITCH = 2;

    private NativeInterface() {
    }

    public static NativeInterface getInstance() {
        if (mInstance == null) {
            synchronized (NativeInterface.class) {
                if (mInstance == null) {
                    mInstance = new NativeInterface();
                }
            }
        }
        return mInstance;
    }

    //for native crash collect
    public native int initCrashCollectNative(char[] path, int pathLength);

    public native int initAnrCollectNative(char[] procName, int procNameLength, char[] path, int pathLength);

    public native int setVersionInfoNative(char[] info, int size);

    public native int setKeyboardShownStateNative(int state);

    public native int setNativeCollectSwitchNative(int which, boolean state);


}
