package com.italankin.lnch.util;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.DimenRes;
import androidx.annotation.Dimension;
import androidx.annotation.Nullable;
import androidx.annotation.Px;

public final class ViewUtils {

    public static void setPadding(View view, @Px int value) {
        view.setPaddingRelative(value, value, value, value);
    }

    public static void setPaddingDp(View view, @Dimension(unit = Dimension.DP) int value) {
        int pxValue = ResUtils.px2dp(view.getContext(), value);
        view.setPaddingRelative(pxValue, pxValue, pxValue, pxValue);
    }

    public static void setPaddingDimen(View view, @DimenRes int res) {
        int value = view.getResources().getDimensionPixelSize(res);
        setPadding(view, value);
    }

    public static void setPaddingStart(View view, @Px int value) {
        view.setPaddingRelative(value, view.getPaddingTop(), view.getPaddingEnd(), view.getPaddingBottom());
    }

    public static void setPaddingStartDimen(View view, @DimenRes int res) {
        int value = view.getResources().getDimensionPixelSize(res);
        setPaddingStart(view, value);
    }

    public static void setPaddingEnd(View view, @Px int value) {
        view.setPaddingRelative(view.getPaddingStart(), view.getPaddingTop(), value, view.getPaddingBottom());
    }

    public static void setPaddingTop(View view, @Px int value) {
        view.setPaddingRelative(view.getPaddingStart(), value, view.getPaddingEnd(), view.getPaddingBottom());
    }

    public static void setPaddingBottom(View view, @Px int value) {
        view.setPaddingRelative(view.getPaddingStart(), view.getPaddingTop(), view.getPaddingEnd(), value);
    }

    public static void onGlobalLayout(View view, ViewTreeObserver.OnGlobalLayoutListener listener) {
        view.getViewTreeObserver().addOnGlobalLayoutListener(new OneTimeListener(view, listener));
    }

    public static Rect getViewBounds(@Nullable View view) {
        if (view == null) {
            return null;
        }
        int[] pos = new int[2];
        view.getLocationOnScreen(pos);
        return new Rect(pos[0], pos[1], pos[0] + view.getWidth(), pos[1] + view.getHeight());
    }

    public static Rect getViewBoundsInsetPadding(@Nullable View view) {
        if (view == null) {
            return null;
        }
        int[] pos = new int[2];
        view.getLocationOnScreen(pos);
        Rect rect = new Rect(pos[0], pos[1], pos[0] + view.getWidth(), pos[1] + view.getHeight());
        rect.inset(Math.max(view.getPaddingLeft(), view.getPaddingRight()),
                Math.max(view.getPaddingTop(), view.getPaddingBottom()));
        return rect;
    }

    public static void setTemporaryPressedState(View view, int duration) {
        view.setPressed(true);
        view.postDelayed(() -> view.setPressed(false), duration);
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
