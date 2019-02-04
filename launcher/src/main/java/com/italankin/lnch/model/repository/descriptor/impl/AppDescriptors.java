package com.italankin.lnch.model.repository.descriptor.impl;

import android.content.pm.LauncherActivityInfo;
import android.content.pm.PackageManager;

import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.repository.prefs.Preferences;

import static com.italankin.lnch.model.repository.descriptor.impl.LauncherActivityInfoUtils.getComponentName;
import static com.italankin.lnch.model.repository.descriptor.impl.LauncherActivityInfoUtils.getDominantIconColor;
import static com.italankin.lnch.model.repository.descriptor.impl.LauncherActivityInfoUtils.getLabel;
import static com.italankin.lnch.model.repository.descriptor.impl.LauncherActivityInfoUtils.getVersionCode;

class AppDescriptors {
    private final Preferences preferences;
    private final PackageManager packageManager;

    AppDescriptors(Preferences preferences, PackageManager packageManager) {
        this.preferences = preferences;
        this.packageManager = packageManager;
    }

    AppDescriptor create(LauncherActivityInfo info) {
        return create(info, false);
    }

    AppDescriptor create(LauncherActivityInfo info, boolean includeComponent) {
        String packageName = info.getApplicationInfo().packageName;
        AppDescriptor item = new AppDescriptor(packageName);
        updateInternal(item, info);
        if (includeComponent) {
            item.componentName = getComponentName(info);
        }
        return item;
    }

    void update(AppDescriptor item, LauncherActivityInfo info) {
        long versionCode = getVersionCode(packageManager, item.packageName);
        if (item.versionCode != versionCode) {
            updateInternal(item, info);
        }
        if (item.componentName != null) {
            item.componentName = getComponentName(info);
        }
    }

    private void updateInternal(AppDescriptor item, LauncherActivityInfo info) {
        item.versionCode = getVersionCode(packageManager, item.packageName);
        item.label = getLabel(info);
        item.color = getDominantIconColor(info,
                preferences.colorTheme() == Preferences.ColorTheme.DARK);
    }
}
