package com.dw;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.aop.AspectBean;
import com.aop.DebugTrace;
import com.database.DBActivity;
import com.database.IntroExampleActivity;
import com.database.SomeFileObserver;
import com.dw.crash.NativeInterface;
import com.dw.fragments.BookListActivity;
import com.dw.gif.GifActivity;
import com.dw.glide.GlideActivity;
import com.dw.js.JSActivity;
import com.dw.recycler.RecyclerList;
import com.dw.resizeicon.ResizeUtils;
import com.dw.touchable.MotionActivity;
import com.dw.utils.RedoWorker;
import com.dw.voice.VoiceContainerActivity;
import com.inject.hack.HackLoad;
import com.sogou.nativecrashcollector.BacKTraceFactory;
import com.sogou.nativecrashcollector.BackTrace;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


public class MainActivity extends Activity {

    Logger log;
    DyHelper dyHelper = new DyHelper();
    Button btn;
    ListView mList;
    View mMove;
    ViewGroup root;
    MyView a;
    MyView b;
    MyView c;
    SomeFileObserver sfo;
    AtomicInteger mAI = new AtomicInteger();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = (ViewGroup)LayoutInflater.from(this).inflate(R.layout.activity_main,null);
        setContentView(root);
        log = Logger.getLogger("lavasoft");
        log.setLevel(Level.ALL);
        getMetaData();
        btn = (Button)findViewById(R.id.button);
        mList = (ListView)findViewById(R.id.mList);
        mMove = findViewById(R.id.move_view);
        AspectBean bean = new AspectBean(5);
        bean.setAge(10);
        EventBus.getDefault().register(this);
        //启动截图功能
//        startActivity(new Intent(this, ScreenCaptureActivity.class));

//        BlockDetectByChoreographer.start();
//        BlockDetectByPrinter.start();

        /*a = new MyView(this,0xFF0000,"red");
        ViewGroup.MarginLayoutParams am = new ViewGroup.MarginLayoutParams(500,500);
        root.addView(a,am);
        b = new MyView(this,0x00FF00,"Green");
        b.setItem(new MyView.OnTouchListen() {
            @Override
            public void getTouch(MotionEvent e, View v) {
                root.removeView(b);
                root.addView(b);
            }
        });
        ViewGroup.MarginLayoutParams bm = new ViewGroup.MarginLayoutParams(800,800);
        root.addView(b,bm);
        c = new MyView(this,0x0000FF,"Blue");
        ViewGroup.MarginLayoutParams cm = new ViewGroup.MarginLayoutParams(400,400);
        cm.leftMargin = 500;
        cm.topMargin = 500;
        root.addView(c,cm);*/
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void event(MessageEvent messageEvent){

    }

    class MessageEvent {
        private String message;

        public MessageEvent(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    protected void onResume(){
        super.onResume();
        btn.setFocusable(true);
        btn.setFocusableInTouchMode(true);
        btn.requestFocus();
        btn.requestFocusFromTouch();
        if (mReceiver == null) mReceiver = new MyReceiver();
        mHandlerThread = new HandlerThread("async");
        mHandlerThread.start();
        Handler h = new Handler(mHandlerThread.getLooper());
        registerReceiver(mReceiver, mIntentFilter, null, h);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
        mHandlerThread.quit();
    }

    public void registAsyncReceiver(View view){
        sendBroadcast(new Intent("dw.asyncreceiver"));
    }

    HandlerThread mHandlerThread;
    MyReceiver mReceiver;
    IntentFilter mIntentFilter = new IntentFilter("dw.asyncreceiver");
    class MyReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            LOGD("MyReceiver onReceive thread id ="+Thread.currentThread().getId()+" name="+Thread.currentThread().getName());
        }
    }


