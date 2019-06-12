package com.chinaredstar.videoplayer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.CountDownTimer;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

/**
 * 播放器控制器，支持全屏
 */

public class VPController extends AbsMediaController implements SeekBar.OnSeekBarChangeListener {

    private static final String TAG = VideoPlayer.TAG;
    protected Context mContext;

    //    private View mBlackView;
    protected LinearLayout mToolbar;
    protected TextView mStatusBar;
    protected RelativeLayout mRelBack;
    protected ImageView mIvCenterBtn;
    protected TextView mCurrentTime;
    protected AppCompatSeekBar mSeekbar;
    protected TextView mTvTotalTime;
    protected ImageView mIvOrientation;
    protected View mLoadingView;
    protected RelativeLayout mShadowLayout;
    protected TextView mHintTitle;
    protected Button mFlowBtn;
    protected RelativeLayout mBottomLayout;
    protected ImageView mShareBtn;

    protected CountDownTimer mDismissTopBottomTimer;
    protected boolean mTopBottomVisible;
    protected boolean mIsDetailPage;//是否是详情页播放(列表播放不显示toolbar)
    //分享
    protected boolean mCanShare;//是否可以分享
    protected OnShareBtnClickListener mOnShareBtnClickListener;

    public VPController(Context context, boolean isDetailPage, boolean canShare) {
        super(context);
        this.mContext = context;
        mIsDetailPage = isDetailPage;
        mCanShare = canShare;
        initView();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.videoplayer_controller, null);
        addView(view);
        mToolbar = findViewById(R.id.toolbar);
//        mBlackView = findViewById(R.id.blackView);
        mStatusBar = findViewById(R.id.statusBar);
        mRelBack = findViewById(R.id.rel_back);
        mIvCenterBtn = findViewById(R.id.iv_centerBtn);
        mCurrentTime = findViewById(R.id.current_time);
        mSeekbar = findViewById(R.id.seekbar);
        mTvTotalTime = findViewById(R.id.tv_total_time);
        mIvOrientation = findViewById(R.id.iv_orientation);
        mLoadingView = findViewById(R.id.loading_view);
        mShadowLayout = findViewById(R.id.shadowLayout);
        mHintTitle = findViewById(R.id.hintTitle);
        mHintTitle.setText(mContext.getString(R.string.mobile_net_hint));
        mFlowBtn = findViewById(R.id.flowBtn);
        mBottomLayout = findViewById(R.id.bottomLayout);
        mShareBtn = findViewById(R.id.share_btn);
        //是详情页
        mToolbar.setVisibility(mIsDetailPage ? View.VISIBLE : View.GONE);
        //分享
        if (mCanShare) {
            mShareBtn.setVisibility(VISIBLE);
            mShareBtn.setOnClickListener(this);
        } else {
            mShareBtn.setVisibility(GONE);
        }

