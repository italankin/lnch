package com.italankin.lnch.model.repository.descriptor.apps.interactors;

import android.content.pm.LauncherActivityInfo;
import android.content.pm.PackageManager;

import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.repository.descriptor.NameNormalizer;
import com.italankin.lnch.model.repository.prefs.Preferences;

import static com.italankin.lnch.model.repository.descriptor.apps.interactors.LauncherActivityInfoUtils.getComponentName;
import static com.italankin.lnch.model.repository.descriptor.apps.interactors.LauncherActivityInfoUtils.getDominantIconColor;
import static com.italankin.lnch.model.repository.descriptor.apps.interactors.LauncherActivityInfoUtils.getVersionCode;

public class AppDescriptorInteractor {

    private final PackageManager packageManager;
    private final Preferences preferences;
    private final NameNormalizer nameNormalizer;

    public AppDescriptorInteractor(PackageManager packageManager, Preferences preferences,
            NameNormalizer nameNormalizer) {
        this.packageManager = packageManager;
        this.preferences = preferences;
        this.nameNormalizer = nameNormalizer;
    }

    public AppDescriptor createItem(LauncherActivityInfo info) {
        return createItem(info, false);
    }

    AppDescriptor createItem(LauncherActivityInfo info, boolean withComponentName) {
        String packageName = info.getApplicationInfo().packageName;
        AppDescriptor item = new AppDescriptor(packageName);
        item.versionCode = getVersionCode(packageManager, packageName);
        item.originalLabel = info.getLabel().toString();
        item.label = nameNormalizer.normalize(info.getLabel());
        if (withComponentName) {
            item.componentName = getComponentName(info);
        }
        item.color = getDominantIconColor(info, isDarkTheme());
        return item;
    }

    boolean updateItem(AppDescriptor app, LauncherActivityInfo info) {
        boolean updated = false;
        long versionCode = getVersionCode(packageManager, app.packageName);
        if (app.versionCode != versionCode) {
            app.versionCode = versionCode;
            app.color = getDominantIconColor(info, isDarkTheme());
            updated = true;
        }
        if (app.componentName != null) {
            String newComponentName = getComponentName(info);
            if (!newComponentName.equals(app.componentName)) {
                app.componentName = newComponentName;
                updated = true;
            }
        }
        String originalLabel = app.originalLabel;
        String newLabel = info.getLabel().toString();
        if (!newLabel.equals(originalLabel)) {
            app.originalLabel = newLabel;
            updated = true;
        }
        app.label = nameNormalizer.normalize(info.getLabel());
        return updated;
    }

    private boolean isDarkTheme() {
        Preferences.ColorTheme colorTheme = preferences.get(Preferences.COLOR_THEME);
        return colorTheme != Preferences.ColorTheme.LIGHT;
    }
}
