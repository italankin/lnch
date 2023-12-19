package com.italankin.lnch.feature.home.adapter.shimmer;

import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.home.adapter.HomeAdapterDelegate;
import com.italankin.lnch.util.ResUtils;

public class ShimmerDescriptorUiAdapter extends HomeAdapterDelegate<ShimmerDescriptorUiAdapter.ViewHolder, ShimmerDescriptorUi> {

    @Override
    protected int getLayoutRes() {
        return R.layout.item_shimmer;
    }

    @NonNull
    @Override
    protected ViewHolder createViewHolder(@NonNull View view) {
        return new ViewHolder(view);
    }

    @Override
    protected boolean isType(int position, Object item, boolean ignoreVisibility) {
        return item instanceof ShimmerDescriptorUi;
    }

    @Override
    public long getItemId(int position, ShimmerDescriptorUi item) {
        return item.widthDp;
    }

    static class ViewHolder extends HomeAdapterDelegate.ViewHolder<ShimmerDescriptorUi> {
        final TextView label;
        private final ShimmerDrawable shimmer;

        ViewHolder(View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.label);
            shimmer = new ShimmerDrawable(itemView.getContext());
            label.setBackground(shimmer);
        }

        @Override
        protected void bind(ShimmerDescriptorUi item) {
            label.setMinimumWidth(ResUtils.px2dp(label.getContext(), item.widthDp));
            shimmer.setPadding(label.getPaddingTop());
        }

        @Override
        protected View getRoot() {
            return label;
        }

        @Nullable
        @Override
        protected TextView getLabel() {
            return label;
        }
    }
}
