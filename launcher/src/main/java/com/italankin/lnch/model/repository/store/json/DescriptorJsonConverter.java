package com.italankin.lnch.model.repository.store.json;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.descriptor.impl.DeepShortcutDescriptor;
import com.italankin.lnch.model.descriptor.impl.GroupDescriptor;
import com.italankin.lnch.model.descriptor.impl.IntentDescriptor;
import com.italankin.lnch.model.descriptor.impl.PinnedShortcutDescriptor;
import com.italankin.lnch.model.repository.store.json.model.AppDescriptorJson;
import com.italankin.lnch.model.repository.store.json.model.DeepShortcutDescriptorJson;
import com.italankin.lnch.model.repository.store.json.model.DescriptorJson;
import com.italankin.lnch.model.repository.store.json.model.GroupDescriptorJson;
import com.italankin.lnch.model.repository.store.json.model.IntentDescriptorJson;
import com.italankin.lnch.model.repository.store.json.model.PinnedShortcutDescriptorJson;

class DescriptorJsonConverter {

    Descriptor fromJson(DescriptorJson descriptorJson) {
        return descriptorJson.toDescriptor();
    }

    DescriptorJson toJson(Descriptor descriptor) {
        if (descriptor instanceof AppDescriptor) {
            return new AppDescriptorJson((AppDescriptor) descriptor);
        }
        if (descriptor instanceof GroupDescriptor) {
            return new GroupDescriptorJson((GroupDescriptor) descriptor);
        }
        if (descriptor instanceof DeepShortcutDescriptor) {
            return new DeepShortcutDescriptorJson((DeepShortcutDescriptor) descriptor);
        }
        if (descriptor instanceof PinnedShortcutDescriptor) {
            return new PinnedShortcutDescriptorJson((PinnedShortcutDescriptor) descriptor);
        }
        if (descriptor instanceof IntentDescriptor) {
            return new IntentDescriptorJson((IntentDescriptor) descriptor);
        }
        throw new IllegalArgumentException("Unknown Descriptor: " + descriptor);
    }
}
