package com.italankin.lnch;

import android.app.Application;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.DynamicColorsOptions;
import com.italankin.lnch.di.service.DaggerService;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.util.activitycallbacks.DynamicColorsActivityCallback;
import com.italankin.lnch.util.activitycallbacks.ThemeActivityCallbacks;
import timber.log.Timber;

public class LauncherApp extends Application {

    public static DaggerService daggerService;

    @Override
    public void onCreate() {
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        super.onCreate();
        daggerService = new DaggerService(this);
        registerActivityLifecycleCallbacks(new ThemeActivityCallbacks());

        if (DynamicColors.isDynamicColorAvailable()) {
            DynamicColors.applyToActivitiesIfAvailable(this, new DynamicColorsOptions.Builder()
                    .setPrecondition((activity, theme) -> daggerService.main().preferences().get(Preferences.DYNAMIC_COLORS))
                    .build());
            registerActivityLifecycleCallbacks(new DynamicColorsActivityCallback());
        }
    }
}
