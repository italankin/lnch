package com.italankin.lnch.util.activitycallbacks;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.util.ThemeUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;

public class ThemeActivityCallbacks implements Application.ActivityLifecycleCallbacks {

    private Disposable disposable = Disposables.disposed();

    @Override
    public void onActivityPreCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        if (disposable.isDisposed()) {
            Preferences preferences = LauncherApp.daggerService.main().preferences();
            ThemeUtils.applyTheme(preferences.get(Preferences.COLOR_THEME));
            disposable = preferences
                    .observeValue(Preferences.COLOR_THEME)
                    .map(Preferences.Value::get)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(ThemeUtils::applyTheme);
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
