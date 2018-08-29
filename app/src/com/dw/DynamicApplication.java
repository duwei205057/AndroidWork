package com.dw;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.dw.touchable.MotionActivity;
import com.dw.utils.Helper;
import com.dw.utils.PingBackUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.realm.Realm;


/**
 * Created by dw on 17-7-10.
 */
public class DynamicApplication extends Application{

    private static final String HACK_JAR = "hackdex.jar";
    public static DynamicApplication mRealApplication;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        mRealApplication = this;
        PackageManager pm = base.getPackageManager();
        try {
            ApplicationInfo appInfo = pm.getPackageInfo(base.getPackageName(), 0).applicationInfo;
            ActivityInfo activityInfo = pm.getActivityInfo(new ComponentName(base, MotionActivity.class), 0);
            Log.d("xx","attachBaseContext this="+this+" base.getPackageName()="+base.getPackageName()+" appInfo.className="+appInfo.className+" appInfo.packageName="+appInfo.packageName+" appInfo.processName="+appInfo.processName+" activityInfo.processName="+activityInfo.processName);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        Log.d("xx","attachBaseContext pid="+android.os.Process.myPid()+"am.processName="+getProcessName(base, android.os.Process.myPid())+" FilesDir="+base.getFilesDir());
        Log.d("xx","attachBaseContext availableProcessors="+ PingBackUtils.getNumberOfCPUCores()+" ABI="+ PingBackUtils.getDeviceCpuABI()+
                " ABIs="+ Arrays.toString(Build.SUPPORTED_ABIS) + " MaxFre="+Arrays.toString(PingBackUtils.getCPUMaxFreqKHz()) +
                " MaxHeap="+Runtime.getRuntime().maxMemory()+" cpuName="+PingBackUtils.getCpuName());
        try {
            File optimiseFile = getDir("dex", Context.MODE_PRIVATE);
            File dexFilePath = new File("/sdcard/app_dex");
            File hackJarPath = new File(dexFilePath, HACK_JAR);
            Log.d("xx","hackJarPath.getAbsolutePath()=="+hackJarPath.getAbsolutePath());
            if(hackJarPath.exists() && hackJarPath.isFile()){
                Log.d("xx","exists hackdex.jar");
                List<File> files = new ArrayList<>();
                files.add(hackJarPath);
                com.dw.InstallDex.installFixDexes(this,getClassLoader(),optimiseFile,files,true);
            }
            String sourceDir = base.getPackageManager().getApplicationInfo("com.dw.debug", 0).sourceDir;
            Log.d("xx","sourceDir="+sourceDir);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static String getProcessName(Context context, int pid){
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo app : am.getRunningAppProcesses()){
            if (app.pid ==  pid) return app.processName;
        }
        return null;
    }

}
