package com.italankin.lnch.feature.widgets.util;

import android.content.Context;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;

public class WidgetResizeFrame extends FrameLayout {

    private boolean resizeMode = false;

    public WidgetResizeFrame(@NonNull Context context) {
        super(context);
    }

    public void setResizeMode(boolean resizeMode) {
        if (this.resizeMode != resizeMode) {
            this.resizeMode = resizeMode;
            invalidate();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return resizeMode;
    }
}
