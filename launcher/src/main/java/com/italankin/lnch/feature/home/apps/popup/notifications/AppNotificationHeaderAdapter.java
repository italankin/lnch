package com.italankin.lnch.feature.home.apps.popup.notifications;

import android.view.View;
import android.widget.TextView;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.home.apps.popup.notifications.item.AppNotificationHeader;
import com.italankin.lnch.util.adapterdelegate.BaseAdapterDelegate;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AppNotificationHeaderAdapter extends
        BaseAdapterDelegate<AppNotificationHeaderAdapter.ViewHolder, AppNotificationHeader> {

    @Override
    public boolean isType(int position, Object item) {
        return item instanceof AppNotificationHeader;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.item_notification_header;
    }

    @NonNull
    @Override
    protected ViewHolder createViewHolder(View itemView) {
        return new ViewHolder(itemView);
    }

    @Override
    public long getItemId(int position, AppNotificationHeader item) {
        return 0;
    }

    @Override
    public void onBind(ViewHolder holder, int position, AppNotificationHeader item) {
        if (item.count != 0) {
            holder.count.setText(String.valueOf(item.count));
        } else {
            holder.count.setText(null);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView count;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            count = itemView.findViewById(R.id.count);
        }
    }
}
