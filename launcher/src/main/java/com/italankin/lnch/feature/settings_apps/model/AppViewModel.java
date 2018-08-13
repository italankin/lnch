package com.italankin.lnch.feature.settings_apps.model;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import com.italankin.lnch.model.repository.descriptors.model.AppDescriptor;

public class AppViewModel {
    public final AppDescriptor item;
    public final String label;
    public final Drawable icon;
    public boolean hidden;

    public AppViewModel(AppDescriptor item, PackageManager pm) {
        this.item = item;
        this.label = item.getVisibleLabel();
        this.icon = getIcon(item, pm);
        this.hidden = item.hidden;
    }

    private static Drawable getIcon(AppDescriptor item, PackageManager pm) {
        try {
            return pm.getApplicationIcon(item.packageName);
        } catch (PackageManager.NameNotFoundException ignored) {
            return null;
        }
    }
}
