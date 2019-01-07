package com.dw.webp;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

@GlideModule
public class WebpModule extends AppGlideModule {

    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {
    }

    @Override
    public boolean isManifestParsingEnabled() {
        return true;
    }

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
    }
}
