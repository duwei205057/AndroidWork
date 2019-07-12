package com.dw.webp;

import com.bumptech.glide.load.Key;

import java.security.MessageDigest;

public class TransformUrl implements Key {


    private String mUrl;
    private volatile byte[] cacheKeyBytes;

    public TransformUrl(String url) {
        this.mUrl = url;
    }

    public String getUrl() {
        return mUrl;
    }

    private byte[] getCacheKeyBytes() {
        if (cacheKeyBytes == null) {
            cacheKeyBytes = mUrl.getBytes(CHARSET);
        }
        return cacheKeyBytes;
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {
        messageDigest.update(getCacheKeyBytes());
    }
}
