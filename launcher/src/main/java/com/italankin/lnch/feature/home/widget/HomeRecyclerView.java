package com.italankin.lnch.feature.home.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.italankin.lnch.model.repository.prefs.HomeTrim;

public class HomeRecyclerView extends RecyclerView {

    private int bottomInset;
    private int selfPaddingBottom;
    private HomeTrim homeTrim = HomeTrim.NONE;

    public HomeRecyclerView(Context context) {
        super(context);
    }

    public HomeRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public HomeRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        selfPaddingBottom = bottom;
        super.setPadding(left, top, right, bottomInset + bottom);
    }

    @Override
    public void setPaddingRelative(int start, int top, int end, int bottom) {
        selfPaddingBottom = bottom;
        super.setPaddingRelative(start, top, end, bottomInset + bottom);
    }

    public void setBottomInset(int value) {
        bottomInset = value;
        super.setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), bottomInset + selfPaddingBottom);
    }

    public boolean setHomeTrim(HomeTrim trim) {
        if (homeTrim.equals(trim)) {
            return false;
        }
        homeTrim = trim;
        requestLayout();
        return true;
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        int width = MeasureSpec.getSize(widthSpec);
        super.setPadding(Math.round(width * homeTrim.left / 100f), getPaddingTop(),
                Math.round(width * homeTrim.right / 100f), bottomInset + selfPaddingBottom);
        super.onMeasure(widthSpec, heightSpec);
    }

    @Nullable
    public View findViewForAdapterPosition(int position) {
        RecyclerView.ViewHolder viewHolder = findViewHolderForAdapterPosition(position);
        if (viewHolder != null) {
            return viewHolder.itemView;
        }
        return null;
    }
}
