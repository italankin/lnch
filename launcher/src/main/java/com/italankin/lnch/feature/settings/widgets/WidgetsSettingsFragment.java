package com.italankin.lnch.feature.settings.widgets;

import android.content.Context;
import android.os.Bundle;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.settings.SettingsToolbarTitle;
import com.italankin.lnch.feature.settings.base.BasePreferenceFragment;

public class WidgetsSettingsFragment extends BasePreferenceFragment implements SettingsToolbarTitle {

    @Override
    public CharSequence getToolbarTitle(Context context) {
        return context.getString(R.string.settings_home_widgets);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.prefs_widgets);
    }
}
