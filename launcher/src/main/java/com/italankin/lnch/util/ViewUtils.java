package com.italankin.lnch.util;

import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.DimenRes;
import androidx.annotation.Px;

public final class ViewUtils {

    public static void setPadding(View view, @Px int value) {
        view.setPadding(value, value, value, value);
    }

    public static void setPaddingDimen(View view, @DimenRes int res) {
        int value = view.getResources().getDimensionPixelSize(res);
        setPadding(view, value);
    }

    public static void setPaddingLeft(View view, @Px int value) {
        view.setPadding(value, view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
    }

    public static void setPaddingLeftDimen(View view, @DimenRes int res) {
        int value = view.getResources().getDimensionPixelSize(res);
        setPaddingLeft(view, value);
    }

    public static void setPaddingRight(View view, @Px int value) {
        view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), value, view.getPaddingBottom());
    }

    public static void setPaddingTop(View view, @Px int value) {
        view.setPadding(view.getPaddingLeft(), value, view.getPaddingRight(), view.getPaddingBottom());
    }

    public static void setPaddingBottom(View view, @Px int value) {
        view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), value);
    }

    public static void onGlobalLayout(View view, ViewTreeObserver.OnGlobalLayoutListener listener) {
        view.getViewTreeObserver().addOnGlobalLayoutListener(new OneTimeListener(view, listener));
    }

    private static class OneTimeListener implements ViewTreeObserver.OnGlobalLayoutListener {
        private final View view;
        private final ViewTreeObserver.OnGlobalLayoutListener delegate;

        private OneTimeListener(View view, ViewTreeObserver.OnGlobalLayoutListener delegate) {
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
