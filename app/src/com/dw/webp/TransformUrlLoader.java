package com.dw.webp;

import com.bumptech.glide.load.Option;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.ModelCache;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;

import java.io.InputStream;

public class TransformUrlLoader implements ModelLoader<TransformUrl, InputStream> {

    private final ModelCache<TransformUrl, TransformUrl> modelCache;

    public static final Option<Integer> TIMEOUT = Option.memory(
            "com.bumptech.glide.load.model.stream.HttpGlideUrlLoader.Timeout", 2500);

    public TransformUrlLoader(ModelCache<TransformUrl, TransformUrl> modelCache) {
        this.modelCache = modelCache;
    }

    @Override
    public LoadData<InputStream> buildLoadData(TransformUrl model, int width, int height,
                                               Options options) {
        // GlideUrls memoize parsed URLs so caching them saves a few object instantiations and time
        // spent parsing urls.
        TransformUrl url = model;
        if (modelCache != null) {
            url = modelCache.get(model, 0, 0);
            if (url == null) {
                modelCache.put(model, 0, 0, model);
                url = model;
            }
        }
        int timeout = options.get(TIMEOUT);
        return new LoadData<>(url, new TransformUrlFetcher(new GlideUrl(url.getUrl()), timeout));
    }

    @Override
    public boolean handles(TransformUrl transformUrl) {
        return true;
    }


    /**
     */
    public static class Factory implements ModelLoaderFactory<TransformUrl, InputStream> {
        private final ModelCache<TransformUrl, TransformUrl> modelCache = new ModelCache<>(500);

        @Override
        public ModelLoader<TransformUrl, InputStream> build(MultiModelLoaderFactory multiFactory) {
            return new TransformUrlLoader(modelCache);
        }

        @Override
        public void teardown() {
            // Do nothing.
        }
    }
}
