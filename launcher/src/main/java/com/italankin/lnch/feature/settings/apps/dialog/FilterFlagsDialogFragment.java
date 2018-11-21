package com.italankin.lnch.feature.settings.apps.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.settings.apps.model.FilterFlag;

import java.io.Serializable;
import java.util.EnumSet;

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
                .setMultiChoiceItems(getFilterTitles(), checkedItems,
                        (dialog, which, isChecked) -> checkedItems[which] = isChecked)
                .setNegativeButton(R.string.cancel, null)
                .setNeutralButton(R.string.settings_apps_filter_reset, (dialog, which) -> {
                    Listener listner = getListener();
                    if (listner != null) {
                        listner.onFlagsReset();
                    }
                })
                .setPositiveButton(R.string.settings_apps_filter_apply, (dialog, which) -> {
                    EnumSet<FilterFlag> newFlags = getFlags();
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
            arguments.putSerializable(ARG_FLAGS, getFlags());
        }
    }

    private boolean[] getCheckedItems() {
        FilterFlag[] values = FilterFlag.values();
        EnumSet<FilterFlag> flags = getFilterFlags();
        boolean itemsState[] = new boolean[values.length];
        for (int i = 0; i < values.length; i++) {
            FilterFlag value = values[i];
            itemsState[i] = flags.contains(value);
        }
        return itemsState;
    }

    @SuppressWarnings("unchecked")
    private EnumSet<FilterFlag> getFilterFlags() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            EnumSet<FilterFlag> flags = (EnumSet<FilterFlag>) arguments.getSerializable(ARG_FLAGS);
            if (flags != null) {
                return flags;
            }
        }
        return EnumSet.allOf(FilterFlag.class);
    }

    private CharSequence[] getFilterTitles() {
        FilterFlag[] values = FilterFlag.values();
        CharSequence[] titles = new CharSequence[values.length];
        for (int i = 0; i < values.length; i++) {
            titles[i] = getText(values[i].title);
        }
        return titles;
    }

    private EnumSet<FilterFlag> getFlags() {
        FilterFlag[] values = FilterFlag.values();
        EnumSet<FilterFlag> flags = EnumSet.noneOf(FilterFlag.class);
        for (int i = 0; i < checkedItems.length; i++) {
            if (checkedItems[i]) {
                flags.add(values[i]);
            }
        }
        return flags;
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

        public Builder setFlags(EnumSet<FilterFlag> flags) {
            arguments.putSerializable(ARG_FLAGS, flags);
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
        void onFlagsSet(EnumSet<FilterFlag> newFlags);

        void onFlagsReset();
    }

    public interface ListenerProvider extends Serializable {
        Listener get(Fragment parentFragment);
    }
}
