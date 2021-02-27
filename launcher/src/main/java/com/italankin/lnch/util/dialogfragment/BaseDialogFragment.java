package com.italankin.lnch.util.dialogfragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

@SuppressWarnings("unchecked")
public abstract class BaseDialogFragment<L> extends DialogFragment {
    @NonNull
    protected Bundle getArgs() {
        Bundle arguments = getArguments();
        if (arguments == null) {
            arguments = new Bundle();
            setArguments(arguments);
        }
        return arguments;
    }

    protected CharSequence getString(String key) {
        Object argument = getArgs().get(key);
        if (argument == null) {
            return null;
        }
        if (argument instanceof CharSequence) {
            return (CharSequence) argument;
        }
        int res = (int) argument;
        return getText(res);
    }

    protected L getListener(Class<? extends L> listener) {
        Fragment parentFragment = getParentFragment();
        if (parentFragment != null && listener.isAssignableFrom(parentFragment.getClass())) {
            return ((L) parentFragment);
        }
        if (listener.isAssignableFrom(requireActivity().getClass())) {
            return ((L) requireActivity());
        }
        return null;
    }

    protected static abstract class BaseBuilder<F extends BaseDialogFragment> implements ArgumentsHolder {
        protected final Bundle arguments = new Bundle(6);

        @Override
        public Bundle getArguments() {
            return arguments;
        }

        public F build() {
            F fragment = createInstance();
            fragment.setArguments(arguments);
            return fragment;
        }

        protected abstract F createInstance();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Builder blocks
    ///////////////////////////////////////////////////////////////////////////

    protected interface ArgumentsHolder {
        Bundle getArguments();
    }

    protected interface TitleBuilder<B extends BaseBuilder> extends ArgumentsHolder {
        String ARG = "title";

        default B setTitle(CharSequence title) {
            getArguments().putCharSequence(ARG, title);
            return (B) this;
        }

        default B setTitle(@StringRes int title) {
            getArguments().putInt(ARG, title);
            return (B) this;
        }
    }

    protected interface MessageBuilder<B extends BaseBuilder> extends ArgumentsHolder {
        String ARG = "message";

        default B setMessage(CharSequence title) {
            getArguments().putCharSequence(ARG, title);
            return (B) this;
        }

        default B setMessage(@StringRes int title) {
            getArguments().putInt(ARG, title);
            return (B) this;
        }
    }

    protected interface PositiveButtonBuilder<B extends BaseBuilder> extends ArgumentsHolder {
        String ARG = "positive_button";

        default B setPositiveButton(CharSequence title) {
            getArguments().putCharSequence(ARG, title);
            return (B) this;
        }

        default B setPositiveButton(@StringRes int title) {
            getArguments().putInt(ARG, title);
            return (B) this;
        }
    }

    protected interface NegativeButtonBuilder<B extends BaseBuilder> extends ArgumentsHolder {
        String ARG = "negative_button";

        default B setNegativeButton(CharSequence title) {
            getArguments().putCharSequence(ARG, title);
            return (B) this;
        }

        default B setNegativeButton(@StringRes int title) {
            getArguments().putInt(ARG, title);
            return (B) this;
        }
    }
}
