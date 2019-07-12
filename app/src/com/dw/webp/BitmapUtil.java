package com.dw.webp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.util.SparseArray;

import com.dw.utils.StreamUtil;

public class BitmapUtil {

    private static final String TAG = "    BitmapUtil";
    private static final boolean DEBUG = false;

    public static Bitmap decodeFile(File f, int requireSize) {
        InputStream inputStream = null;
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            inputStream = new FileInputStream(f);
            BitmapFactory.decodeStream(inputStream, null, o);
            StreamUtil.closeStream(inputStream);
            // Find the correct scale value. It should be the power of 2.
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < requireSize || height_tmp / 2 < requireSize)
                    break;
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            inputStream = new FileInputStream(f);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, o2);
            StreamUtil.closeStream(inputStream);
            return bitmap;
        } catch (Throwable e) {
        } finally {
            StreamUtil.closeStream(inputStream);
        }
        return null;
    }

    public static Bitmap decodeFileMutable(File f ,int requireSize, boolean inMutable){
        InputStream inputStream = null;
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            inputStream = new FileInputStream(f);
            BitmapFactory.decodeStream(inputStream, null, o);
            StreamUtil.closeStream(inputStream);
            // Find the correct scale value. It should be the power of 2.
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < requireSize || height_tmp / 2 < requireSize)
                    break;
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            o2.inMutable = inMutable;
            inputStream = new FileInputStream(f);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, o2);
            StreamUtil.closeStream(inputStream);
            return bitmap;
        } catch (Throwable e) {
        } finally {
            StreamUtil.closeStream(inputStream);
        }
        return null;
    }

    public static Bitmap decodeFile(File imageFile) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(imageFile);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            return bitmap;
        } catch (Throwable e) {
        } finally {
            StreamUtil.closeStream(inputStream);
        }
        return null;
    }

    public static Bitmap decodeAssetsFile(Context context, String fileName,
                                          int requireSize) {
        InputStream inputStream = null;
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            inputStream = context.getAssets().open(fileName);
            BitmapFactory.decodeStream(inputStream, null, o);
            StreamUtil.closeStream(inputStream);
            // Find the correct scale value. It should be the power of 2.
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < requireSize || height_tmp / 2 < requireSize)
                    break;
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            inputStream = context.getAssets().open(fileName);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, o2);
            StreamUtil.closeStream(inputStream);
            return bitmap;
        } catch (Throwable e) {
        } finally {
            StreamUtil.closeStream(inputStream);
        }
        return null;
    }

    public static Bitmap decodeFileStream(InputStream is, int requireSize,
                                          int width, int height) {
        try {
            int scale = 1;
            if (requireSize > 0) {
                while (true) {
                    if (width / 2 < requireSize || height / 2 < requireSize)
                        break;
                    width /= 2;
                    height /= 2;
                    scale *= 2;
                }
            }
            if (DEBUG)
                Log.d(TAG, "scale = " + scale);
            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(is, null, o2);
        } catch (Exception e) {
        }
        return null;
    }

    public static Bitmap mergecandidatePreview_Dimcode(Bitmap candidatePreview,
                                                       Bitmap dimcodePreview) {
        if (candidatePreview == null || dimcodePreview == null || candidatePreview.isRecycled() || dimcodePreview.isRecycled())
            return null;
        int temsize = candidatePreview.getWidth() > candidatePreview
                .getHeight() ? candidatePreview.getHeight() : candidatePreview
                .getWidth();
        float size = (float) (temsize + 0.0);
        int width = (int) size;
        Bitmap new_pic = Bitmap.createBitmap((width * 2) + 6, width,
                Config.ARGB_8888);
        if (new_pic == null)
            return null;
        Canvas canvas = new Canvas(new_pic);
        canvas.drawColor(-1);
        float w1 = (float) (size / dimcodePreview.getWidth());
        float h1 = (float) (size / dimcodePreview.getHeight());
        canvas.drawBitmap(scale(w1, h1, dimcodePreview), 2, 2, null);
        float w2 = (float) (size / candidatePreview.getWidth());
        float h2 = (float) (size / candidatePreview.getHeight());
        canvas.drawBitmap(scale(w2, h2, candidatePreview), width + 4, 2, null);

        dimcodePreview = null;
        candidatePreview = null;
        return new_pic;
    }

    public static Bitmap scale(float scale_width, float scale_height, Bitmap bm) {
        if(bm == null) return null;
        Matrix matrix = new Matrix();
        matrix.postScale(scale_width, scale_height);
        Bitmap newBmp = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
                bm.getHeight(), matrix, true);
        return newBmp;
    }

    public static boolean fillBackgroundColor(String srcPath,
                                              String targetPath, int backgroundColor)
            throws FileNotFoundException {
        if (srcPath == null || targetPath == null) {
            return false;
        }
        File srcFile = new File(srcPath);
        if (!srcFile.exists()) {
            return false;
        }
        Bitmap srcBitmap = null;
        Bitmap targetBitmap = null;
        FileOutputStream fos = null;
        Canvas canvas;
        try {
            srcBitmap = BitmapFactory.decodeFile(srcPath);
            int width = srcBitmap.getWidth();
            int height = srcBitmap.getHeight();
            targetBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
            canvas = new Canvas(targetBitmap);
            canvas.drawColor(backgroundColor);
            canvas.drawBitmap(srcBitmap, 0, 0, null);
            fos = new FileOutputStream(targetPath);
            targetBitmap.compress(CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            return false;
        } finally {
            if (fos != null) {
                StreamUtil.closeStream(fos);
            }
            fos = null;
            canvas = null;
        }
        return true;
    }

    public static Drawable setGreyColorFiler(Drawable d) {
        if (d == null) {
            return null;
        }
        d.clearColorFilter();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter cf = new ColorMatrixColorFilter(cm);
        d.setColorFilter(cf);
        return d;
    }

    public static Drawable clearDrawableColorFilter(Drawable d) {
        if (d == null) {
            return null;
        }
        d.clearColorFilter();
        return d;
    }

    public static void getBitmapSize(File f, int[] oSize) {
        InputStream inputStream = null;
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            inputStream = new FileInputStream(f);
            BitmapFactory.decodeStream(inputStream, null, o);
//			StreamUtil.closeStream(inputStream);
            // Find the correct scale value. It should be the power of 2.
            oSize[0] = o.outWidth;
            oSize[1] = o.outHeight;
        } catch (Exception e) {

        } finally {
            StreamUtil.closeStream(inputStream);
        }
    }

    public static boolean isValidBitmap(File f) {
        int[] bitmapSize = new int[2];
        getBitmapSize(f, bitmapSize);
        if (bitmapSize[0] > 0 && bitmapSize[1] > 0) {
            return true;
        } else {
            return false;
        }
    }

    public static Bitmap decodeFileByWith(File f, int scale) {
        InputStream inputStream = null;
        try {
            // Decode image size

            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inSampleSize = scale;
            inputStream = new FileInputStream(f);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, o);
            return bitmap;
        } catch (FileNotFoundException e) {

        } finally {
            StreamUtil.closeStream(inputStream);
        }
        return null;
    }


    public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(CompressFormat.PNG, 100, output);

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static byte[] bmpToByteArray(final Bitmap bmp, Bitmap.CompressFormat format, int quality, final boolean needRecycle) {
        if (bmp == null) {
            return null;
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        if (format == null) {
            format = Bitmap.CompressFormat.JPEG;
        }
        if (quality > 100) quality = 100;
        else if (quality < 0) quality = 0;
        bmp.compress(format, quality, output);

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static Bitmap setCornerMark(Bitmap srcBitmap, Bitmap markBitmap, float markWidthRatio, float markHeightRatio, boolean recycleSrcBitmap) {
        if (srcBitmap == null || markBitmap == null || markWidthRatio == 0 || markHeightRatio == 0) {
            return srcBitmap;
        }
        int width = srcBitmap.getWidth();
        int height = srcBitmap.getHeight();
        Bitmap resultBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(srcBitmap, 0, 0, null);
        int markWidth = markBitmap.getWidth();
        int markHeight = markBitmap.getHeight();
        float markTargetWidth = markWidthRatio * width;
        float markTargetHeight = markHeightRatio * height;
        int left = width - (int)(markTargetWidth + 0.5f);
        int top = height - (int)(markTargetHeight + 0.5f);
        float widthScale = markTargetWidth / (float)markWidth;
        float heightScale = markTargetHeight / (float)markHeight;
        canvas.drawBitmap(scale(widthScale, heightScale, markBitmap), left, top, null);
        return resultBitmap;
    }

    public static Bitmap setRedDotMark(Bitmap srcBitmap, Bitmap markBitmap, float markWidthRatio, float markHeightRatio, boolean recycleSrcBitmap) {
        if (srcBitmap == null || markBitmap == null || markWidthRatio == 0 || markHeightRatio == 0) {
            return srcBitmap;
        }
        int width = srcBitmap.getWidth();
        int height = srcBitmap.getHeight();
        Bitmap resultBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(srcBitmap, 0, 0, null);
        int markWidth = markBitmap.getWidth();
        int markHeight = markBitmap.getHeight();
        float markTargetWidth = markWidthRatio * width;
        float markTargetHeight = markHeightRatio * height;
        int left = width - (int)(markTargetWidth + 0.5f);
        int top = 0;
        float widthScale = markTargetWidth / (float)markWidth;
        float heightScale = markTargetHeight / (float)markHeight;
        canvas.drawBitmap(scale(widthScale, heightScale, markBitmap), left, top, null);
        return resultBitmap;
    }

    public static boolean getBitmapFromInternet(String url, String localPath) {
        if (url == null || localPath == null ) return false;

        InputStream is = null;
        BufferedInputStream bis = null;
        OutputStream fos = null;
        BufferedOutputStream bos = null;
        try {

            URL mURL = new URL(url);
            is = mURL.openStream();
            bis = new BufferedInputStream(is);
            byte[] b = new byte[10240];
            int len = 0;

            File imageFile = new File(localPath);

            fos = new FileOutputStream(imageFile);
            bos = new BufferedOutputStream(fos);
            while ((len = bis.read(b)) != -1) {
                bos.write(b, 0, len);
            }
            bos.flush();

            return true;

        } catch (Exception e) {
            File resultFile = new File(localPath);
            if(resultFile != null && resultFile.isFile()) {
                resultFile.delete();
            }
        } finally {
            StreamUtil.closeStream(bos);
            StreamUtil.closeStream(fos);
            StreamUtil.closeStream(bis);
            StreamUtil.closeStream(is);
        }
        return false;

    }

    /**
     *
     * @param bmp
     * @param filePath
     * @param format  If it is null, using Bitmap.CompressFormat.JPEG.
     * @param quality 0 - 100
     * @return
     */
    public static boolean bm2File(Bitmap bmp, String filePath, CompressFormat format, int quality) {
        if (format == null) {
            format = Bitmap.CompressFormat.JPEG;
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filePath);
            if (quality > 100) quality = 100;
            else if (quality < 0) quality = 0;
            bmp.compress(format, quality, fos);
        } catch (Exception e) {
            return false;
        } finally {
            StreamUtil.closeStream(fos);
        }
        return true;
    }

    /**
     * getCircleImage
     *
     * @param source
     * @param min
     * @return
     */
    public static Bitmap createCircleImage(Bitmap source, int min)
    {
        min = min *3 /4;
        int oldWidth = source.getWidth();
        int oldHight = source.getHeight();
        float scale = ((float) min) / oldWidth;
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        Bitmap newbm = Bitmap.createBitmap(source, 0, 0, oldWidth, oldHight, matrix,
                true);
        int width = newbm.getWidth();
        int height = newbm.getHeight();
        int roundPx;
        int left,top,right,bottom,dst_left,dst_top,dst_right,dst_bottom;
        if (width <= height) {
            roundPx = width / 2;
            top = 0;
            bottom = width;
            left = 0;
            right = width;
            height = width;
            dst_left = 0;
            dst_top = 0;
            dst_right = width;
            dst_bottom = width;
        } else {
            roundPx = height / 2;
            int clip = (width - height) / 2;
            left = clip;
            right = width - clip;
            top = 0;
            bottom = height;
            width = height;
            dst_left = 0;
            dst_top = 0;
            dst_right = height;
            dst_bottom = height;
        }

        Bitmap output = Bitmap.createBitmap(width,
                height, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect src = new Rect(left, top, right, bottom);
        final Rect dst = new Rect(dst_left, dst_top, dst_right, dst_bottom);
        final RectF rectF = new RectF(dst);

        paint.setAntiAlias(true);

        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(newbm, src, dst, paint);
        newbm = null;
        return output;
    }

    public static Bitmap reduceImage(Bitmap source, int min)
    {

        min = min *3 /4;
        int oldWidth = source.getWidth();
        int oldHight = source.getHeight();
        float scale = ((float) min) / oldWidth;
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        Bitmap newbm = Bitmap.createBitmap(source, 0, 0, oldWidth, oldHight, matrix,
                true);
        return newbm;
    }

    public static Drawable getNewDrawable(Drawable drawable) {
        if (drawable.getConstantState() == null)
            return null;
        return drawable.getConstantState().newDrawable();
    }

    private static final class ColorParam implements Comparable<ColorParam>{
        private int color;
        private int count;

        private ColorParam(int color, int count) {
            this.color = color;
            this.count = count;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj == null) {
                return false;
            }
            if(obj == this) {
                return true;
            }
            if(obj instanceof ColorParam) {
                ColorParam cp = (ColorParam)obj;
                return cp.color == this.color;
            }
            else {
                return false;
            }
        }

        // 按照逆序排序
        @Override
        public int compareTo(ColorParam another) {
            if(this.count > another.count) {
                return -1;
            }
            else if(this.count< another.count) {
                return 1;
            }
            return 0;
        }
    }

    public static Bitmap drawableToBitamp(Drawable drawable) {
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        if(w <= 0 || h <= 0) {
            return null;
        }
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        Canvas canvas = new Canvas(bitmap);
        Rect oldBounds = drawable.copyBounds();
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        drawable.setBounds(oldBounds);
        return bitmap;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static int getCenterPrimaryColor(Drawable drawable, int defColor) {
        if(drawable == null) {
            return defColor;
        }
        if(drawable instanceof ColorDrawable && android.os.Build.VERSION.SDK_INT >= 11) {
            return ((ColorDrawable)drawable).getColor();
        }
        Bitmap bitmap = drawableToBitamp(drawable);
        if(bitmap == null) {
            return defColor;
        }
        int primaryColor = defColor;
        final int rawWidth = bitmap.getWidth();
        final int rawHeight = bitmap.getHeight();
        final int minSize = Math.min(rawWidth, rawHeight)/2;
        if(minSize <= 0) {
            return primaryColor;
        }
        final int clipX = (rawWidth - minSize) / 2;
        final int clipY = (rawHeight - minSize) / 2;
        bitmap = Bitmap.createBitmap(bitmap, clipX, clipY, minSize, minSize);
        SparseArray<ColorParam> colors = new SparseArray<ColorParam>();
        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();
        int color = 0, alpha;
        ColorParam colorParam;
        for(int i=0; i<width; ++i) {
            for(int j=0; j<height; ++j) {
                color = bitmap.getPixel(i, j);
                alpha = Color.alpha(color);
                // 透明度小于0.1
                if(alpha < 25) {
                    continue;
                }
                color = color | 0xff000000;
                colorParam = colors.get(color);
                if(colorParam == null) {
                    colorParam = new ColorParam(color, 1);
                    colors.put(color, colorParam);
                }
                else {
                    colorParam.count = colorParam.count + 1;
                }
            }
        }
        final int colorCount = colors.size();
        ColorParam primaryCp = new ColorParam(primaryColor, 0);
        ColorParam cp;
        for(int i=0; i<colorCount; ++i) {
            cp = colors.valueAt(i);
            if(cp.count > primaryCp.count) {
                primaryCp = cp;
            }
        }
        return primaryCp.color;
    }

    // 给bitmap着色
    public static Bitmap tintBitmap(Context context, Bitmap bitmap, final int tintColor) {
        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();
        Bitmap bitmap2 = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int color = 0;
        int rgb = tintColor & 0x00ffffff;
        for(int w=0; w<width; ++w) {
            for(int h=0; h<height; ++h) {
                color = bitmap.getPixel(w, h);
                color = color & 0xff000000 | rgb;
                bitmap2.setPixel(w, h, color);
            }
        }
        return bitmap2;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and
            // keeps both height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static Bitmap resizeBitmap(Bitmap bitmap, int size) {
        if(bitmap == null){
            return bitmap;
        }
        if((bitmap.getRowBytes() * bitmap.getHeight()) < (size * 1024)) {
            return bitmap;
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        float zoom = (float)Math.sqrt(size * 1024 / (float)out.toByteArray().length);

        Matrix matrix = new Matrix();
        matrix.setScale(zoom, zoom);
        Bitmap result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (bitmap != result) {
            bitmap.recycle();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
    public static Bitmap doBlur(Bitmap sentBitmap, int radius, boolean canReuseInBitmap) {

        Bitmap bitmap;
        if (canReuseInBitmap) {
            bitmap = sentBitmap;
        } else {
            bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
        }

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);
        return (bitmap);
    }
}