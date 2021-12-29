package com.italankin.lnch.feature.home.search;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Px;
import androidx.recyclerview.widget.RecyclerView;

class MarginItemDecoration extends RecyclerView.ItemDecoration {
    private final int margin;

    MarginItemDecoration(@Px int margin) {
        this.margin = margin;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent,
            @NonNull RecyclerView.State state) {
        RecyclerView.ViewHolder viewHolder = parent.findContainingViewHolder(view);
        if (viewHolder == null) {
            outRect.setEmpty();
            return;
        }
        int position = viewHolder.getBindingAdapterPosition();
        if (position == RecyclerView.NO_POSITION) {
            outRect.setEmpty();
            return;
        }
        outRect.set(margin, margin, margin, 0);
        if (position == state.getItemCount() - 1) {
            outRect.bottom = margin;
        }
    }
}
