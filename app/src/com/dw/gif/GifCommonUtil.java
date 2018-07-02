package com.dw.gif;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;

import java.lang.reflect.Field;

/**
 * Created by tianmiao on 17-6-7.
 */
public class GifCommonUtil {
    public static final int DARK_MODE_COLOR_BAIS = -90;
    public static float[] DARK_COLOR_MATRIX = new float[] {
            1, 0, 0, 0, DARK_MODE_COLOR_BAIS,
            0, 1, 0, 0, DARK_MODE_COLOR_BAIS,
            0, 0, 1, 0, DARK_MODE_COLOR_BAIS,
            0, 0, 0, 1, 0 };

    public static boolean checkMemory(int memorySize) {
        int maxMemory = (int) (Runtime.getRuntime().maxMemory());
        int totalMemory = (int) (Runtime.getRuntime().totalMemory());
        int freeMemory = (int) (Runtime.getRuntime().freeMemory());
        int memory = 2 * (maxMemory - totalMemory + freeMemory) / 3;
        return memorySize < memory;
    }

    public static Bitmap getDefaultBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.parseColor("#f2f2f2"));
        return bitmap;
    }

    public static Drawable checkDarkMode(Drawable d, boolean darkModeOn) {
        if (d == null) return null;
        clearColorFilter(d);
        if (darkModeOn) {
            d.setColorFilter(new ColorMatrixColorFilter(DARK_COLOR_MATRIX));
        }
        return d;
    }

    private static void clearColorFilter(Drawable d) {
        if (Build.VERSION.SDK_INT >= 21 && Build.VERSION.SDK_INT <= 22
                && (d instanceof StateListDrawable)) {
            d.clearColorFilter();
            try {
                StateListDrawable stateListDrawable = (StateListDrawable)d;
                DrawableContainer.DrawableContainerState mDrawableContainerState = (DrawableContainer.DrawableContainerState)stateListDrawable.getConstantState();
                if (mDrawableContainerState != null) {
                    Field field2 = DrawableContainer.DrawableContainerState.class
                            .getDeclaredField("mHasColorFilter");
                    field2.setAccessible(true);
                    field2.set(mDrawableContainerState, true);
                }
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        } else {
            d.clearColorFilter();
        }
    }
}
