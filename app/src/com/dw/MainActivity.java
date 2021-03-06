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
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
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
import android.os.StatFs;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.text.format.Formatter;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import com.dw.databinding.ActivityMainBinding;
import com.dw.fragments.BookListActivity;
import com.dw.gif.GifActivity;
import com.dw.glide.GlideActivity;
import com.dw.http.Test;
import com.dw.js.JSActivity;
import com.dw.resizeicon.ResizeUtils;
import com.dw.touchable.MotionActivity;
import com.dw.utils.RedoWorker;
import com.dw.voice.VoiceContainerActivity;
import com.inject.hack.HackLoad;
import com.sogou.nativecrashcollector.BackTrace;
import com.sogou.nativecrashcollector.NativeCrashManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
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
    MyView a;
    MyView b;
    MyView c;
    SomeFileObserver sfo;
    AtomicInteger mAI = new AtomicInteger();
    ActivityMainBinding mActivityMain;//DataBindingUtil.setContentView比较耗时

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setTheme(android.R.style.Theme_DeviceDefault_Light_NoActionBar);
//        mActivityMain = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setContentView(R.layout.activity_main);
        log = Logger.getLogger("lavasoft");
        log.setLevel(Level.ALL);
        getMetaData();
        btn = (Button) findViewById(R.id.button);
        mList = (ListView)findViewById(R.id.mList) ;
        mMove = (MoveView)findViewById(R.id.move_view);
        AspectBean bean = new AspectBean(5);
        bean.setAge(10);
        EventBus.getDefault().register(this);
        LayoutBean lbean = new LayoutBean();
        lbean.firstText = "Click Me";
//        mActivityMain.setBean(lbean);
        btn.setText("");
        //启动截图功能
