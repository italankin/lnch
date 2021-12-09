package com.italankin.lnch.util.widget.popup;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

class ArrowDrawable extends Drawable {

    private static final float HEIGHT_FACTOR = .66f;

    private final Paint paint;
    private final Path path = new Path();
    private int width;
    private int height;
    private Direction direction;

    ArrowDrawable() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        setDirection(Direction.DOWN);
    }

    public void setColor(int color) {
        paint.setColor(color);
        invalidateSelf();
    }

    public void setSize(int size) {
        this.width = size;
        this.height = (int) (size * HEIGHT_FACTOR);
        invalidateSelf();
    }

    public void setDirection(Direction direction) {
        this.direction = direction;

        path.reset();
        if (direction == Direction.DOWN) {
            path.moveTo(0, 0);
            path.lineTo(width, 0);
            path.lineTo(width / 2f, height);
        } else {
            path.moveTo(0, height);
            path.lineTo(width, height);
            path.lineTo(width / 2f, 0);
        }
        path.close();
        invalidateSelf();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (width == 0 || height == 0) {
            return;
        }
        canvas.save();
        Rect b = getBounds();
        canvas.translate(b.left, b.top);
        canvas.drawPath(path, paint);
        canvas.restore();
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
    public int getIntrinsicWidth() {
        return width;
    }

    @Override
    public int getIntrinsicHeight() {
        return height;
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    public Direction getDirection() {
        return direction;
    }

    enum Direction {
        UP,
        DOWN
    }
}
