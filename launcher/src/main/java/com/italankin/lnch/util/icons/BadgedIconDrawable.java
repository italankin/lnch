package com.italankin.lnch.util.icons;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class BadgedIconDrawable extends Drawable {

    private static final float ICON_INSET_FACTOR = 0.8f;
    private static final float BADGE_FACTOR = 2;

    private final Drawable icon;
    private final Drawable badgeIcon;
    private int badgeOffsetX;
    private int badgeOffsetY;

    public BadgedIconDrawable(Drawable icon, Drawable badgeIcon) {
        this.icon = icon;
        this.badgeIcon = badgeIcon;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        icon.draw(canvas);
        canvas.save();
        canvas.translate(badgeOffsetX, badgeOffsetY);
        badgeIcon.draw(canvas);
        canvas.restore();
    }

    @Override
    public void setAlpha(int alpha) {
        icon.setAlpha(alpha);
        badgeIcon.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        icon.setColorFilter(colorFilter);
        badgeIcon.setColorFilter(colorFilter);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        int badgeSize = (int) Math.min(bounds.width() / BADGE_FACTOR, bounds.height() / BADGE_FACTOR);
        badgeIcon.setBounds(0, 0, badgeSize, badgeSize);

        badgeOffsetX = bounds.right - badgeSize;
        badgeOffsetY = bounds.bottom - badgeSize;

        Rect b = new Rect(bounds);
        int width = (int) (bounds.width() * ICON_INSET_FACTOR);
        b.right = width;
        float factor = icon.getIntrinsicHeight() / (float) icon.getIntrinsicWidth();
        b.bottom = (int) (width * factor);
        b.offset((bounds.width() - b.width()) / 2, (bounds.height() - b.height()) / 2);
        icon.setBounds(b);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSPARENT;
    }
}
