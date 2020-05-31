package com.italankin.lnch.feature.home;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.base.BackButtonHandler;
import com.italankin.lnch.feature.common.preferences.ScreenOrientationObservable;
import com.italankin.lnch.feature.common.preferences.SupportsOrientation;
import com.italankin.lnch.feature.common.preferences.ThemeObservable;
import com.italankin.lnch.feature.common.preferences.ThemedActivity;
import com.italankin.lnch.feature.home.apps.AppsFragment;
import com.italankin.lnch.feature.home.util.FakeStatusBarDrawable;
import com.italankin.lnch.feature.home.util.IntentQueue;
import com.italankin.lnch.model.repository.prefs.Preferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class HomeActivity extends AppCompatActivity implements SupportsOrientation, ThemedActivity,
        AppsFragment.Callbacks {

    public static final String STATE_CURRENT_PAGER_ITEM = "current_pager_item";

    private Preferences preferences;
    private IntentQueue intentQueue;

    private View root;
    private ViewPager pager;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private HomePagerAdapter pagerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle state) {
        preferences = LauncherApp.daggerService.main().getPreferences();
        intentQueue = LauncherApp.daggerService.main().getIntentQueue();

        setTheme();
        setScreenOrientation();

        super.onCreate(state);

        setupWindow();
        setContentView(R.layout.activity_home);
        root = findViewById(R.id.root);
        pager = findViewById(R.id.pager);
        setupRoot();
        observePreferences();

        setupPager(state);

        intentQueue.post(getIntent());
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_CURRENT_PAGER_ITEM, pager.getCurrentItem());
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        AppsFragment appsFragment = pagerAdapter.getAppsFragment();
        if (appsFragment != null) {
            appsFragment.onRestart();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        intentQueue.post(intent);
    }

    @Override
    public void onBackPressed() {
        int currentItem = pager.getCurrentItem();
        Fragment fragment = pagerAdapter.getFragmentAt(currentItem);
        boolean handled = false;
        if (fragment instanceof BackButtonHandler) {
            handled = ((BackButtonHandler) fragment).onBackPressed();
        }
        if (!handled && currentItem != HomePagerAdapter.POSITION_APPS) {
            pager.setCurrentItem(HomePagerAdapter.POSITION_APPS, true);
        }
    }

    @Override
    public void onOrientationChange(Preferences.ScreenOrientation screenOrientation, boolean changed) {
        setRequestedOrientation(screenOrientation.value());
    }

    @Override
    public void onThemeChanged(Preferences.ColorTheme colorTheme, boolean changed) {
        switch (colorTheme) {
            case DARK:
                setTheme(R.style.AppTheme_Dark_Launcher);
                break;
            case LIGHT:
                setTheme(R.style.AppTheme_Light_Launcher);
                break;
        }
        if (changed) {
            recreate();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Setup
    ///////////////////////////////////////////////////////////////////////////

    private void setupWindow() {
        Window window = getWindow();
        window.getDecorView().setOnApplyWindowInsetsListener((v, insets) -> {
            int stableInsetTop = insets.getStableInsetTop();
            root.setPadding(insets.getStableInsetLeft(), stableInsetTop,
                    insets.getStableInsetRight(), 0);
            FakeStatusBarDrawable foreground = new FakeStatusBarDrawable(getColor(R.color.status_bar), stableInsetTop);
            Integer statusBarColor = preferences.get(Preferences.STATUS_BAR_COLOR);
            foreground.setColor(statusBarColor);
            root.setForeground(foreground);
            return insets;
        });
        window.setFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER,
                WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
    }

    private void setupPager(@Nullable Bundle state) {
        pagerAdapter = new HomePagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        pager.setCurrentItem(state != null
                ? state.getInt(STATE_CURRENT_PAGER_ITEM)
                : HomePagerAdapter.POSITION_APPS);
    }

    private void setupRoot() {
        root.setBackgroundColor(preferences.get(Preferences.WALLPAPER_OVERLAY_COLOR));
        root.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    private void setScreenOrientation() {
        Disposable disposable = new ScreenOrientationObservable(preferences).subscribe(this);
        compositeDisposable.add(disposable);
    }

    private void setTheme() {
        Disposable disposable = new ThemeObservable(preferences).subscribe(this);
        compositeDisposable.add(disposable);
    }

    private void observePreferences() {
        Disposable overlayColor = preferences.observe(Preferences.APPS_COLOR_OVERLAY)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(value -> {
                    Integer color = value.get();
                    if (color != null) {
                        root.setBackgroundColor(color);
                    }
                });
        compositeDisposable.add(overlayColor);
        Disposable statusBarColor = preferences.observe(Preferences.STATUS_BAR_COLOR)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(value -> {
                    Drawable foreground = root.getForeground();
                    if (foreground instanceof FakeStatusBarDrawable) {
                        FakeStatusBarDrawable drawable = (FakeStatusBarDrawable) foreground;
                        drawable.setColor(value.get());
                    }
                });
        compositeDisposable.add(statusBarColor);
    }
}
