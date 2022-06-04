package com.italankin.lnch.feature.settings.search;

import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.settings.SettingsToolbarTitle;
import com.italankin.lnch.feature.settings.base.BasePreferenceFragment;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.usage.UsageTracker;
import com.italankin.lnch.util.PackageUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class SearchFragment extends BasePreferenceFragment implements CustomFormatDialogFragment.Listener,
        SettingsToolbarTitle {

    private static final String TAG_CUSTOM_SEARCH_ENGINE_FORMAT = "custom_search_engine_format";

    private final CompositeDisposable disposables = new CompositeDisposable();
    private Preferences preferences;
    private UsageTracker usageTracker;
    private Preference formatPreference;

    @Override
    public CharSequence getToolbarTitle(Context context) {
        return context.getString(R.string.settings_category_search);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = LauncherApp.daggerService.main().preferences();
        usageTracker = LauncherApp.daggerService.main().usageTracker();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposables.clear();
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

        formatPreference = findPreference(Preferences.CUSTOM_SEARCH_ENGINE_FORMAT);
        formatPreference.setEnabled(
                preferences.get(Preferences.SEARCH_ENGINE) == Preferences.SearchEngine.CUSTOM
        );
        formatPreference.setSummary(preferences.get(Preferences.CUSTOM_SEARCH_ENGINE_FORMAT));
        formatPreference.setOnPreferenceClickListener(preference -> {
            onFormatPreferenceClick();
            return true;
        });
        subscribeForUpdates();
    }

    @Override
    public void onValueChanged(String newValue) {
        formatPreference.setSummary(newValue);
        preferences.set(Preferences.CUSTOM_SEARCH_ENGINE_FORMAT, newValue);
    }

    private void onFormatPreferenceClick() {
        String customSearchEngineFormat = preferences.get(Preferences.CUSTOM_SEARCH_ENGINE_FORMAT);
        new CustomFormatDialogFragment.Builder()
                .setCustomFormat(customSearchEngineFormat)
                .build()
                .show(getChildFragmentManager(), TAG_CUSTOM_SEARCH_ENGINE_FORMAT);
    }

    private void subscribeForUpdates() {
        Disposable disposable = preferences.observeValue(Preferences.SEARCH_ENGINE)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(value -> {
                    Preferences.SearchEngine engine = value.get();
                    formatPreference.setEnabled(engine == Preferences.SearchEngine.CUSTOM);
                });
        disposables.add(disposable);
    }
}
