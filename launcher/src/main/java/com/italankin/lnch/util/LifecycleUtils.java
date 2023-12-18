package com.italankin.lnch.util;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

public final class LifecycleUtils {

    public static void doOnDestroyOnce(LifecycleOwner lifecycleOwner, Runnable action) {
        Lifecycle lifecycle = lifecycleOwner.getLifecycle();
        if (lifecycle.getCurrentState() == Lifecycle.State.DESTROYED) {
            action.run();
            return;
        }
        lifecycle.addObserver(new DefaultLifecycleObserver() {
            @Override
            public void onDestroy(@NonNull LifecycleOwner owner) {
                owner.getLifecycle().removeObserver(this);
                action.run();
            }
        });
    }

    private LifecycleUtils() {
    }
}
