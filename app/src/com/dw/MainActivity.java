package com.dw;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.Keep;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.aop.AspectBean;
import com.aop.DebugTrace;
import com.database.DBActivity;
import com.database.IntroExampleActivity;
import com.database.SomeFileObserver;
import com.dw.block.BlockDetectByChoreographer;
import com.dw.block.BlockDetectByPrinter;
import com.dw.gif.GifActivity;
import com.dw.js.JSActivity;
import com.dw.recycler.RecyclerList;
import com.dw.resizeicon.ResizeUtils;
import com.dw.touchable.MotionActivity;
import com.dw.utils.Helper;
import com.dw.utils.RedoWorker;
import com.dw.voice.VoiceContainerActivity;
import com.inject.hack.HackLoad;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    public void testDy(View view){
        dyHelper.handleButtonClicked(view);
        try {
            execShellCmd("input swipe 200 1400 500 1400");
        } finally {
        }
        setListData();
        RedoWorker rw = new RedoWorker(Looper.myLooper());
        rw.start(new Runnable() {
            @Override
            public void run() {
                Log.d("xx", "Run~~~~~"+System.currentTimeMillis());
            }
        },50, 200, 500);
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
        Intent i = new Intent(this,VoiceContainerActivity.class);
        startActivity(i);
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

    @TargetApi(Build.VERSION_CODES.M)
    public void resizeIcon(View view){
        try {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
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
            Process process = Runtime.getRuntime().exec("su");
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
