package com.italankin.lnch.feature.home.apps.folder.empty;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.ui.DescriptorUi;

public class EmptyFolderDescriptorUi implements DescriptorUi {

    @Override
    public Descriptor getDescriptor() {
        return EmptyFolderDescriptor.INSTANCE;
    }

    @Override
    public boolean is(DescriptorUi another) {
        return another instanceof EmptyFolderDescriptorUi;
    }

    @Override
    public boolean deepEquals(DescriptorUi another) {
        return another instanceof EmptyFolderDescriptorUi;
    }
}

