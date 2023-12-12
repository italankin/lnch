package com.italankin.lnch.util.activitycallbacks;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.model.repository.prefs.Preferences;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class DynamicColorsActivityCallback implements Application.ActivityLifecycleCallbacks {

    @Override
    public void onActivityPreCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        if (activity instanceof ComponentActivity) {
            ((ComponentActivity) activity).getLifecycle().addObserver(new DefaultLifecycleObserver() {
                private Disposable disposable;

                @Override
                public void onCreate(@NonNull LifecycleOwner owner) {
                    disposable = LauncherApp.daggerService.main().preferences()
                            .observe(Preferences.DYNAMIC_COLORS, false)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(aBoolean -> activity.recreate());
                }

                @Override
                public void onDestroy(@NonNull LifecycleOwner owner) {
                    if (disposable != null) {
                        disposable.dispose();
                        disposable = null;
                    }
                }
            });
        }
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
    }
}
