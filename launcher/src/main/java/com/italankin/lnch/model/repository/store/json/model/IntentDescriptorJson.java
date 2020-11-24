package com.italankin.lnch.model.repository.store.json.model;

import com.google.gson.annotations.SerializedName;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.IntentDescriptor;

import androidx.annotation.Keep;

public final class IntentDescriptorJson implements DescriptorJson {

    public static final String TYPE = "intent";

    @Keep
    @SerializedName(PROPERTY_TYPE)
    public String type = TYPE;

    @SerializedName("id")
    public String id;

    @SerializedName("intent_uri")
    public String intentUri;

    @SerializedName("label")
    public String label;

    @SerializedName("custom_label")
    public String customLabel;

    @SerializedName("color")
    public int color;

    @SerializedName("custom_color")
    public Integer customColor;

    @Keep
    public IntentDescriptorJson() {
    }

    public IntentDescriptorJson(IntentDescriptor descriptor) {
        this.id = descriptor.id;
        this.intentUri = descriptor.intentUri;
        this.label = descriptor.label;
        this.customLabel = descriptor.customLabel;
        this.color = descriptor.color;
        this.customColor = descriptor.customColor;
    }

    @Override
    public Descriptor toDescriptor() {
        IntentDescriptor descriptor = new IntentDescriptor();
        descriptor.id = this.id;
        descriptor.intentUri = this.intentUri;
        descriptor.label = this.label;
        descriptor.customLabel = this.customLabel;
        descriptor.color = this.color;
        descriptor.customColor = this.customColor;
        return descriptor;
    }
}
