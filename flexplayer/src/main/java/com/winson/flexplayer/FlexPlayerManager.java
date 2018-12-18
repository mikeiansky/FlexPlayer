package com.winson.flexplayer;

/**
 * @date on 2018/12/18
 * @Author Winson
 */
public class FlexPlayerManager {

    private static FlexPlayerManager instance;
    private FlexPlayer flexPlayer;

    private FlexPlayerManager() {
    }

    public static synchronized FlexPlayerManager instance() {
        if (instance == null) {
            instance = new FlexPlayerManager();
        }
        return instance;
    }

    public FlexPlayer getCurrentFlexPlayer() {
        return flexPlayer;
    }

    public void setCurrentFlexPlayer(FlexPlayer videoPlayer) {
        if (flexPlayer != videoPlayer) {
            releaseFlexPlayer();
            flexPlayer = videoPlayer;
        }
    }

    public void pauseFlexPlayer() {
        if (flexPlayer != null && flexPlayer.isPlaying()) {
            flexPlayer.pause();
        }
    }

    public void resumeFlexPlayer() {
        if (flexPlayer != null && !flexPlayer.isPlaying()) {
            flexPlayer.play();
        }
    }

    public void releaseFlexPlayer() {
        if (flexPlayer != null) {
            flexPlayer.release();
            flexPlayer = null;
        }
    }

    public void releaseFlexPlayer(FlexPlayer targetPlayer) {
        if (flexPlayer != null && targetPlayer == flexPlayer) {
            flexPlayer.release();
            flexPlayer = null;
        } else {
            targetPlayer.release();
        }
    }

    public boolean onBackPressd() {
        if (flexPlayer != null) {
            if (flexPlayer.isFullScreen()) {
                flexPlayer.exitFullScreen();
                return true;
            }
        }
        return false;
    }

}
