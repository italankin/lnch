package com.italankin.lnch.feature.settings.lookfeel;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import com.google.android.material.color.DynamicColors;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.di.component.ViewModelComponent;
import com.italankin.lnch.feature.base.AppViewModelProvider;
import com.italankin.lnch.feature.settings.SettingsToolbarTitle;
import com.italankin.lnch.feature.settings.base.BasePreferenceFragment;
import com.italankin.lnch.feature.settings.util.TargetPreference;
import com.italankin.lnch.model.repository.prefs.Preferences;

import java.util.HashSet;
import java.util.Set;

public class LookAndFeelFragment extends BasePreferenceFragment implements SettingsToolbarTitle {

    public static LookAndFeelFragment newInstance(String requestKey) {
        Bundle args = new Bundle();
        args.putString(ARG_REQUEST_KEY, requestKey);
        LookAndFeelFragment fragment = new LookAndFeelFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private LookAndFeelViewModel viewModel;
    private Preferences preferences;

    @Override
    public CharSequence getToolbarTitle(Context context) {
        return context.getString(R.string.settings_home_laf);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = AppViewModelProvider.get(this, LookAndFeelViewModel.class, ViewModelComponent::lookAndFeel);
        preferences = LauncherApp.daggerService.main().preferences();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.saveData();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.prefs_look_and_feel);
        findPreference(Preferences.DYNAMIC_COLORS).setVisible(DynamicColors.isDynamicColorAvailable());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findPreference(R.string.pref_key_appearance).setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(requireContext(), AppearanceActivity.class));
            return true;
        });

        Preference folderOverlayColor = findPreference(Preferences.FOLDER_OVERLAY_COLOR);
        folderOverlayColor.setEnabled(
                preferences.get(Preferences.FULLSCREEN_FOLDERS) || preferences.get(Preferences.FOLDER_SHOW_OVERLAY));
        Preference folderShowOverlay = findPreference(Preferences.FOLDER_SHOW_OVERLAY);
        folderShowOverlay.setEnabled(!preferences.get(Preferences.FULLSCREEN_FOLDERS));
        folderShowOverlay.setOnPreferenceChangeListener((preference, newValue) -> {
            folderOverlayColor.setEnabled(preferences.get(Preferences.FULLSCREEN_FOLDERS) || (boolean) newValue);
            return true;
        });
        findPreference(Preferences.FULLSCREEN_FOLDERS).setOnPreferenceChangeListener((preference, newValue) -> {
            boolean isEnabled = (boolean) newValue;
            folderOverlayColor.setEnabled(isEnabled || preferences.get(Preferences.FOLDER_SHOW_OVERLAY));
            folderShowOverlay.setEnabled(!isEnabled);
            return true;
        });
        boolean notificationDotEnabled = preferences.get(Preferences.NOTIFICATION_DOT);
        findPreference(Preferences.NOTIFICATION_DOT_COLOR).setEnabled(notificationDotEnabled);
        findPreference(Preferences.NOTIFICATION_DOT_SIZE).setEnabled(notificationDotEnabled);
        findPreference(Preferences.APPS_LIST_ANIMATE)
                .setEnabled(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O);
        findPreference(Preferences.HIDE_STATUS_BAR).setOnPreferenceChangeListener((preference, newValue) -> {
            updateStatusBarColorDependency(((Boolean) newValue));
            return true;
        });
        updateStatusBarColorDependency(preferences.get(Preferences.HIDE_STATUS_BAR));

        String target = TargetPreference.get(this);
        if (target != null) {
            if (appearancePreferences().contains(target)) {
                startActivity(new Intent(requireContext(), AppearanceActivity.class));
            } else {
                scrollToTarget();
            }
        }
    }

    private Set<String> appearancePreferences() {
        Set<String> prefs = new HashSet<>();
        prefs.add(Preferences.ITEM_TEXT_SIZE.key());
        prefs.add(Preferences.ITEM_PADDING.key());
        prefs.add(Preferences.ITEM_FONT.key());
        prefs.add(Preferences.ITEM_SHADOW_RADIUS.key());
        prefs.add(Preferences.ITEM_SHADOW_COLOR.key());
        return prefs;
    }

    private void updateStatusBarColorDependency(Boolean hideStatusBar) {
        findPreference(Preferences.STATUS_BAR_COLOR).setEnabled(!hideStatusBar);
    }
}
