package com.italankin.lnch.util.widget.popup;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.italankin.lnch.R;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.annotation.Px;

public class ArrowLayout extends FrameLayout implements PopupFrameView.Child {

    private final ArrowDrawable arrowDrawable = new ArrowDrawable();
    private final Rect bounds = new Rect();

    private final int[] colors = new int[ArrowDrawable.Direction.values().length];
    private int anchorX;
    private int anchorY;

    public ArrowLayout(Context context) {
        super(context);
    }

    public ArrowLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ArrowLayout);
        setArrowSize(a.getDimensionPixelSize(R.styleable.ArrowLayout_al_arrowSize, 0));
        setArrowColor(ArrowDrawable.Direction.UP, a.getColor(R.styleable.ArrowLayout_al_colorArrowUp, Color.BLACK));
        setArrowColor(ArrowDrawable.Direction.DOWN, a.getColor(R.styleable.ArrowLayout_al_colorArrowDown, Color.BLACK));
        a.recycle();
    }

    public void setArrowSize(@Px int size) {
        arrowDrawable.setSize(size);
        update();
    }

    @Override
    public void setAnchorPoint(int ax, int ay, PopupFrameView.Location location) {
        anchorX = ax;
        anchorY = ay;
        arrowDrawable.setDirection(location == PopupFrameView.Location.TOP
                ? ArrowDrawable.Direction.DOWN
                : ArrowDrawable.Direction.UP);
        update();
    }

    public void clearAnchorPoint() {
        anchorX = -1;
        anchorY = -1;
        update();
    }

    public void setArrowColor(ArrowDrawable.Direction direction, @ColorInt int color) {
        colors[direction.ordinal()] = color;
        invalidate();
    }

    public void setArrowColors(@ColorInt int color) {
        for (ArrowDrawable.Direction direction : ArrowDrawable.Direction.values()) {
            colors[direction.ordinal()] = color;
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        arrowDrawable.draw(canvas);
    }

    private void update() {
        if (anchorX < 0 || anchorY < 0) {
            bounds.setEmpty();
            return;
        }
        ArrowDrawable.Direction direction = arrowDrawable.getDirection();
        bounds.set(0, 0, arrowDrawable.getIntrinsicWidth(), arrowDrawable.getIntrinsicHeight());
        int left = anchorX - arrowDrawable.getIntrinsicWidth() / 2;
        int yOffset;
        yOffset = direction == ArrowDrawable.Direction.UP
                ? arrowDrawable.getIntrinsicHeight() - getPaddingTop()
                : getPaddingBottom();
        int top = anchorY - yOffset;
        bounds.offset(left, top);
        arrowDrawable.setColor(colors[direction.ordinal()]);
        arrowDrawable.setBounds(bounds);
        invalidate();
    }
}
