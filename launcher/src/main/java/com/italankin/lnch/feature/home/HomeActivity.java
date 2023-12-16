package com.italankin.lnch.feature.home;

import android.appwidget.AppWidgetHost;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.util.Pair;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.common.preferences.SupportsOrientationDelegate;
import com.italankin.lnch.feature.home.apps.AppsFragment;
import com.italankin.lnch.feature.home.apps.events.SearchStateEvent;
import com.italankin.lnch.feature.home.repository.EditModeState;
import com.italankin.lnch.feature.home.repository.HomeBus;
import com.italankin.lnch.feature.home.util.FakeStatusBarDrawable;
import com.italankin.lnch.feature.home.util.IntentQueue;
import com.italankin.lnch.feature.home.util.MainActionHandler;
import com.italankin.lnch.feature.widgets.WidgetsFragment;
import com.italankin.lnch.feature.widgets.events.WidgetEditModeChangeEvent;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.prefs.Preferences.WidgetsPosition;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

import java.util.*;

public class HomeActivity extends AppCompatActivity implements WidgetsFragment.Callback, HomeBus.EventListener,
        EditModeState.Callback {

    private Preferences preferences;
    private IntentQueue intentQueue;

    private View root;
    private ViewPager2 viewPager;
    private HomePagerAdapter homePagerAdapter;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final HomeScreenState homeScreenState = new HomeScreenState();
    private boolean onRestartCalled = false;

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
        setupWidgets();

        intentQueue.post(getIntent());

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                int appsPosition = homePagerAdapter.indexOfFragment(AppsFragment.class);
                if (viewPager.getCurrentItem() != appsPosition) {
                    viewPager.setCurrentItem(appsPosition, true);
                }
            }
        });

        HomeBus homeBus = LauncherApp.daggerService.main().homeBus();
        homeBus.subscribe(this, this);
        EditModeState editModeState = LauncherApp.daggerService.main().editModeState();
        editModeState.addCallback(this, this);

        homeScreenState.editMode = editModeState.isActive();
        syncHomeState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isWidgetsEnabled()) {
            int currentItem = viewPager.getCurrentItem();
            if (currentItem == -1 || currentItem == homePagerAdapter.indexOfFragment(AppsFragment.class)) {
                return;
            }
            AppsFragment appsFragment = getAppsFragment();
            if (appsFragment != null) {
                appsFragment.setAnimateOnResume(false);
            }
        }
        onRestartCalled = false;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        AppsFragment appsFragment = getAppsFragment();
        if (appsFragment != null) {
            appsFragment.onRestart();
        }
        onRestartCalled = true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (Intent.ACTION_MAIN.equals(intent.getAction())) {
            Fragment current = getCurrentPagerFragment();
            if (current instanceof MainActionHandler && ((MainActionHandler) current).handleMainAction()) {
                return;
            }
            int appsPosition = homePagerAdapter.indexOfFragment(AppsFragment.class);
            if (viewPager.getCurrentItem() != appsPosition) {
                viewPager.setCurrentItem(appsPosition, !onRestartCalled);
                onRestartCalled = false;
                return;
            } else {
                onRestartCalled = false;
            }
        }
        intentQueue.post(intent);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().popBackStackImmediate()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void startAppWidgetConfigureActivity(AppWidgetHost appWidgetHost, int appWidgetId) {
        appWidgetHost.startAppWidgetConfigureActivityForResult(this, appWidgetId, 0, 0, null);
    }

    @Override
    public void onHomeEvent(HomeBus bus, HomeBus.Event event) {
        if (event instanceof WidgetEditModeChangeEvent) {
            homeScreenState.widgetsEditMode = event == WidgetEditModeChangeEvent.ENTER;
        } else if (event instanceof SearchStateEvent) {
            homeScreenState.searchVisible = event == SearchStateEvent.SHOWN;
        }
        syncHomeState();
    }

    @Override
    public void onEditModeActivate() {
        homeScreenState.editMode = true;
        syncHomeState();
    }

    @Override
    public void onEditModeDiscard() {
        homeScreenState.editMode = false;
        resetFromUserPreferences();
        syncHomeState();
    }

    @Override
    public void onEditModeCommit() {
        homeScreenState.editMode = false;
        syncHomeState();
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
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
        window.getDecorView().setOnApplyWindowInsetsListener((v, systemInsets) -> {
            boolean hideStatusBar = preferences.get(Preferences.HIDE_STATUS_BAR);
            Insets insets = WindowInsetsCompat.toWindowInsetsCompat(systemInsets)
                    .getInsetsIgnoringVisibility(WindowInsetsCompat.Type.systemBars());
            int topInsets = insets.top;
            root.setPadding(insets.left, topInsets, insets.right, 0);
            FakeStatusBarDrawable foreground = new FakeStatusBarDrawable(getColor(R.color.status_bar), topInsets);
            Integer statusBarColor = hideStatusBar ? Color.TRANSPARENT : preferences.get(Preferences.STATUS_BAR_COLOR);
            foreground.setColor(statusBarColor);
            root.setForeground(foreground);
            return systemInsets;
        });

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(getWindow(), root);
        insetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

        Disposable statusBarDisposable = Observable.combineLatest(
                        preferences.observeValue(Preferences.STATUS_BAR_COLOR, true),
                        preferences.observe(Preferences.HIDE_STATUS_BAR, true),
                        Pair::new
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    boolean hideStatusBar = result.second;
                    if (hideStatusBar) {
                        insetsController.hide(WindowInsetsCompat.Type.statusBars());
                        setStatusBarColor(Color.TRANSPARENT);
                    } else {
                        insetsController.show(WindowInsetsCompat.Type.statusBars());
                        setStatusBarColor(result.first.get());
                    }
                });
        compositeDisposable.add(statusBarDisposable);
    }

    private void setupPager() {
        homePagerAdapter = new HomePagerAdapter(this);
        updateAdapter();
        viewPager.setSaveEnabled(false);
        viewPager.setOffscreenPageLimit(1);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                    AppsFragment appsFragment = getAppsFragment();
                    if (appsFragment != null) {
                        appsFragment.dismissPopups();
                    }
                }
            }
        });
    }

    private void setupWidgets() {
        Set<Preferences.Pref<?>> widgetPrefs = new HashSet<>(Arrays.asList(
                Preferences.ENABLE_WIDGETS,
                Preferences.WIDGETS_POSITION,
                Preferences.WIDGETS_HORIZONTAL_GRID_SIZE,
                Preferences.WIDGETS_HEIGHT_CELL_RATIO
        ));
        Disposable disposable = preferences.observe()
                .filter(prefs -> {
                    for (Preferences.Pref<?> pref : prefs) {
                        if (widgetPrefs.contains(pref)) return true;
                    }
                    return false;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pref -> recreate());
        compositeDisposable.add(disposable);
    }

    private void updateAdapter() {
        List<Class<? extends Fragment>> pages = getPages();
        homePagerAdapter.setPages(pages);
        viewPager.setAdapter(homePagerAdapter);
        int appsPosition = homePagerAdapter.indexOfFragment(AppsFragment.class);
        viewPager.setCurrentItem(appsPosition, false);
    }

    private void setupRoot() {
        Disposable disposable = Observable.combineLatest(
                        preferences.observe(Preferences.WALLPAPER_OVERLAY_COLOR, true),
                        preferences.observe(Preferences.WALLPAPER_OVERLAY_SHOW, true),
                        (overlayColor, showOverlay) -> {
                            return showOverlay ? overlayColor : Color.TRANSPARENT;
                        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(overlayColor -> root.setBackgroundColor(overlayColor));
        compositeDisposable.add(disposable);
    }

    private void setStatusBarColor(Integer color) {
        Drawable foreground = root.getForeground();
        if (foreground instanceof FakeStatusBarDrawable) {
            FakeStatusBarDrawable drawable = (FakeStatusBarDrawable) foreground;
            drawable.setColor(color);
        }
    }

    private AppsFragment getAppsFragment() {
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof AppsFragment) {
                return (AppsFragment) fragment;
            }
        }
        return null;
    }

    private Fragment getCurrentPagerFragment() {
        Class<? extends Fragment> fragmentClass = homePagerAdapter.getFragmentAt(viewPager.getCurrentItem());
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragmentClass.isAssignableFrom(fragment.getClass())) {
                return fragment;
            }
        }
        return null;
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

    private void syncHomeState() {
        viewPager.setUserInputEnabled(homeScreenState.pagerUserInoutEnabled());
    }

    private void resetFromUserPreferences() {
        if (preferences.get(Preferences.WALLPAPER_OVERLAY_SHOW)) {
            root.setBackgroundColor(preferences.get(Preferences.WALLPAPER_OVERLAY_COLOR));
        } else {
            root.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private static class HomeScreenState {
        boolean searchVisible;
        boolean editMode;
        boolean widgetsEditMode;

        boolean pagerUserInoutEnabled() {
            return !(editMode || widgetsEditMode || searchVisible);
        }
    }
}
