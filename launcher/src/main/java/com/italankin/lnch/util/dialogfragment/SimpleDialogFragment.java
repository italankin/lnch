package com.italankin.lnch.util.dialogfragment;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

public class SimpleDialogFragment extends BaseDialogFragment<SimpleDialogFragment.Listener> {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle arguments = getArgs();
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
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

    public static class Builder extends BaseBuilder<SimpleDialogFragment, Listener, Builder> implements
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
        void onPositiveButtonClick(String tag);

        default void onNegativeButtonClick(String tag) {
        }
    }
}
