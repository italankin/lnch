package com.italankin.lnch.feature.settings;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.italankin.lnch.BuildConfig;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.settings.base.BasePreferenceFragment;
import com.italankin.lnch.util.IntentUtils;
import com.italankin.lnch.util.PackageUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;

public class SettingsRootFragment extends BasePreferenceFragment {

    private final static String SOURCE_CODE_URL = "https://github.com/italankin/lnch";

    private Callbacks callbacks;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Context context) {
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
        addPreferencesFromResource(R.xml.prefs_root);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findPreference(R.string.pref_key_home_customize).setOnPreferenceClickListener(preference -> {
            if (callbacks != null) {
                callbacks.launchEditMode();
            }
            return true;
        });
        findPreference(R.string.pref_key_search_settings).setOnPreferenceClickListener(preference -> {
            if (callbacks != null) {
                callbacks.showSearchPreferences();
            }
            return true;
        });
        findPreference(R.string.pref_key_wallpaper).setOnPreferenceClickListener(preference -> {
            if (callbacks != null) {
                callbacks.showWallpaperPreferences();
            }
            return true;
        });
        findPreference(R.string.pref_key_apps_settings).setOnPreferenceClickListener(preference -> {
            if (callbacks != null) {
                callbacks.showAppsSettings();
            }
            return true;
        });
        findPreference(R.string.pref_key_shortcuts).setOnPreferenceClickListener(preference -> {
            if (callbacks != null) {
                callbacks.showShortcutsPreferences();
            }
            return true;
        });
        findPreference(R.string.pref_key_notifications).setOnPreferenceClickListener(preference -> {
            if (callbacks != null) {
                callbacks.showNotificationsPreferences();
            }
            return true;
        });
        findPreference(R.string.pref_key_look_and_feel).setOnPreferenceClickListener(preference -> {
            if (callbacks != null) {
                callbacks.showLookAndFeelPreferences();
            }
            return true;
        });
        findPreference(R.string.pref_key_home_misc).setOnPreferenceClickListener(preference -> {
            if (callbacks != null) {
                callbacks.showMiscPreferences();
            }
            return true;
        });
        findPreference(R.string.pref_key_home_widgets).setOnPreferenceClickListener(preference -> {
            if (callbacks != null) {
                callbacks.showWidgetPreferences();
            }
            return true;
        });
        findPreference(R.string.pref_key_backups).setOnPreferenceClickListener(preference -> {
            if (callbacks != null) {
                callbacks.showBackupPreferences();
            }
            return true;
        });
        Preference version = findPreference(R.string.pref_key_version);
        version.setTitle(getString(R.string.settings_version, BuildConfig.VERSION_NAME));
        Preference sourceCode = findPreference(R.string.pref_key_source_code);
        sourceCode.setOnPreferenceClickListener(preference -> {
            Uri uri = Uri.parse(SOURCE_CODE_URL);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(Intent.createChooser(intent, ""));
            return true;
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.settings, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_system_settings) {
            Context context = requireContext();
            Intent intent = PackageUtils.getPackageSystemSettings(context.getPackageName());
            if (!IntentUtils.safeStartActivity(context, intent)) {
                Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public interface Callbacks {

        void launchEditMode();

        void showSearchPreferences();

        void showAppsSettings();

        void showShortcutsPreferences();

        void showNotificationsPreferences();

        void showLookAndFeelPreferences();

        void showMiscPreferences();

        void showWidgetPreferences();

        void showWallpaperPreferences();

        void showBackupPreferences();
    }
}
