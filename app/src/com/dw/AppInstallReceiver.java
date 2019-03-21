package com.dw;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class AppInstallReceiver extends BroadcastReceiver {

    boolean replacing;
    @Override
    public void onReceive(Context context, Intent intent) {
        replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
        Bundle extras = intent.getExtras();
        extras.putBoolean(Intent.EXTRA_REPLACING, false);
        if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {
            String packageName = intent.getData().getSchemeSpecificPart();
            LODG("安装成功-------------"+packageName);
        }
        if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
            String packageName = intent.getData().getSchemeSpecificPart();
            LODG("卸载成功-------------"+packageName);
        }
        if (intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)) {
            String packageName = intent.getData().getSchemeSpecificPart();
            LODG("替换成功-------------"+packageName);
        }
        if (intent.getAction().equals(Intent.ACTION_PACKAGE_CHANGED)) {
            String packageName = intent.getData().getSchemeSpecificPart();
            LODG("更改成功-------------"+packageName);
        }

    }

    public void LODG(String message) {
        Log.d("xx",message+" replacing=="+replacing);
    }

}
