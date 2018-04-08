package com.italankin.lnch.ui.feature.home;

import android.content.Context;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

public class TopBarBehavior extends CoordinatorLayout.Behavior<View> {
    private static final int ANIM_DURATION = 200;

    private int maxOffset;
    private boolean dragInProgress = false;
    private boolean shown = false;

    @Keep
    public TopBarBehavior() {
    }

    @Keep
    public TopBarBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, View child, int layoutDirection) {
        setupInitialState(child);
        return false;
    }

    private void setupInitialState(View child) {
        maxOffset = child.getHeight();
        child.setTranslationY(-maxOffset);
        child.setAlpha(0);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency instanceof RecyclerView;
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
        if (!dragInProgress && shown) {
            dragInProgress = dy > 0;
        }
        if (dragInProgress) {
            onDrag(child, target, dy);
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
            jumpToActualState(child, target);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Fling
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onNestedPreFling(@NonNull CoordinatorLayout coordinatorLayout,
            @NonNull View child, @NonNull View target, float velocityX, float velocityY) {
        if (velocityY > 0 && shown) {
            hide(child, target);
        }
        return false;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Magic
    ///////////////////////////////////////////////////////////////////////////

    private void onDrag(View child, View target, int dy) {
        float cty = child.getTranslationY() - dy;
        if (cty < -maxOffset) {
            cty = -maxOffset;
        } else if (cty > 0) {
            cty = 0;
        }
        child.setTranslationY(cty);
        child.setAlpha(1 - Math.abs(cty) / maxOffset);
        float tty = target.getTranslationY() - dy;
        if (tty < 0) {
            tty = 0;
        } else if (tty > maxOffset) {
            tty = maxOffset;
        }
        target.setTranslationY(tty);
    }

    private void jumpToActualState(View child, View target) {
        float abs = Math.abs(child.getTranslationY());
        if (shown) {
            if (abs < maxOffset * .25f) {
                show(child, target);
            } else {
                hide(child, target);
            }
        } else {
            if (abs < maxOffset * .75f) {
                show(child, target);
            } else {
                hide(child, target);
            }
        }
    }

    private void show(View child, View target) {
        child.animate()
                .translationY(0)
                .setDuration(ANIM_DURATION)
                .alpha(1)
                .start();
        target.animate()
                .translationY(maxOffset)
                .setDuration(ANIM_DURATION)
                .start();
        dragInProgress = false;
        shown = true;
    }

    private void hide(View child, View target) {
        child.animate()
                .translationY(-maxOffset)
                .setDuration(ANIM_DURATION)
                .alpha(0)
                .start();
        target.animate()
                .translationY(0)
                .setDuration(ANIM_DURATION)
                .start();
        dragInProgress = false;
        shown = false;
    }
}
