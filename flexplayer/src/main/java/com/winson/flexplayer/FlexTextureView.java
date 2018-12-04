package com.winson.flexplayer;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.TextureView;

import androidx.annotation.RequiresApi;

/**
 * @date on 2018/12/4
 * @Author Winson
 */
public class FlexTextureView extends TextureView {

    static final int HORIZONTAL = 0;
    static final int VERTICAL = 1;

    private float rate;
    private int orientation = HORIZONTAL;

    public FlexTextureView(Context context) {
        super(context);
    }

    public FlexTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FlexTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public FlexTextureView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    void setRate(float rate) {
        this.rate = rate;
        requestLayout();
    }

    void setOrientation(int orientation) {
        this.orientation = orientation;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpec = widthMeasureSpec;
        int heightSpec = heightMeasureSpec;
        if (orientation == HORIZONTAL) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            heightSpec = MeasureSpec.makeMeasureSpec(Math.round(width * rate), MeasureSpec.EXACTLY);
        } else {
            int height = MeasureSpec.getSize(heightMeasureSpec);
            widthSpec = MeasureSpec.makeMeasureSpec(Math.round(height * rate), MeasureSpec.EXACTLY);
        }

        super.onMeasure(widthSpec, heightSpec);
    }

}
