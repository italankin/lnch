package com.italankin.lnch.util.icons;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CircleDrawable extends Drawable {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float cx;
    private float cy;
    private float radius;

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.drawCircle(cx, cy, radius, paint);
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
        invalidateSelf();
    }

    public void setColor(@ColorInt int color) {
        paint.setColor(color);
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
        cx = bounds.centerX();
        cy = bounds.centerY();
        radius = Math.min(bounds.width(), bounds.height()) / 2f;
    }
}
