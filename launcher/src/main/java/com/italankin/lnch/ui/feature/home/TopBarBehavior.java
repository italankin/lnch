package com.italankin.lnch.ui.feature.home;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.view.View;

public class TopBarBehavior extends CoordinatorLayout.Behavior<View> {

    private static final int ANIM_DURATION = 200;
    private static final float DRAG_RESISTANCE = 0.55f;

    private View topView;
    private View bottomView;
    private int maxOffset = -1;

    private boolean dragInProgress = false;
    private boolean shown = false;
    private boolean enabled = true;

    private final Listener listener;

    public TopBarBehavior(View topView, View bottomView, Listener listener) {
        this.topView = topView;
        this.bottomView = bottomView;
        this.listener = listener;
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, View child, int layoutDirection) {
        if (maxOffset <= 0) {
            setupInitialState();
        }
        return false;
    }

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
        if (dragInProgress) {
            onDrag(dy);
            consumed[1] = dy;
        }
    }

    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
            @NonNull View child, @NonNull View target,
            int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        if (!dragInProgress) {
            dragInProgress = dyUnconsumed < 0;
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
        if (velocityY > 0 && shown) {
            hide();
        }
        return false;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Magic
    ///////////////////////////////////////////////////////////////////////////

    private void onDrag(int dy) {
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
            if (abs < maxOffset * .25f) {
                show();
            } else {
                hide();
            }
        } else {
            if (abs < maxOffset * .50f) {
                show();
            } else {
                hide();
            }
        }
    }

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

    public boolean isShown() {
        return shown;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Listener
    ///////////////////////////////////////////////////////////////////////////

    public interface Listener {
        void onShow();

        void onHide();
    }
}
