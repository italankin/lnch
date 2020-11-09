package com.italankin.lnch.feature.settings.apps.list.dialog;

import android.app.Dialog;
import android.os.Bundle;

import com.italankin.lnch.R;
import com.italankin.lnch.model.viewmodel.impl.AppViewModel;
import com.italankin.lnch.util.dialogfragment.BaseDialogFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

public class AppSettingsDialogFragment extends BaseDialogFragment<AppSettingsDialogFragment.Listener> {

    private static final String ARG_ITEMS = "items";
    private static final String ARG_NAME = "name";
    private static final String ARG_ID = "id";

    private static final int ARG_SEARCH_VISIBLE = 0;
    private static final int ARG_SHORTCUTS_SEARCH_VISIBLE = 1;

    private boolean[] checkedItems;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkedItems = getArgs().getBooleanArray(ARG_ITEMS);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        CharSequence[] titles = requireContext().getResources().getTextArray(R.array.settings_apps_list_options);
        return new AlertDialog.Builder(requireContext())
                .setTitle(getArgs().getString(ARG_NAME))
                .setMultiChoiceItems(titles, checkedItems,
                        (dialog, which, isChecked) -> checkedItems[which] = isChecked)
                .setNegativeButton(R.string.cancel, null)
                .setNeutralButton(R.string.settings_apps_list_options_reset, (dialog, which) -> {
                    Listener listener = getListener();
                    if (listener != null) {
                        listener.onAppSettingsReset(getArgs().getString(ARG_ID));
                    }
                })
                .setPositiveButton(R.string.settings_apps_list_options_save, (dialog, which) -> {
                    Listener listener = getListener();
                    if (listener != null) {
                        listener.onAppSettingsUpdated(
                                getArgs().getString(ARG_ID),
                                checkedItems[ARG_SEARCH_VISIBLE],
                                checkedItems[ARG_SHORTCUTS_SEARCH_VISIBLE]
                        );
                    }
                })
                .create();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        getArgs().putBooleanArray(ARG_ITEMS, checkedItems);
    }

    public static class Builder extends BaseBuilder<AppSettingsDialogFragment, Listener, Builder> {

        public Builder setApp(AppViewModel app) {
            boolean[] values = {app.isSearchVisible(), app.isShortcutsSearchVisible()};
            arguments.putString(ARG_NAME, app.getVisibleLabel());
            arguments.putBooleanArray(ARG_ITEMS, values);
            arguments.putString(ARG_ID, app.getDescriptor().getId());
            return this;
        }

        @Override
        protected AppSettingsDialogFragment createInstance() {
            return new AppSettingsDialogFragment();
        }
    }

    public interface Listener {
        void onAppSettingsUpdated(String id, boolean searchVisible, boolean shortcutsSearchVisible);

        void onAppSettingsReset(String id);
    }
}
