package com.dw.glide;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.aop.DebugTrace;
import com.bumptech.glide.Glide;
import com.dw.R;
import com.sogou.webp.FrameSequence;
import com.sogou.webp.FrameSequenceDrawable;
import com.sogou.webp.Gifflen;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by dw on 18-11-16.
 */

public class GlideActivity extends Activity {

    RecyclerView mRecyclerView;
    ImageAdapter mWebpAdapter;
    Button mButton;
    Gifflen mGifflen;

    private int mDelayTime = 500;

    private int mQuality = 10;

    private int mColor = 256;
    private TypedArray mDrawableList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setTheme(android.R.style.Theme_DeviceDefault_Light_NoActionBar);
        setContentView(R.layout.activity_glide);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler);
        mButton = (Button)findViewById(R.id.button);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mWebpAdapter = new ImageAdapter(this);
        mRecyclerView.setAdapter(mWebpAdapter);
        mDrawableList = getResources().obtainTypedArray(R.array.source);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void getImage(View view) {
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        Glide.with(view.getContext())
                .load("http://img1.dzwww.com:8080/tupian_pl/20150813/16/7858995348613407436.jpg")
                .into(imageView);
        AsyncTask<Void,Void,byte[]> at = new AsyncTask<Void,Void,byte[]>() {

            @Override
            protected void onPostExecute(byte[] bytes) {
                super.onPostExecute(bytes);
                long start = System.currentTimeMillis();
                FrameSequence fs = FrameSequence.decodeByteArray(bytes);
                Log.d("xx","decodeByteArray cost = "+ (System.currentTimeMillis() - start));
                FrameSequenceDrawable fsd = new FrameSequenceDrawable(fs);
                fsd.start();
                fsd.setCircleMaskEnabled(true);
                mButton.setBackground(fsd);
            }

            @Override
            protected byte[] doInBackground(Void... voids) {
                return decorButton();
            }
        };
        at.execute();
        mGifflen = new Gifflen.Builder()
                .color(mColor)
                .delay(mDelayTime)
                .quality(mQuality)
                .listener(new Gifflen.OnEncodeFinishListener() {
                    @Override
                    public void onEncodeFinish(String path) {
                        Log.d("xx","----------------onEncodeFinish-----------------");
                    }
                })
                .build();
        final String mStorePath = Environment.getExternalStorageDirectory().getAbsolutePath() +
                File.separator + "gifflen-" + mQuality + "-" + mColor + "-" + mDelayTime + "-sapmle.gif";
        new Thread(new Runnable() {
            @Override
            public void run() {
                mGifflen.encode(GlideActivity.this, mStorePath, 320, 320, mDrawableList);
            }
        }).start();

    }



    @SuppressLint("ResourceType")
    private byte[] decorButton() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            long start = System.currentTimeMillis();
            InputStream is = getResources().openRawResource(R.drawable.broken);//动态
//            InputStream is = getResources().openRawResource(R.drawable.boat);//静态
            int len;
            byte[] buffer = new byte[8192];
            while((len = is.read(buffer)) != -1)
                bos.write(buffer,0,len);
            is.close();
            bos.close();
            Log.d("xx","copy into mem cost = "+ (System.currentTimeMillis() - start));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bos.toByteArray();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


}
