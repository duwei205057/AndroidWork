package com.dw.glide;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
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
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.EmptySignature;
import com.dw.R;
import com.dw.gif.BaseGifImageView;
import com.dw.webp.BlurTransformation;
import com.dw.webp.FrameSequence;
import com.dw.webp.FrameSequenceDrawable;
import com.dw.webp.Gifflen;
import com.dw.webp.TransformUrl;
import com.dw.webp.WebpModule;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;


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
    BaseGifImageView imageView;

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
        imageView = (BaseGifImageView) findViewById(R.id.imageView);
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
//        preload();
//        getCache();
        load();
//        loadWebp();
//        loadLocal();
//        getCache2();
//        load1();
//        preload1();
//        getCache();
    }

    private void preload() {
        Glide.with(this)
                .load("https://78.media.tumblr.com/a0c1be3183449f0d207a022c28f4bbf7/tumblr_p1p2cduAiA1wmghc4o1_500.gif")
                .downloadOnly(new SimpleTarget<File>() {
                    @Override
                    public void onResourceReady(File resource, Transition<? super File> transition) {
                        Log.d("xx"," enter  run()---------------");
                    }
                });
    }

    private void preload1() {
        Glide.with(this)
                .load("https://78.media.tumblr.com/a0c1be3183449f0d207a022c28f4bbf7/tumblr_p1p2cduAiA1wmghc4o1_500.gif")
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.DATA))
                .preload();
    }

    private void load() {
        Glide.with(this)
                .load("https://78.media.tumblr.com/a0c1be3183449f0d207a022c28f4bbf7/tumblr_p1p2cduAiA1wmghc4o1_500.gif")
//                .load("file:///sdcard/frame0")
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL).override(200))
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                        Log.d("xx"," enter  run()---------------");
                        if (resource instanceof Animatable) {
                            ((Animatable)resource).start();
                        }
                        imageView.setImageDrawable(resource);
                    }
                });
    }

    /**
     * 读取本地资源(不需要磁盘缓存)
     */
    private void loadLocal() {
        Glide.with(this)
                .load("file:///sdcard/frame0")
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                        Log.d("xx"," enter  run()---------------");
                        if (resource instanceof Animatable) {
                            ((Animatable)resource).start();
                        }
                        imageView.setImageDrawable(resource);
                    }
                });
    }

    /**
     * 需灌色或高斯的bitmap //不需要分享
     */
    private void load1() {
        Glide.with(this.getApplicationContext())
                .load("http://pic19.nipic.com/20120322/8863808_112842518157_2.jpg")
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL).transform(new BlurTransformation(5)).override(300,150))
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                        Log.d("xx"," enter  run()---------------");
                        if (resource instanceof Animatable) {
                            ((Animatable)resource).start();
                        }
                        imageView.setImageDrawable(resource);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                    }
                });
    }

    private void loadWebp() {
        Glide.with(this)
                .load(new TransformUrl("https://www.gstatic.com/webp/animated/1.webp"))
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.DATA))
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                        Log.d("xx"," enter  run()---------------");
                        if (resource instanceof Animatable) {
                            ((Animatable)resource).start();
                        }
                        imageView.setImageDrawable(resource);
                    }
                });
    }

    /**
     * 静态webp 需转bitmap
     */
    private void loadWebp2() {
        Glide.with(this)
                .load("https://www.gstatic.com/webp/gallery3/1_webp_ll.webp")
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.DATA))
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                        Log.d("xx"," enter  run()---------------");
                        if (resource instanceof Animatable) {
                            ((Animatable)resource).start();
                        }
                        imageView.setImageDrawable(resource);
                    }
                });
    }

    private void getCache() {
//        Glide.getPhotoCacheDir()
        DataCacheKey dataCacheKey = new DataCacheKey(new GlideUrl("https://www.gstatic.com/webp/animated/1.webp"), EmptySignature.obtain());
        File file = WebpModule.getGlobalDiskCache().get(dataCacheKey);
        Log.d("xx"," enter  run()---------------");
    }

    //get()方法不允许在主线程调用
    @Deprecated
    private void getCache2() {
        try {
            File f = Glide.with(this).asFile()
                    .load("https://78.media.tumblr.com/a0c1be3183449f0d207a022c28f4bbf7/tumblr_p1p2cduAiA1wmghc4o1_500.gif")
                    .apply(RequestOptions.priorityOf(Priority.HIGH).onlyRetrieveFromCache(true)).submit().get();//get()方法不允许在主线程调用
            Log.d("xx"," enter  run()---------------");
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
/*
    public void getImage(View view) {
        final BaseGifImageView imageView = (BaseGifImageView) findViewById(R.id.imageView);
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
        final String mStorePath = Environment.getExternalStorageDirectory().getAbsolutePath() +
                File.separator + "test/result.gif";
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("xx"," enter  run()---------------");
                    RandomAccessFile ra = new RandomAccessFile("/sdcard/test/test.webp","r");
                    FileChannel fc = ra.getChannel();
                    ByteBuffer bb = ByteBuffer.allocate((int)ra.length());
                    fc.read(bb);
                    bb.flip();
                    ra.close();
                    new Gifflen.Builder()
                            .path(mStorePath)
                            .width(320)
                            .height(320)
                            .listener(new Gifflen.OnEncodeFinishListener() {
                                @Override
                                public void onEncodeFinish(final String path) {
                                    Log.d("xx","----------------onEncodeFinish-----------------");
                                    imageView.setGifImage(path);
                                }
                            })
                            .encodeFrameSequence(bb);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
*/



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
