package com.chinaredstar.videoplayer;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import java.util.Locale;

/**
 * 视频播放器，支持全屏
 */
public class VideoPlayer extends RelativeLayout implements IVideoPlayer,
        TextureView.SurfaceTextureListener {

    public static final String TAG = "videoPlayer";
    private MediaPlayer mMediaPlayer;//全局用一个MediaPlayer

    private Context mContext;
    private AudioManager mAudioManager;

    private int mCurrentState;//当前播放状态
    private int mCurrentMode;//当前播放模式
    private int mCurrentNetStates;//当前网络状态
    private int mCurrentPosition;//当前播放位置
    private int mBufferPercentage;//当前播放百分比

    private RelativeLayout mContrainer;
    private AbsMediaController mMediaController;
    private ScaleTextureView mTextureView;
    private SurfaceTexture mSurfaceTexture;
    private Surface mSurface;

    private boolean mContinueFromLastPosition = true;
    private boolean mIsInList = false;//是否在列表中播放
    private int mSeekToPosition;//指定播放位置
    private String mUrl;
    private String mVideoSize;//视频大小
    private boolean mIsSurfaceTextureDestroyed = false;
    private VideoPlayerCallBack mVideoPlayerCallBack;//视频播放回调
//    private Drawable mSurfacePlotDrawable;//封面图drawable
//    private ImageView mSurfacePlotImage;//封面图
//    private boolean mHasSurfacePlotImage;//是否有封面图

    public VideoPlayer(Context context) {
        this(context, null);
    }

    public VideoPlayer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
//        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.VideoPlayer,
//        defStyleAttr, 0);
//        mSurfacePlotDrawable = typedArray.getDrawable(R.styleable.VideoPlayer_surfacePlot);
//        typedArray.recycle();
        this.mContext = context;
        init();
    }

    /**
     * 初始化播放器，设置背景色为黑色
     */
    private void init() {
        mContrainer = new RelativeLayout(mContext);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT
                , ViewGroup.LayoutParams.MATCH_PARENT);
        addView(mContrainer, params);
        Log.d(TAG, "初始化");
        //初始化封面图
//        addSurfacePlot();
    }

    /**
     * 初始化音频管理器，获取声音焦点
     */
    private void initAudioManager() {
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
            if (mAudioManager != null) {
                mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN);
            }
            Log.d(TAG, "initAudioManager");
        }
    }

    /**
     * 初始化MediaPlayer
     */
    private void initMediaPlayer() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            Log.d(TAG, "initMediaPlayer");
        }
    }

    /**
     * 初始化TextureView
     */
    private void initTextureView() {
        if (mTextureView == null) {
            mTextureView = new ScaleTextureView(mContext);
            mTextureView.setSurfaceTextureListener(this);
            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                    , ViewGroup.LayoutParams.MATCH_PARENT);
            params.addRule(Gravity.CENTER);
            mContrainer.addView(mTextureView, 0, params);
            mContrainer.setBackgroundColor(Color.BLACK);
            Log.d(TAG, "initTextureView");
        } else {
            openVideoPlayer();
        }
    }

    /**
     * 初始化封面图
     */
