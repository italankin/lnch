package com.italankin.lnch.feature.home.adapter.error;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.home.adapter.HomeAdapterDelegate;

public class ErrorDescriptorUiAdapter extends HomeAdapterDelegate<ErrorDescriptorUiAdapter.ViewHolder, ErrorDescriptorUi> {

    private final View.OnClickListener onErrorButtonClickListener;

    public ErrorDescriptorUiAdapter(View.OnClickListener onErrorButtonClickListener) {
        this.onErrorButtonClickListener = onErrorButtonClickListener;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.item_error;
    }

    @NonNull
    @Override
    protected ViewHolder createViewHolder(@NonNull View view) {
        ViewHolder holder = new ViewHolder(view);
        holder.errorButton.setOnClickListener(onErrorButtonClickListener);
        return holder;
    }

    @Override
    protected boolean isType(int position, Object item, boolean ignoreVisibility) {
        return item instanceof ErrorDescriptorUi;
    }

    @Override
    public long getItemId(int position, ErrorDescriptorUi item) {
        return item.hashCode();
    }

    static class ViewHolder extends HomeAdapterDelegate.ViewHolder<ErrorDescriptorUi> {
        final TextView errorText;
        final Button errorButton;

        ViewHolder(View itemView) {
            super(itemView);
            errorText = itemView.findViewById(R.id.error_text);
            errorButton = itemView.findViewById(R.id.error_button);
        }

        @Override
        protected void bind(ErrorDescriptorUi item) {
            String message = itemView.getContext().getString(R.string.error_apps_not_loaded, item.error.toString());
            errorText.setText(message);
        }

        @Nullable
        @Override
        protected TextView getLabel() {
            return null;
        }

        @Override
        protected View getRoot() {
            return itemView;
        }
    }
}
