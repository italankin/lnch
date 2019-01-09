package com.italankin.lnch.feature.home.behavior;

import android.content.Context;
import android.support.annotation.Keep;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

import com.italankin.lnch.feature.home.widget.EditModePanel;
import com.italankin.lnch.feature.home.widget.HomeRecyclerView;

@SuppressWarnings("unused")
@Keep
public class HomeListBehavior extends CoordinatorLayout.Behavior<HomeRecyclerView> {
    public HomeListBehavior() {
    }

    public HomeListBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, HomeRecyclerView child, View dependency) {
        return dependency instanceof EditModePanel;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, HomeRecyclerView child, View dependency) {
        int offset = dependency.getHeight() - dependency.getPaddingBottom() - (int) dependency.getTranslationY();
        child.setPadding(child.getPaddingLeft(), child.getPaddingTop(), child.getPaddingRight(), Math.max(0, offset));
        return false;
    }
}
