package com.italankin.lnch.feature.common.preferences;

import com.italankin.lnch.R;
import com.italankin.lnch.model.repository.prefs.Preferences;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;

public class ThemedActivityDelegate implements LifecycleEventObserver, ThemedActivity {

    public static void attach(AppCompatActivity activity, Preferences preferences) {
        new ThemedActivityDelegate(activity, preferences);
    }

    private final AppCompatActivity activity;
    private final ThemeObservable themeObservable;

    private Disposable disposable = Disposables.disposed();

    private ThemedActivityDelegate(AppCompatActivity activity, Preferences preferences) {
        this.activity = activity;
        this.themeObservable = new ThemeObservable(preferences);

        activity.getLifecycle().addObserver(this);
    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        switch (event) {
            case ON_CREATE:
                disposable = themeObservable.subscribe(this);
                break;
            case ON_DESTROY:
                disposable.dispose();
                break;
        }
    }

    @Override
    public void onThemeChanged(Preferences.ColorTheme colorTheme, boolean changed) {
        switch (colorTheme) {
            case DARK:
                activity.setTheme(R.style.AppTheme_Dark_Launcher);
                break;
            case LIGHT:
                activity.setTheme(R.style.AppTheme_Light_Launcher);
                break;
        }
        if (changed) {
            activity.recreate();
        }
    }
}
