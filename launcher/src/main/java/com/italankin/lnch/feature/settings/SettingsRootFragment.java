package com.italankin.lnch.feature.settings;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;

import com.italankin.lnch.BuildConfig;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.home.fragmentresult.SignalFragmentResultContract;
import com.italankin.lnch.feature.settings.base.BasePreferenceFragment;
import com.italankin.lnch.util.IntentUtils;
import com.italankin.lnch.util.PackageUtils;

public class SettingsRootFragment extends BasePreferenceFragment {

    public static SettingsRootFragment newInstance(String requestKey) {
        Bundle args = new Bundle();
        args.putString(ARG_REQUEST_KEY, requestKey);
        SettingsRootFragment fragment = new SettingsRootFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private static final String SOURCE_CODE_URL = "https://github.com/italankin/lnch";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.prefs_root);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findPreference(R.string.pref_key_home_set_default_launcher).setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(Settings.ACTION_HOME_SETTINGS);
            startActivity(intent);
            return true;
        });
        findPreference(R.string.pref_key_home_customize).setOnPreferenceClickListener(preference -> {
            sendResult(new LaunchEditModeContract().result());
            return true;
        });
        findPreference(R.string.pref_key_search_settings).setOnPreferenceClickListener(preference -> {
            sendResult(new ShowSearchPreferencesContract().result());
            return true;
        });
        findPreference(R.string.pref_key_wallpaper).setOnPreferenceClickListener(preference -> {
            sendResult(new ShowWallpaperPreferences().result());
            return true;
        });
        findPreference(R.string.pref_key_apps_settings).setOnPreferenceClickListener(preference -> {
            sendResult(new ShowAppsSettings().result());
            return true;
        });
        findPreference(R.string.pref_key_shortcuts).setOnPreferenceClickListener(preference -> {
            sendResult(new ShowShortcutsPreferences().result());
            return true;
        });
        findPreference(R.string.pref_key_notifications).setOnPreferenceClickListener(preference -> {
            sendResult(new ShowNotificationsPreferences().result());
            return true;
        });
        findPreference(R.string.pref_key_look_and_feel).setOnPreferenceClickListener(preference -> {
            sendResult(new ShowLookAndFeelPreferences().result());
            return true;
        });
        findPreference(R.string.pref_key_home_misc).setOnPreferenceClickListener(preference -> {
            sendResult(new ShowMiscPreferences().result());
            return true;
        });
        findPreference(R.string.pref_key_home_widgets).setOnPreferenceClickListener(preference -> {
            sendResult(new ShowWidgetPreferences().result());
            return true;
        });
        findPreference(R.string.pref_key_backups).setOnPreferenceClickListener(preference -> {
            sendResult(new ShowBackupPreferences().result());
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
    public void onStart() {
        super.onStart();
        configureSetHomeAction();
    }

    private void configureSetHomeAction() {
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.addCategory(Intent.CATEGORY_HOME);
        Context context = requireContext();
        ResolveInfo resolveInfo = context.getPackageManager()
                .resolveActivity(home, PackageManager.MATCH_DEFAULT_ONLY);
        Preference setDefaultLauncherPref = findPreference(R.string.pref_key_home_set_default_launcher);
        if (resolveInfo != null && resolveInfo.activityInfo != null) {
            String currentHomePackage = resolveInfo.activityInfo.packageName;
            setDefaultLauncherPref.setVisible(!context.getPackageName().equals(currentHomePackage));
        } else {
            // better hide it to not annoy user
            setDefaultLauncherPref.setVisible(false);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
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

    public static class LaunchEditModeContract extends SignalFragmentResultContract {
        public LaunchEditModeContract() {
            super("launch_edit_mode");
        }
    }

    public static class ShowSearchPreferencesContract extends SignalFragmentResultContract {
        public ShowSearchPreferencesContract() {
            super("show_search_preferences");
        }
    }

    public static class ShowAppsSettings extends SignalFragmentResultContract {
        public ShowAppsSettings() {
            super("show_apps_settings");
        }
    }

    public static class ShowShortcutsPreferences extends SignalFragmentResultContract {
        public ShowShortcutsPreferences() {
            super("show_shortcuts_preferences");
        }
    }

    public static class ShowNotificationsPreferences extends SignalFragmentResultContract {
        public ShowNotificationsPreferences() {
            super("show_notifications_preferences");
        }
    }

    public static class ShowLookAndFeelPreferences extends SignalFragmentResultContract {
        public ShowLookAndFeelPreferences() {
            super("show_look_and_feel_preferences");
        }
    }

    public static class ShowMiscPreferences extends SignalFragmentResultContract {
        public ShowMiscPreferences() {
            super("show_misc_preferences");
        }
    }

    public static class ShowWidgetPreferences extends SignalFragmentResultContract {
        public ShowWidgetPreferences() {
            super("show_widget_preferences");
        }
    }

    public static class ShowWallpaperPreferences extends SignalFragmentResultContract {
        public ShowWallpaperPreferences() {
            super("show_wallpaper_preferences");
        }
    }

    public static class ShowBackupPreferences extends SignalFragmentResultContract {
        public ShowBackupPreferences() {
            super("show_backup_preferences");
        }
    }
}
