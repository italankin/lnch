package com.italankin.lnch.util.dialogfragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;

@SuppressWarnings("unchecked")
public abstract class BaseDialogFragment<L> extends DialogFragment {
    protected static final String ARG_PROVIDER = "provider";

    @NonNull
    protected Bundle getArgs() {
        Bundle arguments = getArguments();
        return arguments == null ? Bundle.EMPTY : arguments;
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

    protected L getListener() {
        Object provider = getArgs().getSerializable(ARG_PROVIDER);
        if (provider == null) {
            return null;
        }
        if (provider instanceof ListenerFragment) {
            return ((ListenerFragment<L>) provider).get(getParentFragment());
        } else if (provider instanceof ListenerActivity) {
            return ((ListenerActivity<L>) provider).get(requireActivity());
        }
        return null;
    }

    protected static abstract class BaseBuilder<F extends BaseDialogFragment, L, B extends BaseBuilder> implements ArgumentsHolder {
        protected final Bundle arguments = new Bundle(6);

        @Override
        public Bundle getArguments() {
            return arguments;
        }

        public B setListenerProvider(ListenerFragment<L> provider) {
            arguments.putSerializable(ARG_PROVIDER, provider);
            return (B) this;
        }

        public B setListenerProvider(ListenerActivity<L> provider) {
            arguments.putSerializable(ARG_PROVIDER, provider);
            return (B) this;
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
