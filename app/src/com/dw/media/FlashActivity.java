package com.dw.media;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.dw.MainActivity;
import com.dw.R;

import java.util.HashMap;

/**
 * Created by dw on 19-1-2.
 */

public class FlashActivity extends Activity implements View.OnClickListener {

    private VideoView vv;
    private Button btn_start;
    private static final int WHAT_DELAY = 0x11;// 启动页的延时跳转
    private static final int FRAME_DELAY = 0x12;// 启动页的延时跳转
    private static final int DELAY_TIME = 3000;// 延时时间

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case WHAT_DELAY:// 延时3秒跳转
                    goHome();
                    break;
                case FRAME_DELAY:
                    if(null != msg){
                        btn_start.setBackground(new BitmapDrawable((Bitmap) msg.obj));
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_launch_flash);
        vv = (VideoView) findViewById(R.id.videoview);
        btn_start = (Button) findViewById(R.id.btn_start);
        btn_start.setOnClickListener(this);
        //创建MediaController对象
        MediaController mediaController = new MediaController(this);

        //VideoView与MediaController建立关联
        vv.setMediaController(mediaController);

        initView();
//        handler.sendEmptyMessageDelayed(WHAT_DELAY, DELAY_TIME);
    }

    private void initView() {
        //设置播放加载路径
//        vv.setVideoURI(mediaUrl);
        vv.setVideoURI(Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.animoji_sample_video));
        //播放
        vv.start();
        //循环播放
        vv.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                vv.start();
            }
        });
        vv.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {


            public void onPrepared(MediaPlayer mp) {
                mp.start();
                mp.setLooping(false);


            }
        });
        avaterThread();

    }

    private void goHome() {
        startActivity(new Intent(this, MainActivity.class));
        finish();// 销毁当前活动界面
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_start:
                Toast.makeText(this, "进入了主页", Toast.LENGTH_SHORT).show();
                goHome();
                break;
        }
    }


    private void avaterThread(){
        new Thread(){
            @Override
            public void run() {
                MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                try {
                    String url = "android.resource://" + getPackageName() + "/" + R.raw.animoji_sample_video;
//                    String url = "https://key003.ku6.com/movie/1af61f05352547bc8468a40ba2d29a1d.mp4";
//                    if (Build.VERSION.SDK_INT >= 14) {    //需加入api判断，不然会报IllegalArgumentException
//                        mediaMetadataRetriever.setDataSource(url, new HashMap<String, String>());
//                    } else {
//                        mediaMetadataRetriever.setDataSource(url);
//                    }
                    mediaMetadataRetriever.setDataSource(FlashActivity.this, Uri.parse(url));
                    Bitmap bitmap = mediaMetadataRetriever.getFrameAtTime(1000, MediaMetadataRetriever.OPTION_CLOSEST);
                    Message message = new Message();
                    message.what = FRAME_DELAY;
                    message.obj = bitmap;
                    handler.sendMessage(message);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (SecurityException e) {
                    e.printStackTrace();
                } finally {
                    mediaMetadataRetriever.release();
                }
            }
        }.start();
    }

}
