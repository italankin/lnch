package com.italankin.lnch.ui.feature.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;

import com.italankin.lnch.R;
import com.italankin.lnch.ui.feature.home.HomeActivity;

public class SettingsActivity extends AppCompatActivity implements SettingsRootFragment.Callbacks {

    public static final int RESULT_CHANGED = RESULT_FIRST_USER;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, SettingsActivity.class);
    }

    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setResult(RESULT_CANCELED);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener((sharedPreferences, key) -> {
            setResult(RESULT_CHANGED);
        });

        fragmentManager = getSupportFragmentManager();
        fragmentManager.addOnBackStackChangedListener(() -> {
            ActionBar actionBar = getSupportActionBar();
            assert actionBar != null;
            actionBar.setTitle(getFragmentTitle());
            actionBar.setDisplayHomeAsUpEnabled(fragmentManager.getBackStackEntryCount() > 0);
        });

        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        //noinspection ConstantConditions
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);

        if (savedInstanceState == null) {
            fragmentManager
                    .beginTransaction()
                    .add(R.id.container, new SettingsRootFragment())
                    .commit();
        }
    }

    private CharSequence getFragmentTitle() {
        int index = fragmentManager.getBackStackEntryCount() - 1;
        if (index < 0) {
            return getString(R.string.title_settings);
        }
        return fragmentManager.getBackStackEntryAt(index).getBreadCrumbTitle();
    }

    @Override
    public void launchEditMode() {
        Intent intent = HomeActivity.getEnterEditModeIntent(this);
        startActivity(intent);
    }

    @Override
    public void showSearchPreferences() {
        showFragment(new SearchFragment(), R.string.title_settings_search);
    }

    @Override
    public void showAppsVisibilityPreferences() {
        showFragment(new AppsVisibilityFragment(), R.string.title_settings_apps_visibility);
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
}
