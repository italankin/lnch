package com.italankin.lnch.feature.settings.widgets;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.settings.SettingsToolbarTitle;
import com.italankin.lnch.feature.settings.base.BasePreferenceFragment;
import com.italankin.lnch.feature.settings.util.TargetPreference;
import com.italankin.lnch.feature.widgets.util.WidgetHelper;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.util.DialogUtils;

@RequiresApi(Build.VERSION_CODES.O)
public class WidgetsSettingsFragment extends BasePreferenceFragment implements SettingsToolbarTitle {

    @Override
    public CharSequence getToolbarTitle(Context context) {
        return context.getString(R.string.settings_home_widgets);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.prefs_widgets);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findPreference(R.string.pref_key_widgets_grid).setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(requireContext(), WidgetGridSettingsActivity.class));
            return true;
        });
        findPreference(R.string.pref_key_widgets_remove).setOnPreferenceClickListener(preference -> {
            AlertDialog alertDialog = new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.settings_home_widgets_remove_dialog_title)
                    .setMessage(R.string.settings_home_widgets_remove_dialog_message)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.settings_home_widgets_remove_dialog_confirm, (dialog, which) -> {
                        WidgetHelper.resetAllWidgets();
                        Toast.makeText(requireContext(), R.string.settings_home_widgets_remove_dialog_removed, Toast.LENGTH_SHORT)
                                .show();
                    })
                    .show();
            DialogUtils.dismissOnDestroy(this, alertDialog);
            return true;
        });

        String target = TargetPreference.get(this);
        if (target == null) {
            return;
        }
        if (Preferences.WIDGETS_HORIZONTAL_GRID_SIZE.key().equals(target) ||
                Preferences.WIDGETS_HEIGHT_CELL_RATIO.key().equals(target)) {
            startActivity(new Intent(requireContext(), WidgetGridSettingsActivity.class));
        } else {
            scrollToTarget();
        }
    }
}
