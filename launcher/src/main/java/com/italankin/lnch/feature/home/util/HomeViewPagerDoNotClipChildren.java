package com.italankin.lnch.feature.home.util;

import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.italankin.lnch.R;

/**
 * {@link androidx.viewpager2.widget.ViewPager2} adds a bunch of view parents on top of it's content, which messes our
 * fancy home behaviour.
 */
public class HomeViewPagerDoNotClipChildren {

    public static void apply(@NonNull View view) {
        if (view.isAttachedToWindow()) {
            HomeViewPagerDoNotClipChildren.doNotClipUpToRoot(view);
            return;
        }
        view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(@NonNull View v) {
                v.removeOnAttachStateChangeListener(this);
                HomeViewPagerDoNotClipChildren.doNotClipUpToRoot(v);
            }

            @Override
            public void onViewDetachedFromWindow(@NonNull View v) {
            }
        });
    }

    private static void doNotClipUpToRoot(@Nullable View view) {
        if (view == null || view.getId() == R.id.home_pager) {
            return;
        }
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            viewGroup.setClipChildren(false);
            viewGroup.setClipToPadding(false);
            doNotClipUpToRoot((View) viewGroup.getParent());
        }
    }

    private HomeViewPagerDoNotClipChildren() {

    }
}
