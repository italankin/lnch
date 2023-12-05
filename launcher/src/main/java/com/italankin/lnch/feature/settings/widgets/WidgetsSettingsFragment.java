package com.italankin.lnch.feature.settings.widgets;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.SeekBarPreference;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.settings.SettingsToolbarTitle;
import com.italankin.lnch.feature.settings.base.BasePreferenceFragment;
import com.italankin.lnch.model.repository.prefs.Preferences;

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
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        scrollToTarget();
    }
}
