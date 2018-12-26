package com.sogou.webp;

import com.bumptech.glide.load.engine.Resource;

/**
 * Created by dw on 18-12-20.
 */

public class NinePatchBitmapResource implements Resource{

    private final NinePatchBitmap ninePatchBitmap;


    public NinePatchBitmapResource(NinePatchBitmap npb) {
        ninePatchBitmap = npb;
    }

    @Override
    public Class getResourceClass() {
        return NinePatchBitmap.class;
    }

    @Override
    public Object get() {
        return ninePatchBitmap;
    }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public void recycle() {

    }
}
