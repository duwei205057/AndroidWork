package com.dw.glide;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;

import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.dw.DynamicApplication;
import com.dw.utils.FileOperator;

import java.io.File;

/**
 * Created by dw on 18-12-7.
 */

public class ImageLoader {

    /**
     * 预下载图片，防止大图分享出现的OOM
     */
    public void preloadImage(String folder, final String url, final OnLoadResListener listener) {
        if (TextUtils.isEmpty(folder) || TextUtils.isEmpty(url)) {
            if (listener != null) {
                listener.OnLoadComplate(null, false);
            }

            return;
        }

        String key = revertPhotoPath(url);
        String imagePath = folder + key;
        FileOperator.createDirectory(folder, false, false);
        final File file = new File(imagePath);
        if (file.exists()) {
            if (listener != null) {
                listener.OnLoadComplate("", true);
            }

            return;
        }
        ((DrawableTypeRequest)Glide.with(DynamicApplication.mRealApplication).load(url).diskCacheStrategy(DiskCacheStrategy.NONE)).downloadOnly(new SimpleTarget<File>() {
            @Override
            public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
                Log.d("xx","onResourceReady");
            }

            @Override
            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                super.onLoadFailed(e, errorDrawable);
            }
        });

    }

    public static String revertPhotoPath(String path) {
        if(path == null)
            return null;
        return path.replace('.', '_').replace('/', '_').replace(':', '_').replace('?', '_').replace('&', '-').replace('%', '_');
    }
}
