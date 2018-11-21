package com.dw.touchable;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.dw.R;

import static com.dw.DynamicApplication.getProcessName;

/**
 * Created by dw on 18-1-16.
 */

public class MotionActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.motion_layout);
        PackageManager pm = getPackageManager();
        try {
            ApplicationInfo appInfo = pm.getPackageInfo(getPackageName(), 0).applicationInfo;
            Log.d("xx","MotionActivity getPackageName()="+getPackageName()+" appInfo.className="+appInfo.className+" appInfo.packageName="+appInfo.packageName+" appInfo.processName="+appInfo.processName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        Log.d("xx","MotionActivity pid="+android.os.Process.myPid()+"am.processName="+getProcessName(this, android.os.Process.myPid())+" FilesDir="+getFilesDir());
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
