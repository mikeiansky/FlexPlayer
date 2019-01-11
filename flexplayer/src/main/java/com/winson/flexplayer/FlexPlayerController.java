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
import android.util.TypedValue;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @date on 2018/12/4
 * @Author Winson
 */
public class FlexPlayerController extends FrameLayout implements View.OnClickListener {

    private static final String TAG = FlexPlayerController.class.getSimpleName();

    private boolean seekBarOnTouch;
    private int duration = 250;
    private boolean onAnimator;
    private float barHeight;
    private float speedContentWidth;
    private float selectionContentWidth;

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
    private LinearLayout changeContent;

    // 锁屏模块
    private View lockContent;
    private View lockFlag;
    private boolean isLock;
    private float lockMarginLeft;
    private ValueAnimator showLockContentAnimator;
    private ValueAnimator hiddenLockContentAnimator;
    private boolean lockOnAnimator;
    private boolean onShowLockContent;
    // --------

    // 工具栏组件
    private View top, bottom;
    private ValueAnimator showToolContentAnimator;
    private ValueAnimator hiddenToolContentAnimator;
    private boolean onToolContentHidden;
    // --------

    // 倍速控制组件
    private View playerSpeed;
    private View speedGroup;
    private RecyclerView speedRecyclerView;
    private FlexPlayerSpeedAdapter flexPlayerSpeedAdapter;
    private ObjectAnimator showSpeedContentAnimator;
    private ObjectAnimator hiddenSpeedContentAnimator;
    private boolean onShowSpeedContent;
    // --------

    // 分辨率控制组件
    private View resolutionFlag;
    private View resolutionContent;
    private RecyclerView resolutionRecyclerView;
    private FlexPlayerResolutionAdapter flexPlayerResolutionAdapter;
    private ObjectAnimator showResolutionAnimator;
    private ObjectAnimator hiddenResolutionAnimator;
    private boolean onShowResolutionContent;
    // --------

    // 剧集控制组件
    private View selectionFlag;
    private View selectionContent;
    private RecyclerView selectionRecyclerView;
    private FlexPlayerSelectionAdapter flexPlayerSelectionAdapter;
    private ObjectAnimator showSelectionAnimator;
    private ObjectAnimator hiddenSelectionAnimator;
    private boolean onShowSelectionContent;
    // --------

    private boolean hasRegisterBatteryReceiver; // 是否已经注册了电池广播
    private int gestureDownVolume;
    private float gestureDownBrightness;
    private float downX;
    private float downY;
    private boolean isMove;
    private boolean detailByOnTouchEvent;
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

    /**
     * 隐藏工具栏任务
     */
    private Runnable hiddenToolContentRunnable = new Runnable() {
        @Override
        public void run() {
            showToolBar(false);
        }
    };

    /**
     * 隐藏倍速控制组件任务
     */
    private Runnable hiddenSpeedContentRunnable = new Runnable() {
        @Override
        public void run() {
            if (!onAnimator && onShowSpeedContent) {
                hiddenSpeedContentAnimator.start();
            }
        }
    };

    /**
     * 隐藏分辨率控制组件任务
     */
    private Runnable hiddenResolutionContentRunnable = new Runnable() {
        @Override
        public void run() {
            if (!onAnimator && onShowResolutionContent) {
                hiddenResolutionAnimator.start();
            }
        }
    };

    /**
     * 隐藏剧集控制组件任务
     */
    private Runnable hiddenSelectionContentRunnable = new Runnable() {
        @Override
        public void run() {
            if (!onAnimator && onShowSelectionContent) {
                hiddenSelectionAnimator.start();
            }
        }
    };

    /**
     * 隐藏锁屏控制组件任务
     */
    private Runnable hiddenLockContentRunnable = new Runnable() {
        @Override
        public void run() {
            if (!lockOnAnimator && onShowLockContent) {
                hiddenLockContentAnimator.start();
            }
        }
    };

