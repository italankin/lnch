package com.italankin.lnch.feature.home.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.view.View;

public class TopBarBehavior extends CoordinatorLayout.Behavior<View> {

    private static final int ANIM_DURATION = 160;
    private static final float DRAG_RESISTANCE = 0.77f;
    private static final float SHOWN_SHOW_THRESHOLD = .25f;
    private static final float HIDDEN_SHOW_THRESHOLD = .6f;

    private final View topView;
    private final View bottomView;
    private final int maxOffset;

    private boolean dragInProgress = false;
    private boolean shown = false;
    private boolean enabled = true;

    private final Listener listener;

    public TopBarBehavior(View topView, View bottomView, int maxOffset, Listener listener) {
        this.topView = topView;
        this.bottomView = bottomView;
        this.maxOffset = maxOffset;
        this.listener = listener;

        setupInitialState();
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency == bottomView;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Scroll
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
            @NonNull View child, @NonNull View directTargetChild, @NonNull View target, int axes, int type) {
        return enabled && type == ViewCompat.TYPE_TOUCH && (axes & ViewCompat.SCROLL_AXIS_VERTICAL) > 0;
    }

    @Override
    public void onNestedPreScroll(@NonNull CoordinatorLayout coordinatorLayout,
            @NonNull View child, @NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
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
            int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
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
        if (dragInProgress && type == ViewCompat.TYPE_TOUCH) {
            jumpToActualState();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Fling
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onNestedPreFling(@NonNull CoordinatorLayout coordinatorLayout,
            @NonNull View child, @NonNull View target, float velocityX, float velocityY) {
        if (shown && velocityY > 0) {
            hide();
            return true;
        }
        return false;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Public
    ///////////////////////////////////////////////////////////////////////////

    public void show() {
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
                        if (listener != null) {
                            listener.onShow();
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
                        if (listener != null) {
                            listener.onHide();
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
        topView.setTranslationY(0);
        topView.setAlpha(1);
        if (listener != null) {
            listener.onShow();
        }
        bottomView.setTranslationY(maxOffset);
    }

    public boolean isShown() {
        return shown;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Private
    ///////////////////////////////////////////////////////////////////////////

    private void setupInitialState() {
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
        int actual = (int) (dy * (1 - DRAG_RESISTANCE));
        float cty = topView.getTranslationY() - actual;
        if (cty < -maxOffset) {
            cty = -maxOffset;
        } else if (cty > 0) {
            cty = 0;
        }
        topView.setTranslationY(cty);
        topView.setAlpha(1 - Math.abs(cty) / maxOffset);
        float tty = bottomView.getTranslationY() - actual;
        if (tty < 0) {
            tty = 0;
        } else if (tty > maxOffset) {
            tty = maxOffset;
        }
        bottomView.setTranslationY(tty);
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

    ///////////////////////////////////////////////////////////////////////////
    // Listener
    ///////////////////////////////////////////////////////////////////////////

    public interface Listener {
        void onShow();

        void onHide();
    }
}
