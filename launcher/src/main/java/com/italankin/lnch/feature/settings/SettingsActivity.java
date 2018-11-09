package com.italankin.lnch.feature.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.home.HomeActivity;
import com.italankin.lnch.feature.settings.apps.AppsFragment;
import com.italankin.lnch.feature.settings.base.SimplePreferencesFragment;
import com.italankin.lnch.feature.settings.itemlook.ItemLookFragment;
import com.italankin.lnch.feature.settings.wallpaper.WallpaperFragment;
import com.italankin.lnch.feature.settings.wallpaper.WallpaperOverlayFragment;

public class SettingsActivity extends AppCompatActivity implements
        SettingsRootFragment.Callbacks,
        ItemLookFragment.Callbacks,
        WallpaperFragment.Callbacks,
        WallpaperOverlayFragment.Callbacks {

    public static Intent getStartIntent(Context context) {
        return new Intent(context, SettingsActivity.class);
    }

    private Toolbar toolbar;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fragmentManager = getSupportFragmentManager();
        fragmentManager.addOnBackStackChangedListener(this::updateToolbar);

        setContentView(R.layout.activity_settings);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title_settings);
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
    public void launchEditMode() {
        finish();
        startActivity(new Intent(HomeActivity.ACTION_EDIT_MODE));
    }

    @Override
    public void showSearchPreferences() {
        showFragment(SimplePreferencesFragment.newInstance(R.xml.prefs_search), R.string.title_settings_search);
    }

    @Override
    public void showAppsPreferences() {
        showFragment(new AppsFragment(), R.string.title_settings_apps_list);
    }

    @Override
    public void showItemLookPreferences() {
        showFragment(new ItemLookFragment(), R.string.title_settings_home_item_look);
    }

    @Override
    public void showMiscPreferences() {
        showFragment(SimplePreferencesFragment.newInstance(R.xml.prefs_misc), R.string.title_settings_home_misc);
    }

    @Override
    public void showWallpaperPreferences() {
        showFragment(new WallpaperFragment(), R.string.title_settings_wallpaper);
    }

    @Override
    public void onItemLookFinish() {
        fragmentManager.popBackStack();
    }

    @Override
    public void showWallpaperOverlayPreferences() {
        showFragment(new WallpaperOverlayFragment(), R.string.title_settings_wallpaper_overlay_color);
    }

    @Override
    public void onWallpaperOverlayFinish() {
        fragmentManager.popBackStack();
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
