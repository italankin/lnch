package com.italankin.lnch.feature.home.behavior;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;

public class SearchBarBehavior extends CoordinatorLayout.Behavior<View> {

    private static final int ANIM_DURATION = 160;
    private static final float SHOWN_SHOW_THRESHOLD = .25f;
    private static final float HIDDEN_SHOW_THRESHOLD = .6f;
    private static final float MIN_RESISTANCE_FACTOR = 0.05f;

    private final View topView;
    private final View bottomView;
    private int maxOffset;

    private boolean dragInProgress = false;
    private boolean shown = false;
    private boolean enabled = true;
    private final Listener listener;
    private final Interpolator resistanceInterpolator = new DecelerateInterpolator(0.25f);

    public SearchBarBehavior(View topView, View bottomView, @NonNull Listener listener) {
        this.topView = topView;
        this.bottomView = bottomView;
        this.listener = listener;

        topView.getViewTreeObserver()
                .addOnGlobalLayoutListener(this::setupInitialState);
    }

    @Override
    public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull View child, @NonNull View dependency) {
        return dependency == bottomView;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Scroll
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
            @NonNull View child, @NonNull View directTargetChild, @NonNull View target, int axes, int type) {
        return type == ViewCompat.TYPE_TOUCH && (axes & ViewCompat.SCROLL_AXIS_VERTICAL) > 0;
    }

    @Override
    public void onNestedPreScroll(@NonNull CoordinatorLayout coordinatorLayout,
            @NonNull View child, @NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
        if (!enabled) {
            return;
        }
        if (!dragInProgress && shown) {
            dragInProgress = dy > 0;
        }
        if (dragInProgress && topView.getTranslationY() > -maxOffset) {
            consumed[1] = dy;
            onDrag(dy);
        }
    }

    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
            @NonNull View child, @NonNull View target,
            int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type, @NonNull int[] consumed) {
        if (!enabled) {
            return;
        }
        if (!dragInProgress) {
            dragInProgress = dyUnconsumed < 0;
        }
        if (dragInProgress) {
            onDrag(dyUnconsumed);
        }
    }

    @Override
    public void onStopNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
            @NonNull View child, @NonNull View target, int type) {
        if (enabled && dragInProgress && type == ViewCompat.TYPE_TOUCH) {
            jumpToActualState();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Fling
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onNestedPreFling(@NonNull CoordinatorLayout coordinatorLayout,
            @NonNull View child, @NonNull View target, float velocityX, float velocityY) {
        if (!enabled) {
            return false;
        }
        if (velocityY > 0 && (shown || dragInProgress)) {
            hide();
            return true;
        }
        if (shown && velocityY < 0) {
            listener.onShowExpand();
            return true;
        }
        return false;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Public
    ///////////////////////////////////////////////////////////////////////////

    public void show() {
        show(null);
    }

    public void show(@Nullable Runnable runnable) {
        dragInProgress = false;
        shown = true;
        topView.animate()
                .translationY(0)
                .setDuration(ANIM_DURATION)
                .alpha(1)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        topView.animate().setListener(null);
                        listener.onShow();
                        if (runnable != null) {
                            runnable.run();
                        }
                    }
                })
                .start();
        bottomView.animate()
                .translationY(maxOffset)
                .setDuration(ANIM_DURATION)
                .start();
    }

    public void hide() {
        hide(null);
    }

    public void hide(@Nullable Runnable runnable) {
        dragInProgress = false;
        shown = false;
        topView.animate()
                .translationY(-maxOffset)
                .setDuration(ANIM_DURATION)
                .alpha(0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        topView.animate().setListener(null);
                        listener.onHide();
                        if (runnable != null) {
                            runnable.run();
                        }
                    }
                })
                .start();
        bottomView.animate()
                .translationY(0)
                .setDuration(ANIM_DURATION)
                .start();
    }

    public void showNow() {
        dragInProgress = false;
        shown = true;
        topView.animate().cancel();
        topView.setTranslationY(0);
        topView.setAlpha(1);
        bottomView.animate().cancel();
        bottomView.setTranslationY(maxOffset);
        listener.onShow();
    }

    public boolean isShown() {
        return shown;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Private
    ///////////////////////////////////////////////////////////////////////////

    private void setupInitialState() {
        maxOffset = topView.getHeight();
        if (shown) {
            topView.setTranslationY(0);
            topView.setAlpha(1);
            bottomView.setTranslationY(maxOffset);
        } else {
            topView.setTranslationY(-maxOffset);
            topView.setAlpha(0);
            bottomView.setTranslationY(0);
        }
    }

    private void onDrag(int dy) {
        if (dy == 0) {
            return;
        }
        float currentTopViewTy = topView.getTranslationY();
        float progress = Math.abs(currentTopViewTy) / maxOffset;
        float resistanceFactor = Math.max(resistanceInterpolator.getInterpolation(progress), MIN_RESISTANCE_FACTOR);
        float actualDy = dy * resistanceFactor;

        float topViewTy = box(currentTopViewTy - actualDy, -maxOffset, 0);
        topView.setTranslationY(topViewTy);
        topView.setAlpha(1 - Math.abs(topViewTy) / maxOffset);

        float bottomViewTy = box(bottomView.getTranslationY() - actualDy, 0, maxOffset);
        bottomView.setTranslationY(bottomViewTy);
    }

    private void jumpToActualState() {
        float abs = Math.abs(topView.getTranslationY());
        if (shown) {
            if (abs < maxOffset * SHOWN_SHOW_THRESHOLD) {
                show();
            } else {
                hide();
            }
        } else {
            if (abs < maxOffset * HIDDEN_SHOW_THRESHOLD) {
                show();
            } else {
                hide();
            }
        }
    }

    private static float box(float value, float min, float max) {
        if (value < min) {
            return min;
        } else if (value > max) {
            return max;
        }
        return value;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Listener
    ///////////////////////////////////////////////////////////////////////////

    public interface Listener {

        void onShow();

        void onHide();

        void onShowExpand();
    }
}
