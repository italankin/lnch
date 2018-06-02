package com.italankin.lnch.ui.feature.settings.visibility;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import com.italankin.lnch.bean.AppItem;

class AppViewModel {
    final AppItem item;
    final String label;
    final Drawable icon;
    boolean hidden;

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
