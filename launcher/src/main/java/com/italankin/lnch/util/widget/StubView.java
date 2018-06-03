package com.italankin.lnch.util.widget;

import android.content.Context;
import android.view.View;

public class StubView extends View {
    public StubView(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(0, 0);
    }
}
