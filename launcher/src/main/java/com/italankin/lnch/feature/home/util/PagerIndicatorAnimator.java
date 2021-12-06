package com.italankin.lnch.feature.home.util;

import android.animation.ObjectAnimator;
import android.view.View;

import androidx.viewpager.widget.ViewPager;
import ru.tinkoff.scrollingpagerindicator.ScrollingPagerIndicator;

public class PagerIndicatorAnimator extends ViewPager.SimpleOnPageChangeListener {

    private final ScrollingPagerIndicator pagerIndicator;
    private ObjectAnimator inAnimator;
    private ObjectAnimator outAnimator;

    public PagerIndicatorAnimator(ScrollingPagerIndicator pagerIndicator) {
        this.pagerIndicator = pagerIndicator;
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (pagerIndicator.getVisibility() != View.VISIBLE) {
            return;
        }
        if (state == ViewPager.SCROLL_STATE_IDLE) {
            if (inAnimator != null) {
                inAnimator.cancel();
                inAnimator = null;
            }
            if (pagerIndicator.getAlpha() == 0) {
                return;
            }
            if (outAnimator == null || !outAnimator.isRunning()) {
                outAnimator = ObjectAnimator.ofFloat(pagerIndicator, "alpha", 0);
                outAnimator.setStartDelay(750);
                outAnimator.setDuration(250);
                outAnimator.start();
            }
        } else {
            if (outAnimator != null) {
                outAnimator.cancel();
                outAnimator = null;
            }
            if (pagerIndicator.getAlpha() == 1) {
                return;
            }
            if (inAnimator == null || !inAnimator.isRunning()) {
                inAnimator = ObjectAnimator.ofFloat(pagerIndicator, "alpha", 1);
                inAnimator.setDuration(250);
                inAnimator.start();
            }
        }
    }
}
