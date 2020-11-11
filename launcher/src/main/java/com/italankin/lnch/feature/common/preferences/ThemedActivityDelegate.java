package com.italankin.lnch.feature.common.preferences;

import com.italankin.lnch.model.repository.prefs.Preferences;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import io.reactivex.disposables.Disposable;

public class ThemedActivityDelegate<T extends AppCompatActivity & ThemedActivityDelegate.ThemeProvider>
        implements LifecycleEventObserver, ThemedActivity {

    public static <T extends AppCompatActivity & ThemeProvider> void attach(T activity, Preferences preferences) {
        new ThemedActivityDelegate<>(activity, preferences);
    }

    private final T activity;
    private final Disposable disposable;

    private ThemedActivityDelegate(T activity, Preferences preferences) {
        this.activity = activity;
        this.disposable = new ThemeObservable(preferences).subscribe(this);

        activity.getLifecycle().addObserver(this);
    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            disposable.dispose();
        }
    }

    @Override
    public void onThemeChanged(Preferences.ColorTheme colorTheme, boolean changed) {
        activity.setTheme(activity.getTheme(colorTheme));
        if (changed) {
            activity.recreate();
        }
    }

    public interface ThemeProvider {
        int getTheme(Preferences.ColorTheme colorTheme);
    }
}
