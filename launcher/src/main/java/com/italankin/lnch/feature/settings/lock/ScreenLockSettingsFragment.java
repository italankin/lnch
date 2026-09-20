package com.italankin.lnch.feature.settings.lock;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;

import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.lock.ScreenLock;
import com.italankin.lnch.feature.settings.SettingsToolbarTitle;
import com.italankin.lnch.feature.settings.base.BasePreferenceFragment;
import com.italankin.lnch.model.repository.prefs.Preferences;

public class ScreenLockSettingsFragment extends BasePreferenceFragment implements SettingsToolbarTitle {

    @Override
    public CharSequence getToolbarTitle(Context context) {
        return context.getString(R.string.settings_home_screen_lock);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.prefs_screen_lock);
        CheckBoxPreference screenLock = findPreference(Preferences.DOUBLE_TAP_TO_LOCK);
        screenLock.setOnPreferenceChangeListener((preference, newValue) -> {
            if (!ScreenLock.isAccessibilityEnabled(requireContext())) {
                updatePreferences();
                return false;
            }
            if ((Boolean) newValue) {
                ScreenLock.requestConsent(this, () -> {
                    LauncherApp.daggerService.main().preferences().set(Preferences.DOUBLE_TAP_TO_LOCK, true);
                    updatePreferences();
                });
            } else {
                LauncherApp.daggerService.main().preferences().set(Preferences.DOUBLE_TAP_TO_LOCK, false);
                updatePreferences();
            }
            return false;
        });
        findPreference(R.string.pref_key_screen_lock_accessibility).setOnPreferenceClickListener(preference -> {
            if (ScreenLock.isAccessibilityEnabled(requireContext())) {
                ScreenLock.openAccessibilitySettings(requireContext());
            } else {
                ScreenLock.requestConsent(this, () -> ScreenLock.openAccessibilitySettings(requireContext()));
            }
            return true;
        });
        updatePreferences();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        scrollToTarget();
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePreferences();
    }

    private void updatePreferences() {
        boolean accessEnabled = ScreenLock.isAccessibilityEnabled(requireContext());

        CheckBoxPreference screenLock = findPreference(Preferences.DOUBLE_TAP_TO_LOCK);
        screenLock.setChecked(ScreenLock.isEnabled());
        screenLock.setEnabled(accessEnabled);
        screenLock.setSummary(accessEnabled ? R.string.settings_home_screen_lock_double_tap_summary : R.string.settings_home_screen_lock_access_required);

        Preference access = findPreference(R.string.pref_key_screen_lock_accessibility);
        access.setSummary(accessEnabled ? R.string.settings_home_screen_lock_accessibility_enabled
                : R.string.settings_home_screen_lock_accessibility_disabled);
    }
}
