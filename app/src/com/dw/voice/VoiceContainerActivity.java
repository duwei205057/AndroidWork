package com.dw.voice;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.dw.R;

/**
 * Created by dw on 18-2-1.
 */

public class VoiceContainerActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.voice);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
