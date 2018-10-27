package com.italankin.lnch.util;

import android.content.Context;
import android.content.res.Resources;
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

    private ResUtils() {
        // no instance
    }
}
