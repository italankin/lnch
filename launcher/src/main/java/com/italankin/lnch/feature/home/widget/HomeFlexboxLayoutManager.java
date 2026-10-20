package com.italankin.lnch.feature.home.widget;

import android.content.Context;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

public class HomeFlexboxLayoutManager extends FlexboxLayoutManager {

    public HomeFlexboxLayoutManager(Context context) {
        super(context, FlexDirection.ROW);
    }

    // Flexbox 3.0.0's FLEX_END placement uses right padding at the left edge and
    // left padding at the right edge. Compensate here, keeping the RecyclerView's
    // physical padding unchanged. The total padding used for measurement is unchanged.
    // This also handles RTL, where FLEX_END places items at the physical left edge.
    @Override
    public int getPaddingLeft() {
        return getJustifyContent() == JustifyContent.FLEX_END
                ? super.getPaddingRight() : super.getPaddingLeft();
    }

    @Override
    public int getPaddingRight() {
        return getJustifyContent() == JustifyContent.FLEX_END
                ? super.getPaddingLeft() : super.getPaddingRight();
    }
}
