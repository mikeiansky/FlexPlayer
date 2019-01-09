package com.winson.flexplayer;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
    private float barHeight;
    private TextView changePositionCurrent;
    private View changeContainer;
    private ProgressBar changePositionProgress;
    private ImageView backImage;

    private LinearLayout changeBrightness;
    private ProgressBar changeBrightnessProgress;

    private LinearLayout changeVolume;
    private ProgressBar changeVolumeProgress;

    private LinearLayout batteryTimeLayout;
    private ImageView batteryIV;
    private TextView timeTV;

    private boolean hasRegisterBatteryReceiver; // 是否已经注册了电池广播
    private int gestureDownVolume;
    private float gestureDownBrightness;
    private float downX;
    private float downY;
    private boolean isMove;
    private boolean onCenter;
    private boolean onLeft;
    private boolean onRight;
    private boolean onUpdateTime;
    private float touchSlop;
    private int startPosition;
    private int seekToProgress;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);
    private AudioManager audioManager;
    private Handler handler = new Handler();
    private Runnable hiddenTopAndBottomRunnable = new Runnable() {
        @Override
        public void run() {
            hiddenAnimator.start();
        }
    };
    private Runnable updateTimeRunnable = new Runnable() {
        @Override
        public void run() {
            if (timeTV != null) {
                timeTV.setText(simpleDateFormat.format(new Date()));
            }
            handler.postDelayed(this, 400);
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
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
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

        barHeight = getResources().getDimension(R.dimen.top_bottom_bar_height);

        View content = LayoutInflater.from(context).inflate(R.layout.flex_player_controller, this, false);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        content.setLayoutParams(lp);
        addView(content);

        batteryTimeLayout = content.findViewById(R.id.battery_time);
        batteryIV = content.findViewById(R.id.battery);
        timeTV = content.findViewById(R.id.time);

        changeBrightness = content.findViewById(R.id.change_brightness);
        changeBrightnessProgress = content.findViewById(R.id.change_brightness_progress);

        changeVolume = content.findViewById(R.id.change_volume);
        changeVolumeProgress = content.findViewById(R.id.change_volume_progress);

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
//                top.setAlpha(alpha);
//                bottom.setAlpha(alpha);

                float ra = 1 - alpha;
                top.setTranslationY(-ra * barHeight);
                bottom.setTranslationY(ra * barHeight);

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
//                top.setVisibility(VISIBLE);
//                bottom.setVisibility(VISIBLE);
                if (currentState == FlexPlayer.State.PLAY) {
                    hiddenTopAndBottomDelay();
                } else {
                    clearHiddenRunnable();
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                onAnimator = false;
//                top.setVisibility(VISIBLE);
//                bottom.setVisibility(VISIBLE);
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

                float ra = 1 - alpha;
                top.setTranslationY(-ra * barHeight);
                bottom.setTranslationY(ra * barHeight);

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
                clearHiddenRunnable();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                hiddenBar();
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

        hiddenBar();
    }

    private void startUpdateTime(FlexPlayer.Mode mode) {
        if (mode == FlexPlayer.Mode.FULL_SCREEN) {
            if (!onUpdateTime) {
                handler.removeCallbacks(updateTimeRunnable);
                updateTimeRunnable.run();
                onUpdateTime = true;
            }
        }
    }

    private void stopUpdateTime() {
        handler.removeCallbacks(updateTimeRunnable);
        onUpdateTime = false;
    }

    private void hiddenBar() {
        onAnimator = false;
        onHidden = true;
        top.setTranslationY(-barHeight);
        bottom.setTranslationY(barHeight);

        clearHiddenRunnable();
    }

    private void clearHiddenRunnable() {
        handler.removeCallbacks(hiddenTopAndBottomRunnable);
    }

    private void hiddenTopAndBottomDelay() {
        handler.removeCallbacks(hiddenTopAndBottomRunnable);
        handler.postDelayed(hiddenTopAndBottomRunnable, 5000);
    }

    public void showBackImage(boolean show) {
        backImage.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    public void setUp(Context context, String path) {
        if (flexPlayer != null) {
            flexPlayer.release();
            flexPlayer.setUp(context, path);
        }
    }

    /**
     * 电池状态即电量变化广播接收器
     */
    private BroadcastReceiver batterReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS,
                    BatteryManager.BATTERY_STATUS_UNKNOWN);
            if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                // 充电中
                batteryIV.setImageResource(R.drawable.battery_charging);
            } else if (status == BatteryManager.BATTERY_STATUS_FULL) {
                // 充电完成
                batteryIV.setImageResource(R.drawable.battery_full);
            } else {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
                int percentage = (int) (((float) level / scale) * 100);
                if (percentage <= 10) {
                    batteryIV.setImageResource(R.drawable.battery_10);
                } else if (percentage <= 20) {
                    batteryIV.setImageResource(R.drawable.battery_20);
                } else if (percentage <= 50) {
                    batteryIV.setImageResource(R.drawable.battery_50);
                } else if (percentage <= 80) {
                    batteryIV.setImageResource(R.drawable.battery_80);
                } else if (percentage <= 100) {
                    batteryIV.setImageResource(R.drawable.battery_100);
                }
            }
        }
    };

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
                downX = event.getX();
                downY = event.getY();
                // is left or right
                if (downX < getWidth() * 0.2f) {
                    onLeft = true;
                } else if (downX > getWidth() * 0.8f) {
                    onRight = true;
                    gestureDownVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                } else {
                    onCenter = true;
                }

                isMove = false;
                startPosition = flexPlayer.getPosition();
                break;
            case MotionEvent.ACTION_MOVE:
                float cx = event.getX();
                float cy = event.getY();
                if (!isMove) {
                    if (Math.abs(cx - downX) > touchSlop || Math.abs(cy - downY) > touchSlop) {
                        isMove = true;
                        if (onLeft) {
                            gestureDownBrightness = FlexPlayerUtils.scanForActivity(getContext())
                                    .getWindow().getAttributes().screenBrightness;
                            changeBrightness.setVisibility(View.VISIBLE);
                        } else if (onRight) {
                            changeVolume.setVisibility(View.VISIBLE);
                        } else {
                            changeContainer.setVisibility(View.VISIBLE);
                            seekBarOnTouch = true;
                        }
                    }
                } else {
                    if (onLeft) {
                        float deltaY = -(cy - downY);

                        float deltaBrightness = deltaY / getHeight();
                        float newBrightness = gestureDownBrightness + deltaBrightness;
                        newBrightness = Math.max(0, Math.min(newBrightness, 1));
                        float newBrightnessPercentage = newBrightness;
                        WindowManager.LayoutParams params = FlexPlayerUtils.scanForActivity(getContext())
                                .getWindow().getAttributes();
                        params.screenBrightness = newBrightnessPercentage;
                        FlexPlayerUtils.scanForActivity(getContext()).getWindow().setAttributes(params);
                        int newBrightnessProgress = (int) (100f * newBrightnessPercentage);
                        changeBrightnessProgress.setProgress(newBrightnessProgress);

                    } else if (onRight) {
                        float deltaY = -(cy - downY);

                        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                        int deltaVolume = (int) (maxVolume * deltaY / getHeight());
                        int newVolume = gestureDownVolume + deltaVolume;
                        newVolume = Math.max(0, Math.min(maxVolume, newVolume));
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, AudioManager.FLAG_PLAY_SOUND);

                        int newVolumeProgress = (int) (100f * newVolume / maxVolume);
                        changeVolumeProgress.setProgress(newVolumeProgress);

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
                    if (onLeft) {
                        changeBrightness.setVisibility(View.GONE);
                    } else if (onRight) {
                        changeVolume.setVisibility(View.GONE);
                    } else {
                        flexPlayer.seekTo(seekToProgress);
                    }
                }

                onRight = false;
                onLeft = false;
                onCenter = false;
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
            batteryTimeLayout.setVisibility(View.VISIBLE);
            if (!hasRegisterBatteryReceiver) {
                getContext().registerReceiver(batterReceiver,
                        new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                hasRegisterBatteryReceiver = true;
            }
            startUpdateTime(mode);
        } else {
            enterFullScreen.setBackgroundResource(R.drawable.ic_player_enlarge);
            batteryTimeLayout.setVisibility(View.GONE);
            if (hasRegisterBatteryReceiver) {
                getContext().unregisterReceiver(batterReceiver);
                hasRegisterBatteryReceiver = false;
            }
            stopUpdateTime();
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

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopUpdateTime();
    }

    public void updateProgress() {
        long position = flexPlayer.getPosition();
        long duration = flexPlayer.getDuration();
        positionTextView.setText(FlexPlayerUtils.formatTime(position));
        durationTextView.setText(FlexPlayerUtils.formatTime(duration));
        startUpdateTime(currentMode);
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
                hiddenBar();
                break;
            case PREPARE:
            case BUFFER_START:
                coverImage.setVisibility(View.GONE);
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
                coverImage.setVisibility(View.VISIBLE);
                restartOrPause.setBackgroundResource(R.drawable.ic_player_start);
                loadingView.setVisibility(View.GONE);
                if (!onAnimator) {
                    if (!onHidden) {
                        hiddenAnimator.start();
                    }
                }
                break;
        }
        currentState = state;
    }

}
