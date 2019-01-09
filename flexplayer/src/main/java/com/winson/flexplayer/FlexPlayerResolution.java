package com.winson.flexplayer;

/**
 * FlexPlayer 播放器分辨率
 *
 * @date on 2019/1/9
 * @Author Winson
 */
public interface FlexPlayerResolution {

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


}
