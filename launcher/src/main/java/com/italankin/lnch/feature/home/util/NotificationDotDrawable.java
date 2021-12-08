package com.italankin.lnch.feature.home.util;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class NotificationDotDrawable extends Drawable {

    private static final AccelerateDecelerateInterpolator INTERPOLATOR = new AccelerateDecelerateInterpolator();
    private static final long DURATION = 350;
    private static final float[] VALUES_APPEAR = {0, 1};
    private static final float[] VALUES_DISAPPEAR = {1, 0};

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int defaultColor;
    private final Rect rect = new Rect();
    private final int size;
    private final int radius;
    private int margin;

    private boolean visible = false;
    private ValueAnimator animator;
    private float scale = 0f;
    private final ValueAnimator.AnimatorUpdateListener updateListener = animation -> {
        this.scale = (float) animation.getAnimatedValue();
        invalidateSelf();
    };

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    public NotificationDotDrawable(int size, int defaultColor, int shadowColor) {
        this.size = size;
        this.radius = size / 2;
        this.defaultColor = defaultColor;
        paint.setColor(defaultColor);
        paint.setShadowLayer(size / 4, 0, 0, shadowColor);
    }

    public void setColor(Integer color) {
        int newColor = color != null ? color : defaultColor;
        if (paint.getColor() != newColor) {
            paint.setColor(newColor);
            invalidateSelf();
        }
    }

    public void setMargin(int margin) {
        if (this.margin != margin) {
            this.margin = margin;
            invalidateSelf();
        }
    }

    public void setBadgeVisible(boolean visible, boolean animated) {
        if (this.visible != visible || animated) {
            this.visible = visible;
            if (animator != null) {
                animator.cancel();
            }
            if (animated) {
                animator = ObjectAnimator.ofFloat(visible ? VALUES_APPEAR : VALUES_DISAPPEAR);
                animator.setInterpolator(INTERPOLATOR);
                animator.addUpdateListener(updateListener);
                animator.setDuration(DURATION);
                animator.start();
                scale = visible ? 0f : 1f;
            } else {
                scale = visible ? 1f : 0f;
            }
            invalidateSelf();
        }
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        int rad = (int) (radius * scale);
        if (rad == 0 || !isVisible()) {
            return;
        }
        canvas.drawCircle(rect.centerX(), rect.centerY(), rad, paint);
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSPARENT;
    }

    @Override
    public int getIntrinsicWidth() {
        return size;
    }

    @Override
    public int getIntrinsicHeight() {
        return size;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        rect.set(bounds);
        rect.offset(-margin, margin);
    }
}
