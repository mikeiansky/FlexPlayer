package com.winson.flexplayer;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;

/**
 * @date on 2018/12/4
 * @Author Winson
 */
public class FlexPlayerController extends FrameLayout implements View.OnClickListener {

    private static final String TAG = FlexPlayerController.class.getSimpleName();

    private FlexPlayer flexPlayer;
    private FlexPlayer.State currentState;
    private FlexPlayer.Mode currentMode;
    private View enterFullScreen;
    private ImageView coverImage;
    private View loadingView;
    private View centerStart;
    private TextView positionTextView, durationTextView;
    private TextView titleTextView;
    private TextView noWifiNotifyTextView;
    private SeekBar seekBar;
    private ImageView restartOrPause;
    private boolean seekBarOnTouch;
    private View top, bottom;
    private ValueAnimator showAnimator;
    private ValueAnimator hiddenAnimator;
    private boolean onAnimator;
    private boolean onHidden;
    private TextView changePositionCurrent;
    private View changeContainer;
    private ProgressBar changePositionProgress;
    private ImageView backImage;
    private float downX;
    private float downY;
    private boolean isMove;
    private float touchSlop;
    private int startPosition;
    private int seekToProgress;
    private Handler handler = new Handler();
    private Runnable hiddenTopAndBottomRunnable = new Runnable() {
        @Override
        public void run() {
            hiddenAnimator.start();
        }
    };

