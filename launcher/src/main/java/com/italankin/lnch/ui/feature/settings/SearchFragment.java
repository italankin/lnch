package com.italankin.lnch.ui.feature.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.italankin.lnch.R;

public class SearchFragment extends AppPreferenceFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.prefs_search);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
