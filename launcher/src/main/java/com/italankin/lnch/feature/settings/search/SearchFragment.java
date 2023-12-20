package com.italankin.lnch.feature.settings.search;

import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.settings.SettingsToolbarTitle;
import com.italankin.lnch.feature.settings.base.BasePreferenceFragment;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.usage.UsageTracker;
import com.italankin.lnch.util.PackageUtils;

public class SearchFragment extends BasePreferenceFragment implements SettingsToolbarTitle {

    private UsageTracker usageTracker;

    @Override
    public CharSequence getToolbarTitle(Context context) {
        return context.getString(R.string.settings_category_search);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        usageTracker = LauncherApp.daggerService.main().usageTracker();
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.prefs_search);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ComponentName searchActivity = PackageUtils.getGlobalSearchActivity(requireContext());
        if (searchActivity == null) {
            Preference preference = findPreference(Preferences.SEARCH_SHOW_GLOBAL_SEARCH);
            preference.setEnabled(false);
        }

        findPreference(R.string.pref_key_search_most_used_reset).setOnPreferenceClickListener(preference -> {
            usageTracker.clearStatistics();
            Toast.makeText(requireContext(), R.string.settings_search_history_most_used_reset_cleared, Toast.LENGTH_SHORT)
                    .show();
            return true;
        });

        SearchEnginePreference searchEnginePreference = findPreference(Preferences.SEARCH_ENGINE);
        searchEnginePreference.setOnCustomFormatClickListener(v -> {
            new CustomFormatDialogFragment().show(getChildFragmentManager(), null);
        });

        scrollToTarget();
    }
}
