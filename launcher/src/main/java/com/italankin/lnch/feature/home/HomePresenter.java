package com.italankin.lnch.feature.home;

import android.graphics.Color;
import androidx.core.util.Pair;
import com.arellomobile.mvp.InjectViewState;
import com.italankin.lnch.feature.base.AppPresenter;
import com.italankin.lnch.model.repository.prefs.Preferences;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

import javax.inject.Inject;

@InjectViewState
public class HomePresenter extends AppPresenter<HomeView> {

    private final Preferences preferences;

    @Inject
    HomePresenter(Preferences preferences) {
        this.preferences = preferences;
    }

    @Override
    protected void onFirstViewAttach() {
        Observable.combineLatest(
                        preferences.observe(Preferences.WALLPAPER_OVERLAY_COLOR),
                        preferences.observe(Preferences.WALLPAPER_OVERLAY_SHOW),
                        (overlayColor, showOverlay) -> {
                            return showOverlay ? overlayColor : Color.TRANSPARENT;
                        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new State<Integer>() {
                    @Override
                    protected void onNext(HomeView viewState, Integer appsColorOverlay) {
                        viewState.onAppsColorOverlayChanged(appsColorOverlay);
                    }
                });

        Observable.combineLatest(
                        preferences.observeValue(Preferences.STATUS_BAR_COLOR),
                        preferences.observe(Preferences.HIDE_STATUS_BAR),
                        Pair::new
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new State<Pair<Preferences.Value<Integer>, Boolean>>() {
                    @Override
                    protected void onNext(HomeView viewState, Pair<Preferences.Value<Integer>, Boolean> result) {
                        boolean hideStatusBar = result.second;
                        if (hideStatusBar) {
                            viewState.onStatusBarColorChanged(Color.TRANSPARENT);
                        } else {
                            Preferences.Value<Integer> statusBarColor = result.first;
                            viewState.onStatusBarColorChanged(statusBarColor.get());
                        }
                    }
                });

        preferences.observe()
                .filter(pref -> {
                    return pref == Preferences.ENABLE_WIDGETS ||
                            pref == Preferences.WIDGETS_POSITION ||
                            pref == Preferences.WIDGETS_HORIZONTAL_GRID_SIZE ||
                            pref == Preferences.WIDGETS_HEIGHT_CELL_RATIO;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new State<Object>() {
                    @Override
                    protected void onNext(HomeView viewState, Object ignored) {
                        viewState.onWidgetPreferencesUpdated();
                    }
                });
    }
}
