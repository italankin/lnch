package com.italankin.lnch.feature.home.util;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.Nullable;

import com.italankin.lnch.R;
import com.italankin.lnch.util.ViewUtils;

public final class HomeItemViewUtils {

    @Nullable
    public static Rect getItemBoundsInWindow(@Nullable View itemView) {
        View label = itemView != null ? itemView.findViewById(R.id.itemLabel) : null;
        if (label == null) {
            return null;
        }
        int[] location = new int[2];
        label.getLocationInWindow(location);
        return new Rect(location[0], location[1],
                location[0] + label.getWidth(), location[1] + label.getHeight());
    }

    @Nullable
    public static Rect getPopupAnchorBounds(@Nullable View itemView) {
        View label = itemView != null ? itemView.findViewById(R.id.itemLabel) : null;
        return ViewUtils.getViewBoundsInsetPadding(label);
    }

    private HomeItemViewUtils() {
    }
}
