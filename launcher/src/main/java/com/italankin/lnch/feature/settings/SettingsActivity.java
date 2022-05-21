package com.italankin.lnch.feature.settings;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.api.LauncherIntents;
import com.italankin.lnch.feature.base.BackButtonHandler;
import com.italankin.lnch.feature.common.preferences.SupportsOrientationDelegate;
import com.italankin.lnch.feature.home.fragmentresult.FragmentResultManager;
import com.italankin.lnch.feature.settings.apps.AppsSettingsFragment;
import com.italankin.lnch.feature.settings.apps.details.AppDetailsFragment;
import com.italankin.lnch.feature.settings.apps.details.aliases.AppAliasesFragment;
import com.italankin.lnch.feature.settings.backup.BackupFragment;
import com.italankin.lnch.feature.settings.experimental.ExperimentalSettingsFragment;
import com.italankin.lnch.feature.settings.hidden_items.HiddenItemsFragment;
import com.italankin.lnch.feature.settings.lookfeel.AppearanceFragment;
import com.italankin.lnch.feature.settings.lookfeel.LookAndFeelFragment;
import com.italankin.lnch.feature.settings.misc.MiscFragment;
import com.italankin.lnch.feature.settings.notifications.NotificationsFragment;
import com.italankin.lnch.feature.settings.search.SearchFragment;
import com.italankin.lnch.feature.settings.wallpaper.WallpaperFragment;
import com.italankin.lnch.feature.settings.wallpaper.WallpaperOverlayFragment;
import com.italankin.lnch.feature.settings.widgets.WidgetsSettingsFragment;
import com.italankin.lnch.model.repository.prefs.Preferences;

public class SettingsActivity extends AppCompatActivity implements
        WallpaperOverlayFragment.Callbacks,
        LookAndFeelFragment.Callbacks,
        AppsSettingsFragment.Callbacks,
        AppDetailsFragment.Callbacks,
        MiscFragment.Callbacks {

    public static ComponentName getComponentName(Context context) {
        return new ComponentName(context, SettingsActivity.class);
    }

    private static final String REQUEST_KEY_SETTINGS = "settings";

    private Toolbar toolbar;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Preferences preferences = LauncherApp.daggerService.main().preferences();
        SupportsOrientationDelegate.attach(this, preferences);

        super.onCreate(savedInstanceState);

        fragmentManager = getSupportFragmentManager();
        fragmentManager.addOnBackStackChangedListener(this::updateToolbar);

        setContentView(R.layout.activity_settings);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.settings_title);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());


        new FragmentResultManager(getSupportFragmentManager(), this, REQUEST_KEY_SETTINGS)
                .register(new SettingsRootFragment.LaunchEditModeContract(), result -> {
                    finish();
                    startActivity(new Intent(LauncherIntents.ACTION_EDIT_MODE));
                })
                .register(new SettingsRootFragment.ShowSearchPreferencesContract(), result -> {
                    showFragment(new SearchFragment(), R.string.settings_category_search);
                })
                .register(new SettingsRootFragment.ShowAppsSettings(), result -> {
                    showFragment(new AppsSettingsFragment(), R.string.settings_apps_list);
                })
                .register(new SettingsRootFragment.ShowShortcutsPreferences(), result -> {
                    showFragment(new ShortcutsFragment(), R.string.settings_home_misc_shortcuts);
                })
                .register(new SettingsRootFragment.ShowNotificationsPreferences(), result -> {
                    showFragment(new NotificationsFragment(), R.string.settings_home_misc_notifications);
                })
                .register(new SettingsRootFragment.ShowLookAndFeelPreferences(), result -> {
                    showFragment(new LookAndFeelFragment(), R.string.settings_home_laf);
                })
                .register(new SettingsRootFragment.ShowMiscPreferences(), result -> {
                    showFragment(new MiscFragment(), R.string.settings_home_misc);
                })
                .register(new SettingsRootFragment.ShowWidgetPreferences(), result -> {
                    showFragment(new WidgetsSettingsFragment(), R.string.settings_home_widgets);
                })
                .register(new SettingsRootFragment.ShowHiddenItems(), result -> {
                    showFragment(new HiddenItemsFragment(), R.string.settings_home_hidden_items);
                })
                .register(new SettingsRootFragment.ShowWallpaperPreferences(), result -> {
                    showFragment(WallpaperFragment.newInstance(REQUEST_KEY_SETTINGS), R.string.settings_home_wallpaper);
                })
                .register(new SettingsRootFragment.ShowBackupPreferences(), result -> {
                    showFragment(new BackupFragment(), R.string.settings_other_bar);
                })
                .register(new AppearanceFragment.AppearanceFinishedContract(), result -> {
                    fragmentManager.popBackStack();
                })
                .register(new WallpaperFragment.ShowWallpaperOverlay(), result -> {
                    showFragment(new WallpaperOverlayFragment(), R.string.settings_home_wallpaper_overlay_color);
                })
                .attach();

        if (savedInstanceState == null) {
            fragmentManager
                    .beginTransaction()
                    .add(R.id.container, SettingsRootFragment.newInstance(REQUEST_KEY_SETTINGS))
                    .commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateToolbar();
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
    public void showItemLookPreferences() {
        showFragment(AppearanceFragment.newInstance(REQUEST_KEY_SETTINGS), R.string.settings_home_laf_appearance);
    }

    @Override
    public void onWallpaperOverlayFinish() {
        fragmentManager.popBackStack();
    }

    @Override
    public void showAppDetails(String descriptorId) {
        showFragment(AppDetailsFragment.newInstance(descriptorId), R.string.settings_app_details);
    }

    @Override
    public void showAppAliases(String descriptorId) {
        showFragment(AppAliasesFragment.newInstance(descriptorId), R.string.settings_app_aliases);
    }

    @Override
    public void onAppDetailsError() {
        fragmentManager.popBackStack();
    }

    @Override
    public void showExperimentalPreferences() {
        showFragment(new ExperimentalSettingsFragment(), R.string.settings_home_misc_experimental);
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
}
