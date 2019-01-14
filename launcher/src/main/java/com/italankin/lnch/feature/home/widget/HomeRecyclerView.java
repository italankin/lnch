package com.italankin.lnch.feature.home.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class HomeRecyclerView extends RecyclerView {
    private int bottomInset;
    private int selfPaddingBottom;

    public HomeRecyclerView(Context context) {
        super(context);
    }

    public HomeRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public HomeRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setBottomInset(int value) {
        bottomInset = value;
        super.setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), bottomInset + selfPaddingBottom);
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        selfPaddingBottom = bottom;
        super.setPadding(left, top, right, bottomInset + bottom);
    }
}
