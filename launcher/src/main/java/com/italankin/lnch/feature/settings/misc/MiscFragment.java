package com.italankin.lnch.feature.settings.misc;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.settings.base.BasePreferenceFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MiscFragment extends BasePreferenceFragment {

    private Callbacks callbacks;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        callbacks = (Callbacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbacks = null;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.prefs_misc);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findPreference(R.string.pref_key_misc_experimental).setOnPreferenceClickListener(preference -> {
            if (callbacks != null) {
                callbacks.showExperimentalPreferences();
            }
            return true;
        });
    }

    public interface Callbacks {
        void showExperimentalPreferences();
    }
}
