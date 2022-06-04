package com.italankin.lnch.feature.home.util;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.italankin.lnch.R;

public class NotificationDotDrawable extends Drawable {

    private static final AccelerateDecelerateInterpolator INTERPOLATOR = new AccelerateDecelerateInterpolator();
    private static final long DURATION = 350;
    private static final float[] VALUES_APPEAR = {0, 1};
    private static final float[] VALUES_DISAPPEAR = {1, 0};

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Resources resources;
    private final int defaultColor;
    private final Rect rect = new Rect();
    private int radius;
    private int margin;
    private int gravity = Gravity.END | Gravity.TOP;
    private Size size = Size.NORMAL;

    private boolean visible = false;
    private ValueAnimator animator;
    private float scale = 0f;
    private final ValueAnimator.AnimatorUpdateListener updateListener = animation -> {
        this.scale = (float) animation.getAnimatedValue();
        invalidateSelf();
    };

    public NotificationDotDrawable(Context context, int shadowColor) {
        this.resources = context.getResources();
        this.defaultColor = ContextCompat.getColor(context, R.color.notification_dot);
        paint.setColor(defaultColor);
        float shadowRadius = resources.getDimension(R.dimen.notification_dot_shadow);
        paint.setShadowLayer(shadowRadius, 0, 0, shadowColor);
        setSize(size);
    }

    public void setSize(Size size) {
        if (this.size != size) {
            this.size = size;
            updateRect();
        }
    }

    public void setColor(Integer color) {
        int newColor = color != null ? color : defaultColor;
        if (paint.getColor() != newColor) {
            paint.setColor(newColor);
            invalidateSelf();
        }
    }

    public void setGravity(int gravity) {
        if (this.gravity != gravity) {
            this.gravity = gravity;
            updateRect();
        }
    }

    public void setMargin(int margin) {
        if (this.margin != margin) {
            this.margin = margin;
            updateRect();
        }
    }

    private void updateRect() {
        radius = resources.getDimensionPixelSize(size.dimenRes);
        int size = (radius + margin) * 2;
        this.rect.set(0, 0, size, size);
        onBoundsChange(getBounds());
        invalidateSelf();
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

    public void cancelAnimation() {
        if (animator != null) {
            animator.cancel();
            if (visible) {
                scale = 1f;
            } else {
                scale = 0f;
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
    protected void onBoundsChange(Rect bounds) {
        Gravity.apply(gravity, rect.width(), rect.height(), bounds, rect);
    }

    public enum Size {
        SMALL(R.dimen.notification_dot_size_small),
        NORMAL(R.dimen.notification_dot_size_normal),
        LARGE(R.dimen.notification_dot_size_large);

        @DimenRes
        final int dimenRes;

        Size(int dimenRes) {
            this.dimenRes = dimenRes;
        }
    }
}
