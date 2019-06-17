package com.rong.videoplayer;

import android.net.ConnectivityManager;

/**
 * 视频播放器接口
 */
public interface IVideoPlayer {
    ////////////播放状态start//////////////
    /**
     * 播放错误
     **/
    int STATE_ERROR = -1;
    /**
     * 播放未开始
     **/
    int STATE_IDLE = 0;
    /**
     * 播放准备中
     **/
    int STATE_PREPARING = 1;
    /**
     * 播放准备就绪
     **/
    int STATE_PREPARED = 2;
    /**
     * 正在播放
     **/
    int STATE_PLAYING = 3;
    /**
     * 暂停播放
     **/
    int STATE_PAUSED = 4;
    /**
     * 正在缓冲(播放器正在播放时，缓冲区数据不足，进行缓冲，缓冲区数据足够后恢复播放)
     **/
    int STATE_BUFFERING_PLAYING = 5;
    /**
     * 正在缓冲(播放器正在播放时，缓冲区数据不足，进行缓冲，此时暂停播放器，继续缓冲，缓冲区数据足够后恢复暂停)
     **/
    int STATE_BUFFERING_PAUSED = 6;
    /**
     * 播放完成
     **/
    int STATE_COMPLETED = 7;
    /////////////播放状态end//////////////////

    ////////////播放模式start//////////////
    /**
     * 普通模式
     **/
    int MODE_NORMAL = 10;
    /**
     * 全屏模式
     **/
    int MODE_FULL_SCREEN = 11;
    ////////////播放械start//////////////

    ////////////网络状态start//////////////
    /**
     * 无网络
     */
    int NET_STATES_NO = -1;
    /**
     * WIFI
     */
    int NET_STATES_WIFI = ConnectivityManager.TYPE_WIFI;
    /**
     * 移动网络
     */
    int NET_STATES_MOBILE = ConnectivityManager.TYPE_MOBILE;
    ////////////网络状态end//////////////

    /**
     * 启动，设置视频播放参数
     *
     * @param url       视频播放链接
     * @param videoSize 视频大小（耗费多少M流量）
     * @param continueFromLastPosition 是否从上次播放的位置继续播放
     */
    void setUp(String url, String videoSize, boolean continueFromLastPosition);

    /**
     * 开始播放
     */
    void start();

    /**
     * 在指定位置开始播放
     *
     * @param position
     */
    void start(int position);

    /**
     * 重新播放，暂停、播放出错、播放完成状态下重新播放
     */
    boolean restart();

    /**
     * 暂停
     */
    boolean pause();

    /**
     * 停止播放
     */
//    void stop();

    /**
     * 切换进度
     *
     * @param pos
     */
    void seekTo(int pos);

    /**************************
     *以下方法是获取播放器播放状态
     * @return
     */
    boolean isIdle();

    boolean isPreparing();

    boolean isPrepared();

    boolean isBufferingPlaying();

    boolean isBufferingPaused();

    boolean isPlaying();

    boolean isPaused();

    boolean isError();

    boolean isCompleted();
    /****************************/

    /**
     * 播放器窗口状态
     *
     * @return
     */
    boolean isFullScreen();

    boolean isNormal();

    /**
     * 网络状态
     *
     * @return
     */
    boolean isWifi();

    boolean isMobileNet();

    boolean isNotNet();

    /**
     * 获取视频大小
     *
     * @return
     */
    String getVideoSize();

    /**
     * 获取视频总时长
     *
     * @return
     */
    int getDuration();

    /**
     * 获取当前播放位置
     *
     * @return
     */
    int getCurrentPosition();

    /**
     * 获取缓冲进度(%)
     *
     * @return
     */
    int getBufferPercentage();

    /**
     * 进入全屏模式
     */
    void enterFullScreen();

    /**
     * 退出全屏模式
     *
     * @return
     */
    boolean exitFullScreen();

    /**
     * 只是释放播放器
     */
    void releasePlayer();

    /**
     * 回收播放器，重置控制器状态，退出全屏模式
     */
    void release();
}
