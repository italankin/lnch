package com.italankin.lnch.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.util.TypedValue;

public final class ResUtils {

    public static int px2dp(Context context, int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                context.getResources().getDisplayMetrics());
    }

    public static int px2dp(int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                Resources.getSystem().getDisplayMetrics());
    }

    public static int resolveAttribute(Context context, @AttrRes int attr) {
        if (context == context.getApplicationContext()) {
            throw new IllegalArgumentException("Cannot accept app context");
        }
        TypedValue out = new TypedValue();
        context.getTheme().resolveAttribute(attr, out, true);
        return out.resourceId;
    }

    public static Drawable resolveDrawable(Context context, @AttrRes int attr) {
        return context.getDrawable(resolveAttribute(context, attr));
    }

    @ColorInt
    public static int resolveColor(Context context, @AttrRes int attr) {
        return context.getColor(resolveAttribute(context, attr));
    }

    private ResUtils() {
        // no instance
    }
}
