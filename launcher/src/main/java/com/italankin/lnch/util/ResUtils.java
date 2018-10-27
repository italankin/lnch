package com.italankin.lnch.util;

import android.content.Context;
import android.support.annotation.Dimension;
import android.util.TypedValue;

public final class ResUtils {

    public static int px2dp(Context context, @Dimension int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                context.getResources().getDisplayMetrics());
    }

    private ResUtils() {
        // no instance
    }
}
