package com.dw.glide;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {

        public ImageViewHolder(View itemView) {
            super(itemView);
        }

    }

}
