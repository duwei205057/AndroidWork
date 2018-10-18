package com.sogou.nativecrashcollector;

import android.text.TextUtils;
import android.util.Log;

import java.io.File;

/**
 * Created by dw on 18-10-15.
 */

public class NativeCrashManager {

    private static final boolean DEBUG = true;
//    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "xx";

    private static String sUsingSoName = "";
    private static boolean sLoaded = false;
    private final int ERROR = -1;
    private final int OK = 0;

    public static boolean sCollectLogcat = false;

    private static volatile NativeCrashManager mInstance;
    private NativeCrashManager() {
    }

    public static NativeCrashManager getInstance() {
        if (mInstance == null) {
            synchronized (NativeCrashManager.class) {
                if (mInstance == null) {
                    mInstance = new NativeCrashManager();
                }
            }
        }
        return mInstance;
    }

    public void loadLibrary(String soPath) throws UnsatisfiedLinkError {
        try{
            System.load(soPath);
            String[] pathSeg = soPath.split("/");
            sUsingSoName = pathSeg[pathSeg.length > 0 ? pathSeg.length - 1 : 0];
            sLoaded = true;
        } catch (UnsatisfiedLinkError ule) {
            ule.printStackTrace();
            sLoaded = false;
        }
    }

    public void loadLibrary() throws UnsatisfiedLinkError {
        try{
            System.loadLibrary("crash_collect");
            sLoaded = true;
        } catch (UnsatisfiedLinkError ule) {
            ule.printStackTrace();
            sLoaded = false;
        }
    }

    /**
     * 崩溃时.是否收集logcat信息,只有在 {@link #enableCrashCollect(boolean)} 设置为true时有效
     * @param enable
     */
    public void enableLogcatCollect(boolean enable) {
        sCollectLogcat = enable;
    }

    public void enableCrashCollect(boolean enable) {
        if (!sLoaded) return;
        NativeInterface.getInstance().setNativeCollectSwitchNative(NativeInterface.CRASH_SWITCH, enable);
    }

    public void enableANRCollect(boolean enable) {
        if (!sLoaded) return;
        NativeInterface.getInstance().setNativeCollectSwitchNative(NativeInterface.ANR_SWITCH, enable);
    }

    /** 初始化崩溃收集模块
     * @param crashLogPath 崩溃日志文件路径
     * */
    public int initCrashCollect(String crashLogPath) {
        LOGD("initCrashCollect   crashLogPath=="+crashLogPath);
        if (!sLoaded || TextUtils.isEmpty(crashLogPath)) return ERROR;
        File file = new File(crashLogPath);
        try {
            if(!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return NativeInterface.getInstance().initCrashCollectNative(crashLogPath.toCharArray(), crashLogPath.length());
    }

    /** 初始化崩溃收集模块
     * @param anrLogPath 崩溃日志文件路径
     * */
    public int initANRCollect(String processName, String anrLogPath) {
        LOGD("initANRCollect  processName=="+processName+" anrLogPath=="+anrLogPath);
        if (!sLoaded || TextUtils.isEmpty(processName) || TextUtils.isEmpty(anrLogPath)) return ERROR;
        File file = new File(anrLogPath);
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return NativeInterface.getInstance().initAnrCollectNative(processName.toCharArray(), processName.length(), anrLogPath.toCharArray(), anrLogPath.length());
    }

    /**
     * 设置版本信息
     * @param info 版本信息
     * @return
     */
    public int setVersionInfo(String info) {
        if (TextUtils.isEmpty(info)) return ERROR;
        return NativeInterface.getInstance().setVersionInfoNative(info.toCharArray(), info.length());
    }

    /**
     * 设置键盘是否显示
     * @param state 1:显示  0:关闭
     * */
    public int setKeyboardShownState(int state){
        if (!sLoaded) return ERROR;
        return NativeInterface.getInstance().setKeyboardShownStateNative(state);
    }

    private void LOGD(String message) {
        if (DEBUG)
            Log.d(TAG, message);
    }

}
