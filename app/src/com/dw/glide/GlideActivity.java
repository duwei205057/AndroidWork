package com.dw.glide;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.dw.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by dw on 18-11-16.
 */

public class GlideActivity extends Activity {

    RecyclerView mRecyclerView;
    ImageAdapter mWebpAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glide);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mWebpAdapter = new ImageAdapter(this, getAnimatedWebpUrls());
        mRecyclerView.setAdapter(mWebpAdapter);
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

    private static final String[] SIMPLE_WEBP = {
            "http://img1.dzwww.com:8080/tupian_pl/20150813/16/7858995348613407436.jpg",
            "http://www.gstatic.com/webp/gallery/1.webp",
            "http://www.gstatic.com/webp/gallery/2.webp",
            "http://www.gstatic.com/webp/gallery/3.webp",
            "http://www.gstatic.com/webp/gallery/4.webp",
            "http://www.gstatic.com/webp/gallery/5.webp",
    };
    private static final String[] ALPHA_WEBP = {
            "https://www.gstatic.com/webp/gallery3/1_webp_ll.webp",
            "https://www.gstatic.com/webp/gallery3/2_webp_ll.webp",
            "https://www.gstatic.com/webp/gallery3/3_webp_ll.webp",
            "https://www.gstatic.com/webp/gallery3/4_webp_ll.webp",
            "https://www.gstatic.com/webp/gallery3/5_webp_ll.webp",
            "https://www.gstatic.com/webp/gallery3/1_webp_a.webp",
            "https://www.gstatic.com/webp/gallery3/2_webp_a.webp",
            "https://www.gstatic.com/webp/gallery3/3_webp_a.webp",
            "https://www.gstatic.com/webp/gallery3/4_webp_a.webp",
            "https://www.gstatic.com/webp/gallery3/5_webp_a.webp",
    };
    private static final String[] ANIM_WEBP = {
            //"https://raw.githubusercontent.com/1290846731/RecordMySelf/master/chect.webp",
            "https://www.gstatic.com/webp/animated/1.webp",
            "https://mathiasbynens.be/demo/animated-webp-supported.webp",
            "https://isparta.github.io/compare-webp/image/gif_webp/webp/2.webp",
            "http://osscdn.ixingtu.com/musi_file/20181108/a20540641eb7de9a8bf186261a8ccf57.webp",
    };

    private static final String[] ANIM_GIF = {
            "https://78.media.tumblr.com/a0c1be3183449f0d207a022c28f4bbf7/tumblr_p1p2cduAiA1wmghc4o1_500.gif",
            "https://78.media.tumblr.com/31ff4ea771940d2403323c1416b81064/tumblr_p1ymv2Xghn1qbt8b8o2_500.gif",
            "https://78.media.tumblr.com/45c7b305f0dbdb9a3c941be1d86aceca/tumblr_p202yd8Jz11uashjdo3_500.gif",
            "https://78.media.tumblr.com/167e9c5a0534d2718853a2e3985d64e2/tumblr_p1yth5CHXk1srs2u0o1_500.gif",
            "https://78.media.tumblr.com/e7548bfe04a9fdadcac440a5802fb570/tumblr_p1zj4dyrxN1u4mwxfo1_500.gif",
    };

    private List<String> getAnimatedWebpUrls() {
        List<String> webpUrls = new ArrayList<>(Arrays.asList(SIMPLE_WEBP));
        String resUrl = "android.resource://" + getPackageName() + "/" + R.drawable.broken;
        webpUrls.add(resUrl);
        return webpUrls;
    }
}
