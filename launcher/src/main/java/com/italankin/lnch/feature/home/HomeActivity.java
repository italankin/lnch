package com.italankin.lnch.feature.home;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.italankin.lnch.BuildConfig;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import ru.tinkoff.scrollingpagerindicator.ScrollingPagerIndicator;

public class HomeActivity extends AppActivity implements HomeView {

    @InjectPresenter
    HomePresenter presenter;

    private Preferences preferences;
    private IntentQueue intentQueue;

    private View root;
    private ViewPager pager;
    private ScrollingPagerIndicator pagerIndicator;
    private HomePagerAdapter pagerAdapter;

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

        if (BuildConfig.SAMSUNG_ANR_FIX) {
            samsungAnrFix();
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
    public void onBackPressed() {
        int currentItem = pager.getCurrentItem();
        Fragment fragment = pagerAdapter.getFragmentAt(currentItem);
        boolean handled = false;
        if (fragment instanceof BackButtonHandler) {
            handled = ((BackButtonHandler) fragment).onBackPressed();
        }
        if (!handled) {
            if (getSupportFragmentManager().popBackStackImmediate()) {
                return;
            }
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

    ///////////////////////////////////////////////////////////////////////////
    // Setup
    ///////////////////////////////////////////////////////////////////////////

    private void setupWindow() {
        Window window = getWindow();
        window.getDecorView().setOnApplyWindowInsetsListener((v, insets) -> {
            int stableInsetTop = insets.getStableInsetTop();
            root.setPadding(insets.getStableInsetLeft(), stableInsetTop, insets.getStableInsetRight(), 0);
            FakeStatusBarDrawable foreground = new FakeStatusBarDrawable(getColor(R.color.status_bar), stableInsetTop);
            Integer statusBarColor = preferences.get(Preferences.STATUS_BAR_COLOR);
            foreground.setColor(statusBarColor);
            root.setForeground(foreground);

            MarginLayoutParams layoutParams = (MarginLayoutParams) pagerIndicator.getLayoutParams();
            layoutParams.bottomMargin = insets.getStableInsetBottom() + ResUtils.px2dp(this, 16);
            pagerIndicator.requestLayout();
            pagerIndicator.invalidate();

            return insets;
        });
        window.setFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER,
                WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
    }

    private void setupPager() {
        pagerAdapter = new HomePagerAdapter(getSupportFragmentManager());
        updateAdapter();
        pager.addOnPageChangeListener(new PagerIndicatorAnimator(pagerIndicator));
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && preferences.get(Preferences.ENABLE_WIDGETS)) {
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

    private void samsungAnrFix() {
        class SamsungRunnable implements Runnable {
            private static final long MAX_HANDLING_TIME = 1000L;
            private final long start = System.currentTimeMillis();

            @Override
            public void run() {
                long now = System.currentTimeMillis();
                if (now - start > MAX_HANDLING_TIME) {
                    Intent restartIntent = new Intent(HomeActivity.this, HomeActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(restartIntent);
                    Toast.makeText(HomeActivity.this, "Restarting", Toast.LENGTH_LONG).show();
                    System.exit(0);
                }
            }
        }
        new Handler().post(new SamsungRunnable());
    }
}
