package com.italankin.lnch.model.repository.descriptor.actions;

import android.content.Intent;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.IntentDescriptor;

import java.util.List;

public class EditIntentAction extends BaseAction {

    private final String id;
    private final Intent intent;
    private final String label;

    public EditIntentAction(String id, Intent intent, String label) {
        this.id = id;
        this.intent = intent;
        this.label = label;
    }

    @Override
    public void apply(List<Descriptor> items) {
        IntentDescriptor descriptor = findById(items, id);
        if (descriptor != null) {
            descriptor.intentUri = intent.toUri(Intent.URI_INTENT_SCHEME | Intent.URI_ALLOW_UNSAFE);
            descriptor.setCustomLabel(label);
        }
    }
}
