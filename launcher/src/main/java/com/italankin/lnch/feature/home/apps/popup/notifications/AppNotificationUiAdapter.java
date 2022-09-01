package com.italankin.lnch.feature.home.apps.popup.notifications;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.italankin.lnch.R;
import me.italankin.adapterdelegates.BaseAdapterDelegate;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AppNotificationUiAdapter extends BaseAdapterDelegate<AppNotificationUiAdapter.ViewHolder, AppNotificationUi> {

    private static final int MASK_SBN = 0xf2100000;

    private final Listener listener;

    public AppNotificationUiAdapter(Listener listener) {
        this.listener = listener;
    }

    @Override
    public boolean isType(int position, Object item) {
        return item instanceof AppNotificationUi;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.item_notification;
    }

    @NonNull
    @Override
    protected ViewHolder createViewHolder(View itemView) {
        ViewHolder holder = new ViewHolder(itemView);
        holder.itemView.setOnClickListener(v -> {
            if (holder.getBindingAdapterPosition() != RecyclerView.NO_POSITION) {
                listener.onNotificationClick(holder.item);
            }
        });
        return holder;
    }

    @Override
    public long getItemId(int position, AppNotificationUi item) {
        return MASK_SBN | item.sbn.getId();
    }

    @Override
    public void onBind(ViewHolder holder, int position, AppNotificationUi item) {
        holder.bind(item);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public AppNotificationUi item;
        final ImageView icon;
        final TextView title;
        final TextView content;
        final TextView count;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            title = itemView.findViewById(R.id.title);
            content = itemView.findViewById(R.id.content);
            count = itemView.findViewById(R.id.count);
        }

        void bind(AppNotificationUi item) {
            this.item = item;
            icon.setImageDrawable(item.icon);
            if (item.title != null) {
                title.setText(item.title);
                title.setVisibility(View.VISIBLE);
            } else {
                title.setVisibility(View.GONE);
            }
            content.setText(item.text);
            if (item.summaryCount == 0) {
                count.setVisibility(View.GONE);
            } else {
                count.setText(String.valueOf(item.summaryCount));
                count.setVisibility(View.VISIBLE);
            }
        }
    }

    public interface Listener {
        void onNotificationClick(AppNotificationUi item);
    }
}
