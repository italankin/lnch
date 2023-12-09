package com.italankin.lnch.model.repository.descriptor.actions;

import android.content.Intent;
import com.italankin.lnch.model.descriptor.impl.IntentDescriptor;
import com.italankin.lnch.model.descriptor.mutable.MutableDescriptor;

import java.util.List;

public class EditIntentAction extends BaseAction {

    private final String id;
    private final Intent intent;

    public EditIntentAction(String id, Intent intent) {
        this.id = id;
        this.intent = intent;
    }

    @Override
    public void apply(List<MutableDescriptor<?>> items) {
        IntentDescriptor.Mutable descriptor = findById(items, id);
        if (descriptor != null) {
            descriptor.setIntentUri(intent.toUri(Intent.URI_INTENT_SCHEME | Intent.URI_ALLOW_UNSAFE));
        }
    }
}
