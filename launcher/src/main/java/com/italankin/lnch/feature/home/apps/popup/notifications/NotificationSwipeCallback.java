package com.italankin.lnch.feature.home.apps.popup.notifications;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class NotificationSwipeCallback extends ItemTouchHelper.Callback {

    private static final int MOVABLE_FLAGS = makeMovementFlags(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
    private static final int NON_MOVABLE_FLAGS = makeMovementFlags(0, 0);

    private final Listener listener;

    public NotificationSwipeCallback(Listener listener) {
        this.listener = listener;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        if (!(viewHolder instanceof AppNotificationUiAdapter.ViewHolder)) {
            return NON_MOVABLE_FLAGS;
        }
        AppNotificationUi item = ((AppNotificationUiAdapter.ViewHolder) viewHolder).item;
        if (item.sbn.isOngoing()) {
            return NON_MOVABLE_FLAGS;
        }
        return MOVABLE_FLAGS;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
            @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        AppNotificationUi item = ((AppNotificationUiAdapter.ViewHolder) viewHolder).item;
        listener.onNotificationSwiped(item);
    }

    public interface Listener {
        void onNotificationSwiped(AppNotificationUi item);
    }
}
