package com.italankin.lnch.feature.settings.apps.dialog;

import android.app.Dialog;
import android.os.Bundle;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.settings.apps.model.FilterFlag;
import com.italankin.lnch.util.dialogfragment.BaseDialogFragment;

import java.util.EnumSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

public class FilterFlagsDialogFragment extends BaseDialogFragment<FilterFlagsDialogFragment.Listener> {
    private static final String ARG_FLAGS = "flags";

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
                    Listener listener = getListener();
                    if (listener != null) {
                        listener.onFlagsReset();
                    }
                })
                .setPositiveButton(R.string.settings_apps_filter_apply, (dialog, which) -> {
                    EnumSet<FilterFlag> newFlags = getFlags();
                    Listener listener = getListener();
                    if (listener != null) {
                        listener.onFlagsSet(newFlags);
                    }
                })
                .create();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
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

    public static class Builder extends BaseBuilder<FilterFlagsDialogFragment, Listener, Builder> {

        public Builder setFlags(EnumSet<FilterFlag> flags) {
            arguments.putSerializable(ARG_FLAGS, flags);
            return this;
        }

        @Override
        protected FilterFlagsDialogFragment createInstance() {
            return new FilterFlagsDialogFragment();
        }
    }

    public interface Listener {
        void onFlagsSet(EnumSet<FilterFlag> newFlags);

        void onFlagsReset();
    }
}
