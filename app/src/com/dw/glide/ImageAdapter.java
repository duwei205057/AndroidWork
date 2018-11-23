package com.dw.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.dw.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dw on 18-11-20.
 */

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private Context mContext;
    private List<String> mImageUrls;

    public ImageAdapter(Context context, List<String> urls) {
        mContext = context;
        mImageUrls = new ArrayList<>();
        mImageUrls.addAll(urls);
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
        loadImage(holder.target, url);
//        loadImage(holder.imageView, url);
    }

    @Override
    public void onViewRecycled(ImageViewHolder holder) {
        super.onViewRecycled(holder);
//        Glide.clear(holder.imageView);
    }

    private void loadImage(InnerTarget target, String url) {
        Glide.with(mContext).load(url)
//                .asBitmap()
                .placeholder(R.drawable.image_loading)
                .error(R.drawable.image_error)
                .into(target);
    }

    private void loadImage(ImageView imageView, String url) {
        Glide.with(mContext).load(url)
                .asBitmap()
                .placeholder(R.drawable.image_loading)
                .error(R.drawable.image_error)
                .into(imageView);
    }

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
        public void onLoadFailed(Exception e, Drawable errorDrawable) {
            super.onLoadFailed(e, errorDrawable);
            LOGD("----------onLoadFailed---------e="+e+" errorDrawable=="+errorDrawable);
            imageView.setImageDrawable(errorDrawable);
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

        @Override
        public void onResourceReady(Object resource, GlideAnimation glideAnimation) {
            LOGD("----------onResourceReady---------resource="+resource+"  glideAnimation="+glideAnimation);
            if (resource instanceof Bitmap)
                imageView.setImageDrawable(new BitmapDrawable((Bitmap)resource));
            else if (resource instanceof Drawable)
                imageView.setImageDrawable((Drawable) resource);
        }
    }

    private void LOGD(String message) {
        Log.d("xx",message);
    }
}
