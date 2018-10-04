package com.italankin.lnch.feature.settings_root;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.settings_apps.AppsFragment;
import com.italankin.lnch.feature.settings_search.SearchFragment;
import com.italankin.lnch.feature.settings_wallpaper.WallpaperFragment;

import timber.log.Timber;

public class SettingsActivity extends AppCompatActivity implements SettingsRootFragment.Callbacks,
        SharedPreferences.OnSharedPreferenceChangeListener {

    public static final int RESULT_EDIT_MODE = RESULT_FIRST_USER;
    private Toolbar toolbar;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, SettingsActivity.class);
    }

    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(this);

        fragmentManager = getSupportFragmentManager();
        fragmentManager.addOnBackStackChangedListener(this::updateToolbar);

        setContentView(R.layout.activity_settings);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        if (savedInstanceState == null) {
            fragmentManager
                    .beginTransaction()
                    .add(R.id.container, new SettingsRootFragment())
                    .commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateToolbar();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void launchEditMode() {
        setResult(RESULT_EDIT_MODE);
        finish();
    }

    @Override
    public void showSearchPreferences() {
        showFragment(new SearchFragment(), R.string.title_settings_search_behavior);
    }

    @Override
    public void showAppsVisibilityPreferences() {
        showFragment(new AppsFragment(), R.string.title_settings_apps);
    }

    @Override
    public void showWallpaperPreferences() {
        showFragment(new WallpaperFragment(), R.string.title_settings_wallpaper);
    }

    private void showFragment(Fragment fragment, @StringRes int title) {
        fragmentManager
                .beginTransaction()
                .setCustomAnimations(R.animator.fragment_in, R.animator.fragment_out,
                        R.animator.fragment_in, R.animator.fragment_out)
                .replace(R.id.container, fragment)
                .setBreadCrumbTitle(title)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Timber.d("onSharedPreferenceChanged: key=%s", key);
    }

    private void updateToolbar() {
        toolbar.setTitle(getFragmentTitle());
        toolbar.setNavigationIcon(fragmentManager.getBackStackEntryCount() > 0
                ? R.drawable.ic_arrow_back
                : R.drawable.ic_close);
    }

    private CharSequence getFragmentTitle() {
        int index = fragmentManager.getBackStackEntryCount() - 1;
        return index < 0
                ? getString(R.string.title_settings)
                : fragmentManager.getBackStackEntryAt(index).getBreadCrumbTitle();
    }
}
