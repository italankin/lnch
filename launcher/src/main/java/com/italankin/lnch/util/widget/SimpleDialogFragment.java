package com.italankin.lnch.util.widget;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import com.italankin.lnch.util.dialogfragment.BaseDialogFragment;

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
                Listener listener = getListener();
                if (listener != null) {
                    listener.onPositiveButtonClick();
                }
            });
        }
        if (arguments.containsKey(NegativeButtonBuilder.ARG)) {
            CharSequence text = getString(NegativeButtonBuilder.ARG);
            builder.setNegativeButton(text, (dialog, which) -> {
                Listener listener = getListener();
                if (listener != null) {
                    listener.onNegativeButtonClick();
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
        void onPositiveButtonClick();

        default void onNegativeButtonClick() {
        }
    }
}
