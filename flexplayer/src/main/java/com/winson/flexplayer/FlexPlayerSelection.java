package com.winson.flexplayer;

/**
 * FlexPlayer 播放器剧集
 *
 * @date on 2019/1/9
 * @Author Winson
 */
public interface FlexPlayerSelection {

    /**
     * 获取分辨率的id标志
     *
     * @return
     */
    String getId();

    /**
     * 获取播放的url
     *
     * @return 播放url
     */
    String getUrl();

    /**
     * 获取title
     *
     * @return title
     */
    String getTitle();

    /**
     * 获取封面信息
     *
     * @return 封面信息
     */
    String getCover();

    /**
     * 获取描述信息
     *
     * @return 描述信息
     */
    String getDiscription();


}
