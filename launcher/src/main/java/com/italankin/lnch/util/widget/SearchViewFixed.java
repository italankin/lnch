package com.italankin.lnch.util.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;

public class SearchViewFixed extends SearchView {

    public SearchViewFixed(@NonNull Context context) {
        super(context);
    }

    public SearchViewFixed(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SearchViewFixed(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        View searchEditFrame = findViewById(androidx.appcompat.R.id.search_edit_frame);
        if (searchEditFrame != null) {
            ViewGroup.LayoutParams layoutParams = searchEditFrame.getLayoutParams();
            if (layoutParams instanceof MarginLayoutParams) {
                ((MarginLayoutParams) layoutParams).setMarginStart(0);
            }
        }
    }
}