//        startActivity(new Intent(this, ScreenCaptureActivity.class));


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

    private void readWeb() {
        try {
            TypedValue value = new TypedValue();
            @SuppressLint("ResourceType")
            InputStream in = getResources().openRawResource(R.drawable.last_wp);
            Log.d("xx","webp head = "+(char)(in.read() & 0xFF) + (char)(in.read() & 0xFF) + (char)(in.read() & 0xFF) + (char)(in.read() & 0xFF));
            in.close();
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        getApplicationContext().registerReceiver(mScreenOffReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        h.post(new Runnable() {
            @Override
            public void run() {
                Log.d("xx"," int other thread");
                try {
                    URL url = new URL("http://qs.shouji.sogou.com/gettpl/0978c499e4fb8b16c6be6d1be8b5fbc2");
                    URLConnection conn = url.openConnection();
                    conn.connect();
                    conn.getInputStream();
                    Log.d("xx"," int other thread");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
//        copyFile();
//        readWeb();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
        mHandlerThread.quit();
    }

    private void copyFile() {
        try {
            FileChannel fi = new RandomAccessFile("/system/lib/libhwui.so","r").getChannel();
            FileChannel fo = new RandomAccessFile(new File(getFilesDir(),"myhwui.so"),"rw").getChannel();
            fo.transferFrom(fi,0,fi.size());
            fi.close();
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private BroadcastReceiver mScreenOffReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            testRunnableOnScreenOff();
        }
    };

    Thread testScreenOff;
    private void testRunnableOnScreenOff() {
        if (testScreenOff != null) return;
        testScreenOff = new Thread(new Runnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                while(true) {
                    if (Thread.interrupted()) {
                        Log.d("xx", "---------------quit when interrupted--------------------");
                        return;
                    }
                    try {
                        Thread.sleep(60000);
                    } catch (InterruptedException e) {
                        return;
                    }
                    Log.d("xx","last time = "+(System.currentTimeMillis() - start));
                }
            }
        });
        testScreenOff.start();
    }

    @BackTrace
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
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        Test test = new Test();
        test.httpsConnect();

        //根据包名跳转到系统自带的应用程序信息界面
//        Uri packageURI = Uri.parse("package:" + "com.dw.debug");
//        Intent intent =  new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
//        startActivity(intent);

        // 跳转到应用程序界面
//        Intent intent =  new Intent(Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS);
//        startActivity(intent);

        // 跳转存储设置界面【内部存储】 OR 跳转 存储设置 【记忆卡存储】
        Intent intent =  new Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS);
//        Intent intent =  new Intent(Settings.ACTION_MEMORY_CARD_SETTINGS);
        PackageManager packageManager = getApplicationContext().getPackageManager();
        ResolveInfo resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfo != null) {
            startActivity(intent);
        }
        Log.d("xx","DataDir ="+getMemoryInfo(new File("/data/data")));
        Log.d("xx","SdcardDir ="+getMemoryInfo(new File("/sdcard/")));
    }

    private String getMemoryInfo(File path) {
        // 获得一个磁盘状态对象
        StatFs stat = new StatFs(path.getPath());

        long blockSize = stat.getBlockSize();    // 获得一个扇区的大小

        long totalBlocks = stat.getBlockCount();    // 获得扇区的总数

        long availableBlocks = stat.getAvailableBlocks();    // 获得可用的扇区数量

        // 总空间
        String totalMemory = Formatter.formatFileSize(this, totalBlocks * blockSize);
        // 可用空间
        String availableMemory = Formatter.formatFileSize(this, availableBlocks * blockSize);

        return "总空间: " + totalMemory + "\n可用空间: " + availableMemory;
    }

    public void showMotionView(View view){
        Intent i = new Intent(this,MotionActivity.class);
        startActivity(i);
    }

    public void normalButton(View view){
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

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
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
//        new Thread(new Runnable() {
//            @Override

//            public void run() {
//                Log.d("xx","mInterface.getStringFromNative()====================pid=="+ Process.myPid()+" thread name="+Thread.currentThread().getName());
//                getStringFromNative();
//            }
//        }).start();
        String s = getStringFromNative();
        Button b = (Button)view;
        Animation bigger = AnimationUtils.loadAnimation(this, R.anim.scale_big);
        b.startAnimation(bigger);
//        b.setText(b.getText()+"_"+s);
    }

//    @BackTrace
    private String getStringFromNative() {
        String s = NativeInterface.getInstance().getCrashStringFromNative();
        s += "  " + NativeInterface.getInstance().getStringFromNative();
        Log.d("xx","mInterface.getStringFromNative()===================="+s);
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
        NativeCrashManager.getInstance().enableBackTraceAspect(Math.random() > 0.5 ? true : false);
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

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void setListData(){
        View head = new View(this);
        head.setBackground(new ColorDrawable(Color.RED));
        float headHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,20,getResources().getDisplayMetrics());
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int)headHeight);
        head.setLayoutParams(lp);
        mList.setAdapter(new ArrayAdapter<String>(this,R.layout.list_item,new String[]{"A","B","C"}));
        mList.addHeaderView(head);
    }

    public static final String ACTION_ADD_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT";
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void addShortcut(View view) {
        Intent addShortcutIntent = new Intent(ACTION_ADD_SHORTCUT);
        // 不允许重复创建，不是根据快捷方式的名字判断重复的
        addShortcutIntent.putExtra("duplicate", false);

        addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "Shortcut Name");

        //图标
        addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(this, R.mipmap.ic_launcher));

        // 设置关联程序
        Intent launcherIntent = new Intent();
        launcherIntent.setClass(this, JSActivity.class);
        addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launcherIntent);

        // 发送广播
        sendBroadcast(addShortcutIntent);


        addShortcutIntent = new Intent(ACTION_ADD_SHORTCUT);
        // 不允许重复创建，不是根据快捷方式的名字判断重复的
        addShortcutIntent.putExtra("duplicate", true);

        addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "Shortcut Name1");

        //图标
        addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(this, R.mipmap.aa));

        // 设置关联程序
        launcherIntent = new Intent();
        launcherIntent.setClass(this, JSActivity.class);
        launcherIntent.putExtra("adb", "efg");
        addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launcherIntent);

        // 发送广播
        sendBroadcast(addShortcutIntent);

        /*@SuppressLint("WrongConstant") ShortcutManager shortcutManager = (ShortcutManager) this.getSystemService(Context.SHORTCUT_SERVICE);

        if (shortcutManager.isRequestPinShortcutSupported()) {
            Intent shortcutInfoIntent = new Intent(this, JSActivity.class);
            shortcutInfoIntent.setAction(Intent.ACTION_VIEW); //action必须设置，不然报错

            ShortcutInfo info = new ShortcutInfo.Builder(this, "The only id")
                    .setIcon(Icon.createWithResource(this, R.mipmap.ic_launcher))
                    .setShortLabel("Short Label")
                    .setIntent(shortcutInfoIntent)
                    .build();

            //当添加快捷方式的确认弹框弹出来时，将被回调
            PendingIntent shortcutCallbackIntent = PendingIntent.getBroadcast(this, 0, new Intent(this, MyReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);

            shortcutManager.requestPinShortcut(info, shortcutCallbackIntent.getIntentSender());
        }*/


        //使用ShortcutManagerCompat添加桌面快捷方式,需要support.v4
        /*if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            Intent shortcutInfoIntent = new Intent(context, ShortcutActivity.class);
            shortcutInfoIntent.setAction(Intent.ACTION_VIEW); //action必须设置，不然报错

            ShortcutInfoCompat info = new ShortcutInfoCompat.Builder(context, "The only id")
                    .setIcon(R.mipmap.ic_shortcut)
                    .setShortLabel("Short Label")
                    .setIntent(shortcutInfoIntent)
                    .build();

            //当添加快捷方式的确认弹框弹出来时，将被回调
            PendingIntent shortcutCallbackIntent = PendingIntent.getBroadcast(context, 0, new Intent(context, MyReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);
            ShortcutManagerCompat.requestPinShortcut(context, info, shortcutCallbackIntent.getIntentSender());
        }*/
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
        Log.d("xx","-----MainActivity onDestroy-------");
        if (sfo != null) sfo.stopWatching();
        try {
            unregisterReceiver(mScreenOffReceiver);
        } catch (Exception e) {}
        if (testScreenOff != null) {
            testScreenOff.interrupt();
            Log.d("xx","-----onDestroy interrupt  testScreenOff-------");
        }
    }
}
