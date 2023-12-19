package com.italankin.lnch.feature.home.util;

import android.animation.ValueAnimator;
import android.graphics.drawable.ColorDrawable;
import com.google.android.material.animation.ArgbEvaluatorCompat;

public class AnimatedColorDrawable extends ColorDrawable {

    private static final int DURATION = 500;

    private ValueAnimator colorAnimator;

    @Override
    public void setColor(int color) {
        setColor(color, true);
    }

    public void setColor(int color, boolean animated) {
        int currentColor = getColor();
        if (color == currentColor) {
            return;
        }
        if (!animated) {
            super.setColor(color);
            return;
        }
        if (colorAnimator != null && colorAnimator.isRunning()) {
            colorAnimator.cancel();
        }
        colorAnimator = ValueAnimator.ofInt(currentColor, color);
        colorAnimator.setEvaluator(ArgbEvaluatorCompat.getInstance());
        colorAnimator.addUpdateListener(animation -> {
            int animatedValue = (int) animation.getAnimatedValue();
            super.setColor(animatedValue);
        });
        colorAnimator.setDuration(DURATION);
        colorAnimator.start();
    }
}
