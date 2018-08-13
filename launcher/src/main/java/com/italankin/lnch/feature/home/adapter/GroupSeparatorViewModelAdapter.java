package com.italankin.lnch.feature.home.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.home.model.GroupSeparatorViewModel;
import com.italankin.lnch.util.adapterdelegate.BaseAdapterDelegate;

public class GroupSeparatorViewModelAdapter extends BaseAdapterDelegate<GroupSeparatorViewModelHolder, GroupSeparatorViewModel> {
    private final Listener listener;

    public GroupSeparatorViewModelAdapter(Listener listener) {
        this.listener = listener;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.item_separator;
    }

    @NonNull
    @Override
    protected GroupSeparatorViewModelHolder createViewHolder(View itemView) {
        GroupSeparatorViewModelHolder holder = new GroupSeparatorViewModelHolder(itemView);
        if (listener != null) {
            itemView.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onSeparatorClick(pos, getItem(pos));
                }
            });
            itemView.setOnLongClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onSeparatorLongClick(pos, getItem(pos));
                }
                return true;
            });
        }
        return holder;
    }

    @Override
    public void onBind(GroupSeparatorViewModelHolder holder, int position, GroupSeparatorViewModel item) {
        holder.bind(item);
    }

    @Override
    public long getItemId(int position, GroupSeparatorViewModel item) {
        return item.hashCode();
    }

    @Override
    public boolean isType(int position, Object item) {
        return item instanceof GroupSeparatorViewModel;
    }

    public interface Listener {
        void onSeparatorClick(int position, GroupSeparatorViewModel item);

        void onSeparatorLongClick(int position, GroupSeparatorViewModel item);
    }
}


class GroupSeparatorViewModelHolder extends RecyclerView.ViewHolder {
    final TextView label;

    GroupSeparatorViewModelHolder(View itemView) {
        super(itemView);
        label = itemView.findViewById(R.id.label);
    }

    void bind(GroupSeparatorViewModel item) {
        label.setText(item.label);
        label.setTextColor(item.color);
    }
}