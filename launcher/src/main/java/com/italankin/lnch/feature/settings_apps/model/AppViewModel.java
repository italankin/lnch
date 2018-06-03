package com.italankin.lnch.feature.settings_apps.model;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import com.italankin.lnch.bean.AppItem;

public class AppViewModel {
    public final AppItem item;
    public final String label;
    public final Drawable icon;
    public boolean hidden;

    public AppViewModel(AppItem item, PackageManager pm) {
        this.item = item;
        this.label = item.getLabel();
        this.icon = getIcon(item, pm);
        this.hidden = item.hidden;
    }

    private static Drawable getIcon(AppItem item, PackageManager pm) {
        try {
            return pm.getApplicationIcon(item.packageName);
        } catch (PackageManager.NameNotFoundException ignored) {
            return null;
        }
    }
}
