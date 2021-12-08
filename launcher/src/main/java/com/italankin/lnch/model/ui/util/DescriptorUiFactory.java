package com.italankin.lnch.model.ui.util;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.descriptor.impl.DeepShortcutDescriptor;
import com.italankin.lnch.model.descriptor.impl.FolderDescriptor;
import com.italankin.lnch.model.descriptor.impl.IntentDescriptor;
import com.italankin.lnch.model.descriptor.impl.PinnedShortcutDescriptor;
import com.italankin.lnch.model.ui.DescriptorUi;
import com.italankin.lnch.model.ui.impl.AppDescriptorUi;
import com.italankin.lnch.model.ui.impl.DeepShortcutDescriptorUi;
import com.italankin.lnch.model.ui.impl.FolderDescriptorUi;
import com.italankin.lnch.model.ui.impl.IntentDescriptorUi;
import com.italankin.lnch.model.ui.impl.PinnedShortcutDescriptorUi;

import java.util.ArrayList;
import java.util.List;

public final class DescriptorUiFactory {

    public static List<DescriptorUi> createItems(List<Descriptor> descriptors) {
        List<DescriptorUi> result = new ArrayList<>(descriptors.size());
        for (Descriptor descriptor : descriptors) {
            result.add(createItem(descriptor));
        }
        return result;
    }

    public static DescriptorUi createItem(Descriptor descriptor) {
        if (descriptor instanceof AppDescriptor) {
            return new AppDescriptorUi((AppDescriptor) descriptor);
        }
        if (descriptor instanceof FolderDescriptor) {
            return new FolderDescriptorUi((FolderDescriptor) descriptor);
        }
        if (descriptor instanceof PinnedShortcutDescriptor) {
            return new PinnedShortcutDescriptorUi((PinnedShortcutDescriptor) descriptor);
        }
        if (descriptor instanceof DeepShortcutDescriptor) {
            return new DeepShortcutDescriptorUi((DeepShortcutDescriptor) descriptor);
        }
        if (descriptor instanceof IntentDescriptor) {
            return new IntentDescriptorUi((IntentDescriptor) descriptor);
        }
        throw new IllegalArgumentException("Unknown descriptor: " + descriptor.toString());
    }

    private DescriptorUiFactory() {
        // no instance
    }
}
