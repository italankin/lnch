package com.italankin.lnch.feature.home.util;

import android.content.Context;
import android.support.annotation.Keep;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import com.italankin.lnch.feature.home.widget.EditModePanel;

@SuppressWarnings("unused")
@Keep
public class AppsListBehavior extends CoordinatorLayout.Behavior<RecyclerView> {
    public AppsListBehavior() {
        super();
    }

    public AppsListBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, RecyclerView child, View dependency) {
        return dependency instanceof EditModePanel;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, RecyclerView child, View dependency) {
        int height = dependency.getHeight();
        child.setPadding(child.getPaddingLeft(), child.getPaddingTop(), child.getPaddingRight(),
                (int) (height - dependency.getTranslationY()));
        return false;
    }
}
