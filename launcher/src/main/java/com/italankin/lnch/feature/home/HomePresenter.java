package com.italankin.lnch.feature.home;

import android.graphics.Color;

import com.arellomobile.mvp.InjectViewState;
import com.italankin.lnch.feature.base.AppPresenter;
import com.italankin.lnch.model.repository.prefs.Preferences;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

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
                preferences.observe(Preferences.WALLPAPER_OVERLAY_COLOR)
                        .map(Preferences.Value::get)
                        .startWith(preferences.get(Preferences.WALLPAPER_OVERLAY_COLOR)),
                preferences.observe(Preferences.WALLPAPER_OVERLAY_SHOW)
                        .map(Preferences.Value::get)
                        .startWith(preferences.get(Preferences.WALLPAPER_OVERLAY_SHOW)),
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

        preferences.observe(Preferences.STATUS_BAR_COLOR)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new State<Preferences.Value<Integer>>() {
                    @Override
                    protected void onNext(HomeView viewState, Preferences.Value<Integer> statusBarColor) {
                        viewState.onStatusBarColorChanged(statusBarColor.get());
                    }
                });

        Observable.merge(preferences.observe(Preferences.ENABLE_WIDGETS), preferences.observe(Preferences.WIDGETS_POSITION))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new State<Object>() {
                    @Override
                    protected void onNext(HomeView viewState, Object ignored) {
                        viewState.onWidgetPreferencesUpdated();
                    }
                });
    }
}
