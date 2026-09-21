package com.italankin.lnch.feature.home.adapter;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.home.model.Appearance;
import com.italankin.lnch.feature.home.model.UserPrefs;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.ui.impl.DividerDescriptorUi;

public class DividerDescriptorUiAdapter
        extends HomeAdapterDelegate<DividerDescriptorUiAdapter.ViewHolder, DividerDescriptorUi> {

    private final Listener listener;

    public DividerDescriptorUiAdapter(Listener listener) {
        super(new Params(false, false,
                prefs -> Preferences.ItemWidth.MATCH_PARENT,
                prefs -> Preferences.HomeAlignment.CENTER));
        this.listener = listener;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.item_divider;
    }

    @Override
    protected void update(ViewHolder holder, TextView label, UserPrefs.ItemPrefs itemPrefs, Appearance appearance) {
        super.update(holder, label, itemPrefs, appearance);
        label.setPadding(label.getPaddingLeft(), label.getPaddingTop() / 2,
                label.getPaddingRight(), label.getPaddingBottom() / 2);
    }

    @NonNull
    @Override
    protected ViewHolder createViewHolder(View itemView) {
        ViewHolder holder = new ViewHolder(itemView, listener);
        holder.label.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                listener.onDividerClick(pos, getItem(pos));
            }
        });
        holder.label.setOnLongClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                listener.onDividerLongClick(pos, getItem(pos));
            }
            return true;
        });
        return holder;
    }

    @Override
    public boolean isType(int position, Object item, boolean ignoreVisibility) {
        return item instanceof DividerDescriptorUi;
    }

    public interface Listener {
        boolean isDividerEditingEnabled();

        void onDividerClick(int position, DividerDescriptorUi item);

        void onDividerLongClick(int position, DividerDescriptorUi item);
    }

    static class ViewHolder extends HomeAdapterDelegate.ViewHolder<DividerDescriptorUi> {
        final TextView label;
        private final Listener listener;

        ViewHolder(View itemView, Listener listener) {
            super(itemView);
            this.listener = listener;
            label = itemView.findViewById(R.id.itemLabel);
        }

        @Override
        protected void bind(DividerDescriptorUi item) {
            boolean editing = listener.isDividerEditingEnabled();
            label.setClickable(editing);
            label.setLongClickable(editing);
            label.setBackgroundResource(editing ? R.drawable.selector_item : 0);
            if (!editing) {
                label.setPressed(false);
            }
            label.setText(item.getVisibleLabel());
            label.setTextColor(item.getVisibleColor());
            label.setContentDescription(item.getVisibleLabel().isEmpty()
                    ? label.getContext().getString(R.string.edit_divider_label)
                    : item.getVisibleLabel());
        }

        @Nullable
        @Override
        protected TextView getLabel() {
            return label;
        }
    }
}
