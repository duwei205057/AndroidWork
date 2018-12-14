package com.dw.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.dw.R;
import com.sogou.webp.GlideApp;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by dw on 18-11-20.
 */

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private Context mContext;
    private List<String> mImageUrls;

    public ImageAdapter(Context context) {
        mContext = context;
        mImageUrls = new ArrayList<>();
        mImageUrls.addAll(getAnimatedWebpUrls());
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.webp_image_item, null);
        ImageViewHolder viewHolder = new ImageAdapter.ImageViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        String url = mImageUrls.get(position);
        holder.imageView.setTag(R.id.webp_image,url);
        holder.textView.setText(url);
//        loadImage(holder.target, url);
        loadImage(holder.imageView, url);
    }

    @Override
    public void onViewRecycled(ImageViewHolder holder) {
        super.onViewRecycled(holder);
//        Glide.clear(holder.imageView);
    }

    private void loadImage(InnerTarget target, String url) {
        ImageLoader il = new ImageLoader();
        il.preloadImage("/sdcard/debug/",url ,null);
    }

    private void loadImage(final ImageView imageView, String url) {
        RequestOptions myOptions = new RequestOptions()
                .placeholder(R.drawable.image_loading)
                .error(R.drawable.image_error).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).override(50,50);
        GlideApp.with(mContext)/*.asBitmap()*/.load(new File("/sdcard/xingxing1.gif"))
//                .asBitmap()
                .apply(myOptions)
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                        imageView.setImageDrawable(resource);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                    }
                });
//        GlideApp.with(mContext)/*.asBitmap()*/.load(url)
////                .asBitmap()
//                .apply(myOptions)
//                .into(new SimpleTarget<Drawable>() {
//                    @Override
//                    public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
//                        imageView.setImageDrawable(resource);
//                    }
//                });
//        GlideApp.with(mContext).downloadOnly().load(url).into(new SimpleTarget<File>() {
//            @Override
//            public void onResourceReady(File resource, Transition<? super File> transition) {
//                Log.d("xx","resource==="+resource);
//            }
//        });
    }

    private List<String> getAnimatedWebpUrls() {
        List<String> webpUrls = new ArrayList<>();
//        List<String> webpUrls = new ArrayList<>(Arrays.asList(ANIM_GIF));
//        String resUrl = "android.resource://" + getPackageName() + "/" + R.drawable.last_wp;
//        String resUrl = "https://www.gstatic.com/webp/animated/1.webp";
        String resUrl = "https://www.gstatic.com/webp/gallery3/1_webp_ll.webp";
//        String resUrl = "android.resource://" + mContext.getPackageName() + "/" + R.drawable.broken;
        webpUrls.add(resUrl);
//        webpUrls.add("https://www.gstatic.com/webp/animated/1.webp");
//        webpUrls.add("https://78.media.tumblr.com/a0c1be3183449f0d207a022c28f4bbf7/tumblr_p1p2cduAiA1wmghc4o1_500.gif");
//        webpUrls.add("http://www.gstatic.com/webp/gallery/1.webp");
        return webpUrls;
    }

    private static final String[] SIMPLE_WEBP = {
//            "http://img1.dzwww.com:8080/tupian_pl/20150813/16/7858995348613407436.jpg",
            "http://www.gstatic.com/webp/gallery/1.webp",
//            "http://www.gstatic.com/webp/gallery/2.webp",
//            "http://www.gstatic.com/webp/gallery/3.webp",
//            "http://www.gstatic.com/webp/gallery/4.webp",
//            "http://www.gstatic.com/webp/gallery/5.webp",
//            "http://osscdn.ixingtu.com/musi_file/20181108/a20540641eb7de9a8bf186261a8ccf57.webp",
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
//            "https://78.media.tumblr.com/31ff4ea771940d2403323c1416b81064/tumblr_p1ymv2Xghn1qbt8b8o2_500.gif",
//            "https://78.media.tumblr.com/45c7b305f0dbdb9a3c941be1d86aceca/tumblr_p202yd8Jz11uashjdo3_500.gif",
//            "https://78.media.tumblr.com/167e9c5a0534d2718853a2e3985d64e2/tumblr_p1yth5CHXk1srs2u0o1_500.gif",
//            "https://78.media.tumblr.com/e7548bfe04a9fdadcac440a5802fb570/tumblr_p1zj4dyrxN1u4mwxfo1_500.gif",
    };

    @Override
    public int getItemCount() {
        return mImageUrls.size();
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView textView;
        InnerTarget target;

        public ImageViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.webp_image);
            textView = (TextView) itemView.findViewById(R.id.webp_text);
            target = new InnerTarget(imageView);
        }

    }

    class InnerTarget extends SimpleTarget{

        ImageView imageView;

        public InnerTarget(ImageView imageView) {
            super();
            this.imageView = imageView;
        }

        @Override
        public void setRequest(Request request) {
            super.setRequest(request);
        }

        @Override
        public Request getRequest() {
            return super.getRequest();
        }

        @Override
        public void onLoadCleared(Drawable placeholder) {
            super.onLoadCleared(placeholder);
            LOGD("----------onLoadCleared---------placeholder="+placeholder);
        }

        @Override
        public void onLoadStarted(Drawable placeholder) {
            super.onLoadStarted(placeholder);
            LOGD("----------onLoadStarted---------placeholder="+placeholder);
            imageView.setImageDrawable(placeholder);
        }

        @Override
        public void onResourceReady(Object resource, Transition transition) {
            LOGD("----------onResourceReady---------resource="+resource+"  transition="+transition);
            if (resource instanceof Bitmap)
                imageView.setImageDrawable(new BitmapDrawable((Bitmap)resource));
            else if (resource instanceof Drawable)
                imageView.setImageDrawable((Drawable) resource);
//            if (resource instanceof GlideDrawable) {
//                ((GlideDrawable) resource).start();
//            }
        }

        @Override
        public void onStart() {
            super.onStart();
            LOGD("----------onStart---------");
        }

        @Override
        public void onStop() {
            super.onStop();
            LOGD("----------onStop---------");
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            LOGD("----------onDestroy---------");
        }

        public InnerTarget(int width, int height) {
            super(width, height);
        }

    }

    private void LOGD(String message) {
        Log.d("xx",message);
    }

}
