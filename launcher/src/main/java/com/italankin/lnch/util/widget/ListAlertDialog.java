package com.italankin.lnch.util.widget;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.italankin.lnch.R;

import java.util.ArrayList;
import java.util.List;

public final class ListAlertDialog {

    public static Builder builder(Context context) {
        return new Builder(context);
    }

    public static class Builder {
        private final AlertDialog.Builder builder;
        private final List<Item> items = new ArrayList<>();

        public Builder(Context context) {
            builder = new AlertDialog.Builder(context);
        }

        public Builder setTitle(CharSequence title) {
            builder.setTitle(title);
            return this;
        }

        public Builder setTitle(@StringRes int title) {
            builder.setTitle(title);
            return this;
        }

        public Builder addItem(@DrawableRes int icon, @StringRes int title, OnClickListener listener) {
            items.add(new Item(icon, title, listener));
            return this;
        }

        public AlertDialog build() {
            builder.setAdapter(new Adapter(builder.getContext(), items), (dialog, which) -> {
                Item item = items.get(which);
                if (item.listener != null) {
                    item.listener.onItemClick();
                }
            });
            return builder.create();
        }

        public AlertDialog show() {
            AlertDialog dialog = build();
            dialog.show();
            return dialog;
        }
    }

    public interface OnClickListener {
        void onItemClick();
    }

    private static class Item {
        @DrawableRes
        private final int icon;
        @StringRes
        private final int title;
        private final OnClickListener listener;

        Item(@DrawableRes int icon, @StringRes int title, OnClickListener listener) {
            this.icon = icon;
            this.title = title;
            this.listener = listener;
        }
    }

    private static class Adapter extends BaseAdapter {
        private final LayoutInflater inflater;
        private final List<Item> dataset;

        Adapter(Context context, List<Item> dataset) {
            this.inflater = LayoutInflater.from(context);
            this.dataset = dataset;
        }

        @Override
        public int getCount() {
            return dataset.size();
        }

        @Override
        public Item getItem(int position) {
            return dataset.get(position);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_action, parent, false);
                holder = new Holder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }
            holder.bind(getItem(position));
            return convertView;
        }

        static class Holder {
            final TextView label;

            Holder(View itemView) {
                label = itemView.findViewById(R.id.label);
            }

            void bind(Item item) {
                label.setCompoundDrawablesWithIntrinsicBounds(item.icon, 0, 0, 0);
                label.setText(item.title);
            }
        }
    }

    private ListAlertDialog() {
        // no instance
    }
}
