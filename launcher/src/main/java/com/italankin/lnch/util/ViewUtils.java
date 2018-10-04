package com.italankin.lnch.util;

import android.view.View;
import android.view.ViewTreeObserver;

public final class ViewUtils {

    public static void onGlobalLayout(View view, ViewTreeObserver.OnGlobalLayoutListener listener) {
        view.getViewTreeObserver().addOnGlobalLayoutListener(new OneTimeListener(view, listener));
    }

    private static class OneTimeListener implements ViewTreeObserver.OnGlobalLayoutListener {
        private final View view;
        private final ViewTreeObserver.OnGlobalLayoutListener delegate;

        public OneTimeListener(View view, ViewTreeObserver.OnGlobalLayoutListener delegate) {
            this.view = view;
            this.delegate = delegate;
        }

        @Override
        public void onGlobalLayout() {
            view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            delegate.onGlobalLayout();
        }
    }

    private ViewUtils() {
        // no instance
    }
}