        mRelBack.setOnClickListener(this);
        mIvCenterBtn.setOnClickListener(this);
        mIvOrientation.setOnClickListener(this);
        mFlowBtn.setOnClickListener(this);
        mSeekbar.setOnSeekBarChangeListener(this);
        mSeekbar.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                return false;
            }
        });

        this.setOnClickListener(this);
    }

    /**
     * 隐藏放大缩小按钮
     */
    public void hideIvOrientation() {
        if (mIvOrientation != null)
            mIvOrientation.setVisibility(View.INVISIBLE);
    }

    @Override
    public void updateProgress() {
        int position = mVideoPlayer.getCurrentPosition();
        int duration = mVideoPlayer.getDuration();

        mSeekbar.setProgress((int) (100f * position / duration));
        mCurrentTime.setText(getTime(position));
        mTvTotalTime.setText(getTime(duration));
    }

    @Override
    public void onPlayStatesChanged(int playStatus) {
        switch (playStatus) {
            case IVideoPlayer.STATE_IDLE:
//                mBlackView.setVisibility(VISIBLE);
                Log.d(TAG, "onPlayStatesChanged: STATE_IDLE");
                break;
            case IVideoPlayer.STATE_PREPARING:
                mLoadingView.setVisibility(VISIBLE);
                mIvCenterBtn.setVisibility(GONE);
                mShadowLayout.setVisibility(GONE);
//                mBlackView.setVisibility(VISIBLE);
                Log.d(TAG, "onPlayStatesChanged: STATE_PREPARING");
                break;
            case IVideoPlayer.STATE_PREPARED:
//                mBlackView.setVisibility(VISIBLE);
                startUpdateProgressTimer();
                Log.d(TAG, "onPlayStatesChanged: STATE_PREPARED");
                break;
            case IVideoPlayer.STATE_PLAYING:
                mLoadingView.setVisibility(GONE);
                mIvCenterBtn.setImageResource(R.drawable.icon_pause_btn);
                mShadowLayout.setVisibility(GONE);
//                mBlackView.setVisibility(GONE);
                startDismissTopBottomTimer();
                Log.d(TAG, "onPlayStatesChanged: STATE_PLAYING");
                break;
            case IVideoPlayer.STATE_PAUSED:
                mLoadingView.setVisibility(GONE);
                mIvCenterBtn.setVisibility(VISIBLE);
                mIvCenterBtn.setImageResource(R.drawable.icon_play_btn);
                mShadowLayout.setVisibility(GONE);
//                mBlackView.setVisibility(GONE);
                cancelDismissTopBottomTimer();
                Log.d(TAG, "onPlayStatesChanged: STATE_PAUSED");
                break;
            case IVideoPlayer.STATE_BUFFERING_PLAYING:
                mLoadingView.setVisibility(VISIBLE);
                mIvCenterBtn.setVisibility(GONE);
                mIvCenterBtn.setImageResource(R.drawable.icon_pause_btn);
                mShadowLayout.setVisibility(GONE);
//                mBlackView.setVisibility(GONE);
                startDismissTopBottomTimer();
                Log.d(TAG, "onPlayStatesChanged: STATE_BUFFERING_PLAYING");
                break;
            case IVideoPlayer.STATE_BUFFERING_PAUSED:
                mLoadingView.setVisibility(GONE);
                mIvCenterBtn.setVisibility(VISIBLE);
                mIvCenterBtn.setImageResource(R.drawable.icon_play_btn);
                mShadowLayout.setVisibility(GONE);
//                mBlackView.setVisibility(GONE);
                cancelDismissTopBottomTimer();
                Log.d(TAG, "onPlayStatesChanged: STATE_BUFFERING_PAUSED");
                break;
            case IVideoPlayer.STATE_COMPLETED:
                Log.d(TAG, "onPlayStatesChanged: STATE_COMPLETED");
            case IVideoPlayer.STATE_ERROR:
                mLoadingView.setVisibility(GONE);
                cancelUpdateProgressTimer();
                cancelDismissTopBottomTimer();
                setTopBottomVisible(false);
                //全屏或者详情页要显示返回按钮
                if (mVideoPlayer.isFullScreen()
                        || mIsDetailPage) {
                    mToolbar.setVisibility(VISIBLE);
                }
//                if (playStatus == IVideoPlayer.STATE_COMPLETED) {
//                    mBlackView.setVisibility(GONE);
//                } else {
//                    mBlackView.setVisibility(VISIBLE);
//                }
                mIvCenterBtn.setVisibility(VISIBLE);
                mIvCenterBtn.setImageResource(R.drawable.icon_replay_btn);
                Log.d(TAG, "onPlayStatesChanged: STATE_ERROR");
                break;
            default:
                break;
        }
    }

    @Override
    public void onPlayModeChanged(int model) {
        if (model == IVideoPlayer.MODE_FULL_SCREEN) {
            mToolbar.setVisibility(VISIBLE);
            mIvOrientation.setImageResource(R.drawable.icon_make_min_white);
        } else {
            if (!mIsDetailPage) {
                mToolbar.setVisibility(GONE);
            }
            mIvOrientation.setImageResource(R.drawable.icon_make_max);
        }
    }

    @Override
    public void onNetStatesChanged(int netStates) {
        switch (netStates) {
            case IVideoPlayer.NET_STATES_NO:
                mShadowLayout.setVisibility(GONE);
                mLoadingView.setVisibility(GONE);
                if (!(mVideoPlayer.isPlaying() || mVideoPlayer.isBufferingPlaying())) {
                    mIvCenterBtn.setImageResource(R.drawable.icon_replay_btn);
                    mIvCenterBtn.setVisibility(VISIBLE);
                }
                Toast.makeText(mContext, R.string.network_error, Toast.LENGTH_SHORT).show();
                break;
            case IVideoPlayer.NET_STATES_MOBILE:
                if (mVideoPlayer.isCompleted()
                        || mVideoPlayer.isError()
                        || VideoPlayerManager.getInstance().isCanPlayOnMobileNet()) {
                    break;
                }
                mShadowLayout.setVisibility(VISIBLE);
                mIvCenterBtn.setVisibility(GONE);
                mFlowBtn.setText(String.format("%sM流量", mVideoPlayer.getVideoSize()));
                setTopBottomVisible(false);
                break;
            case IVideoPlayer.NET_STATES_WIFI:
                mShadowLayout.setVisibility(GONE);
                break;
        }
    }

    @Override
    public void reset() {
        cancelUpdateProgressTimer();
        cancelDismissTopBottomTimer();
        mSeekbar.setProgress(0);
        mCurrentTime.setText(getTime(0));
        mTvTotalTime.setText(getTime(0));
        mLoadingView.setVisibility(GONE);
        mShadowLayout.setVisibility(GONE);
        mIvCenterBtn.setImageResource(R.drawable.icon_replay_btn);
        mIvOrientation.setImageResource(R.drawable.icon_make_max);
        setTopBottomVisible(false);
        if (mIsDetailPage) {
            mToolbar.setVisibility(VISIBLE);
        }
        Log.d(TAG, "reset");
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (mVideoPlayer.isPaused() || mVideoPlayer.isBufferingPaused()) {
            mVideoPlayer.restart();
        }

        int position = (int) (mVideoPlayer.getDuration() * seekBar.getProgress() / 100f);
        mVideoPlayer.seekTo(position);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();

        if (i == R.id.rel_back) {//返回按钮
            if (mVideoPlayer.isFullScreen()) {
                mVideoPlayer.exitFullScreen();
            } else {
                if (mContext instanceof Activity) {
                    ((Activity) mContext).finish();
                }
            }

        } else if (i == R.id.iv_orientation) {//旋转按钮
            if (mVideoPlayer.isFullScreen()) {
                mVideoPlayer.exitFullScreen();
            } else {
                mVideoPlayer.enterFullScreen();
            }

        } else if (i == R.id.iv_centerBtn) {//中间按钮
            if (mVideoPlayer.isPlaying() || mVideoPlayer.isBufferingPlaying()) {
                mVideoPlayer.pause();
            } else if (mVideoPlayer.isPaused()
                    || mVideoPlayer.isBufferingPaused()
                    || mVideoPlayer.isError()
                    || mVideoPlayer.isCompleted()) {
                if (!NetUtil.isNetworkConnected(mContext)) {//无网络的状态直接返回
                    Toast.makeText(mContext, R.string.network_error, Toast.LENGTH_SHORT).show();
                }
                mVideoPlayer.restart();
            } else if (mVideoPlayer.isIdle()) {
                mVideoPlayer.start();
            }

        } else if (i == R.id.flowBtn) {//流量按钮
            VideoPlayerManager.getInstance().setCanPlayOnMobileNet(true);
            if (mVideoPlayer.isPaused()
                    || mVideoPlayer.isBufferingPaused()
                    || mVideoPlayer.isError()
                    || mVideoPlayer.isCompleted()) {
                mVideoPlayer.restart();
            } else if (mVideoPlayer.isIdle()) {
                mVideoPlayer.start();
            }

        } else if (i == R.id.share_btn) {//分享按钮
            if (mOnShareBtnClickListener != null) {
                mOnShareBtnClickListener.onShare();
            }

        } else {//点击其他区域切换头部和底部显示状态
            if (mVideoPlayer.isPlaying()
                    || mVideoPlayer.isBufferingPlaying()
                    || mVideoPlayer.isPaused()
                    || mVideoPlayer.isBufferingPaused()) {
                setTopBottomVisible(!mTopBottomVisible);
            }

        }
    }

    /**
     * 显示隐藏动画
     */
    private Animation mAnimationIn;
    private Animation mAnimationOut;

    /**
     * 设置头部和底部Ui是否可见
     *
     * @param visible
     */
    private void setTopBottomVisible(boolean visible) {
        mTopBottomVisible = visible;
        if (mTopBottomVisible) {
            if (mAnimationIn == null) {
                mAnimationIn = new AlphaAnimation(0, 1);
                mAnimationIn.setDuration(350);
                mToolbar.startAnimation(mAnimationIn);
                mBottomLayout.startAnimation(mAnimationIn);
            }
            if (mVideoPlayer.isFullScreen()
                    || mIsDetailPage) {//全屏或者详情页才显示返回按钮
                mToolbar.setVisibility(VISIBLE);
            }
            mBottomLayout.setVisibility(VISIBLE);
            if (mLoadingView.getVisibility() == GONE) {
                mIvCenterBtn.setVisibility(VISIBLE);
            } else {
                mIvCenterBtn.setVisibility(GONE);
            }
        } else {
            if (mAnimationOut == null) {
                mAnimationOut = new AlphaAnimation(1, 0);
                mAnimationOut.setDuration(350);
                mToolbar.startAnimation(mAnimationOut);
                mBottomLayout.startAnimation(mAnimationOut);
            }
            mToolbar.setVisibility(GONE);
            mBottomLayout.setVisibility(GONE);
            mIvCenterBtn.setVisibility(GONE);
        }
    }

    /**
     * 开始计时隐藏头部和底部
     */
    private void startDismissTopBottomTimer() {
        cancelDismissTopBottomTimer();
        if (mDismissTopBottomTimer == null) {
            mDismissTopBottomTimer = new CountDownTimer(3000, 3000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    setTopBottomVisible(false);
                }
            };
        }
        mDismissTopBottomTimer.start();
    }

    /**
     * 取消计时
     */
    private void cancelDismissTopBottomTimer() {
        if (mDismissTopBottomTimer != null) {
            mDismissTopBottomTimer.cancel();
        }
    }

    /**
     * 获取时间，格式为00:00
     *
     * @param progress
     */
    private String getTime(int progress) {
        String str_hour = "";
        progress = progress / 1000;
        if (progress / 3600 > 0) {
            str_hour = String.format(Locale.getDefault(), "%02d:", progress / 3600);
        }

        String str_minute = String.format(Locale.getDefault(), "%02d", progress % 3600 / 60);
        String str_second = String.format(Locale.getDefault(), "%02d", progress % 60);
        String format_time = String.format(Locale.getDefault(), "%s%s:%s", str_hour, str_minute,
                str_second);
        return format_time;
    }

    /**
     * 设置分享按钮监听
     *
     * @param onShareBtnClickListener
     */
    public void setOnShareBtnClickListener(OnShareBtnClickListener onShareBtnClickListener) {
        mOnShareBtnClickListener = onShareBtnClickListener;
    }

    public interface OnShareBtnClickListener {
        void onShare();
    }
}
