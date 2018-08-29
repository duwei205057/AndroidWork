package com.dw.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

/**
 * Created by dw on 18-5-9.
 */

public class Helper {

    static final String IME_PACAKGE_NAME = "com.sohu.inputmethod.sogou";

    //是否勾选搜狗输入法
    public static boolean checkSelected(Context context) {
        InputMethodManager localInputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        List<InputMethodInfo> imeList = localInputMethodManager.getEnabledInputMethodList();
        int i;
        for (i=0; i<imeList.size(); i++) {
            if (imeList.get(i).getId().startsWith(IME_PACAKGE_NAME+"/") /*&& !imeList.get(i).getId().contains("pad")*/)
                break;
        }
        if (i == imeList.size())
            return false;
        return true;
    }

    //是否默认搜狗输入法
    public static boolean checkDefault(Context context) {
        String mLastInputMethodId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
        if (mLastInputMethodId != null && mLastInputMethodId.startsWith(IME_PACAKGE_NAME + "/"))
            return true;
        return false;
    }

    //调用系统设置默认输入法
    public static void promptImePick(Context context){
        InputMethodManager localInputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        localInputMethodManager.showInputMethodPicker();
    }

    //调用系统设置勾选输入法
    public static void promptImeSelect(Context context){
        Intent startImeSettings = new Intent();
        startImeSettings.setAction(Intent.ACTION_MAIN);
        startImeSettings.setAction(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS);
        context.startActivity(startImeSettings);
    }

}