    private void getMetaData() {
        try {
            ApplicationInfo info = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            Log.d("xx",info.metaData.get("versiondate").toString());
            log.info("info.metaData.get(\"versiondate\").toString()");
            log.fine("info.metaData.get(\"versiondate\").toString()");
            System.out.println(HackLoad.class);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

//    @BackTrace
    public void testDy(View view){
        dyHelper.handleButtonClicked(view);
        /*try {
            execShellCmd("input swipe 200 1400 500 1400");
        } finally {
        }*/
        setListData();
        RedoWorker rw = new RedoWorker(Looper.getMainLooper());
        if (rw.ismIsWorking()) rw.done();
        else {
            rw.start(new Runnable() {
                @Override
                @DebugTrace
                public void run() {
                    Log.d("xx", "---------------testDy--------------------");
                }
            },50,100,300);
        }
//        FileMapUtils.load();
    }

    public void showRecyclerView(View view){
        Intent i = new Intent(this,RecyclerList.class);
        startActivity(i);
    }

    public void showMotionView(View view){
        Intent i = new Intent(this,MotionActivity.class);
        startActivity(i);
    }

    public void showVoiceView(View view){
//        test();
        Intent i = new Intent(this,VoiceContainerActivity.class);
        startActivity(i);
//        ScreenCaptureHelper.getInstance(this).startCapture(new Rect(0, 0, 2, 4), new ScreenCaptureHelper.Callback() {
//            @Override
//            public void getBitmap(Bitmap bitmap) {
//                test1(bitmap);
//            }
//        });
    }

    private void test(){
        Log.d("xx","mInterface.getStringFromNative()===================="+ NativeInterface.getInstance().getStringFromNative());
        BitmapFactory.Options op = new BitmapFactory.Options();
//        int drawableId = R.drawable.books;
        int drawableId = R.drawable.imageb1537339657774;
        Bitmap b1 = BitmapFactory.decodeResource(getResources(), drawableId);
        op.inPreferredConfig = Bitmap.Config.RGB_565;
//        op.inSampleSize = 2;
        Bitmap b2 = BitmapFactory.decodeResource(getResources(), drawableId, op);
        Log.d("xx","原大小:"+b1.getByteCount()+" 改变后:"+b2.getByteCount());
        try {
            FileOutputStream fo = openFileOutput("a.png", Context.MODE_PRIVATE);
            b2.compress(Bitmap.CompressFormat.PNG, 50, fo);
            fo.close();
            @SuppressLint("ResourceType") InputStream is = getResources().openRawResource(drawableId);
            Log.d("xx","resource inputstream size=="+is.available());
            is.close();
            is = openFileInput("a.png");
            Log.d("xx","a file inputstream size=="+is.available());
            fo = openFileOutput("b.png", Context.MODE_PRIVATE);
            GZIPOutputStream go = new GZIPOutputStream(fo);
            int a = -1;
            while ((a = is.read()) != -1)
                go.write(a);
            go.close();
            fo.close();
            GZIPInputStream gi = new GZIPInputStream(openFileInput("b.png"));
            fo = openFileOutput("c.png", Context.MODE_PRIVATE);
            while ((a = gi.read()) != -1)
                fo.write(a);
            gi.close();
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("xx","原大小:"+b1.getByteCount()+" 改变后:"+b2.getByteCount());
        if (b1 != null && !b1.isRecycled()) b1.recycle();
        if (b2 != null && !b2.isRecycled()) b2.recycle();
    }

    private void test1(Bitmap bitmap){
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int[] pixels = new int[w * h];
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h);
        Bitmap b2 = Bitmap.createBitmap(pixels, 0, w, w, h, Bitmap.Config.RGB_565);
        b2.getPixels(pixels, 0, w, 0, 0, w, h);
        Bitmap b3 = Bitmap.createBitmap(pixels, 0, w, w, h, Bitmap.Config.ALPHA_8);
        b3.getPixels(pixels, 0, w, 0, 0, w, h);


        Bitmap b4 = Bitmap.createBitmap(5, 5, Bitmap.Config.ALPHA_8);
        Canvas canvas = new Canvas();
        canvas.setBitmap(b4);
        Paint p = new Paint();
        p.setColor(0x66000000);
        canvas.drawRect(new Rect(1,1,4,4), p);
        int[] pixels1 = new int[5 * 5];
        b4.getPixels(pixels1, 0, 5, 0, 0, 5, 5);

        outputBitmap(bitmap,Bitmap.CompressFormat.PNG,30);
        outputBitmap(b2,Bitmap.CompressFormat.PNG,30);
        outputBitmap(b3,Bitmap.CompressFormat.PNG,30);
        outputBitmap(bitmap,Bitmap.CompressFormat.JPEG,100);
        outputBitmap(bitmap,Bitmap.CompressFormat.JPEG,50);
        outputBitmap(b2,Bitmap.CompressFormat.JPEG,100);
        outputBitmap(b2,Bitmap.CompressFormat.JPEG,30);
        outputBitmap(b3,Bitmap.CompressFormat.JPEG,30);
        outputBitmap(b4,Bitmap.CompressFormat.PNG,100);

        btn.setBackground(new BitmapDrawable(getResources(), b4));

    }

    private void outputBitmap(Bitmap bitmap, Bitmap.CompressFormat format, int quality) {
        try {
            Log.d("xx",format + "------------------------------Bitmap大小:"+bitmap.getByteCount());
            FileOutputStream fo = openFileOutput("sample", Context.MODE_PRIVATE);
            bitmap.compress(format, quality, fo);
            fo.close();
            InputStream is = openFileInput("sample");
            Log.d("xx",format + "------------------------------File大小:"+is.available());
            is.close();
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            bitmap.compress(format, quality, bao);
            Log.d("xx",format + "------------------------------bytearray大小:"+bao.size());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void startGif(View view){
        Intent i = new Intent(this,GifActivity.class);
        startActivity(i);
    }

    public void startSqlite(View view){
        Intent i = new Intent(this,DBActivity.class);
        startActivity(i);
    }
    public void startJS(View view){
        Intent i = new Intent(this,JSActivity.class);
        startActivity(i);
    }

    public void startRealm(View view){
        Intent i = new Intent(this,IntroExampleActivity.class);
        startActivity(i);
        /*if (!Helper.checkSelected(this)){
            Helper.promptImeSelect(this);
        }
        if (Helper.checkSelected(this) && !Helper.checkDefault(this)){
            Helper.promptImePick(this);
        }*/
    }

//    @BackTrace
    public void startNative(View view){
        /*AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                String s = mInterface.getStringFromNative();
                Log.d("xx","mInterface.getStringFromNative()===================="+s);
            }
        });*/
//        String s = NativeInterface.getInstance().getStringFromNative();
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        new Thread(new Runnable() {
            @Override

            public void run() {
                Log.d("xx","mInterface.getStringFromNative()====================pid=="+ Process.myPid()+" thread name="+Thread.currentThread().getName());
                getStringFromNative();
            }
        }).start();
        String s = getStringFromNative();
        Log.d("xx","mInterface.getStringFromNative()===================="+s);
        Button b = (Button)view;
//        b.setText(b.getText()+"_"+s);
    }

    @BackTrace
    private String getStringFromNative() {
        String s = NativeInterface.getInstance().getCrashStringFromNative();
        s += "  " + NativeInterface.getInstance().getStringFromNative();
        Log.d("xx","mInterface.getStringFromNative()===================="+s);
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        return s;
    }

    public void startGlide (View view) {
        Intent i = new Intent(this,GlideActivity.class);
        startActivity(i);
    }

    public void startFragment (View view) {
        Intent i = new Intent(this,BookListActivity.class);
        startActivity(i);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void resizeIcon(View view){
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                return;
            }
            ResizeUtils.resizeThemeNew("/sdcard/res",ResizeUtils.DEFAULT_TRANSFER_ICONS_RAW,1080,720);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void setListData(){
        View head = new View(this);
        head.setBackground(new ColorDrawable(Color.RED));
        float headHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,20,getResources().getDisplayMetrics());
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int)headHeight);
        head.setLayoutParams(lp);
        mList.setAdapter(new ArrayAdapter<String>(this,R.layout.list_item,new String[]{"A","B","C"}));
        mList.addHeaderView(head);
    }

    private void execShellCmd(String cmd) {

        try {
            // 申请获取root权限，这一步很重要，不然会没有作用
            java.lang.Process process = Runtime.getRuntime().exec("su");
            // 获取输出流
            OutputStream outputStream = process.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(
                    outputStream);
            dataOutputStream.writeBytes(cmd);
            dataOutputStream.flush();
            dataOutputStream.close();
            outputStream.close();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


    private void LOGD(String message){
        Log.d("xx", message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sfo != null) sfo.stopWatching();
    }
}
