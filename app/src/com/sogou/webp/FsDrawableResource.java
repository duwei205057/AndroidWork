package com.sogou.webp;

import com.bumptech.glide.load.resource.drawable.DrawableResource;

public class FsDrawableResource extends DrawableResource<FrameSequenceDrawable> {

    public FsDrawableResource(FrameSequenceDrawable drawable) {
        super(drawable);
    }

    @Override
    public Class<FrameSequenceDrawable> getResourceClass() {
        return FrameSequenceDrawable.class;
    }


    @Override
    public int getSize() {
        return drawable.getSize();
    }

    @Override
    public void recycle() {
        drawable.stop();
        drawable.destroy();
    }
}
