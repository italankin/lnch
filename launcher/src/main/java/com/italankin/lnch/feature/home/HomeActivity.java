package com.italankin.lnch.feature.home;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetHost;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.base.AppActivity;
import com.italankin.lnch.feature.base.BackButtonHandler;
import com.italankin.lnch.feature.common.preferences.SupportsOrientationDelegate;
import com.italankin.lnch.feature.home.apps.AppsFragment;
import com.italankin.lnch.feature.home.util.FakeStatusBarDrawable;
import com.italankin.lnch.feature.home.util.HomePagerHost;
import com.italankin.lnch.feature.home.util.IntentQueue;
import com.italankin.lnch.feature.home.util.MainActionHandler;
import com.italankin.lnch.feature.widgets.WidgetsFragment;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.prefs.Preferences.WidgetsPosition;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HomeActivity extends AppActivity implements HomeView, HomePagerHost, WidgetsFragment.Callback {

    @InjectPresenter
    HomePresenter presenter;

    private Preferences preferences;
    private IntentQueue intentQueue;

    private View root;
    private ViewPager2 viewPager;
    private HomePagerAdapter homePagerAdapter;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private boolean onRestartCalled = false;

    @ProvidePresenter
    HomePresenter providePresenter() {
        return LauncherApp.daggerService.presenters().home();
    }

    @Override
    protected void onCreate(@Nullable Bundle state) {
        preferences = LauncherApp.daggerService.main().preferences();
        intentQueue = LauncherApp.daggerService.main().intentQueue();

        SupportsOrientationDelegate.attach(this, preferences);

        super.onCreate(state);

        setupWindow();
        setContentView(R.layout.activity_home);
        root = findViewById(R.id.root);
        viewPager = findViewById(R.id.home_pager);
        setupRoot();
        setupPager();

        intentQueue.post(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isWidgetsEnabled()) {
            int currentItem = viewPager.getCurrentItem();
            if (currentItem == -1 || currentItem == homePagerAdapter.indexOfFragment(AppsFragment.class)) {
                return;
            }
            AppsFragment appsFragment = homePagerAdapter.getAppsFragment();
            if (appsFragment != null) {
                appsFragment.setAnimateOnResume(false);
            }
        }
        onRestartCalled = false;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        AppsFragment appsFragment = homePagerAdapter.getAppsFragment();
        if (appsFragment != null) {
            appsFragment.onRestart();
        }
        onRestartCalled = true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (Intent.ACTION_MAIN.equals(intent.getAction())) {
            Fragment current = homePagerAdapter.getFragmentAt(viewPager.getCurrentItem());
            if (current instanceof MainActionHandler && ((MainActionHandler) current).handle()) {
                return;
            }
            int appsPosition = homePagerAdapter.indexOfFragment(AppsFragment.class);
            if (viewPager.getCurrentItem() != appsPosition) {
                viewPager.setCurrentItem(appsPosition, !onRestartCalled);
                return;
            }
        }
        intentQueue.post(intent);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().popBackStackImmediate()) {
            return;
        }
        int currentItem = viewPager.getCurrentItem();
        Fragment fragment = homePagerAdapter.getFragmentAt(currentItem);
        boolean handled = false;
        if (fragment instanceof BackButtonHandler) {
            handled = ((BackButtonHandler) fragment).onBackPressed();
        }
        if (!handled) {
            int appsPosition = homePagerAdapter.indexOfFragment(AppsFragment.class);
            if (currentItem != appsPosition) {
                viewPager.setCurrentItem(appsPosition, true);
            }
        }
    }

    @Override
    public void startAppWidgetConfigureActivityForResult(AppWidgetHost appWidgetHost, int appWidgetId) {
        appWidgetHost.startAppWidgetConfigureActivityForResult(this, appWidgetId, 0, 0, null);
    }

    @Override
    public void setPagerEnabled(boolean enabled) {
        viewPager.setUserInputEnabled(enabled);
    }

    @Override
    public void onAppsColorOverlayChanged(Integer color) {
        root.setBackgroundColor(color);
    }

    @Override
    public void onStatusBarColorChanged(Integer color) {
        Drawable foreground = root.getForeground();
        if (foreground instanceof FakeStatusBarDrawable) {
            FakeStatusBarDrawable drawable = (FakeStatusBarDrawable) foreground;
            drawable.setColor(color);
        }
    }

    @Override
    public void onWidgetPreferencesUpdated() {
        updateAdapter();
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
            boolean hideStatusBar = preferences.get(Preferences.HIDE_STATUS_BAR);
            int stableInsetTop = hideStatusBar ? 0 : insets.getStableInsetTop();
            root.setPadding(insets.getStableInsetLeft(), stableInsetTop, insets.getStableInsetRight(), 0);
            FakeStatusBarDrawable foreground = new FakeStatusBarDrawable(getColor(R.color.status_bar), stableInsetTop);
            Integer statusBarColor = hideStatusBar ? Color.TRANSPARENT : preferences.get(Preferences.STATUS_BAR_COLOR);
            foreground.setColor(statusBarColor);
            root.setForeground(foreground);

            return insets;
        });
        Disposable disposable = preferences.observe(Preferences.HIDE_STATUS_BAR)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(hideStatusBar -> {
                    int mask = WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER |
                            WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    int flags = WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER;
                    if (hideStatusBar) {
                        flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    }
                    getWindow().setFlags(flags, mask);
                });
        compositeDisposable.add(disposable);
    }

    private void setupPager() {
        homePagerAdapter = new HomePagerAdapter(this);
        updateAdapter();
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                    AppsFragment appsFragment = homePagerAdapter.getAppsFragment();
                    if (appsFragment != null) {
                        appsFragment.dismissPopups();
                    }
                }
            }
        });
    }

    private void updateAdapter() {
        List<Class<? extends Fragment>> pages = getPages();
        homePagerAdapter.setPages(pages);
        viewPager.setAdapter(homePagerAdapter);
        int appsPosition = homePagerAdapter.indexOfFragment(AppsFragment.class);
        viewPager.setCurrentItem(appsPosition, false);
    }

    private void setupRoot() {
        root.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    private List<Class<? extends Fragment>> getPages() {
        if (isWidgetsEnabled()) {
            WidgetsPosition position = preferences.get(Preferences.WIDGETS_POSITION);
            switch (position) {
                case RIGHT:
                    return Arrays.asList(AppsFragment.class, WidgetsFragment.class);
                default:
                case LEFT:
                    return Arrays.asList(WidgetsFragment.class, AppsFragment.class);
            }
        } else {
            return Collections.singletonList(AppsFragment.class);
        }
    }

    private boolean isWidgetsEnabled() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && preferences.get(Preferences.ENABLE_WIDGETS);
    }
}
