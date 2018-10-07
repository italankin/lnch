package com.italankin.lnch.feature.settings_root;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.settings_item.ItemLookActivity;
import com.italankin.lnch.util.IntentUtils;

public class SettingsRootFragment extends PreferenceFragmentCompat {
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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findPreference(R.string.key_home_customize).setOnPreferenceClickListener(preference -> {
            if (callbacks != null) {
                callbacks.launchEditMode();
            }
            return true;
        });
        findPreference(R.string.key_search_behavior).setOnPreferenceClickListener(preference -> {
            if (callbacks != null) {
                callbacks.showSearchPreferences();
            }
            return true;
        });
        findPreference(R.string.key_wallpaper).setOnPreferenceClickListener(preference -> {
            if (callbacks != null) {
                callbacks.showWallpaperPreferences();
            }
            return true;
        });
        findPreference(R.string.key_apps_list).setOnPreferenceClickListener(preference -> {
            if (callbacks != null) {
                callbacks.showAppsPreferences();
            }
            return true;
        });
        findPreference(R.string.key_home_item_look).setOnPreferenceClickListener(preference -> {
            Intent intent = ItemLookActivity.getStartIntent(getContext());
            startActivity(intent);
            return true;
        });
        findPreference(R.string.key_home_misc).setOnPreferenceClickListener(preference -> {
            if (callbacks != null) {
                callbacks.showMiscPreferences();
            }
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
            Context context = getContext();
            Intent intent = IntentUtils.getPackageSystemSettings(context.getPackageName());
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Preference findPreference(@StringRes int key) {
        return findPreference(getString(key));
    }

    public interface Callbacks {
        void launchEditMode();

        void showSearchPreferences();

        void showAppsPreferences();

        void showMiscPreferences();

        void showWallpaperPreferences();
    }
}
