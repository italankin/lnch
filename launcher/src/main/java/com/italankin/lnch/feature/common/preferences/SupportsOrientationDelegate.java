package com.italankin.lnch.feature.common.preferences;

import android.annotation.SuppressLint;

import com.italankin.lnch.model.repository.prefs.Preferences;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import io.reactivex.disposables.Disposable;

public class SupportsOrientationDelegate implements LifecycleEventObserver, SupportsOrientation {

    public static void attach(AppCompatActivity activity, Preferences preferences) {
        new SupportsOrientationDelegate(activity, preferences);
    }

    private final AppCompatActivity activity;
    private final Disposable disposable;

    private SupportsOrientationDelegate(AppCompatActivity activity, Preferences preferences) {
        this.activity = activity;
        this.disposable = new ScreenOrientationObservable(preferences).subscribe(this);

        activity.getLifecycle().addObserver(this);
    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            disposable.dispose();
        }
    }

    @SuppressLint("WrongConstant")
    @Override
    public void onOrientationChange(Preferences.ScreenOrientation screenOrientation, boolean changed) {
        activity.setRequestedOrientation(screenOrientation.value());
    }
}
