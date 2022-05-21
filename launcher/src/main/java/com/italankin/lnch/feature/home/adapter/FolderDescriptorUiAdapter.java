package com.italankin.lnch.feature.home.adapter;

import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.home.util.NotificationDotDrawable;
import com.italankin.lnch.model.ui.impl.FolderDescriptorUi;
import com.italankin.lnch.util.ResUtils;

import java.util.List;

public class FolderDescriptorUiAdapter
        extends HomeAdapterDelegate<FolderDescriptorUiAdapter.ViewHolder, FolderDescriptorUi> {

    private final Listener listener;

    public FolderDescriptorUiAdapter(Listener listener) {
        super(false);
        this.listener = listener;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.item_folder;
    }

    @NonNull
    @Override
    protected ViewHolder createViewHolder(View itemView) {
        ViewHolder holder = new ViewHolder(itemView);
        itemView.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                listener.onFolderClick(pos, getItem(pos));
            }
        });
        itemView.setOnLongClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                listener.onFolderLongClick(pos, getItem(pos));
            }
            return true;
        });
        return holder;
    }

    @Override
    public boolean isType(int position, Object item, boolean ignoreVisibility) {
        return item instanceof FolderDescriptorUi;
    }

    public interface Listener {
        void onFolderClick(int position, FolderDescriptorUi item);

        void onFolderLongClick(int position, FolderDescriptorUi item);
    }

    static class ViewHolder extends HomeAdapterDelegate.ViewHolder<FolderDescriptorUi> {
        final TextView label;
        final NotificationDotDrawable notificationDot;

        ViewHolder(View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.label);
            notificationDot = new NotificationDotDrawable(
                    itemView.getResources().getDimensionPixelSize(R.dimen.notification_dot_size),
                    ContextCompat.getColor(itemView.getContext(), R.color.notification_dot),
                    ResUtils.resolveColor(label.getContext(), R.attr.colorItemShadowDefault));
            label.setForegroundGravity(Gravity.END | Gravity.TOP);
            label.setForeground(notificationDot);
        }

        @Override
        protected void bind(FolderDescriptorUi item) {
            bindItem(item);
            notificationDot.setBadgeVisible(item.isBadgeVisible(), false);
        }

        @Override
        protected void bind(FolderDescriptorUi item, List<Object> payloads) {
            bindItem(item);
            notificationDot.setBadgeVisible(item.isBadgeVisible(), payloads.contains(FolderDescriptorUi.PAYLOAD_BADGE));
        }

        private void bindItem(FolderDescriptorUi item) {
            label.setText(item.getVisibleLabel());
            label.setTextColor(item.getVisibleColor());
            notificationDot.setMargin(itemPrefs.itemPadding * 2);
        }

        @Nullable
        @Override
        protected TextView getLabel() {
            return label;
        }
    }
}
