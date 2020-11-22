package com.italankin.lnch.feature.settings;

import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.settings.base.BasePreferenceFragment;
import com.italankin.lnch.model.repository.prefs.Preferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ShortcutsFragment extends BasePreferenceFragment {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.prefs_shortcuts);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findPreference(Preferences.MAX_DYNAMIC_SHORTCUTS).setEnabled(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1);
    }
}
