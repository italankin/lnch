package com.italankin.lnch.feature.home.adapter;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.home.util.NotificationDotDrawable;
import com.italankin.lnch.model.ui.impl.AppDescriptorUi;
import com.italankin.lnch.util.ResUtils;

import java.util.List;

public class AppDescriptorUiAdapter extends HomeAdapterDelegate<AppDescriptorUiAdapter.ViewHolder, AppDescriptorUi> {

    private final Listener listener;

    public AppDescriptorUiAdapter(Listener listener) {
        this(listener, Params.DEFAULT);
    }

    public AppDescriptorUiAdapter(Listener listener, Params params) {
        super(params);
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
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                listener.onAppClick(pos, getItem(pos));
            }
        });
        itemView.setOnLongClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                listener.onAppLongClick(pos, getItem(pos));
                return true;
            }
            return false;
        });
        return holder;
    }

    @Override
    protected boolean isType(int position, Object item, boolean ignoreVisibility) {
        return item instanceof AppDescriptorUi &&
                (ignoreVisibility || !((AppDescriptorUi) item).isIgnored());
    }

    @Override
    public boolean onFailedToRecycle(ViewHolder holder) {
        holder.notificationDot.cancelAnimation();
        return true;
    }

    public interface Listener {
        void onAppClick(int position, AppDescriptorUi item);

        void onAppLongClick(int position, AppDescriptorUi item);
    }

    static class ViewHolder extends HomeAdapterDelegate.ViewHolder<AppDescriptorUi> {
        final FrameLayout root;
        final TextView label;
        final NotificationDotDrawable notificationDot;

        ViewHolder(View itemView) {
            super(itemView);
            root = (FrameLayout) itemView;
            label = itemView.findViewById(R.id.label);

            notificationDot = new NotificationDotDrawable(
                    itemView.getContext(),
                    ResUtils.resolveColor(itemView.getContext(), R.attr.colorItemShadowDefault));
            root.setForeground(notificationDot);
        }

        @Override
        protected void bind(AppDescriptorUi item) {
            bindItem(item);
            notificationDot.setBadgeVisible(item.isBadgeVisible(), false);
        }

        @Override
        protected void bind(AppDescriptorUi item, List<?> payloads) {
            bindItem(item);
            notificationDot.setBadgeVisible(item.isBadgeVisible(), payloads.contains(AppDescriptorUi.PAYLOAD_BADGE));
        }

        @Override
        protected View getRoot() {
            return root;
        }

        @Nullable
        @Override
        protected TextView getLabel() {
            return label;
        }

        @Nullable
        @Override
        protected NotificationDotDrawable getNotificationDot() {
            return notificationDot;
        }

        private void bindItem(AppDescriptorUi item) {
            label.setText(item.getVisibleLabel());
            label.setTextColor(item.getVisibleColor());
            Integer badgeColor = item.getCustomBadgeColor();
            notificationDot.setColor(badgeColor != null ? badgeColor : itemPrefs.notificationDotColor);
            notificationDot.setMargin(itemPrefs.itemPadding * 2);
        }
    }
}
