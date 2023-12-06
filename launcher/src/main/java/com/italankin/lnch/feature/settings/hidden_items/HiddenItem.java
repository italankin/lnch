package com.italankin.lnch.feature.settings.hidden_items;

import android.net.Uri;
import com.italankin.lnch.model.descriptor.IgnorableDescriptor;
import com.italankin.lnch.model.descriptor.PackageDescriptor;
import com.italankin.lnch.util.DescriptorUtils;
import com.italankin.lnch.util.imageloader.resourceloader.PackageIconLoader;
import com.italankin.lnch.util.search.Searchable;

import java.util.Set;

class HiddenItem implements Searchable {

    final IgnorableDescriptor descriptor;
    final String originalLabel;
    final String visibleLabel;
    final Uri uri;
    private final Set<String> searchTokens;

    HiddenItem(IgnorableDescriptor descriptor) {
        this.descriptor = descriptor;
        this.visibleLabel = DescriptorUtils.getVisibleLabel(descriptor);
        this.originalLabel = descriptor.getOriginalLabel();
        if (descriptor instanceof PackageDescriptor) {
            this.uri = PackageIconLoader.uriFrom(((PackageDescriptor) descriptor).getPackageName());
        } else {
            this.uri = null;
        }
        searchTokens = Searchable.createTokens(visibleLabel, originalLabel);
    }

    @Override
    public Set<String> getSearchTokens() {
        return searchTokens;
    }
}
