package com.winson.flexplayer;

import android.content.Context;

/**
 * @date on 2018/12/4
 * @Author Winson
 */
public interface FlexPlayer {

    enum State {
        NONE, PREPARE, PAUSE, PLAY, BUFFER_START, BUFFER_END, COMPLETE
    }

    enum Mode {
        NORMAL, FULL_SCREEN
    }

    /**
     * 获取当前播放进度
     *
     * @return 播放进度
     */
    int getPosition();

    /**
     * 获取总时长
     *
     * @return 总时长
     */
    int getDuration();

    /**
     * 获取视频缓冲百分比
     *
     * @return 缓冲白百分比
     */
    int getBufferPercentage();

    /**
     * 是否已经装载播放资源
     *
     * @return true 为已经装载播放资源
     */
    boolean haveDataSource();

    /**
     * 是否在全屏状态
     *
     * @return true 为全屏状态
     */
    boolean isFullScreen();

    /**
     * 跳转到相应进度
     *
     * @param mesc 进度
     */
    void seekTo(int mesc);

    /**
     * 是否在播放中
     *
     * @return true 为播放中
     */
    boolean isPlaying();

    /**
     * 加载资源并播放
     */
    void start();

    /**
     * 暂停
     */
    void pause();

    /**
     * 播放
     */
    void play();

    /**
     * 进入全屏模式
     */
    void enterFullScreen();

    /**
     * 退出全屏模式
     */
    void exitFullScreen();

    /**
     * 装载播放资源
     *
     * @param context 上下文环境
     * @param path    需要播放视频的路径
     */
    void setUp(Context context, String path);

    /**
     * 设置播放器控制类
     *
     * @param controller
     */
    void setPlayerController(FlexPlayerController controller);

}
