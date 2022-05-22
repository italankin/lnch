package com.italankin.lnch.feature.settings.hidden_items;

import android.net.Uri;

import com.italankin.lnch.model.descriptor.IgnorableDescriptor;
import com.italankin.lnch.model.descriptor.PackageDescriptor;
import com.italankin.lnch.util.DescriptorUtils;
import com.italankin.lnch.util.picasso.PackageIconHandler;

class HiddenItem {

    final IgnorableDescriptor descriptor;
    final String originalLabel;
    final String visibleLabel;
    final Uri uri;

    HiddenItem(IgnorableDescriptor descriptor) {
        this.descriptor = descriptor;
        this.visibleLabel = DescriptorUtils.getVisibleLabel(descriptor);
        this.originalLabel = descriptor.getOriginalLabel();
        if (descriptor instanceof PackageDescriptor) {
            this.uri = PackageIconHandler.uriFrom(((PackageDescriptor) descriptor).getPackageName());
        } else {
            this.uri = null;
        }
    }
}