//    public void addSurfacePlot() {
//        if (mSurfacePlotDrawable != null) {
//            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
//                    , ViewGroup.LayoutParams.MATCH_PARENT);
//            mSurfacePlotImage = new ImageView(mContext);
//            mSurfacePlotImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
//            mSurfacePlotImage.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (isIdle()) {//封面图显示，应该未初始化
//                        start();
//                    }
//                }
//            });
//            mSurfacePlotImage.setImageDrawable(mSurfacePlotDrawable);
//            params.gravity = Gravity.CENTER;
//            mContrainer.addView(mSurfacePlotImage, params);
//            mHasSurfacePlotImage = true;
//        }
//    }
    private void openVideoPlayer() {
        //设置屏幕常亮
        setKeepScreenOn(true);

        mMediaPlayer.setOnPreparedListener(mOnPreparedListener);
        mMediaPlayer.setOnInfoListener(mOnInfoListener);
        mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
        mMediaPlayer.setOnErrorListener(mOnErrorListener);
        mMediaPlayer.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
        mMediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                if (mTextureView != null) {
                    mTextureView.setVideoWidth(width, height);
                }
            }
        });

        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(mUrl);
            if (mSurface == null) {
                mSurface = new Surface(mSurfaceTexture);
            }
            mMediaPlayer.setSurface(mSurface);
            mMediaPlayer.prepareAsync();
            mCurrentState = STATE_PREPARING;
            onPlayStatesChanged(mCurrentState);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "openVideoPlayer");
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (mSurfaceTexture == null || mIsSurfaceTextureDestroyed) {
            mIsSurfaceTextureDestroyed = false;
            mSurfaceTexture = surface;
            openVideoPlayer();
        } else {
            if (mTextureView.getSurfaceTexture() != mSurfaceTexture) {
                mTextureView.setSurfaceTexture(mSurfaceTexture);
            }
        }
        Log.d(TAG, "onSurfaceTextureAvailable");
    }


    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.d(TAG, "onSurfaceTextureAvailable");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.d(TAG, "onSurfaceTextureDestroyed:" + isIdle());
        return isIdle();
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
//        Log.d(TAG, "onSurfaceTextureUpdated");
    }

    @Override
    public void setUp(String url, String videoSize, boolean continueFromLastPosition) {
        mUrl = url;
        mVideoSize = videoSize;
        mContinueFromLastPosition = continueFromLastPosition;
        Log.d(TAG, "setUp方法:url=" + url + "\nvideoSize=" + videoSize + ";continueFromLastPosition" +
                "=" + continueFromLastPosition);
    }

    @Override
    public void start() {
        if (mVideoPlayerCallBack != null) {
            mVideoPlayerCallBack.onStart();
        }
        mSeekToPosition = getPlayPosition();
        /**
         * 只有在未开始状态才能开始
         */
        if (mCurrentState == STATE_IDLE) {
            VideoPlayerManager.getInstance().setCurrentVideoPlayer(this);
            if (!VideoPlayerManager.getInstance().checkCanPlay(mContext)) {
                if (mMediaController != null) {
                    mMediaController.onNetStatesChanged(VideoPlayerManager.getInstance().getNetWorkStates(mContext));
                }
                return;
            }
            initAudioManager();
            initMediaPlayer();
            initTextureView();
        }
    }

    @Override
    public void start(int position) {
        mSeekToPosition = position;
        start();
    }

    @Override
    public boolean restart() {
        if (isPaused()) {
            mMediaPlayer.start();
            mCurrentState = STATE_PLAYING;
            onPlayStatesChanged(mCurrentState);
            return true;
        } else if (isBufferingPaused()) {
            mMediaPlayer.start();
            mCurrentState = STATE_BUFFERING_PLAYING;
            onPlayStatesChanged(mCurrentState);
            return true;
        } else if (isCompleted() || isError()) {
            openVideoPlayer();
            return true;
        }
        return false;
    }

    @Override
    public boolean pause() {
        if (mMediaPlayer == null || mMediaController == null) {
            return false;
        }
        if (isPlaying()) {
            mMediaPlayer.pause();
            mCurrentState = STATE_PAUSED;
            onPlayStatesChanged(mCurrentState);
            return true;
        } else if (isBufferingPlaying()) {
            mMediaPlayer.pause();
            mCurrentState = STATE_BUFFERING_PAUSED;
            onPlayStatesChanged(mCurrentState);
            return true;
        }
        return false;
    }

    @Override
    public void seekTo(int pos) {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(pos);
        }
    }

    private MediaPlayer.OnPreparedListener mOnPreparedListener =
            new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mCurrentState = STATE_PREPARED;
                    onPlayStatesChanged(mCurrentState);
                    mp.start();
                    //从上次播放位置开始播放
                    if (mContinueFromLastPosition) {
                        mp.seekTo(mSeekToPosition);
                    }
