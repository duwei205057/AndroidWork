package com.dw.media;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.dw.R;

/**
 * Created by dw on 19-1-2.
 */

public class FlashActivity1 extends Activity implements SurfaceHolder.Callback,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnInfoListener, View.OnClickListener,
        MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnVideoSizeChangedListener,
        SeekBar.OnSeekBarChangeListener, View.OnTouchListener ,
        MediaPlayer.OnBufferingUpdateListener{

    private ImageView playOrPauseIv;
    private SurfaceView videoSuf;
    private MediaPlayer mPlayer;
    private SeekBar mSeekBar;
    private String path;
    private Uri mUri;
    private RelativeLayout rootViewRl;
    private LinearLayout controlLl;
    private TextView startTime, endTime;
    private ImageView forwardButton, backwardButton;
    private boolean isShow = false;

    public static final int UPDATE_TIME = 0x0001;
    public static final int HIDE_CONTROL = 0x0002;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TIME:
                    updateTime();
                    mHandler.sendEmptyMessageDelayed(UPDATE_TIME, 500);
                    break;
                case HIDE_CONTROL:
                    hideControl();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_launch_flash1);
        initViews();
        initData();
        initSurfaceView();
        initPlayer();
        initEvent();
    }
    private void initData() {
        path = Environment.getExternalStorageDirectory().getPath() + "/20180730.mp4";//这里写上你的视频地址
        mUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.animoji_sample_video);
    }

    private void initEvent() {
        playOrPauseIv.setOnClickListener(this);
        rootViewRl.setOnClickListener(this);
        rootViewRl.setOnTouchListener(this);
        forwardButton.setOnClickListener(this);
        backwardButton.setOnClickListener(this);
        mSeekBar.setOnSeekBarChangeListener(this);
    }
    private void initSurfaceView() {
        videoSuf = (SurfaceView) findViewById(R.id.surfaceView);
        videoSuf.setZOrderOnTop(false);
        videoSuf.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        videoSuf.getHolder().addCallback(this);
    }

    private void initPlayer() {
        mPlayer = new MediaPlayer();
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnErrorListener(this);
        mPlayer.setOnInfoListener(this);
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnSeekCompleteListener(this);
        mPlayer.setOnVideoSizeChangedListener(this);
        mPlayer.setOnBufferingUpdateListener(this);
        try {
            //使用手机本地视频
            mPlayer.setDataSource(this, mUri);
//            mPlayer.setDataSource(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initViews() {
        playOrPauseIv = (ImageView) findViewById(R.id.playOrPause);
        startTime = (TextView) findViewById(R.id.tv_start_time);
        endTime = (TextView) findViewById(R.id.tv_end_time);
        mSeekBar = (SeekBar) findViewById(R.id.tv_progess);
        rootViewRl = (RelativeLayout) findViewById(R.id.root_rl);
        controlLl = (LinearLayout) findViewById(R.id.control_ll);
        forwardButton = (ImageView) findViewById(R.id.tv_forward);
        backwardButton = (ImageView) findViewById(R.id.tv_backward);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mPlayer.setDisplay(holder);
        mPlayer.prepareAsync();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
    @Override
    public void onPrepared(MediaPlayer mp) {
        startTime.setText(FormatTimeUtil.formatLongToTimeStr(mp.getCurrentPosition()));
        endTime.setText(FormatTimeUtil.formatLongToTimeStr(mp.getDuration()));
        mSeekBar.setMax(mp.getDuration());
        mSeekBar.setProgress(mp.getCurrentPosition());
    }
    @Override
    public void onCompletion(MediaPlayer mp) {
        mPlayer.start();
    }
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }
    private void play() {
        if (mPlayer == null) {
            return;
        }
        Log.i("playPath", path);
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
            mHandler.removeMessages(UPDATE_TIME);
            mHandler.removeMessages(HIDE_CONTROL);
            playOrPauseIv.setVisibility(View.VISIBLE);
            playOrPauseIv.setImageResource(android.R.drawable.ic_media_play);
        } else {
            mPlayer.start();
            mHandler.sendEmptyMessageDelayed(UPDATE_TIME, 500);
            mHandler.sendEmptyMessageDelayed(HIDE_CONTROL, 5000);
            playOrPauseIv.setVisibility(View.INVISIBLE);
            playOrPauseIv.setImageResource(android.R.drawable.ic_media_pause);
        }
    }
    @Override
    public void onSeekComplete(MediaPlayer mp) {
        //TODO
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_backward:
                backWard();
                break;
            case R.id.tv_forward:
                forWard();
                break;
            case R.id.playOrPause:
                play();
                break;
            case R.id.root_rl:
                showControl();
                break;
        }
    }
    /**
     * 更新播放时间
     */
    private void updateTime() {

        startTime.setText(FormatTimeUtil.formatLongToTimeStr(
                mPlayer.getCurrentPosition()));
        mSeekBar.setProgress(mPlayer.getCurrentPosition());
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    private static class FormatTimeUtil {
        private static String formatLongToTimeStr(long time) {
            long sec = time / 1000;
            String secS = String.valueOf(sec % 60);
            if (secS.length() == 1) secS = "0" + secS;
            String miniteS = String.valueOf(sec / 60);
            if (miniteS.length() == 1) miniteS = "0" + miniteS;
            return miniteS + "." + secS;
        }
    }

    /**
     * 隐藏进度条
     */
    private void hideControl() {
        isShow = false;
        mHandler.removeMessages(UPDATE_TIME);
        controlLl.animate().setDuration(300).translationY(controlLl.getHeight());
    }
    /**
     * 显示进度条
     */
    private void showControl() {
        if (isShow) {
            play();
        }
        isShow = true;
        mHandler.removeMessages(HIDE_CONTROL);
        mHandler.sendEmptyMessage(UPDATE_TIME);
        mHandler.sendEmptyMessageDelayed(HIDE_CONTROL, 5000);
        controlLl.animate().setDuration(300).translationY(0);
    }
    /**
     * 设置快进10秒方法
     */
    private void forWard(){
        if(mPlayer != null){
            int position = mPlayer.getCurrentPosition();
            mPlayer.seekTo(position + 10000);
        }
    }

    /**
     * 设置快退10秒的方法
     */
    public void backWard(){
        if(mPlayer != null){
            int position = mPlayer.getCurrentPosition();
            if(position > 10000){
                position-=10000;
            }else{
                position = 0;
            }
            mPlayer.seekTo(position);
        }
    }

    //OnSeekBarChangeListener
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
//        if(mPlayer != null && b){
//            mPlayer.seekTo(progress);
//        }
        //start changed(多次) stop
        Log.d("xx","onProgressChanged---------------progress="+progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        Log.d("xx","onStartTrackingTouch---------------");
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if(mPlayer != null){
            mPlayer.seekTo(seekBar.getProgress());
        }
        Log.d("xx","onStopTrackingTouch---------------");
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }
}
