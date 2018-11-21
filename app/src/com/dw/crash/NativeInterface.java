package com.dw.crash;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Process;
import android.util.Log;

import com.aop.DebugTrace;
import com.dw.DynamicApplication;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Created by dw on 18-9-18.
 */

public class NativeInterface {

    private static NativeInterface mInstance = new NativeInterface();

    static {
        System.loadLibrary("myso");
        Log.d("xx","NativeInterface  loadlibrary pid=="+ Process.myPid());
    }

//    static {
//        //使用加固的so
//        String path = cpFromAssert("mmyso");
//        System.load(path);
//    }

    private NativeInterface(){

    }

    public static String cpFromAssert(String name) {
        try {
            Context context = DynamicApplication.mRealApplication;
            InputStream is = context.getAssets().open(name);
            OutputStream os = context.openFileOutput(name, Context.MODE_PRIVATE);
            byte[] buffer = new byte[8092];
            int len;
            while ( (len = is.read(buffer)) != -1)
                os.write(buffer, 0 ,len);
            is.close();
            os.close();
            return context.getFilesDir().getAbsolutePath() + File.separator + name;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*public void load(){
        try{
            System.loadLibrary("nativehelper");
            Log.d("xx","NativeInterface  loadlibrary pid=="+ Process.myPid());
        } catch (UnsatisfiedLinkError ule) {
            ule.printStackTrace();
        }
    }*/

    public static NativeInterface getInstance(){
        return mInstance;
    }

    @DebugTrace
    public native String getStringFromNative();

    public native String getCrashStringFromNative();

}
