package com.italankin.lnch.feature.home.adapter;

import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.home.util.NotificationBadgeDrawable;
import com.italankin.lnch.model.viewmodel.impl.AppViewModel;
import com.italankin.lnch.util.ResUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class AppViewModelAdapter
        extends HomeAdapterDelegate<AppViewModelAdapter.ViewHolder, AppViewModel> {

    private final Listener listener;

    public AppViewModelAdapter(Listener listener) {
        this.listener = listener;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.item_app;
    }

    @NonNull
    @Override
    protected ViewHolder createViewHolder(View itemView) {
        ViewHolder holder = new ViewHolder(itemView);
        itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                listener.onAppClick(pos, getItem(pos));
            }
        });
        itemView.setOnLongClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                listener.onAppLongClick(pos, getItem(pos));
                return true;
            }
            return false;
        });
        return holder;
    }

    @Override
    public boolean isType(int position, Object item) {
        return item instanceof AppViewModel && ((AppViewModel) item).isVisible();
    }

    public interface Listener {
        void onAppClick(int position, AppViewModel item);

        void onAppLongClick(int position, AppViewModel item);
    }

    static class ViewHolder extends HomeAdapterDelegate.ViewHolder<AppViewModel> {
        final TextView label;
        final NotificationBadgeDrawable notificationBadge;

        ViewHolder(View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.label);

            int size = itemView.getResources().getDimensionPixelSize(R.dimen.notification_badge_size);
            notificationBadge = new NotificationBadgeDrawable(size,
                    ContextCompat.getColor(itemView.getContext(), R.color.notification_badge),
                    ResUtils.resolveColor(label.getContext(), R.attr.colorItemShadowDefault));
            label.setForegroundGravity(Gravity.END | Gravity.TOP);
            label.setForeground(notificationBadge);
        }

        @Override
        void bind(AppViewModel item) {
            label.setText(item.getVisibleLabel());
            label.setTextColor(item.getVisibleColor());
            notificationBadge.setMargin(itemPrefs.itemPadding * 2);
            notificationBadge.setVisible(item.isBadgeVisible());
        }

        @Nullable
        @Override
        TextView getLabel() {
            return label;
        }
    }
}
