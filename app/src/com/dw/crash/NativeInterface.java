package com.dw.crash;

import android.os.Process;
import android.util.Log;


/**
 * Created by dw on 18-9-18.
 */

public class NativeInterface {

    private static NativeInterface mInstance = new NativeInterface();

    static {
        System.loadLibrary("myso");
        Log.d("xx","NativeInterface  loadlibrary pid=="+ Process.myPid());
    }

    private NativeInterface(){

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

    public native String getStringFromNative();
}
