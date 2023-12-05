package com.italankin.lnch.feature.home;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.WindowManager;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.base.AppActivity;
import com.italankin.lnch.feature.base.BackButtonHandler;
import com.italankin.lnch.feature.common.preferences.SupportsOrientationDelegate;
import com.italankin.lnch.feature.home.apps.AppsFragment;
import com.italankin.lnch.feature.home.util.FakeStatusBarDrawable;
import com.italankin.lnch.feature.home.util.IntentQueue;
import com.italankin.lnch.feature.home.util.PagerIndicatorAnimator;
import com.italankin.lnch.feature.widgets.WidgetsFragment;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.prefs.Preferences.WidgetsPosition;
import com.italankin.lnch.util.ResUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import ru.tinkoff.scrollingpagerindicator.ScrollingPagerIndicator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HomeActivity extends AppActivity implements HomeView {

    @InjectPresenter
    HomePresenter presenter;

    private Preferences preferences;
    private IntentQueue intentQueue;

    private View root;
    private ViewPager pager;
    private ScrollingPagerIndicator pagerIndicator;
    private HomePagerAdapter pagerAdapter;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

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
        pager = findViewById(R.id.pager);
        pagerIndicator = findViewById(R.id.pager_indicator);
        setupRoot();
        setupPager();

        intentQueue.post(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isWidgetsEnabled()) {
            int currentItem = pager.getCurrentItem();
            if (currentItem == -1 || currentItem == pagerAdapter.indexOfFragment(AppsFragment.class)) {
                return;
            }
            AppsFragment appsFragment = pagerAdapter.getAppsFragment();
            if (appsFragment != null) {
                appsFragment.setAnimateOnResume(false);
            }
        }
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
        if (Intent.ACTION_MAIN.equals(intent.getAction())) {
            int appsPosition = pagerAdapter.indexOfFragment(AppsFragment.class);
            if (pager.getCurrentItem() != appsPosition) {
                pager.setCurrentItem(appsPosition, true);
                return;
            }
        }
        intentQueue.post(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && WidgetsFragment.isWidgetRequestCode(requestCode)) {
            int index = pagerAdapter.indexOfFragment(WidgetsFragment.class);
            if (index >= 0) {
                ((WidgetsFragment) pagerAdapter.getFragmentAt(index)).onActivityResult(requestCode, resultCode, data);
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().popBackStackImmediate()) {
            return;
        }
        int currentItem = pager.getCurrentItem();
        Fragment fragment = pagerAdapter.getFragmentAt(currentItem);
        boolean handled = false;
        if (fragment instanceof BackButtonHandler) {
            handled = ((BackButtonHandler) fragment).onBackPressed();
        }
        if (!handled) {
            int appsPosition = pagerAdapter.indexOfFragment(AppsFragment.class);
            if (currentItem != appsPosition) {
                pager.setCurrentItem(appsPosition, true);
            }
        }
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
    public void onHomePagerIndicatorVisibilityChanged(boolean visible) {
        updatePagerIndicatorState(visible);
    }

    private void updatePagerIndicatorState() {
        updatePagerIndicatorState(preferences.get(Preferences.HOME_PAGER_INDICATOR));
    }

    private void updatePagerIndicatorState(boolean visible) {
        boolean pages = pagerAdapter.getCount() > 1;
        if (visible && pages) {
            pagerIndicator.setVisibility(View.VISIBLE);
            pagerIndicator.setAlpha(0);
            pagerIndicator.attachToPager(pager);
        } else {
            pagerIndicator.detachFromPager();
            pagerIndicator.setVisibility(View.GONE);
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

            MarginLayoutParams layoutParams = (MarginLayoutParams) pagerIndicator.getLayoutParams();
            layoutParams.bottomMargin = insets.getStableInsetBottom() + ResUtils.px2dp(this, 22);
            pagerIndicator.requestLayout();
            pagerIndicator.invalidate();

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
        pagerAdapter = new HomePagerAdapter(getSupportFragmentManager());
        updateAdapter();
        pager.addOnPageChangeListener(new PagerIndicatorAnimator(pagerIndicator));
        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                    AppsFragment appsFragment = pagerAdapter.getAppsFragment();
                    if (appsFragment != null) {
                        appsFragment.dismissPopups();
                    }
                }
            }
        });
    }

    private void updateAdapter() {
        if (pagerIndicator.getVisibility() == View.VISIBLE) {
            // detach pager indicator, because it does not handle adapter changes correctly
            pagerIndicator.detachFromPager();
        }
        List<Class<? extends Fragment>> pages = getPages();
        pagerAdapter.setPages(pages);
        pager.setAdapter(pagerAdapter);
        int appsPosition = pagerAdapter.indexOfFragment(AppsFragment.class);
        pager.setCurrentItem(appsPosition, false);
        updatePagerIndicatorState();
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
