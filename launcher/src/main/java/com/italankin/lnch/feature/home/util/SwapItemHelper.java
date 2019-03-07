package com.italankin.lnch.feature.home.util;

import android.graphics.Canvas;
import android.graphics.Color;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import timber.log.Timber;

public class SwapItemHelper extends ItemTouchHelper.Callback {

    private static final int DRAG_FLAGS = ItemTouchHelper.UP | ItemTouchHelper.DOWN |
            ItemTouchHelper.START | ItemTouchHelper.END;

    private final Callback callback;

    private RecyclerView.ViewHolder target;
    private int dropDistance = 100;

    public SwapItemHelper(Callback callback) {
        this.callback = callback;
    }

    @Override
    public boolean canDropOver(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder current, @NonNull RecyclerView.ViewHolder target) {
        if (calcDist(current, target) <= dropDistance) {
            setNewTarget(target);
        } else {
            unsetTarget(target);
        }
        return true;
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        if (target == null) {
            return;
        }
        if (calcDist(viewHolder, target) > dropDistance) {
            unsetTarget(target);
        }
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
            @NonNull RecyclerView.ViewHolder target) {
        unsetTarget(target);
        callback.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public int getBoundingBoxMargin() {
        return -120;
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        unsetTarget(viewHolder);
    }

    @Override
    public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);
        if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
            if (target != null) {
                Timber.e("DROP TARGET = %s", getLabel(target));
            }
            setNewTarget(null);
        }
    }

    private void setNewTarget(RecyclerView.ViewHolder newTarget) {
        if (target != null) {
            target.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
        target = newTarget;
        if (newTarget != null) {
            newTarget.itemView.setBackgroundColor(Color.RED);
        }
    }

    private void unsetTarget(RecyclerView.ViewHolder target) {
        if (this.target == target) {
            setNewTarget(null);
        }
    }

    private double calcDist(@NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        float x1 = viewHolder.itemView.getLeft() + viewHolder.itemView.getWidth() / 2f + viewHolder.itemView.getTranslationX();
        float x2 = target.itemView.getLeft() + target.itemView.getWidth() / 2f + target.itemView.getTranslationX();
        float dx = x2 - x1;
        float y1 = viewHolder.itemView.getTop() + viewHolder.itemView.getHeight() / 2f + viewHolder.itemView.getTranslationY();
        float y2 = target.itemView.getTop() + target.itemView.getHeight() / 2f + target.itemView.getTranslationY();
        float dy = y1 - y2;
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(DRAG_FLAGS, 0);
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
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
    }

    public interface Callback {
        void onItemMove(int from, int to);
    }

    private String getLabel(RecyclerView.ViewHolder vh) {
        if (vh.itemView instanceof TextView) {
            return ((TextView) vh.itemView).getText().toString();
        }
        return "hz";
    }
}
