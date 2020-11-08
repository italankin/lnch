package com.italankin.lnch.feature.common.preferences;

import com.italankin.lnch.model.repository.prefs.Preferences;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;

public class SupportsOrientationDelegate implements LifecycleEventObserver, SupportsOrientation {

    public static void attach(AppCompatActivity activity, Preferences preferences) {
        new SupportsOrientationDelegate(activity, preferences);
    }

    private final AppCompatActivity activity;
    private final ScreenOrientationObservable screenOrientationObservable;

    private Disposable disposable = Disposables.disposed();

    private SupportsOrientationDelegate(AppCompatActivity activity, Preferences preferences) {
        this.activity = activity;
        this.screenOrientationObservable = new ScreenOrientationObservable(preferences);

        activity.getLifecycle().addObserver(this);
    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        switch (event) {
            case ON_CREATE:
                disposable = screenOrientationObservable.subscribe(this);
                break;
            case ON_DESTROY:
                disposable.dispose();
                break;
        }
    }

    @Override
    public void onOrientationChange(Preferences.ScreenOrientation screenOrientation, boolean changed) {
        activity.setRequestedOrientation(screenOrientation.value());
    }
}
