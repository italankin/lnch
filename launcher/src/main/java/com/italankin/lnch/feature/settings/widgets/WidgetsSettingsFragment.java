package com.italankin.lnch.feature.settings.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.preference.Preference;
import androidx.preference.SeekBarPreference;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.settings.SettingsToolbarTitle;
import com.italankin.lnch.feature.settings.base.BasePreferenceFragment;
import com.italankin.lnch.feature.widgets.util.WidgetHelper;
import com.italankin.lnch.model.repository.prefs.Preferences;

import java.util.ArrayList;
import java.util.List;

@RequiresApi(Build.VERSION_CODES.O)
public class WidgetsSettingsFragment extends BasePreferenceFragment implements SettingsToolbarTitle {

    public static final float HEIGHT_CELL_RATIO_STEP = .25f;
    private Preferences preferences;

    @Override
    public CharSequence getToolbarTitle(Context context) {
        return context.getString(R.string.settings_home_widgets);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        preferences = LauncherApp.daggerService.main().preferences();
        addPreferencesFromResource(R.xml.prefs_widgets);
        SeekBarPreference horizontalGridSizePref = findPreference(Preferences.WIDGETS_HORIZONTAL_GRID_SIZE);
        horizontalGridSizePref.setMin(Preferences.WIDGETS_HORIZONTAL_GRID_SIZE.min());
        horizontalGridSizePref.setMax(Preferences.WIDGETS_HORIZONTAL_GRID_SIZE.max());
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

        setupHeightCellRatioPref();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        scrollToTarget();
    }

    private void setupHeightCellRatioPref() {
        Preference heightCellRatioPref = findPreference(Preferences.WIDGETS_HEIGHT_CELL_RATIO);
        heightCellRatioPref.setOnPreferenceClickListener(preference -> {
            float currentValue = preferences.get(Preferences.WIDGETS_HEIGHT_CELL_RATIO);
            int currentValueIndex = -1;
            List<HeightCellRatioItem> values = new ArrayList<>();
            int i = 0;
            float min = Preferences.WIDGETS_HEIGHT_CELL_RATIO.min(), max = Preferences.WIDGETS_HEIGHT_CELL_RATIO.max();
            for (float r = min; r <= max; r += HEIGHT_CELL_RATIO_STEP) {
                values.add(new HeightCellRatioItem(r));
                if (Float.compare(currentValue, r) == 0) {
                    currentValueIndex = i;
                }
                i++;
            }
            HeightCellRatioItem[] items = values.toArray(new HeightCellRatioItem[0]);
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(preference.getTitle())
                    .setSingleChoiceItems(items, currentValueIndex, (dialog, which) -> {
                        float newValue = items[which].value;
                        preferences.set(Preferences.WIDGETS_HEIGHT_CELL_RATIO, newValue);
                        heightCellRatioPref.setSummary(HeightCellRatioItem.stringValue(newValue));
                        dialog.dismiss();
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
            return false;
        });
        heightCellRatioPref.setSummary(HeightCellRatioItem.stringValue(preferences.get(Preferences.WIDGETS_HEIGHT_CELL_RATIO)));
    }

    private static class HeightCellRatioItem implements CharSequence {

        @SuppressLint("DefaultLocale")
        static String stringValue(float f) {
            return String.format("%.2f", f);
        }

        final float value;
        final String stringValue;

        private HeightCellRatioItem(float value) {
            this.value = value;
            this.stringValue = stringValue(value);
        }

        @Override
        public int length() {
            return stringValue.length();
        }

        @Override
        public char charAt(int index) {
            return stringValue.charAt(index);
        }

        @NonNull
        @Override
        public CharSequence subSequence(int start, int end) {
            return stringValue.subSequence(start, end);
        }

        @NonNull
        @Override
        public String toString() {
            return stringValue;
        }
    }
}
