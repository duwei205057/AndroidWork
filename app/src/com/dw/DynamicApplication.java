package com.dw;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
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
        Log.d("xx","attachBaseContext");
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
}
