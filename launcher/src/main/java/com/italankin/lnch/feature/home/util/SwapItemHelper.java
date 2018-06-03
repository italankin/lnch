package com.italankin.lnch.feature.home.util;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

public class SwapItemHelper extends ItemTouchHelper.Callback {
    private final Callback callback;

    public SwapItemHelper(Callback callback) {
        this.callback = callback;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
            RecyclerView.ViewHolder target) {
        int pos = target.getAdapterPosition();
        if (pos > 0 && pos < recyclerView.getAdapter().getItemCount()) {
            callback.onItemMove(viewHolder.getAdapterPosition(), pos);
            return true;
        }
        return false;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN |
                ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(dragFlags, 0);
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
    }

    public interface Callback {
        void onItemMove(int from, int to);
    }
}