//            if (mHasSurfacePlotImage) {//是否有封面图
//                mSurfacePlotImage.setVisibility(GONE);
//            }
                    if (mVideoPlayerCallBack != null) {
                        mVideoPlayerCallBack.onPrepared();
                    }
                }
            };

    private MediaPlayer.OnInfoListener mOnInfoListener = new MediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            switch (what) {
                case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                    //播放器开始渲染
                    mCurrentState = STATE_PLAYING;
                    onPlayStatesChanged(mCurrentState);
                    break;
                case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                    // MediaPlayer暂时不播放，以缓冲更多的数据
                    if (mCurrentState == STATE_PAUSED || mCurrentState == STATE_BUFFERING_PAUSED) {
                        mCurrentState = STATE_BUFFERING_PAUSED;
                    } else {
                        mCurrentState = STATE_BUFFERING_PLAYING;
                    }
                    onPlayStatesChanged(mCurrentState);
                    break;
                case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                    //缓冲完成,继续播放或者暂停
                    if (mCurrentState == STATE_BUFFERING_PAUSED) {
                        mCurrentState = STATE_PAUSED;
                        onPlayStatesChanged(mCurrentState);
                    }

                    if (mCurrentState == STATE_BUFFERING_PLAYING) {
                        mCurrentState = STATE_PLAYING;
                        onPlayStatesChanged(mCurrentState);
                    }
                    break;
                default:
                    break;
            }
            return true;
        }
    };

    private MediaPlayer.OnCompletionListener mOnCompletionListener =
            new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mCurrentState = STATE_COMPLETED;
                    savePlayPosition(0);
                    onPlayStatesChanged(mCurrentState);
                    setKeepScreenOn(false);
//            if (mHasSurfacePlotImage) {//是否有封面图
//                mSurfacePlotImage.setVisibility(VISIBLE);
//            }
                    if (mVideoPlayerCallBack != null) {
                        mVideoPlayerCallBack.onStop();
                    }
                }
            };

    private MediaPlayer.OnErrorListener mOnErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
            /*
                1、直播流播放时去调用mediaPlayer.getDuration会导致-38和-2147483648错误，忽略该错
                2、what=1&&extra=-1004是mediaPlayer执行重新加载时出现stutter
             */
            Log.d(TAG, String.format(Locale.CHINA, "onError: framework_err=%d   impl_err=%d",
                    framework_err, impl_err));
            if (framework_err != -38 && framework_err != -2147483648 && impl_err != -38 && impl_err != -2147483648) {
                mCurrentState = STATE_ERROR;
                onPlayStatesChanged(mCurrentState);
//                LogUtil.d("onError ——> STATE_ERROR ———— framework_err：" + framework_err + ",
//                impl_err: " + impl_err);
            }
            //onError不返回true的话就会调用MediaPlayer.OnCompletionListener.onCompletion()
            return true;
        }
    };

    private MediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener = new MediaPlayer
            .OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            mBufferPercentage = percent;
        }
    };

    /**
     * 设置控制器
     *
     * @param mediaController 控制器
     */
    public void setMediaController(AbsMediaController mediaController) {
        mContrainer.removeView(mMediaController);
        mMediaController = mediaController;
        mMediaController.reset();
        mMediaController.setVideoPlayer(this);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mContrainer.addView(mMediaController, params);
    }

    public boolean isInList() {
        return mIsInList;
    }

    public void setInList(boolean inList) {
        mIsInList = inList;
    }

