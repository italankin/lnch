package com.italankin.lnch.util.widget;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;

import java.io.Serializable;

public class SimpleDialogFragment extends DialogFragment {

    private static final String ARG_TITLE = "title";
    private static final String ARG_MESSAGE = "message";
    private static final String ARG_POSITIVE_BUTTON = "positive_button";
    private static final String ARG_NEGATIVE_BUTTON = "negative_button";
    private static final String ARG_PROVIDER = "provider";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments == null) {
            throw new IllegalArgumentException();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setTitle(arguments.getCharSequence(ARG_TITLE))
                .setMessage(arguments.getCharSequence(ARG_MESSAGE));
        if (arguments.containsKey(ARG_POSITIVE_BUTTON)) {
            CharSequence text = arguments.getCharSequence(ARG_POSITIVE_BUTTON);
            builder.setPositiveButton(text, (dialog, which) -> {
                Listener listener = getListener();
                if (listener != null) {
                    listener.onPositiveButtonClick();
                }
            });
        }
        if (arguments.containsKey(ARG_NEGATIVE_BUTTON)) {
            CharSequence text = arguments.getCharSequence(ARG_NEGATIVE_BUTTON);
            builder.setNegativeButton(text, (dialog, which) -> {
                Listener listener = getListener();
                if (listener != null) {
                    listener.onNegativeButtonClick();
                }
            });
        }
        return builder.create();
    }

    private Listener getListener() {
        Bundle arguments = getArguments();
        if (arguments == null) {
            return null;
        }
        ListenerProvider provider = (ListenerProvider) arguments.getSerializable(ARG_PROVIDER);
        if (provider == null) {
            return null;
        }
        return provider.get(getParentFragment());
    }

    public static class Builder {
        private final Bundle arguments = new Bundle();

        public Builder setTitle(CharSequence title) {
            arguments.putCharSequence(ARG_TITLE, title);
            return this;
        }

        public Builder setMessage(CharSequence message) {
            arguments.putCharSequence(ARG_MESSAGE, message);
            return this;
        }

        public Builder setPositiveButton(CharSequence text) {
            arguments.putCharSequence(ARG_POSITIVE_BUTTON, text);
            return this;
        }

        public Builder setNegativeButton(CharSequence text) {
            arguments.putCharSequence(ARG_NEGATIVE_BUTTON, text);
            return this;
        }

        public Builder setListenerProvider(ListenerProvider provider) {
            arguments.putSerializable(ARG_PROVIDER, provider);
            return this;
        }

        public SimpleDialogFragment build() {
            if (!arguments.containsKey(ARG_PROVIDER)) {
                throw new IllegalArgumentException(ARG_PROVIDER + " is required");
            }
            SimpleDialogFragment fragment = new SimpleDialogFragment();
            fragment.setArguments(arguments);
            return fragment;
        }
    }

    public interface Listener {
        void onPositiveButtonClick();

        default void onNegativeButtonClick() {
        }
    }

    public interface ListenerProvider extends Serializable {
        Listener get(Fragment parentFragment);
    }
}
