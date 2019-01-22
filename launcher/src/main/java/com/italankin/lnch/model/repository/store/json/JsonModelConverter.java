package com.italankin.lnch.model.repository.store.json;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.descriptor.impl.DeepShortcutDescriptor;
import com.italankin.lnch.model.descriptor.impl.GroupDescriptor;
import com.italankin.lnch.model.descriptor.impl.IntentDescriptor;
import com.italankin.lnch.model.descriptor.impl.PinnedShortcutDescriptor;
import com.italankin.lnch.model.repository.store.json.model.AppModel;
import com.italankin.lnch.model.repository.store.json.model.DeepShortcutModel;
import com.italankin.lnch.model.repository.store.json.model.GroupModel;
import com.italankin.lnch.model.repository.store.json.model.IntentModel;
import com.italankin.lnch.model.repository.store.json.model.JsonModel;
import com.italankin.lnch.model.repository.store.json.model.PinnedShortcutModel;

class JsonModelConverter {

    Descriptor fromJson(JsonModel jsonModel) {
        return jsonModel.toDescriptor();
    }

    JsonModel toJson(Descriptor descriptor) {
        if (descriptor instanceof AppDescriptor) {
            return new AppModel((AppDescriptor) descriptor);
        }
        if (descriptor instanceof GroupDescriptor) {
            return new GroupModel((GroupDescriptor) descriptor);
        }
        if (descriptor instanceof DeepShortcutDescriptor) {
            return new DeepShortcutModel((DeepShortcutDescriptor) descriptor);
        }
        if (descriptor instanceof PinnedShortcutDescriptor) {
            return new PinnedShortcutModel((PinnedShortcutDescriptor) descriptor);
        }
        if (descriptor instanceof IntentDescriptor) {
            return new IntentModel((IntentDescriptor) descriptor);
        }
        throw new IllegalArgumentException("Unknown Descriptor: " + descriptor);
    }
}
