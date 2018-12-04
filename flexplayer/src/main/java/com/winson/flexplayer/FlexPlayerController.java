package com.winson.flexplayer;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

/**
 * @date on 2018/12/4
 * @Author Winson
 */
public class FlexPlayerController extends FrameLayout implements View.OnClickListener {

    protected FlexPlayer flexPlayer;
    protected FlexPlayer.State currentState;
    protected FlexPlayer.Mode currentMode;

    protected View enterFullScreen;
    protected ImageView coverImage;
    protected View loadingView;
    protected View centerStart;
    protected TextView positionTextView, durationTextView;
    protected SeekBar seekBar;
    protected ImageView restartOrPause;

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
        View content = LayoutInflater.from(context).inflate(R.layout.flex_player_controller, this, false);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        content.setLayoutParams(lp);
        addView(content);

        coverImage = content.findViewById(R.id.cover_image);
        loadingView = content.findViewById(R.id.loading);

        enterFullScreen = content.findViewById(R.id.full_screen);
        enterFullScreen.setOnClickListener(this);

        centerStart = content.findViewById(R.id.center_start);
        centerStart.setOnClickListener(this);

        seekBar = content.findViewById(R.id.seek);

        positionTextView = content.findViewById(R.id.position);
        durationTextView = content.findViewById(R.id.duration);
        restartOrPause = content.findViewById(R.id.restart_or_pause);
        restartOrPause.setOnClickListener(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
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
            if (flexPlayer != null) {
                flexPlayer.start();
            }
        } else if (id == R.id.restart_or_pause) {
            if (flexPlayer != null) {
                if(currentState == FlexPlayer.State.PAUSE){
                    flexPlayer.play();
                }else{
                    flexPlayer.pause();
                }
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

    public void updateProgress() {
        long position = flexPlayer.getPosition();
        long duration = flexPlayer.getDuration();
        int bufferPercentage = flexPlayer.getBufferPercentage();
        seekBar.setSecondaryProgress(bufferPercentage);
        int progress = (int) (100f * position / duration);
        seekBar.setProgress(progress);
        positionTextView.setText(FlexPlayerUtils.formatTime(position));
        durationTextView.setText(FlexPlayerUtils.formatTime(duration));
    }

    public void setCurrentState(FlexPlayer.State state) {
        switch (state) {
            case NONE:
                coverImage.setVisibility(View.VISIBLE);
                loadingView.setVisibility(View.GONE);
                restartOrPause.setBackgroundResource(R.drawable.ic_player_start);
                break;
            case BUFFER_START:
                loadingView.setVisibility(View.VISIBLE);
                break;
            case BUFFER_END:
                loadingView.setVisibility(View.GONE);
                break;
            case PLAY:
                restartOrPause.setBackgroundResource(R.drawable.ic_player_pause);
                coverImage.setVisibility(View.GONE);
                loadingView.setVisibility(View.GONE);
                break;
            case PAUSE:
                restartOrPause.setBackgroundResource(R.drawable.ic_player_start);
                break;
            case COMPLETE:
                restartOrPause.setBackgroundResource(R.drawable.ic_player_start);
                loadingView.setVisibility(View.GONE);
                break;
        }
        currentState = state;
    }

}
