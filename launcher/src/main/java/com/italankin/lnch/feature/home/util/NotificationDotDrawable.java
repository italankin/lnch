package com.italankin.lnch.feature.home.util;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class NotificationDotDrawable extends Drawable {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect rect = new Rect();
    private final int size;
    private final int radius;
    private int margin;

    private boolean isVisible = false;

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    public NotificationDotDrawable(int size, int color, int shadowColor) {
        this.size = size;
        this.radius = size / 2;
        paint.setColor(color);
        paint.setShadowLayer(size / 4, 0, 0, shadowColor);
    }

    public void setMargin(int margin) {
        if (this.margin != margin) {
            this.margin = margin;
            invalidateSelf();
        }
    }

    public void setVisible(boolean isVisible) {
        if (this.isVisible != isVisible) {
            this.isVisible = isVisible;
            invalidateSelf();
        }
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (isVisible) {
            canvas.drawCircle(rect.centerX(), rect.centerY(), radius, paint);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
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
