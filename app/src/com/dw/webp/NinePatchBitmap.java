package com.dw.webp;

import android.graphics.Bitmap;

/**
 * Created by dw on 18-12-20.
 */

public class NinePatchBitmap {

    private final Bitmap bitmap;

    public Bitmap getBitmap() {
        return bitmap;
    }

    public NinePatchBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
