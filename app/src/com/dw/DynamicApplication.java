package com.dw;

import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Process;
import android.os.StatFs;
import android.text.format.Formatter;
import android.util.Log;

import com.dw.block.BlockDetectByChoreographer;
import com.dw.block.BlockDetectByPrinter;
import com.dw.crash.NativeInterface;
import com.dw.touchable.MotionActivity;
import com.dw.utils.PingBackUtils;
import com.dw.utils.StreamUtil;
import com.sogou.nativecrashcollector.NativeCrashManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by dw on 17-7-10.
 */
public class DynamicApplication extends Application {//RePluginApplication 使用replugin

    private static final String HACK_JAR = "hackdex.jar";
    public static DynamicApplication mRealApplication;
    public static String mProcessName;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        mRealApplication = this;
        mProcessName = getCurrentProcessName();
        PackageManager pm = base.getPackageManager();
        try {
            ApplicationInfo appInfo = pm.getPackageInfo(base.getPackageName(), 0).applicationInfo;
            ActivityInfo activityInfo = pm.getActivityInfo(new ComponentName(base, MotionActivity.class), 0);
            Log.d("xx","attachBaseContext this="+this+" base.getPackageName()="+base.getPackageName()+" appInfo.className="+appInfo.className+" appInfo.packageName="+appInfo.packageName+" appInfo.processName="+appInfo.processName+" activityInfo.processName="+activityInfo.processName);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

//        BlockDetectByChoreographer.start();
//        BlockDetectByPrinter.start();


        Log.d("xx","attachBaseContext pid="+ Process.myPid()+" am.processName="+getProcessName(base, Process.myPid())+" FilesDir="+base.getFilesDir());
        Log.d("xx","attachBaseContext availableProcessors="+ PingBackUtils.getNumberOfCPUCores()+" ABI="+ PingBackUtils.getDeviceCpuABI()+
                /*" ABIs="+ Arrays.toString(Build.SUPPORTED_ABIS) +*/ " MaxFre="+Arrays.toString(PingBackUtils.getCPUMaxFreqKHz()) +
                " MaxHeap="+Runtime.getRuntime().maxMemory()+" cpuName="+PingBackUtils.getCpuName());
        Log.d("xx","max mem ="+Runtime.getRuntime().maxMemory()+" total mem ="+Runtime.getRuntime().totalMemory()+" free mem ="+Runtime.getRuntime().freeMemory());
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Log.d("xx","DataDir ="+getMemoryInfo(new File("/data/data")));
            Log.d("xx","SdcardDir ="+getMemoryInfo(new File("/sdcard/")));
//        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityManager activityService = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
            activityService.getMemoryInfo(info);
            Log.d("xx","ActivityManager.getMemoryClass=="+activityService.getMemoryClass()+" MemoryInfo  avail="+info.availMem+" threshold="+info.threshold+" totalmem="+info.totalMem);
        }
        try {
            File optimiseFile = getDir("dex", Context.MODE_PRIVATE);
            File dexFilePath = new File("/sdcard/app_dex");
            File hackJarPath = new File(dexFilePath, HACK_JAR);
            Log.d("xx","hackJarPath.getAbsolutePath()=="+hackJarPath.getAbsolutePath());
            if(hackJarPath.exists() && hackJarPath.isFile()){
                Log.d("xx","exists hackdex.jar");
                List<File> files = new ArrayList<>();
                files.add(hackJarPath);
                InstallDex.installFixDexes(this,getClassLoader(),optimiseFile,files,true);
            }
            String sourceDir = base.getPackageManager().getApplicationInfo("com.dw.debug", 0).sourceDir;
            Log.d("xx","sourceDir="+sourceDir);
        }catch (Exception e){
            e.printStackTrace();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                NativeInterface.getInstance();
                NativeCrashManager.getInstance().loadLibrary();
                NativeCrashManager.getInstance().setVersionInfo("+++++++++++++++++))))))))))))))))))))))))))");
                NativeCrashManager.getInstance().setKeyboardShownState(1);
//                NativeCrashManager.getInstance().initCrashCollect("/sdcard/native_crash.txt");
                NativeCrashManager.getInstance().initCrashCollect(new File(getFilesDir(),"native_crash.txt").getAbsolutePath());
                NativeCrashManager.getInstance().initANRCollect(mProcessName, new File(getFilesDir(),"anr_crash.txt").getAbsolutePath());
            }
        }).start();

    }

    public static String getProcessName(Context context, int pid){
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo app : am.getRunningAppProcesses()){
            if (app.pid ==  pid) return app.processName;
        }
        return null;
    }

    private String getCurrentProcessName() {
        int pid = android.os.Process.myPid();
        String pName = null;

        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader cmdlineReader = null;
        StringBuilder processName = new StringBuilder();
        try {
            fis = new FileInputStream("/proc/" + pid + "/cmdline");
            isr = new InputStreamReader(fis, "iso-8859-1");
            cmdlineReader = new BufferedReader(isr);
            int c;
            while ((c = cmdlineReader.read()) > 0) {
                processName.append((char) c);
            }
            pName = processName.toString();
        } catch (Throwable t) {
            ActivityManager mActivityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
            if (mActivityManager != null) {
                List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfoList = mActivityManager.getRunningAppProcesses();
                if (runningAppProcessInfoList != null && !runningAppProcessInfoList.isEmpty()) {
                    for (ActivityManager.RunningAppProcessInfo appProcess : runningAppProcessInfoList) {
                        if (appProcess.pid == pid) {
                            pName = appProcess.processName;
                            break;
                        }
                    }
                }
            }
        } finally {
            StreamUtil.closeStream(cmdlineReader);
            StreamUtil.closeStream(isr);
            StreamUtil.closeStream(fis);
        }

        return pName;
    }


    private String getMemoryInfo(File path) {
        // 获得一个磁盘状态对象
        StatFs stat = new StatFs(path.getPath());

        long blockSize = stat.getBlockSize();    // 获得一个扇区的大小

        long totalBlocks = stat.getBlockCount();    // 获得扇区的总数

        long availableBlocks = stat.getAvailableBlocks();    // 获得可用的扇区数量

        // 总空间
        String totalMemory = Formatter.formatFileSize(this, totalBlocks * blockSize);
        // 可用空间
        String availableMemory = Formatter.formatFileSize(this, availableBlocks * blockSize);

        return "总空间: " + totalMemory + "\n可用空间: " + availableMemory;
    }

}
