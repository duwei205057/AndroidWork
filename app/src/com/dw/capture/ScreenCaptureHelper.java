package com.dw.capture;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by dw on 18-9-19.
 */

public class ScreenCaptureHelper {

    private static Intent mResultData = null;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private ImageReader mImageReader;

    private int mScreenWidth;
    private int mScreenHeight;
    private int mScreenDensity;

    private Context mContext;
    private static volatile ScreenCaptureHelper mInstance;

    public static Intent getmResultData() {
        return mResultData;
    }

    public static void setmResultData(Intent mResultData) {
        ScreenCaptureHelper.mResultData = mResultData;
    }

    public static ScreenCaptureHelper getInstance(Context context) {
        if (mInstance == null)
            synchronized (ScreenCaptureHelper.class) {
                if (mInstance == null)
                    mInstance = new ScreenCaptureHelper(context.getApplicationContext());
            }
        return mInstance;
    }

    private ScreenCaptureHelper(Context context) {
        mContext = context;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;

        mImageReader = ImageReader.newInstance(mScreenWidth, mScreenHeight, PixelFormat.RGBA_8888, 1);
    }


    private void startScreenShot(final Rect rect,Callback callback) {


//        Handler handler1 = new Handler();
        startVirtual();

        startCapture(rect, callback);
    }

    public void startVirtual() {
        if (mMediaProjection != null) {
            virtualDisplay();
        } else if(setUpMediaProjection()) {
                virtualDisplay();
        }
    }

    public boolean setUpMediaProjection() {
        if (mResultData == null) {
            return false;
        } else {
            MediaProjectionManager mpm = (MediaProjectionManager) mContext.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            mMediaProjection = mpm.getMediaProjection(Activity.RESULT_OK, mResultData);
            return true;
        }
    }

    private void virtualDisplay() {
        mVirtualDisplay = mMediaProjection.createVirtualDisplay("screen-mirror",
                mScreenWidth, mScreenHeight, mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(), null, null);
    }

    public void startCapture(Rect rect,Callback callback) {

        Image image = mImageReader.acquireLatestImage();

        if (image == null) {
            startScreenShot(rect, callback);
        } else {
            SaveTask mSaveTask = new SaveTask(callback);
            mSaveTask.execute(image, rect);
        }
    }

    public interface Callback {
        void getBitmap(Bitmap bitmap);
    }


    public class SaveTask extends AsyncTask<Object, Void, Bitmap> {
        Callback mCallback;

        public SaveTask(Callback callback) {
            mCallback = callback;
        }

        @Override
        protected Bitmap doInBackground(Object... params) {

            if (params == null || params.length < 2 || params[0] == null || params[1] == null) {

                return null;
            }

            Image image = (Image)params[0];
            Rect rect = (Rect)params[1];

            int width = image.getWidth();
            int height = image.getHeight();
            final Image.Plane[] planes = image.getPlanes();
            final ByteBuffer buffer = planes[0].getBuffer();
            //每个像素的间距
            int pixelStride = planes[0].getPixelStride();
            //总的间距
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * width;
            int bitmapWidth =  width + rowPadding / pixelStride;
            int bitmapHeight = height;
            Bitmap bit = outPutToFile(bitmapWidth, bitmapHeight, buffer, rect, Bitmap.Config.ARGB_8888);

            image.close();
            return bit;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            //预览图片
            if (bitmap != null) {
                mCallback.getBitmap(bitmap);
            }
        }
    }

    private Bitmap outPutToFile(int width, int height, ByteBuffer buffer, Rect rect, Bitmap.Config config) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, config);
        bitmap.copyPixelsFromBuffer(buffer);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
        File fileImage = null;
        if (bitmap != null) {
            Canvas canvas = new Canvas();
            canvas.setBitmap(bitmap);
            Paint p = new Paint();
            p.setStyle(Paint.Style.STROKE);
            p.setTextSize(60);
            p.setStrokeWidth(5);
            p.setColor(0xFFFF0000);
            canvas.drawRect(rect, p);
            canvas.drawText(rect.toString(), 0, rect.toString().length(), 300, 300, p);
            Bitmap bitmap1 = Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height());
            return bitmap1;
        }
        return null;
    }
}