    /**
     * 更新时间任务
     */
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
                if (isLock) {
                    if (!lockOnAnimator) {
                        if (onShowLockContent) {
                            hiddenLockContentRunnableDelay(false);
                        } else {
                            showLockContent();
                        }
                    }
                } else {
                    if (currentState == FlexPlayer.State.NONE || currentState == FlexPlayer.State.SETUP) {
                        if (flexPlayer != null) {
                            flexPlayer.start();
                        }
                    } else {
                        showToolBar(onToolContentHidden);
                    }
                }
            }
        });

        barHeight = getResources().getDimension(R.dimen.top_bottom_bar_height);
        speedContentWidth = getResources().getDimension(R.dimen.player_speed_content_width);
        selectionContentWidth = getResources().getDimension(R.dimen.player_selection_content_width);

        View content = LayoutInflater.from(context).inflate(R.layout.flex_player_controller, this, false);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        content.setLayoutParams(lp);
        addView(content);

        initSelectionContent();
        initResolutionContent();
        initSpeedContent();
        initToolContent();
        initLockContent();

        changeContent = content.findViewById(R.id.change_content);
        playerSpeed = content.findViewById(R.id.player_speed);
        playerSpeed.setOnClickListener(this);

        resolutionFlag = content.findViewById(R.id.resolution_flag);
        resolutionFlag.setOnClickListener(this);

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

        hiddenSpeedContent();
        hiddenToolContent();
        hiddenResolutionContent();
        hiddenSelectionContent();
        hiddenLockContent();
    }

    /**
     * 初始化锁屏模块
     */
    private void initLockContent() {
        lockMarginLeft = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100f,
                getResources().getDisplayMetrics());
        lockContent = findViewById(R.id.lock_content);
        lockFlag = findViewById(R.id.lock_flag);

        showLockContentAnimator = ObjectAnimator.ofFloat(lockContent, "translationX",
                -lockMarginLeft, 0);
        showLockContentAnimator.setDuration(duration);
        showLockContentAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                lockOnAnimator = true;
                onShowLockContent = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                lockOnAnimator = false;

            }

            @Override
            public void onAnimationCancel(Animator animation) {
                lockOnAnimator = false;

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        hiddenLockContentAnimator = ObjectAnimator.ofFloat(lockContent, "translationX",
                0, -lockMarginLeft);
        hiddenLockContentAnimator.setDuration(duration);
        hiddenLockContentAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                lockOnAnimator = true;
                onShowLockContent = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                lockOnAnimator = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                lockOnAnimator = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        lockContent.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLock) {
                    lockFlag.setBackgroundResource(R.drawable.ic_lock_open);
                    isLock = false;
                    hiddenLockContentRunnableDelay(false);
                } else {
                    lockFlag.setBackgroundResource(R.drawable.ic_lock);
                    isLock = true;
                    showToolBar(false);
                }
            }
        });
    }

    /**
     * 初始化工具栏模块
     */
    private void initToolContent() {
        top = findViewById(R.id.top);
        bottom = findViewById(R.id.bottom);
        showToolContentAnimator = ValueAnimator.ofFloat(0f, 1f);
        showToolContentAnimator.setDuration(duration);
        showToolContentAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
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
        showToolContentAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                onAnimator = true;
                onToolContentHidden = false;
                hiddenToolContentDelay(true);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                onAnimator = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                onAnimator = false;

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        hiddenToolContentAnimator = ValueAnimator.ofFloat(1f, 0f);
        hiddenToolContentAnimator.setDuration(duration);
        hiddenToolContentAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
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
        hiddenToolContentAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                onAnimator = true;
                onToolContentHidden = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                hiddenToolContent();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                onAnimator = false;

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    /**
     * 初始化剧集模块
     */
    private void initSelectionContent() {
        selectionFlag = findViewById(R.id.selection_flag);
        selectionFlag.setOnClickListener(this);
        selectionContent = findViewById(R.id.selection_content);
        selectionRecyclerView = findViewById(R.id.selection_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        flexPlayerSelectionAdapter = new FlexPlayerSelectionAdapter();
        selectionRecyclerView.setLayoutManager(layoutManager);
        selectionRecyclerView.setAdapter(flexPlayerSelectionAdapter);
        ArrayList<FlexPlayerSelection> selections = new ArrayList<>();
        TestSelection selection1 = new TestSelection("1", "http://play.g3proxy.lecloud.com/vod/v2/MjUxLzE2LzgvbGV0di11dHMvMTQvdmVyXzAwXzIyLTExMDc2NDEzODctYXZjLTE5OTgxOS1hYWMtNDgwMDAtNTI2MTEwLTE3MDg3NjEzLWY1OGY2YzM1NjkwZTA2ZGFmYjg2MTVlYzc5MjEyZjU4LTE0OTg1NTc2ODY4MjMubXA0?b=259&mmsid=65565355&tm=1499247143&key=f0eadb4f30c404d49ff8ebad673d3742&platid=3&splatid=345&playid=0&tss=no&vtype=21&cvid=2026135183914&payff=0&pip=08cc52f8b09acd3eff8bf31688ddeced&format=0&sign=mb&dname=mobile&expect=1&tag=mobile&xformat=super", "one", "one", "one");
        TestSelection selection2 = new TestSelection("2", "http://play.g3proxy.lecloud.com/vod/v2/MjQ5LzM3LzIwL2xldHYtdXRzLzE0L3Zlcl8wMF8yMi0xMTA3NjQxMzkwLWF2Yy00MTk4MTAtYWFjLTQ4MDAwLTUyNjExMC0zMTU1NTY1Mi00ZmJjYzFkNzA1NWMyNDc4MDc5OTYxODg1N2RjNzEwMi0xNDk4NTU3OTYxNzQ4Lm1wNA==?b=479&mmsid=65565355&tm=1499247143&key=98c7e781f1145aba07cb0d6ec06f6c12&platid=3&splatid=345&playid=0&tss=no&vtype=13&cvid=2026135183914&payff=0&pip=08cc52f8b09acd3eff8bf31688ddeced&format=0&sign=mb&dname=mobile&expect=1&tag=mobile&xformat=super", "two", "one", "one");
        TestSelection selection3 = new TestSelection("3", "http://play.g3proxy.lecloud.com/vod/v2/MjQ5LzM3LzIwL2xldHYtdXRzLzE0L3Zlcl8wMF8yMi0xMTA3NjQxMzkwLWF2Yy00MTk4MTAtYWFjLTQ4MDAwLTUyNjExMC0zMTU1NTY1Mi00ZmJjYzFkNzA1NWMyNDc4MDc5OTYxODg1N2RjNzEwMi0xNDk4NTU3OTYxNzQ4Lm1wNA==?b=479&mmsid=65565355&tm=1499247143&key=98c7e781f1145aba07cb0d6ec06f6c12&platid=3&splatid=345&playid=0&tss=no&vtype=13&cvid=2026135183914&payff=0&pip=08cc52f8b09acd3eff8bf31688ddeced&format=0&sign=mb&dname=mobile&expect=1&tag=mobile&xformat=super", "three", "one", "one");
        TestSelection selection4 = new TestSelection("4", "http://play.g3proxy.lecloud.com/vod/v2/MjQ5LzM3LzIwL2xldHYtdXRzLzE0L3Zlcl8wMF8yMi0xMTA3NjQxMzkwLWF2Yy00MTk4MTAtYWFjLTQ4MDAwLTUyNjExMC0zMTU1NTY1Mi00ZmJjYzFkNzA1NWMyNDc4MDc5OTYxODg1N2RjNzEwMi0xNDk4NTU3OTYxNzQ4Lm1wNA==?b=479&mmsid=65565355&tm=1499247143&key=98c7e781f1145aba07cb0d6ec06f6c12&platid=3&splatid=345&playid=0&tss=no&vtype=13&cvid=2026135183914&payff=0&pip=08cc52f8b09acd3eff8bf31688ddeced&format=0&sign=mb&dname=mobile&expect=1&tag=mobile&xformat=super", "four", "one", "one");
        TestSelection selection5 = new TestSelection("5", "http://play.g3proxy.lecloud.com/vod/v2/MjQ5LzM3LzIwL2xldHYtdXRzLzE0L3Zlcl8wMF8yMi0xMTA3NjQxMzkwLWF2Yy00MTk4MTAtYWFjLTQ4MDAwLTUyNjExMC0zMTU1NTY1Mi00ZmJjYzFkNzA1NWMyNDc4MDc5OTYxODg1N2RjNzEwMi0xNDk4NTU3OTYxNzQ4Lm1wNA==?b=479&mmsid=65565355&tm=1499247143&key=98c7e781f1145aba07cb0d6ec06f6c12&platid=3&splatid=345&playid=0&tss=no&vtype=13&cvid=2026135183914&payff=0&pip=08cc52f8b09acd3eff8bf31688ddeced&format=0&sign=mb&dname=mobile&expect=1&tag=mobile&xformat=super", "four", "one", "one");
        TestSelection selection6 = new TestSelection("6", "http://play.g3proxy.lecloud.com/vod/v2/MjQ5LzM3LzIwL2xldHYtdXRzLzE0L3Zlcl8wMF8yMi0xMTA3NjQxMzkwLWF2Yy00MTk4MTAtYWFjLTQ4MDAwLTUyNjExMC0zMTU1NTY1Mi00ZmJjYzFkNzA1NWMyNDc4MDc5OTYxODg1N2RjNzEwMi0xNDk4NTU3OTYxNzQ4Lm1wNA==?b=479&mmsid=65565355&tm=1499247143&key=98c7e781f1145aba07cb0d6ec06f6c12&platid=3&splatid=345&playid=0&tss=no&vtype=13&cvid=2026135183914&payff=0&pip=08cc52f8b09acd3eff8bf31688ddeced&format=0&sign=mb&dname=mobile&expect=1&tag=mobile&xformat=super", "four", "one", "one");
        TestSelection selection7 = new TestSelection("7", "http://play.g3proxy.lecloud.com/vod/v2/MjQ5LzM3LzIwL2xldHYtdXRzLzE0L3Zlcl8wMF8yMi0xMTA3NjQxMzkwLWF2Yy00MTk4MTAtYWFjLTQ4MDAwLTUyNjExMC0zMTU1NTY1Mi00ZmJjYzFkNzA1NWMyNDc4MDc5OTYxODg1N2RjNzEwMi0xNDk4NTU3OTYxNzQ4Lm1wNA==?b=479&mmsid=65565355&tm=1499247143&key=98c7e781f1145aba07cb0d6ec06f6c12&platid=3&splatid=345&playid=0&tss=no&vtype=13&cvid=2026135183914&payff=0&pip=08cc52f8b09acd3eff8bf31688ddeced&format=0&sign=mb&dname=mobile&expect=1&tag=mobile&xformat=super", "four", "one", "one");
        selections.add(selection1);
        selections.add(selection2);
        selections.add(selection3);
        selections.add(selection4);
        selections.add(selection5);
        selections.add(selection6);
        selections.add(selection7);
        flexPlayerSelectionAdapter.updateSelections(selections);
        showSelectionAnimator = ObjectAnimator.ofFloat(selectionContent,
                "translationX", selectionContentWidth, 0f);
        showSelectionAnimator.setDuration(duration);
        showSelectionAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                onAnimator = true;
                onShowSelectionContent = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                onAnimator = false;
                hiddenSelectionContentDelay(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                onAnimator = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        hiddenSelectionAnimator = ObjectAnimator.ofFloat(selectionContent,
                "translationX", 0f, selectionContentWidth);
        hiddenSelectionAnimator.setDuration(duration);
        hiddenSelectionAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                onAnimator = true;
                onShowSelectionContent = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                onAnimator = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                onAnimator = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        selectionRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_UP) {
                    if (onShowSelectionContent) {
                        hiddenSelectionContentDelay(true);
                    }
                }
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });
    }

    /**
     * 初始化分辨率模块
     */
    private void initResolutionContent() {
        resolutionContent = findViewById(R.id.resolution_content);
        resolutionRecyclerView = findViewById(R.id.resolution_recycler_view);
        flexPlayerResolutionAdapter = new FlexPlayerResolutionAdapter();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        resolutionRecyclerView.setLayoutManager(linearLayoutManager);
        resolutionRecyclerView.setAdapter(flexPlayerResolutionAdapter);
        flexPlayerResolutionAdapter.setOnItemClickListener(new FlexPlayerResolutionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View child, int position, FlexPlayerResolution resolution) {
                setUp(child.getContext(), resolution.getUrl());
                if (flexPlayer != null) {
                    flexPlayer.start();
                }
                hiddenResolutionContentDelay(false);
            }
        });

        ArrayList<FlexPlayerResolution> resolutions = new ArrayList<>();
        TestResolution resolution1 = new TestResolution("1", "http://play.g3proxy.lecloud.com/vod/v2/MjUxLzE2LzgvbGV0di11dHMvMTQvdmVyXzAwXzIyLTExMDc2NDEzODctYXZjLTE5OTgxOS1hYWMtNDgwMDAtNTI2MTEwLTE3MDg3NjEzLWY1OGY2YzM1NjkwZTA2ZGFmYjg2MTVlYzc5MjEyZjU4LTE0OTg1NTc2ODY4MjMubXA0?b=259&mmsid=65565355&tm=1499247143&key=f0eadb4f30c404d49ff8ebad673d3742&platid=3&splatid=345&playid=0&tss=no&vtype=21&cvid=2026135183914&payff=0&pip=08cc52f8b09acd3eff8bf31688ddeced&format=0&sign=mb&dname=mobile&expect=1&tag=mobile&xformat=super", "1080p");
        TestResolution resolution2 = new TestResolution("2", "http://play.g3proxy.lecloud.com/vod/v2/MjQ5LzM3LzIwL2xldHYtdXRzLzE0L3Zlcl8wMF8yMi0xMTA3NjQxMzkwLWF2Yy00MTk4MTAtYWFjLTQ4MDAwLTUyNjExMC0zMTU1NTY1Mi00ZmJjYzFkNzA1NWMyNDc4MDc5OTYxODg1N2RjNzEwMi0xNDk4NTU3OTYxNzQ4Lm1wNA==?b=479&mmsid=65565355&tm=1499247143&key=98c7e781f1145aba07cb0d6ec06f6c12&platid=3&splatid=345&playid=0&tss=no&vtype=13&cvid=2026135183914&payff=0&pip=08cc52f8b09acd3eff8bf31688ddeced&format=0&sign=mb&dname=mobile&expect=1&tag=mobile&xformat=super", "720p");
        TestResolution resolution3 = new TestResolution("3", "http://play.g3proxy.lecloud.com/vod/v2/MjQ5LzM3LzIwL2xldHYtdXRzLzE0L3Zlcl8wMF8yMi0xMTA3NjQxMzkwLWF2Yy00MTk4MTAtYWFjLTQ4MDAwLTUyNjExMC0zMTU1NTY1Mi00ZmJjYzFkNzA1NWMyNDc4MDc5OTYxODg1N2RjNzEwMi0xNDk4NTU3OTYxNzQ4Lm1wNA==?b=479&mmsid=65565355&tm=1499247143&key=98c7e781f1145aba07cb0d6ec06f6c12&platid=3&splatid=345&playid=0&tss=no&vtype=13&cvid=2026135183914&payff=0&pip=08cc52f8b09acd3eff8bf31688ddeced&format=0&sign=mb&dname=mobile&expect=1&tag=mobile&xformat=super", "480p");
        TestResolution resolution4 = new TestResolution("4", "http://play.g3proxy.lecloud.com/vod/v2/MjQ5LzM3LzIwL2xldHYtdXRzLzE0L3Zlcl8wMF8yMi0xMTA3NjQxMzkwLWF2Yy00MTk4MTAtYWFjLTQ4MDAwLTUyNjExMC0zMTU1NTY1Mi00ZmJjYzFkNzA1NWMyNDc4MDc5OTYxODg1N2RjNzEwMi0xNDk4NTU3OTYxNzQ4Lm1wNA==?b=479&mmsid=65565355&tm=1499247143&key=98c7e781f1145aba07cb0d6ec06f6c12&platid=3&splatid=345&playid=0&tss=no&vtype=13&cvid=2026135183914&payff=0&pip=08cc52f8b09acd3eff8bf31688ddeced&format=0&sign=mb&dname=mobile&expect=1&tag=mobile&xformat=super", "360p");
        resolutions.add(resolution1);
        resolutions.add(resolution2);
        resolutions.add(resolution3);
        resolutions.add(resolution4);
        flexPlayerResolutionAdapter.updateData(resolutions);

        showResolutionAnimator = ObjectAnimator.ofFloat(resolutionContent, "translationX", speedContentWidth, 0f);
        showResolutionAnimator.setDuration(duration);
        showResolutionAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                onAnimator = true;
                onShowResolutionContent = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                onAnimator = false;
                hiddenResolutionContentDelay(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                onAnimator = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        hiddenResolutionAnimator = ObjectAnimator.ofFloat(resolutionContent, "translationX", 0f, speedContentWidth);
        hiddenResolutionAnimator.setDuration(duration);
        hiddenResolutionAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                onAnimator = true;
                onShowResolutionContent = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                onAnimator = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                onAnimator = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        resolutionRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_UP) {
                    if (onShowResolutionContent) {
                        hiddenResolutionContentDelay(true);
                    }
                }
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });
    }

    /**
     * 初始化倍速模块
     */
    private void initSpeedContent() {
        speedGroup = findViewById(R.id.speed_content);
        speedRecyclerView = findViewById(R.id.speed_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        flexPlayerSpeedAdapter = new FlexPlayerSpeedAdapter();
        flexPlayerSpeedAdapter.setOnItemClickListener(new FlexPlayerSpeedAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View child, int position, FlexPlayerSpeed speed) {
                if (flexPlayer != null) {
                    flexPlayer.setSpeed(speed.getSpeed());
                }
            }
        });
        speedRecyclerView.setLayoutManager(layoutManager);
        speedRecyclerView.setAdapter(flexPlayerSpeedAdapter);

        showSpeedContentAnimator = ObjectAnimator.ofFloat(speedGroup, "translationX", speedContentWidth, 0f);
        showSpeedContentAnimator.setDuration(duration);
        showSpeedContentAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                onAnimator = true;
                onShowSpeedContent = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                onAnimator = false;
                hiddenSpeedContentDelay(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                onAnimator = false;

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        hiddenSpeedContentAnimator = ObjectAnimator.ofFloat(speedGroup, "translationX", 0f, speedContentWidth);
        hiddenSpeedContentAnimator.setDuration(duration);
        hiddenSpeedContentAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                onAnimator = true;
                onShowSpeedContent = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                onAnimator = false;

            }

            @Override
            public void onAnimationCancel(Animator animation) {
                onAnimator = false;

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        speedRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_UP) {
                    if (onShowSpeedContent) {
                        hiddenSpeedContentDelay(true);
                    }
                }
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });
    }

    /**
     * 展示上部和下部的控制工具栏
     *
     * @param show 是否显示，true显示，false 不显示
     */
    private void showToolBar(boolean show) {
        if (!onAnimator) {
            if (show && onToolContentHidden) {
                showToolContentAnimator.start();
            } else if (!show && !onToolContentHidden) {
                hiddenToolContentAnimator.start();
            }
        }
        if (currentMode == FlexPlayer.Mode.FULL_SCREEN) {
            if (show) {
                showLockContent();
            } else {
                hiddenLockContentRunnableDelay(false);
            }
        }
    }

    /**
     * 启动更新时间任务，只在全屏模式下才会更新
     *
     * @param mode
     */
    private void startUpdateTime(FlexPlayer.Mode mode) {
        if (mode == FlexPlayer.Mode.FULL_SCREEN) {
            if (!onUpdateTime) {
                handler.removeCallbacks(updateTimeRunnable);
                updateTimeRunnable.run();
                onUpdateTime = true;
            }
        }
    }

    /**
     * 停止更新时间任务
     */
    private void stopUpdateTime() {
        handler.removeCallbacks(updateTimeRunnable);
        onUpdateTime = false;
    }

    /**
     * 隐藏锁定组件，非动画形式
     */
    private void hiddenLockContent() {
        onShowLockContent = false;
        handler.removeCallbacks(hiddenLockContentRunnable);
        lockContent.setTranslationX(-lockMarginLeft);
    }

    /**
     * 隐藏锁定组件，动画形式
     *
     * @param delay 是否延迟，true 延迟， false 不延迟
     */
    private void hiddenLockContentRunnableDelay(boolean delay) {
        handler.removeCallbacks(hiddenLockContentRunnable);
        if (delay) {
            handler.postDelayed(hiddenLockContentRunnable, 5000);
        } else {
            hiddenLockContentRunnable.run();
        }
    }

    /**
     * 显示锁定组件，动画形式
     */
    private void showLockContent() {
        handler.removeCallbacks(hiddenLockContentRunnable);
        if (!lockOnAnimator && !onShowLockContent) {
            showLockContentAnimator.start();
        }
    }

    /**
     * 显示锁定组件，非动画形式
     */
    private void showLockContentMoment() {
        handler.removeCallbacks(hiddenSpeedContentRunnable);
        onShowLockContent = true;
        lockContent.setTranslationX(0);
    }

    /**
     * 隐藏倍速控制组件，非动画形式
     */
    private void hiddenSpeedContent() {
        onShowSpeedContent = false;
        handler.removeCallbacks(hiddenSpeedContentRunnable);
        speedGroup.setTranslationX(speedContentWidth);
    }

    /**
     * 隐藏分辨率控制组件，非动画形式
     */
    private void hiddenResolutionContent() {
        onShowResolutionContent = false;
        handler.removeCallbacks(hiddenResolutionContentRunnable);
        resolutionContent.setTranslationX(speedContentWidth);
    }

    /**
     * 隐藏剧集控制组件，非动画形式
     */
    private void hiddenSelectionContent() {
        onShowSelectionContent = false;
        handler.removeCallbacks(hiddenSelectionContentRunnable);
        selectionContent.setTranslationX(selectionContentWidth);
    }

    /**
     * 隐藏工具栏组件，非动画形式
     */
    private void hiddenToolContent() {
        handler.removeCallbacks(hiddenToolContentRunnable);
        onAnimator = false;
        onToolContentHidden = true;
        top.setTranslationY(-barHeight);
        bottom.setTranslationY(barHeight);
    }

    /**
     * 隐藏工具栏组件，动画形式
     *
     * @param delay 是否延迟触发，true延迟触发，false不延迟
     */
    private void hiddenToolContentDelay(boolean delay) {
        handler.removeCallbacks(hiddenToolContentRunnable);
        if (delay) {
            handler.postDelayed(hiddenToolContentRunnable, 5000);
        } else {
            hiddenToolContentRunnable.run();
        }
    }

    /**
     * 显示倍速控制组件，动画形式
     */
    private void showSpeedContent() {
        handler.removeCallbacks(hiddenSpeedContentRunnable);
        if (!onShowSpeedContent) {
            showSpeedContentAnimator.start();
        }
    }

    /**
     * 隐藏倍速控制组件，动画形式
     *
     * @param delay 是否延迟，true延迟，false不延迟
     */
    private void hiddenSpeedContentDelay(boolean delay) {
        handler.removeCallbacks(hiddenSpeedContentRunnable);
        if (delay) {
            handler.postDelayed(hiddenSpeedContentRunnable, 5000);
        } else {
            hiddenSpeedContentRunnable.run();
        }
    }

    /**
     * 显示剧集控制组件，动画形式
     */
    private void showSelectionContent() {
        handler.removeCallbacks(hiddenSelectionContentRunnable);
        if (!onShowSelectionContent) {
            showSelectionAnimator.start();
        }
    }

    /**
     * 隐藏剧集组件
     *
     * @param delay 是否需要延迟触发，true需要延迟，false不需要延迟
     */
    private void hiddenSelectionContentDelay(boolean delay) {
        handler.removeCallbacks(hiddenSelectionContentRunnable);
        if (delay) {
            handler.postDelayed(hiddenSelectionContentRunnable, 5000);
        } else {
            hiddenSelectionContentRunnable.run();
        }
    }

    /**
     * 显示分辨率组件
     */
    private void showResolutionContent() {
        handler.removeCallbacks(hiddenResolutionContentRunnable);
        if (!onShowResolutionContent) {
            showResolutionAnimator.start();
        }
    }

    /**
     * 隐藏分辨率组件
     *
     * @param delay 是否延迟，true延迟，false不延迟
     */
    private void hiddenResolutionContentDelay(boolean delay) {
        handler.removeCallbacks(hiddenResolutionContentRunnable);
        if (delay) {
            handler.postDelayed(hiddenResolutionContentRunnable, 5000);
        } else {
            hiddenResolutionContentRunnable.run();
        }
    }

    /**
     * 清除各种隐藏定时控制
     */
    private void clearAllHiddenRunnable() {
        handler.removeCallbacks(hiddenSelectionContentRunnable);
        handler.removeCallbacks(hiddenResolutionContentRunnable);
        handler.removeCallbacks(hiddenSpeedContentRunnable);
        handler.removeCallbacks(hiddenToolContentRunnable);
    }

    /**
     * 是否显示返回按钮
     *
     * @param show
     */
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
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            clearAllHiddenRunnable();
        } else if (action == MotionEvent.ACTION_UP) {
            if (!isLock && !detailByOnTouchEvent) {
                hiddenToolContentDelay(true);
            }
            detailByOnTouchEvent = false;
        }
        return super.dispatchTouchEvent(ev);
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
        if (isLock || currentState == FlexPlayer.State.NONE
                || currentState == FlexPlayer.State.PREPARE) {
            return super.onTouchEvent(event);
        }

        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_CANCEL:
                break;
            case MotionEvent.ACTION_DOWN:
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
                detailByOnTouchEvent = true;
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
                    if (onShowSpeedContent) {
                        hiddenSpeedContentDelay(false);
                    } else if (onShowResolutionContent) {
                        hiddenResolutionContentDelay(false);
                    } else if (onShowSelectionContent) {
                        hiddenSelectionContentDelay(false);
                    } else {
                        showToolBar(onToolContentHidden);
                    }
                } else {
                    hiddenToolContentDelay(true);
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
        updateSpeed(flexPlayer.getSpeed());
    }

    /**
     * 退出全屏模式，在全屏模式下退出并且检测是否需要自己处理
     *
     * @return true 需要自己处理，false 不需要自己处理
     */
    public boolean exitFullScreen() {
        if (isLock) {
            return true;
        }
        if (onShowSpeedContent) {
            hiddenSpeedContentDelay(false);
            return true;
        } else if (onShowResolutionContent) {
            hiddenResolutionContentDelay(false);
            return true;
        } else if (onShowSelectionContent) {
            hiddenSelectionContentDelay(false);
            return true;
        }
        hiddenLockContent();
        return false;
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
        } else if (id == R.id.player_speed) {
            showToolBar(false);
            showSpeedContent();
        } else if (id == R.id.resolution_flag) {
            showToolBar(false);
            showResolutionContent();
        } else if (id == R.id.selection_flag) {
            showToolBar(false);
            showSelectionContent();
        }

    }

    /**
     * 设置当前的播放窗口模式，全屏或非全屏
     *
     * @param mode
     */
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
            changeContent.setVisibility(View.VISIBLE);
            enterFullScreen.setVisibility(View.GONE);
            if (!onToolContentHidden) {
                showLockContentMoment();
            }
        } else {
            enterFullScreen.setBackgroundResource(R.drawable.ic_player_enlarge);
            batteryTimeLayout.setVisibility(View.GONE);
            if (hasRegisterBatteryReceiver) {
                getContext().unregisterReceiver(batterReceiver);
                hasRegisterBatteryReceiver = false;
            }
            stopUpdateTime();
            changeContent.setVisibility(View.GONE);
            enterFullScreen.setVisibility(View.VISIBLE);
        }
        currentMode = mode;
    }

    /**
     * 获取封面ImageView
     * @return 封面控件
     */
    public ImageView getCoverImage() {
        return coverImage;
    }

    /**
     * 设置中间播放按钮的资源
     * @param res 资源
     */
    public void setCenterStartBackground(int res) {
        centerStart.setBackgroundResource(res);
    }

    /**
     * 设置标题
     * @param title
     */
    public void setTitle(String title) {
        titleTextView.setText(title);
    }

    /**
     * 设置标题
     * @param titleRes
     */
    public void setTitle(@StringRes int titleRes) {
        titleTextView.setText(titleRes);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopUpdateTime();
    }

    /**
     * 更新播放速度
     *
     * @param speed 播放速度
     */
    private void updateSpeed(float speed) {
        if (flexPlayerSpeedAdapter != null) {
            flexPlayerSpeedAdapter.updateSpeed(speed);
        }
    }

    /**
     * 更新播放进度
     */
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

    /**
     * 设置当前播放器的播放状态
     *
     * @param state 播放状态
     */
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
                hiddenToolContent();
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
                updateSpeed(flexPlayer.getSpeed());
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
                showToolBar(false);
                break;
        }
        currentState = state;
    }

}
