package com.italankin.lnch.model.repository.descriptor.apps.interactors;

import android.content.pm.LauncherActivityInfo;
import android.content.pm.PackageManager;

import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.repository.prefs.Preferences;

import static com.italankin.lnch.model.repository.descriptor.apps.interactors.LauncherActivityInfoUtils.getComponentName;
import static com.italankin.lnch.model.repository.descriptor.apps.interactors.LauncherActivityInfoUtils.getDominantIconColor;
import static com.italankin.lnch.model.repository.descriptor.apps.interactors.LauncherActivityInfoUtils.getLabel;
import static com.italankin.lnch.model.repository.descriptor.apps.interactors.LauncherActivityInfoUtils.getVersionCode;

public class AppDescriptorInteractor {

    private final PackageManager packageManager;
    private final Preferences preferences;

    public AppDescriptorInteractor(PackageManager packageManager, Preferences preferences) {
        this.packageManager = packageManager;
        this.preferences = preferences;
    }

    public AppDescriptor createItem(LauncherActivityInfo info) {
        return createItem(info, false);
    }

    AppDescriptor createItem(LauncherActivityInfo info, boolean withComponentName) {
        String packageName = info.getApplicationInfo().packageName;
        AppDescriptor item = new AppDescriptor(packageName);
        item.versionCode = getVersionCode(packageManager, packageName);
        item.label = getLabel(info);
        if (withComponentName) {
            item.componentName = getComponentName(info);
        }
        item.color = getDominantIconColor(info,
                preferences.get(Preferences.COLOR_THEME) == Preferences.ColorTheme.DARK);
        return item;
    }

    void updateItem(AppDescriptor app, LauncherActivityInfo info) {
        long versionCode = getVersionCode(packageManager, app.packageName);
        if (app.versionCode != versionCode) {
            app.versionCode = versionCode;
            app.label = getLabel(info);
            app.color = getDominantIconColor(info,
                    preferences.get(Preferences.COLOR_THEME) == Preferences.ColorTheme.DARK);
        }
        if (app.componentName != null) {
            app.componentName = getComponentName(info);

        }
    }
}
