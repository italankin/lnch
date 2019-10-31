package com.italankin.lnch.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;

public final class ResUtils {

    public static int px2dp(Context context, int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                context.getResources().getDisplayMetrics());
    }

    public static int px2dp(int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                Resources.getSystem().getDisplayMetrics());
    }

    public static TypedValue resolveAttribute(Context context, @AttrRes int attr) {
        if (context == context.getApplicationContext()) {
            throw new IllegalArgumentException("Cannot accept app context");
        }
        TypedValue out = new TypedValue();
        if (context.getTheme().resolveAttribute(attr, out, true)) {
            return out;
        }
        return null;
    }

    @ColorInt
    public static int resolveColor(Context context, @AttrRes int attr) {
        TypedValue out = resolveAttribute(context, attr);
        if (out == null) {
            return Color.BLACK;
        }
        return out.data;
    }

    public static Bitmap bitmapFromDrawable(Drawable icon, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas();
        canvas.setBitmap(bitmap);
        icon.setBounds(0, 0, width, height);
        icon.draw(canvas);
        return bitmap;
    }

    private ResUtils() {
        // no instance
    }
}
