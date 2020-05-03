package com.italankin.lnch.feature.settings;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.base.BackButtonHandler;
import com.italankin.lnch.feature.common.preferences.ScreenOrientationObservable;
import com.italankin.lnch.feature.common.preferences.SupportsOrientation;
import com.italankin.lnch.feature.common.preferences.ThemeObservable;
import com.italankin.lnch.feature.common.preferences.ThemedActivity;
import com.italankin.lnch.feature.home.HomeActivity;
import com.italankin.lnch.feature.settings.apps.list.AppsListFragment;
import com.italankin.lnch.feature.settings.backup.BackupFragment;
import com.italankin.lnch.feature.settings.base.SimplePreferencesFragment;
import com.italankin.lnch.feature.settings.lookfeel.ItemAppearanceFragment;
import com.italankin.lnch.feature.settings.lookfeel.LookAndFeelFragment;
import com.italankin.lnch.feature.settings.search.SearchFragment;
import com.italankin.lnch.feature.settings.wallpaper.WallpaperFragment;
import com.italankin.lnch.feature.settings.wallpaper.WallpaperOverlayFragment;
import com.italankin.lnch.model.repository.prefs.Preferences;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class SettingsActivity extends AppCompatActivity implements
        ThemedActivity, SupportsOrientation,
        SettingsRootFragment.Callbacks,
        ItemAppearanceFragment.Callbacks,
        WallpaperFragment.Callbacks,
        WallpaperOverlayFragment.Callbacks,
        LookAndFeelFragment.Callbacks {

    public static ComponentName getComponentName(Context context) {
        return new ComponentName(context, SettingsActivity.class);
    }

    private Toolbar toolbar;
    private FragmentManager fragmentManager;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme();
        super.onCreate(savedInstanceState);
        setScreenOrientation();

        fragmentManager = getSupportFragmentManager();
        fragmentManager.addOnBackStackChangedListener(this::updateToolbar);

        setContentView(R.layout.activity_settings);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.settings_title);
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
        compositeDisposable.dispose();
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = fragmentManager.findFragmentById(R.id.container);
        if (fragment instanceof BackButtonHandler && !((BackButtonHandler) fragment).onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onThemeChanged(Preferences.ColorTheme colorTheme, boolean changed) {
        switch (colorTheme) {
            case DARK:
                setTheme(R.style.AppTheme_Dark_Preferences);
                break;
            case LIGHT:
                setTheme(R.style.AppTheme_Light_Preferences);
                break;
        }
        if (changed) {
            recreate();
        }
    }

    @Override
    public void onOrientationChange(Preferences.ScreenOrientation screenOrientation, boolean changed) {
        setRequestedOrientation(screenOrientation.value());
    }

    @Override
    public void launchEditMode() {
        finish();
        startActivity(new Intent(HomeActivity.ACTION_EDIT_MODE));
    }

    @Override
    public void showSearchPreferences() {
        showFragment(new SearchFragment(), R.string.settings_category_search);
    }

    @Override
    public void showAppsPreferences() {
        showFragment(new AppsListFragment(), R.string.settings_apps_list);
    }

    @Override
    public void showItemLookPreferences() {
        showFragment(new ItemAppearanceFragment(), R.string.settings_home_laf_appearance);
    }

    @Override
    public void showLookAndFeelPreferences() {
        showFragment(new LookAndFeelFragment(), R.string.settings_home_laf);
    }

    @Override
    public void showMiscPreferences() {
        showFragment(SimplePreferencesFragment.newInstance(R.xml.prefs_misc), R.string.settings_home_misc);
    }

    @Override
    public void showWallpaperPreferences() {
        showFragment(new WallpaperFragment(), R.string.settings_home_wallpaper);
    }

    @Override
    public void showBackupPreferences() {
        showFragment(new BackupFragment(), R.string.settings_other_bar);
    }

    @Override
    public void onItemAppearanceFinish() {
        fragmentManager.popBackStack();
    }

    @Override
    public void showWallpaperOverlayPreferences() {
        showFragment(new WallpaperOverlayFragment(), R.string.settings_home_wallpaper_overlay_color);
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
                ? getString(R.string.settings_title)
                : fragmentManager.getBackStackEntryAt(index).getBreadCrumbTitle();
    }

    private void showFragment(Fragment fragment, @StringRes int title) {
        fragmentManager
                .beginTransaction()
                .setCustomAnimations(R.animator.fragment_in, R.animator.fragment_out,
                        R.animator.fragment_bs_in, R.animator.fragment_bs_out)
                .replace(R.id.container, fragment)
                .setBreadCrumbTitle(title)
                .addToBackStack(null)
                .commit();
    }

    private void setScreenOrientation() {
        Preferences preferences = LauncherApp.daggerService.main().getPreferences();
        Disposable disposable = new ScreenOrientationObservable(preferences).subscribe(this);
        compositeDisposable.add(disposable);
    }

    private void setTheme() {
        Preferences preferences = LauncherApp.daggerService.main().getPreferences();
        Disposable disposable = new ThemeObservable(preferences).subscribe(this);
        compositeDisposable.add(disposable);
    }

}
