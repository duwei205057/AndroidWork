package com.dw.gif;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.View;

import com.database.SomeFileObserver;
import com.dw.R;
import com.dw.crash.NativeInterface;
import com.sogou.nativecrashcollector.NativeCrashManager;

import java.io.File;
import java.io.IOException;
import java.net.Socket;


/**
 * Created by dw on 18-3-6.
 */

public class GifActivity extends Activity {

    LongSparseArray<Bitmap> mLsa = new LongSparseArray<Bitmap>();
    private Drawable drawable1;
    private BaseGifImageView mGifView;
    private BaseGifImageView mGifView1;
    private SomeFileObserver sfo;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gif);
        drawable1 = getResources().getDrawable(R.drawable.books);
        drawable1.setAlpha(100);
        Drawable drawable2 = getResources().getDrawable(R.drawable.books);
        View view1 = findViewById(R.id.btn1);
        view1.setBackground(drawable1);
        View view2 = findViewById(R.id.btn2);
        view2.setBackground(drawable2);
        mGifView = (BaseGifImageView)findViewById(R.id.gif1);
        mGifView.setGifImage(R.drawable.a_gif);
        mGifView.setLoadingDrawable(getResources().getDrawable(R.drawable.books));
        mGifView1 = (BaseGifImageView)findViewById(R.id.gif2);
        mGifView1.setGifImage(R.drawable.fish);
        mGifView1.setLoadingDrawable(getResources().getDrawable(R.drawable.books));
        LOGD("nativeLibraryDir=="+getApplicationInfo().nativeLibraryDir);
        sfo = new SomeFileObserver("/sdcard/sogou/mutual/");
        sfo.startWatching();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LOGD("GifActivity onDestroy");
        for(int i = 0; i < mLsa.size(); i++){
            Bitmap bitmap = mLsa.valueAt(i);
//            bitmap.recycle();
        }
        mLsa.clear();
        sfo.stopWatching();
    }

    public void decodeFile(View view) {
//        int i = 0;
//        while (i++ < 100) {
//            long startTime = SystemClock.currentThreadTimeMillis();
//            Bitmap tmp = BitmapFactory.decodeResource(getResources(), R.drawable.books);
//            LOGD("decode consume time = " + (SystemClock.currentThreadTimeMillis() - startTime) + " ms , Memory get =" + tmp.getByteCount() + " bytes. tmp=" + tmp);
//            mLsa.put(startTime, tmp);
//
//        }
//        LOGD("mLsa.size()===" + mLsa.size());
//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        Log.d("xx", "decodeFile====================");
        String s = NativeInterface.getInstance().getStringFromNative();
        Log.d("xx", "mInterface.getStringFromNative()====================" + s);

        /*try {
            Socket socket = new Socket("localhost", 99);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    private void LOGD(String message){
        Log.d("xx", "GifActivity "+message);
    }
}