    public FlexPlayerController(@NonNull Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public FlexPlayerController(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public FlexPlayerController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public FlexPlayerController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    protected void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!onAnimator) {
                    if (onHidden) {
                        showAnimator.start();
                    } else {
                        hiddenAnimator.start();
                    }
                }
            }
        });
        View content = LayoutInflater.from(context).inflate(R.layout.flex_player_controller, this, false);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        content.setLayoutParams(lp);
        addView(content);

        noWifiNotifyTextView = content.findViewById(R.id.no_wifi_notify_tv);
        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        changeContainer = content.findViewById(R.id.change_position);
        changePositionCurrent = content.findViewById(R.id.change_position_current);
        changePositionProgress = content.findViewById(R.id.change_position_progress);

        top = content.findViewById(R.id.top);
        bottom = content.findViewById(R.id.bottom);

        showAnimator = ValueAnimator.ofFloat(0f, 1f);
        showAnimator.setDuration(250);
        showAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float alpha = (float) animation.getAnimatedValue();
                top.setAlpha(alpha);
                bottom.setAlpha(alpha);
                if (currentState == FlexPlayer.State.PAUSE) {
                    centerStart.setAlpha(alpha);
                }
            }
        });
        showAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                onAnimator = true;
                onHidden = false;
                top.setVisibility(VISIBLE);
                bottom.setVisibility(VISIBLE);
                if (currentState == FlexPlayer.State.PLAY) {
                    hiddenTopAndBottomDelay();
                } else {
                    clearHiddenRunnable();
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                onAnimator = false;
                top.setVisibility(VISIBLE);
                bottom.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                onAnimator = false;

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        hiddenAnimator = ValueAnimator.ofFloat(1f, 0f);
        hiddenAnimator.setDuration(250);
        hiddenAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float alpha = (float) animation.getAnimatedValue();
                top.setAlpha(alpha);
                bottom.setAlpha(alpha);
                if (currentState == FlexPlayer.State.PAUSE) {
                    centerStart.setAlpha(alpha);
                }
            }
        });
        hiddenAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                onAnimator = true;
                onHidden = true;
                top.setVisibility(VISIBLE);
                bottom.setVisibility(VISIBLE);
                clearHiddenRunnable();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                onAnimator = false;
                top.setVisibility(GONE);
                bottom.setVisibility(GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                onAnimator = false;

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        backImage = content.findViewById(R.id.back);
        backImage.setOnClickListener(this);
        backImage.setVisibility(View.INVISIBLE);

        titleTextView = findViewById(R.id.title);

        coverImage = content.findViewById(R.id.cover_image);
        loadingView = content.findViewById(R.id.loading);

        enterFullScreen = content.findViewById(R.id.full_screen);
        enterFullScreen.setOnClickListener(this);

        centerStart = content.findViewById(R.id.center_start);
        centerStart.setOnClickListener(this);

        seekBar = content.findViewById(R.id.seek);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBarOnTouch = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBarOnTouch = false;
                int position = (int) (flexPlayer.getDuration() * seekBar.getProgress() / 100f);
                flexPlayer.seekTo(position);
                if (flexPlayer.haveDataSource()) {
                    if (!flexPlayer.isPlaying()) {
                        flexPlayer.play();
                    }
                }
            }
        });

        positionTextView = content.findViewById(R.id.position);
        durationTextView = content.findViewById(R.id.duration);
        restartOrPause = content.findViewById(R.id.restart_or_pause);
        restartOrPause.setOnClickListener(this);

        hiddenTopAndBottom();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    private void clearHiddenRunnable() {
        handler.removeCallbacks(hiddenTopAndBottomRunnable);
    }

    private void hiddenTopAndBottomDelay() {
        handler.removeCallbacks(hiddenTopAndBottomRunnable);
        handler.postDelayed(hiddenTopAndBottomRunnable, 5000);
    }

    private void hiddenTopAndBottom() {
        onHidden = true;
        top.setVisibility(View.GONE);
        bottom.setVisibility(View.GONE);
    }

    public void showBackImage(boolean show) {
        backImage.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (onAnimator) {
            return super.onTouchEvent(event);
        }
        // 只有在有数据和全屏的情况下才能拖动
        if (!flexPlayer.haveDataSource() || !flexPlayer.isFullScreen()) {
            return super.onTouchEvent(event);
        }
        // 没有装载数据和加载数据的时候
        if (currentState == FlexPlayer.State.NONE
                || currentState == FlexPlayer.State.PREPARE) {
            return super.onTouchEvent(event);
        }

        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                clearHiddenRunnable();
                downX = event.getRawX();
                downY = event.getRawY();
                isMove = false;
                startPosition = flexPlayer.getPosition();
                break;
            case MotionEvent.ACTION_MOVE:
                float cx = event.getRawX();
                float cy = event.getRawY();
                if (!isMove) {
                    if (Math.abs(cx - downX) > touchSlop || Math.abs(cy - downY) > touchSlop) {
                        isMove = true;
                        changeContainer.setVisibility(View.VISIBLE);
                        seekBarOnTouch = true;
                    }
                } else {
                    int width = getWidth();
                    float offsetX = cx - downX;
                    float rate = offsetX / width;
                    int duration = flexPlayer.getDuration();
                    float offsetProgress = rate * duration;
                    float targetProgress = offsetProgress + startPosition;
                    if (targetProgress < 0) {
                        targetProgress = 0;
                        startPosition = 0;
                        downX = cx;
                    }
                    if (targetProgress > duration) {
                        targetProgress = duration;
                        startPosition = duration;
                        downX = cx;
                    }
                    String positionText = FlexPlayerUtils.formatTime((long) targetProgress);
                    positionTextView.setText(positionText);
                    changePositionCurrent.setText(positionText);
                    int progress = (int) (100f * targetProgress / duration);
                    seekBar.setProgress(progress);
                    changePositionProgress.setProgress(progress);
                    seekToProgress = (int) targetProgress;
                }
                break;
            case MotionEvent.ACTION_UP:
                // 点击事件
                if (!isMove) {
                    if (!onAnimator) {
                        if (onHidden) {
                            showAnimator.start();
                        } else {
                            hiddenAnimator.start();
                        }
                    }
                } else {
                    hiddenTopAndBottomDelay();
                    flexPlayer.seekTo(seekToProgress);
                }

                seekBarOnTouch = false;
                changeContainer.setVisibility(View.GONE);
                break;
        }
        return true;
    }

    public void setFlexPlayer(FlexPlayer flexPlayer) {
        this.flexPlayer = flexPlayer;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.full_screen) {
            if (flexPlayer != null) {
                if (currentMode != FlexPlayer.Mode.FULL_SCREEN) {
                    flexPlayer.enterFullScreen();
                } else {
                    flexPlayer.exitFullScreen();
                }
            }
        } else if (id == R.id.center_start) {
            if (currentState == FlexPlayer.State.NONE || currentState == FlexPlayer.State.SETUP) {
                if (flexPlayer != null) {
                    flexPlayer.start();
                }
            } else {
                if (flexPlayer != null) {
                    flexPlayer.play();
                }
            }
        } else if (id == R.id.restart_or_pause) {
            if (flexPlayer != null) {
                if (currentState != FlexPlayer.State.PREPARE) {
                    if (currentState == FlexPlayer.State.NONE) {
                        flexPlayer.start();
                    } else {
                        if (currentState == FlexPlayer.State.PAUSE) {
                            flexPlayer.play();
                        } else {
                            flexPlayer.pause();
                        }
                    }
                }
            }
        } else if (id == R.id.back) {
            if (flexPlayer != null && currentMode == FlexPlayer.Mode.FULL_SCREEN) {
                flexPlayer.exitFullScreen();
            }
        }
    }

    public void setCurrentMode(FlexPlayer.Mode mode) {
        if (mode == FlexPlayer.Mode.FULL_SCREEN) {
            enterFullScreen.setBackgroundResource(R.drawable.ic_player_shrink);
        } else {
            enterFullScreen.setBackgroundResource(R.drawable.ic_player_enlarge);
        }
        currentMode = mode;
    }

    public ImageView getCoverImage() {
        return coverImage;
    }

    public void setCenterStartBackground(int res) {
        centerStart.setBackgroundResource(res);
    }

    public void setTitle(String title) {
        titleTextView.setText(title);
    }

    public void setTitle(@StringRes int titleRes) {
        titleTextView.setText(titleRes);
    }

    public void updateProgress() {
        long position = flexPlayer.getPosition();
        long duration = flexPlayer.getDuration();
        positionTextView.setText(FlexPlayerUtils.formatTime(position));
        durationTextView.setText(FlexPlayerUtils.formatTime(duration));
        if (!seekBarOnTouch) {
            int bufferPercentage = flexPlayer.getBufferPercentage();
            seekBar.setSecondaryProgress(bufferPercentage);
            int progress = (int) (100f * position / duration);
            seekBar.setProgress(progress);
        }
    }

    public void setCurrentState(FlexPlayer.State state) {
        switch (state) {
            case SETUP:
                // check wifi connect
                String videoPath = flexPlayer.getVideoPath();
                if (videoPath != null && videoPath.startsWith("http")) {
                    boolean isWifiConnect = FlexPlayerUtils.isWifiConnect(getContext());
                    if (!isWifiConnect) {
                        // need show notify
                        noWifiNotifyTextView.setVisibility(View.VISIBLE);
                    } else {
                        noWifiNotifyTextView.setVisibility(View.GONE);
                    }
                }
            case NONE:
                centerStart.setVisibility(View.VISIBLE);
                coverImage.setVisibility(View.VISIBLE);
                loadingView.setVisibility(View.GONE);
                restartOrPause.setBackgroundResource(R.drawable.ic_player_start);
                break;
            case PREPARE:
            case BUFFER_START:
                noWifiNotifyTextView.setVisibility(View.GONE);
                centerStart.setVisibility(View.GONE);
                loadingView.setVisibility(View.VISIBLE);
                break;
            case BUFFER_END:
                loadingView.setVisibility(View.GONE);
                break;
            case PLAY:
                noWifiNotifyTextView.setVisibility(View.GONE);
                centerStart.setVisibility(View.GONE);
                restartOrPause.setBackgroundResource(R.drawable.ic_player_pause);
                coverImage.setVisibility(View.GONE);
                loadingView.setVisibility(View.GONE);
                break;
            case PAUSE:
                centerStart.setVisibility(View.VISIBLE);
                restartOrPause.setBackgroundResource(R.drawable.ic_player_start);
                break;
            case COMPLETE:
                centerStart.setVisibility(View.VISIBLE);
                restartOrPause.setBackgroundResource(R.drawable.ic_player_start);
                loadingView.setVisibility(View.GONE);
                break;
        }
        currentState = state;
    }

}
