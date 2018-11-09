package com.italankin.lnch.feature.settings.apps.model;

import android.net.Uri;

import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.viewmodel.impl.AppViewModel;
import com.italankin.lnch.util.picasso.PackageIconHandler;

public class AppWithIconViewModel extends AppViewModel {
    public final Uri icon;

    public AppWithIconViewModel(AppDescriptor descriptor) {
        super(descriptor);
        this.icon = PackageIconHandler.uriFrom(descriptor.packageName);
    }
}