//    public void setSurfacePlotImageClickListener(OnClickListener onClickListener) {
//        if (mSurfacePlotImage != null) {
//            mSurfacePlotImage.setOnClickListener(onClickListener);
//        }
//    }

    /**
     * 设置播放器回调（对外）
     *
     * @param videoPlayerCallBack
     */
    public void setVideoPlayerCallBack(VideoPlayerCallBack videoPlayerCallBack) {
        mVideoPlayerCallBack = videoPlayerCallBack;
    }

    /**
     * 判断播放器是否在屏幕外，需要回收
     *
     * @return
     */
    public boolean outScreenAndNeedRelease(View parent) {
        if (parent == null) {
            throw new NullPointerException("outScreenAndNeedRelease param parent is null");
        }
        if (!isIdle()
                && !parent.getLocalVisibleRect(new Rect())) {
            release();
            return true;
        }
        return false;
    }

    @Override
    public boolean isIdle() {
        return mCurrentState == STATE_IDLE;
    }

    @Override
    public boolean isPreparing() {
        return mCurrentState == STATE_PREPARING;
    }

    @Override
    public boolean isPrepared() {
        return mCurrentState == STATE_PREPARED;
    }

    @Override
    public boolean isBufferingPlaying() {
        return mCurrentState == STATE_BUFFERING_PLAYING;
    }

    @Override
    public boolean isBufferingPaused() {
        return mCurrentState == STATE_BUFFERING_PAUSED;
    }

    @Override
    public boolean isPlaying() {
        return mCurrentState == STATE_PLAYING;
    }

    @Override
    public boolean isPaused() {
        return mCurrentState == STATE_PAUSED;
    }

    @Override
    public boolean isError() {
        return mCurrentState == STATE_ERROR;
    }

    @Override
    public boolean isCompleted() {
        return mCurrentState == STATE_COMPLETED;
    }

    @Override
    public boolean isFullScreen() {
        return mCurrentMode == MODE_FULL_SCREEN;
    }

    @Override
    public boolean isNormal() {
        return mCurrentMode == MODE_NORMAL;
    }

    @Override
    public boolean isWifi() {
        return false;
    }

    @Override
    public boolean isMobileNet() {
        return false;
    }

    @Override
    public boolean isNotNet() {
        return false;
    }

    @Override
    public String getVideoSize() {
        return mVideoSize;
    }

    @Override
    public int getDuration() {
        return mMediaPlayer == null ? 0 : mMediaPlayer.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return mMediaPlayer == null ? 0 : mMediaPlayer.getCurrentPosition();
    }

    @Override
    public int getBufferPercentage() {
        return mBufferPercentage;
    }

    @Override
    public void enterFullScreen() {
        if (isFullScreen() || !(mContext instanceof Activity)) {
            return;
        }
        if (mVideoPlayerCallBack != null) {
            mVideoPlayerCallBack.onVideoPlayerRotationPrepare(IVideoPlayer.MODE_FULL_SCREEN);
        }
        //设置屏幕全屏
        final Activity activity = (Activity) mContext;
        //禁止返回关闭
//        activity.setEnableBackLayout(false);
        //全屏处理
        fullScreen(true);
        //列表中播放的时候，需要将播放器添加到顶层view中
        if (isInList()) {
            final ViewGroup contentView = activity.findViewById(android.R.id.content);
            removeView(mContrainer);
            final LayoutParams params = new LayoutParams(
                    LayoutParams.MATCH_PARENT
                    , LayoutParams.MATCH_PARENT);
            measureTextureView();
            contentView.addView(mContrainer, params);

//            contentView.post(new Runnable() {
//                @Override
//                public void run() {
//                    final LayoutParams params = new LayoutParams(
//                            LayoutParams.MATCH_PARENT
//                            , LayoutParams.MATCH_PARENT);
//                    measureTextureView();
//                    contentView.addView(mContrainer, params);
//                }
//            });
        }
        //旋转屏幕
        rotateScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
//                , WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mCurrentMode = MODE_FULL_SCREEN;
        if (mMediaController != null) {
            mMediaController.onPlayModeChanged(mCurrentMode);
        }
        if (mVideoPlayerCallBack != null) {
            mVideoPlayerCallBack.onVideoPlayerRotationFinished(IVideoPlayer.MODE_FULL_SCREEN);
        }
    }

    @Override
    public boolean exitFullScreen() {
        if (isFullScreen() && (mContext instanceof Activity)) {
            if (mVideoPlayerCallBack != null) {
                mVideoPlayerCallBack.onVideoPlayerRotationPrepare(IVideoPlayer.MODE_NORMAL);
            }
            //设置屏幕全屏
            Activity activity = (Activity) mContext;
            //开户返回关闭
//            activity.setEnableBackLayout(false);
            //全屏处理
            fullScreen(false);
//            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            //列表中播放的时候，将播放器添加到原来的view中
            if (isInList()) {
                ViewGroup contentView = activity.findViewById(android.R.id.content);
                contentView.removeViewInLayout(mContrainer);
                LayoutParams params = new LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
                measureTextureView();
                this.addView(mContrainer, params);
            }
            //旋转屏幕
            rotateScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            mCurrentMode = MODE_NORMAL;
            if (mMediaController != null) {
                mMediaController.onPlayModeChanged(mCurrentMode);
            }
            if (mVideoPlayerCallBack != null) {
                mVideoPlayerCallBack.onVideoPlayerRotationFinished(IVideoPlayer.MODE_NORMAL);
            }
            return true;
        }
        return false;
    }

    private void releaseSurface() {
//        if (mSurfaceTexture != null) {
//            mSurfaceTexture.release();
//            mSurfaceTexture = null;
//        }

        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }
        mIsSurfaceTextureDestroyed = true;
    }

    @Override
    public void releasePlayer() {
        if (mAudioManager != null) {
            mAudioManager.abandonAudioFocus(null);
            mAudioManager = null;
        }

        releaseSurface();

        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        mCurrentState = STATE_IDLE;

        if (mVideoPlayerCallBack != null) {
            mVideoPlayerCallBack.onReleasePlayer();
        }
    }

    @Override
    public void release() {
        // 保存播放位置
        if (isPlaying() || isBufferingPlaying() || isBufferingPaused() || isPaused()) {
            savePlayPosition(getCurrentPosition());
        } else if (isCompleted()) {
            savePlayPosition(0);
        }

        //退出全屏
        if (isFullScreen()) {
            exitFullScreen();
        }

        //恢复控制器
        if (mMediaController != null) {
            mMediaController.reset();
        }

        //移除textureView
        mContrainer.removeView(mTextureView);
        mTextureView = null;

        //释放播放器
        releasePlayer();
        mContrainer.setBackgroundColor(Color.TRANSPARENT);

        if (mVideoPlayerCallBack != null) {
            mVideoPlayerCallBack.onRelease();
        }
        Runtime.getRuntime().gc();
    }

    /**
     * 播放状态改变
     *
     * @param states
     */
    private void onPlayStatesChanged(int states) {
        if (mMediaController != null) {
            mMediaController.onPlayStatesChanged(states);
        }
    }

    /**
     * 网络状态改变时要修改播放器当前的状态
     *
     * @param netStates
     */
    public void onNetWorkChanged(int netStates) {
        mCurrentNetStates = netStates;
        mMediaController.onNetStatesChanged(netStates);
    }

    /**
     * 保存当前播放位置
     *
     * @return
     */
    private void savePlayPosition(int position) {
//        Repository.setLocalInt(mUrl, position);
        if (getContext() == null) {
            throw new NullPointerException("context is null, are you sure videoPlayer is attach " +
                    "to window");
        }
        getContext().getSharedPreferences("video", Context.MODE_PRIVATE)
                .edit().putInt(mUrl, position).apply();
    }

    /**
     * 获取之前播放的位置
     *
     * @return
     */
    private int getPlayPosition() {
        if (getContext() == null) {
            throw new NullPointerException("context is null, are you sure videoPlayer is attach " +
                    "to window");
        }
        return getContext().getSharedPreferences("video", Context.MODE_PRIVATE)
                .getInt(mUrl, 0);
    }

    /**
     * 计算textureView的宽高
     */
    private void measureTextureView() {
        /*
            texture默认撑满VideoPlayer,
            旋转后VideoPlayer宽高比有可能发生改变视频会被拉伸，
            需要重新计算textureView的宽高
         */
        mTextureView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mTextureView != null) {
                    mTextureView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mTextureView.updateTextureViewSizeCenter();
                }
            }
        });
    }

    /**
     * 旋转屏幕方向
     *
     * @param orientation
     */
    private void rotateScreenOrientation(int orientation) {
        switch (orientation) {
            case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
                ((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
                ((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
        }
    }

    /**
     * 控制是否全屏显示
     *
     * @param isFull
     */
    private void fullScreen(boolean isFull) {
        if (getContext() instanceof Activity) {
            Activity activity = (Activity) getContext();
            if (isFull) {
                //隐藏虚拟按键，并且全屏
                View decorView = activity.getWindow().getDecorView();
                if (decorView == null) return;
                if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
                    decorView.setSystemUiVisibility(View.GONE);
                } else if (Build.VERSION.SDK_INT >= 19) {
                    int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
                    decorView.setSystemUiVisibility(uiOptions);
                }
            } else {
                View decorView = activity.getWindow().getDecorView();
                if (decorView == null) return;

                if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
                    decorView.setSystemUiVisibility(View.VISIBLE);
                } else if (Build.VERSION.SDK_INT >= 19) {
                    decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                }
            }

        }
    }
}
