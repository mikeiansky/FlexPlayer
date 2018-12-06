package com.winson.flexplayer;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import java.io.IOException;

/**
 * @date on 2018/12/4
 * @Author Winson
 */
public class FlexPlayerView extends FrameLayout implements FlexPlayer, MediaPlayer.OnPreparedListener, MediaPlayer.OnVideoSizeChangedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnInfoListener {

    public static final String TAG = FlexPlayerView.class.getSimpleName();

    private MediaPlayer mediaPlayer;
    private SurfaceTexture surfaceTexture;
    private FlexPlayerController controller;
    private FrameLayout container;
    private FlexTextureView textureView;
    private Mode currentMode = Mode.NORMAL;
    private State currentState = State.NONE;
    private boolean haveDataSource;
    private Handler handler = new Handler();
    private String videoPath;
    private int bufferPercent;
    private int videoWidth;
    private int videoHeight;

    private Runnable updateProgressRunnable = new Runnable() {
        @Override
        public void run() {
            updateProgress();
        }
    };

    public FlexPlayerController getController() {
        return controller;
    }

    public FlexPlayerView(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public FlexPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public FlexPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FlexPlayerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setContainerBackground(Drawable drawable) {
        container.setBackground(drawable);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnVideoSizeChangedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnInfoListener(this);

        container = new FrameLayout(getContext());
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(container, lp);

        textureView = new FlexTextureView(getContext());
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                if (surfaceTexture == null) {
                    surfaceTexture = surface;
                    Surface s = new Surface(surfaceTexture);
                    mediaPlayer.setSurface(s);
                } else {
                    textureView.setSurfaceTexture(surfaceTexture);
                }

            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
        FrameLayout.LayoutParams tlp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        tlp.gravity = Gravity.CENTER;
        container.addView(textureView, tlp);
        container.setBackground(getResources().getDrawable(R.drawable.flex_video_black_bg));

        controller = new FlexPlayerController(getContext());
        setPlayerController(controller);

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        release();
    }

    private void release() {
        removeUpdateProgress();
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
                mediaPlayer.reset();
                mediaPlayer.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void startUpdateProgress() {
        handler.removeCallbacks(updateProgressRunnable);
        updateProgress();
    }

    private void removeUpdateProgress() {
        handler.removeCallbacks(updateProgressRunnable);
    }

    private void updateProgress() {
        if (currentState == State.COMPLETE) {
            return;
        }
        controller.updateProgress();
        handler.postDelayed(updateProgressRunnable, 400);
    }

    @Override
    public int getPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    @Override
    public int getBufferPercentage() {
        return bufferPercent;
    }

    @Override
    public void seekTo(int mesc) {
        mediaPlayer.seekTo(mesc);
    }

    @Override
    public boolean haveDataSource() {
        return haveDataSource;
    }

    @Override
    public boolean isFullScreen() {
        return currentMode == Mode.FULL_SCREEN;
    }

    @Override
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public void play() {
        if (haveDataSource) {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
            currentState = State.PLAY;
            controller.setCurrentState(currentState);
            startUpdateProgress();
            controller.setKeepScreenOn(true);
        }
    }

    @Override
    public void pause() {
        if (haveDataSource) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            currentState = State.PAUSE;
            controller.setCurrentState(currentState);
            removeUpdateProgress();
            controller.setKeepScreenOn(false);
        }
    }

    @Override
    public void start() {
        if (videoPath == null) {
            return;
        }
        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
            mediaPlayer.reset();
            mediaPlayer.setDataSource(getContext(), Uri.parse(videoPath));
            mediaPlayer.prepareAsync();
            haveDataSource = true;
            currentState = State.PREPARE;
            controller.setCurrentState(currentState);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void enterFullScreen() {
        if (currentMode == Mode.FULL_SCREEN) {
            return;
        }
        // 隐藏ActionBar、状态栏，并横屏
        FlexPlayerUtils.hideActionBar(getContext());
        FlexPlayerUtils.scanForActivity(getContext())
                .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        ViewGroup contentView = (ViewGroup) FlexPlayerUtils.scanForActivity(getContext())
                .findViewById(android.R.id.content);

        this.removeView(container);

        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        contentView.addView(container, params);

        currentMode = Mode.FULL_SCREEN;
        if (controller != null) {
            controller.setCurrentMode(currentMode);
        }

        controller.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                calculateSize();
                controller.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

    }

    @Override
    public void exitFullScreen() {
        if (currentMode == Mode.FULL_SCREEN) {
            FlexPlayerUtils.showActionBar(getContext());
            FlexPlayerUtils.scanForActivity(getContext())
                    .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            ViewGroup contentView = (ViewGroup) FlexPlayerUtils.scanForActivity(getContext())
                    .findViewById(android.R.id.content);
            contentView.removeView(container);
            LayoutParams params = new LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            this.addView(container, params);

            currentMode = Mode.NORMAL;
            if (controller != null) {
                controller.setCurrentMode(currentMode);
            }

            controller.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    calculateSize();
                    controller.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        }
    }

    @Override
    public void setUp(Context context, String path) {
        videoPath = path;
        currentState = State.NONE;
        controller.setCurrentState(currentState);
    }

    @Override
    public void setPlayerController(FlexPlayerController controller) {
        this.controller = controller;
        controller.setFlexPlayer(this);

        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        container.addView(controller, lp);
        controller.setCurrentMode(currentMode);
        controller.setCurrentState(currentState);
    }

    @Override
    public boolean onBackPressed() {
        if (currentMode == Mode.FULL_SCREEN) {
            exitFullScreen();
            return true;
        }
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        currentState = State.PLAY;
        controller.setCurrentState(currentState);
        startUpdateProgress();
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        videoWidth = width;
        videoHeight = height;

        calculateSize();
    }

    private void calculateSize() {
        if (videoWidth <= 0 || videoHeight <= 0) {
            return;
        }
        int maxWidth = container.getWidth();
        int maxHeight = container.getHeight();
        float wr = 1f * videoWidth / maxWidth;
        float hr = 1f * videoHeight / maxHeight;
        if (wr > hr) {
            textureView.setOrientation(FlexTextureView.HORIZONTAL);
            textureView.setRate(1f * videoHeight / videoWidth);
        } else {
            textureView.setOrientation(FlexTextureView.VERTICAL);
            textureView.setRate(1f * videoWidth / videoHeight);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        currentState = State.COMPLETE;
        controller.setCurrentState(currentState);
        removeUpdateProgress();
        controller.setKeepScreenOn(false);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        bufferPercent = percent;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
            // 播放器开始渲染
            case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                break;
            // MediaPlayer暂时不播放，以缓冲更多的数据
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                currentState = State.BUFFER_START;
                controller.setCurrentState(currentState);
                break;
            // 填充缓冲区后，MediaPlayer恢复播放/暂停
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                currentState = State.BUFFER_END;
                controller.setCurrentState(currentState);
                break;
        }
        return false;
    }

}
