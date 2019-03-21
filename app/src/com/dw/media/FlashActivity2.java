package com.dw.media;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.dw.R;

import java.io.IOException;

/**
 * Created by dw on 19-1-2.
 * use SurfaceView MediaPlaer MediaController
 */

public class FlashActivity2 extends Activity implements MediaController.MediaPlayerControl,
            MediaPlayer.OnBufferingUpdateListener , SurfaceHolder.Callback{

    private MediaPlayer mediaPlayer;
    private MediaController controller;
    private int bufferPercentage = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_launch_flash2);
        mediaPlayer = new MediaPlayer();
        controller = new MediaController(this);
        controller.setAnchorView(findViewById(R.id.root_ll));
        initSurfaceView();
    }

    private void initSurfaceView() {
        SurfaceView videoSuf = (SurfaceView) findViewById(R.id.controll_surfaceView);
        videoSuf.setZOrderOnTop(false);
        videoSuf.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        videoSuf.getHolder().addCallback(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
//            http://rbv01.ku6.com/omtSn0z_PTREtneb3GRtGg.mp4   1.36
//            http://rbv01.ku6.com/7lut5JlEO-v6a8K3X9xBNg.mp4    3.49
//            https://key003.ku6.com/movie/1af61f05352547bc8468a40ba2d29a1d.mp4    1.46
//            https://key002.ku6.com/xy/d7b3278e106341908664638ac5e92802.mp4    1.47

            Uri mUri = Uri.parse("https://key003.ku6.com/movie/1af61f05352547bc8468a40ba2d29a1d.mp4");
//            Uri mUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.animoji_sample_video);
            mediaPlayer.setDataSource(this, mUri);
            mediaPlayer.setOnBufferingUpdateListener(this);
            mediaPlayer.setLooping(true);
            mediaPlayer.prepareAsync();

            controller.setMediaPlayer(this);
            controller.setEnabled(true);

        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        controller.show();
        return super.onTouchEvent(event);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        Log.d("xx","onBufferingUpdate  percent=="+percent);
        bufferPercentage = percent;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mediaPlayer.setDisplay(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    //begin implements MediaPlayerControl
    @Override
    public void start() {
        if (null != mediaPlayer){
            mediaPlayer.start();
        }
    }

    @Override
    public void pause() {
        if (null != mediaPlayer){
            mediaPlayer.pause();
        }
    }

    @Override
    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    @Override
    public void seekTo(int pos) {
        mediaPlayer.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return bufferPercentage;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    //end implements MediaPlayerControl
}
