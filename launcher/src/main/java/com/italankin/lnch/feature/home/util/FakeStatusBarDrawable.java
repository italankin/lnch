package com.italankin.lnch.feature.home.util;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class FakeStatusBarDrawable extends Drawable {

    private final ColorDrawable drawable;
    private final int defaultColor;
    private final int height;

    public FakeStatusBarDrawable(int color, int height) {
        this.drawable = new ColorDrawable(color);
        this.defaultColor = color;
        this.height = height;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        drawable.draw(canvas);
    }

    @Override
    public void setAlpha(int alpha) {
        drawable.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        drawable.setColorFilter(colorFilter);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSPARENT;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        bounds.bottom = height;
        drawable.setBounds(bounds);
    }

    public void setColor(@ColorInt Integer color) {
        drawable.setColor(color != null ? color : defaultColor);
        invalidateSelf();
    }
}
