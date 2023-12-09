package com.italankin.lnch.util.dialogfragment;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SimpleDialogFragment extends BaseDialogFragment<SimpleDialogFragment.Listener> {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle arguments = getArgs();
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(TitleBuilder.ARG))
                .setMessage(getString(MessageBuilder.ARG));
        if (arguments.containsKey(PositiveButtonBuilder.ARG)) {
            CharSequence text = getString(PositiveButtonBuilder.ARG);
            builder.setPositiveButton(text, (dialog, which) -> {
                Listener listener = getListener(Listener.class);
                if (listener != null) {
                    listener.onPositiveButtonClick(getTag());
                }
            });
        }
        if (arguments.containsKey(NegativeButtonBuilder.ARG)) {
            CharSequence text = getString(NegativeButtonBuilder.ARG);
            builder.setNegativeButton(text, (dialog, which) -> {
                Listener listener = getListener(Listener.class);
                if (listener != null) {
                    listener.onNegativeButtonClick(getTag());
                }
            });
        }
        return builder.create();
    }

    public static class Builder extends BaseBuilder<SimpleDialogFragment> implements
            TitleBuilder<Builder>,
            MessageBuilder<Builder>,
            PositiveButtonBuilder<Builder>,
            NegativeButtonBuilder<Builder> {

        @Override
        protected SimpleDialogFragment createInstance() {
            return new SimpleDialogFragment();
        }
    }

    public interface Listener {
        void onPositiveButtonClick(@Nullable String tag);

        default void onNegativeButtonClick(@Nullable String tag) {
        }
    }
}
