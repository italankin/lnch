package com.italankin.lnch.feature.settings.base;

import android.os.Bundle;

import com.italankin.lnch.feature.home.fragmentresult.FragmentResultSender;
import com.italankin.lnch.model.repository.prefs.Preferences;

import androidx.annotation.StringRes;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public abstract class BasePreferenceFragment extends PreferenceFragmentCompat implements FragmentResultSender {

    @SuppressWarnings("unchecked")
    protected <T extends Preference> T findPreference(@StringRes int key) {
        return (T) findPreference(getString(key));
    }

    @SuppressWarnings("unchecked")
    protected <T extends Preference> T findPreference(Preferences.Pref<?> pref) {
        return (T) findPreference(pref.key());
    }

    @Override
    public void sendResult(Bundle result) {
        String requestKey = requireArguments().getString(ARG_REQUEST_KEY);
        getParentFragmentManager().setFragmentResult(requestKey, result);
    }
}
