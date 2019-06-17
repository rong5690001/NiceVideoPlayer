package com.rong.videoplayer;import android.content.Context;import android.content.Intent;import android.os.Bundle;import android.support.v7.app.AppCompatActivity;import android.text.TextUtils;import android.view.WindowManager;import android.widget.Toast;/** * 视频播放页面 */public class VideoPlayerActivity extends AppCompatActivity {    private VideoPlayer mVideoPlayer;    public static void start(Context context, String url, String videoSize) {        Intent intent = new Intent(context, VideoPlayerActivity.class);        intent.putExtra("url", url);        intent.putExtra("videoSize", videoSize);        context.startActivity(intent);    }    @Override    protected void onCreate(Bundle savedInstanceState) {        super.onCreate(savedInstanceState);        setContentView(R.layout.activity_video_player);        mVideoPlayer = findViewById(R.id.videoPlayer);        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);    }    @Override    protected void onStart() {        super.onStart();        String url = "";        String videoSize = "";        if (getIntent() != null) {            url = getIntent().getStringExtra("url");            videoSize = getIntent().getStringExtra("videoSize");        }        VideoPlayerManager.getInstance().registerNetWorkChangedReceiver(this);        VPController vpController = new VPController(this, true, false);        vpController.hideIvOrientation();        mVideoPlayer.setMediaController(vpController);        //播放        if (TextUtils.isEmpty(url)) {            Toast.makeText(this, "视频连接为空", Toast.LENGTH_SHORT).show();        } else {            mVideoPlayer.setUp(url, videoSize, true);            if (mVideoPlayer.isIdle()) {                mVideoPlayer.start();            }        }    }    @Override    protected void onResume() {        super.onResume();        VideoPlayerManager.getInstance().onResumeVideoPlayer();    }    @Override    protected void onPause() {        super.onPause();        VideoPlayerManager.getInstance().onPauseVideoPlayer();    }    @Override    protected void onDestroy() {        VideoPlayerManager.getInstance().releaseVideoPlayerAndGone();        VideoPlayerManager.getInstance().unregisterNetWorkChangedReceiver(this);        super.onDestroy();    }}