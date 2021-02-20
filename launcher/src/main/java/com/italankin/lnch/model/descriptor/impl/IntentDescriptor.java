package com.italankin.lnch.model.descriptor.impl;

import android.content.Intent;

import com.italankin.lnch.model.descriptor.CustomColorDescriptor;
import com.italankin.lnch.model.descriptor.CustomLabelDescriptor;
import com.italankin.lnch.model.descriptor.Descriptor;

import java.util.UUID;

public final class IntentDescriptor implements Descriptor, CustomColorDescriptor, CustomLabelDescriptor {

    public String id;
    public String intentUri;
    public String label;
    public String customLabel;
    public int color;
    public Integer customColor;

    public IntentDescriptor() {
    }

    public IntentDescriptor(Intent intent, String label, int color) {
        this.id = "intent/" + UUID.randomUUID().toString();
        this.intentUri = intent.toUri(Intent.URI_INTENT_SCHEME);
        this.label = label;
        this.color = color;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int getColor() {
        return color;
    }

    @Override
    public void setCustomColor(Integer color) {
        this.customColor = color;
    }

    @Override
    public Integer getCustomColor() {
        return customColor;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public void setCustomLabel(String label) {
        this.customLabel = label;
    }

    @Override
    public String getCustomLabel() {
        return customLabel;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj.getClass() != IntentDescriptor.class) {
            return false;
        }
        IntentDescriptor that = (IntentDescriptor) obj;
        return this.id.equals(that.id);
    }

    @Override
    public String toString() {
        return "Intent{" + intentUri + '}';
    }

    @Override
    public IntentDescriptor copy() {
        IntentDescriptor copy = new IntentDescriptor();
        copy.id = id;
        copy.intentUri = intentUri;
        copy.label = label;
        copy.customLabel = customLabel;
        copy.color = color;
        copy.customColor = customColor;
        return copy;
    }
}
