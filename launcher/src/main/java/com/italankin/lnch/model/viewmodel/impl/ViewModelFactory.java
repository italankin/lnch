package com.italankin.lnch.model.viewmodel.impl;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.descriptor.impl.DeepShortcutDescriptor;
import com.italankin.lnch.model.descriptor.impl.GroupDescriptor;
import com.italankin.lnch.model.descriptor.impl.PinnedShortcutDescriptor;
import com.italankin.lnch.model.viewmodel.DescriptorItem;

import java.util.ArrayList;
import java.util.List;

public enum ViewModelFactory {
    INSTANCE;

    public List<DescriptorItem> createItems(List<Descriptor> descriptors) {
        List<DescriptorItem> result = new ArrayList<>(descriptors.size());
        for (Descriptor descriptor : descriptors) {
            result.add(createItem(descriptor));
        }
        return result;
    }

    public DescriptorItem createItem(Descriptor descriptor) {
        if (descriptor instanceof AppDescriptor) {
            return new AppViewModel((AppDescriptor) descriptor);
        }
        if (descriptor instanceof GroupDescriptor) {
            return new GroupViewModel((GroupDescriptor) descriptor);
        }
        if (descriptor instanceof PinnedShortcutDescriptor) {
            return new PinnedShortcutViewModel((PinnedShortcutDescriptor) descriptor);
        }
        if (descriptor instanceof DeepShortcutDescriptor) {
            return new DeepShortcutViewModel((DeepShortcutDescriptor) descriptor);
        }
        throw new IllegalArgumentException("Unknown descriptor: " + descriptor.toString());
    }
}
