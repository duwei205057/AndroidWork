package com.dw;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by dw on 17-10-10.
 */

public class PhoneReceiver extends BroadcastReceiver {
    String TAG = "PhoneReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("xx","outgoing"+intent.getAction());
        if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            // 如果是去电（拨出）

            Log.d("xx","outgoing");
        }
    }


}
