package com.italankin.lnch.feature.home.behavior;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.italankin.lnch.feature.home.widget.EditModePanel;
import com.italankin.lnch.feature.home.widget.HomeRecyclerView;
import com.italankin.lnch.util.ViewUtils;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

@SuppressWarnings("unused")
@Keep
public class HomeListBehavior extends CoordinatorLayout.Behavior<HomeRecyclerView> {
    public HomeListBehavior() {
    }

    public HomeListBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull HomeRecyclerView child, @NonNull View dependency) {
        return dependency instanceof EditModePanel;
    }

    @Override
    public boolean onDependentViewChanged(@NonNull CoordinatorLayout parent, @NonNull HomeRecyclerView child, @NonNull View dependency) {
        int offset = dependency.getHeight() - dependency.getPaddingBottom() - (int) dependency.getTranslationY();
        ViewUtils.setPaddingBottom(child, Math.max(0, offset));
        return false;
    }
}
