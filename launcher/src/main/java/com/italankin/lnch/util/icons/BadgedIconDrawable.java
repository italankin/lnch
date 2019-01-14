package com.italankin.lnch.util.icons;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BadgedIconDrawable extends Drawable {

    private static final float ICON_INSET_FACTOR = 0.8f;
    private static final float BADGE_FACTOR = 2;

    private final Drawable icon;
    private final Drawable badge;

    public BadgedIconDrawable(Drawable icon, Drawable badge) {
        this.icon = icon;
        this.badge = badge;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        icon.draw(canvas);
        badge.draw(canvas);
    }

    @Override
    public void setAlpha(int alpha) {
        icon.setAlpha(alpha);
        badge.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        icon.setColorFilter(colorFilter);
        badge.setColorFilter(colorFilter);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        int badgeSize = (int) Math.min(bounds.width() / BADGE_FACTOR, bounds.height() / BADGE_FACTOR);
        Rect bb = new Rect(0, 0, badgeSize, badgeSize);
        bb.offset(bounds.right - badgeSize, bounds.bottom - badgeSize);
        badge.setBounds(bb);

        Rect ib = new Rect(bounds);
        int width = (int) (bounds.width() * ICON_INSET_FACTOR);
        ib.right = width;
        float factor = icon.getIntrinsicHeight() / (float) icon.getIntrinsicWidth();
        ib.bottom = (int) (width * factor);
        ib.offset((bounds.width() - ib.width()) / 2, (bounds.height() - ib.height()) / 2);
        icon.setBounds(ib);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSPARENT;
    }
}
