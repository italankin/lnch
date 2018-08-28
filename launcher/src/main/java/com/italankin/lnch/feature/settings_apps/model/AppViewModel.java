package com.italankin.lnch.feature.settings_apps.model;

import android.net.Uri;

import com.italankin.lnch.model.repository.descriptors.model.AppDescriptor;
import com.italankin.lnch.util.picasso.PackageManagerRequestHandler;

public class AppViewModel {
    public final AppDescriptor item;
    public final Uri icon;
    public final String label;
    public boolean hidden;

    public AppViewModel(AppDescriptor item) {
        this.item = item;
        this.icon = PackageManagerRequestHandler.uriFrom(item.packageName);
        this.label = item.getVisibleLabel();
        this.hidden = item.hidden;
    }
}
