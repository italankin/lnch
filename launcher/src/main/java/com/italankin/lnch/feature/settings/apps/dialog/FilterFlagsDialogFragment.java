package com.italankin.lnch.feature.settings.apps.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.settings.apps.adapter.AppsFilter;

import java.io.Serializable;

public class FilterFlagsDialogFragment extends DialogFragment {
    private static final String ARG_FLAGS = "flags";
    private static final String ARG_PROVIDER = "provider";

    private boolean[] checkedItems;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkedItems = getCheckedItems();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(requireContext())
                .setTitle(R.string.settings_apps_filter)
                .setMultiChoiceItems(R.array.settings_apps_filter_items, checkedItems,
                        (dialog, which, isChecked) -> checkedItems[which] = isChecked)
                .setNegativeButton(R.string.cancel, null)
                .setNeutralButton(R.string.settings_apps_filter_reset, (dialog, which) -> {
                    Listener listner = getListener();
                    if (listner != null) {
                        listner.onFlagsReset();
                    }
                })
                .setPositiveButton(R.string.settings_apps_filter_apply, (dialog, which) -> {
                    int newFlags = getFlags();
                    Listener listner = getListener();
                    if (listner != null) {
                        listner.onFlagsSet(newFlags);
                    }
                })
                .create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            arguments.putInt(ARG_FLAGS, getFlags());
        }
    }

    private boolean[] getCheckedItems() {
        Bundle arguments = getArguments();
        if (arguments == null) {
            return new boolean[2];
        }
        int flags = arguments.getInt(ARG_FLAGS, 0);
        return new boolean[]{
                (flags & AppsFilter.FLAG_VISIBLE) > 0,
                (flags & AppsFilter.FLAG_HIDDEN) > 0
        };
    }

    private int getFlags() {
        return (checkedItems[0] ? AppsFilter.FLAG_VISIBLE : 0)
                | (checkedItems[1] ? AppsFilter.FLAG_HIDDEN : 0);
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

        public Builder setFlags(int flags) {
            arguments.putInt(ARG_FLAGS, flags);
            return this;
        }

        public Builder setListenerProvider(ListenerProvider provider) {
            arguments.putSerializable(ARG_PROVIDER, provider);
            return this;
        }

        public FilterFlagsDialogFragment build() {
            if (!arguments.containsKey(ARG_PROVIDER)) {
                throw new IllegalArgumentException(ARG_PROVIDER + " is required");
            }
            FilterFlagsDialogFragment fragment = new FilterFlagsDialogFragment();
            fragment.setArguments(arguments);
            return fragment;
        }
    }

    public interface Listener {
        void onFlagsSet(int newFlags);

        void onFlagsReset();
    }

    public interface ListenerProvider extends Serializable {
        Listener get(Fragment parentFragment);
    }
}
