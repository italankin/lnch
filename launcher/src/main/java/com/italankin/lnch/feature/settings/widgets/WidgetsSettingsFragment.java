package com.italankin.lnch.feature.settings.widgets;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.preference.SeekBarPreference;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.settings.SettingsToolbarTitle;
import com.italankin.lnch.feature.settings.base.BasePreferenceFragment;
import com.italankin.lnch.feature.widgets.util.WidgetHelper;
import com.italankin.lnch.model.repository.prefs.Preferences;

@RequiresApi(Build.VERSION_CODES.O)
public class WidgetsSettingsFragment extends BasePreferenceFragment implements SettingsToolbarTitle {

    @Override
    public CharSequence getToolbarTitle(Context context) {
        return context.getString(R.string.settings_home_widgets);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.prefs_widgets);
        SeekBarPreference seekBarPreference = findPreference(Preferences.WIDGETS_HORIZONTAL_GRID_SIZE);
        seekBarPreference.setMin(Preferences.WIDGETS_HORIZONTAL_GRID_SIZE.min());
        seekBarPreference.setMax(Preferences.WIDGETS_HORIZONTAL_GRID_SIZE.max());
        findPreference(R.string.pref_key_widgets_remove).setOnPreferenceClickListener(preference -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.settings_home_widgets_remove_dialog_title)
                    .setMessage(R.string.settings_home_widgets_remove_dialog_message)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.settings_home_widgets_remove_dialog_confirm, (dialog, which) -> {
                        WidgetHelper.resetAllWidgets();
                        Toast.makeText(requireContext(), R.string.settings_home_widgets_remove_dialog_removed, Toast.LENGTH_SHORT)
                                .show();
                    })
                    .show();
            return true;
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        scrollToTarget();
    }
}
