package com.dw.touchable;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.dw.R;

/**
 * Created by dw on 18-1-16.
 */

public class MotionActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.motion_layout);
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
