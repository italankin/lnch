package com.italankin.lnch.model.repository.store.json.model;

import androidx.annotation.Keep;
import com.google.gson.annotations.SerializedName;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.IntentDescriptor;

public final class IntentDescriptorJson implements DescriptorJson {

    public static final String TYPE = "intent";

    @Keep
    @SerializedName(PROPERTY_TYPE)
    public String type = TYPE;

    @SerializedName("id")
    public String id;

    @SerializedName("intent_uri")
    public String intentUri;

    @SerializedName("original_label")
    public String originalLabel;

    @SerializedName("label")
    public String label;

    @SerializedName("custom_label")
    public String customLabel;

    @SerializedName("color")
    public int color;

    @SerializedName("custom_color")
    public Integer customColor;

    @SerializedName("ignored")
    public Boolean ignored;

    @Keep
    public IntentDescriptorJson() {
    }

    public IntentDescriptorJson(IntentDescriptor descriptor) {
        this.id = descriptor.id;
        this.intentUri = descriptor.intentUri;
        this.originalLabel = descriptor.originalLabel;
        this.label = descriptor.label;
        this.customLabel = descriptor.customLabel;
        this.color = descriptor.color;
        this.customColor = descriptor.customColor;
        this.ignored = descriptor.ignored ? true : null;
    }

    @Override
    public Descriptor toDescriptor() {
        IntentDescriptor.Mutable mutable = new IntentDescriptor.Mutable(id, intentUri, originalLabel);
        mutable.setLabel(label);
        mutable.setCustomLabel(customLabel);
        mutable.setColor(color);
        mutable.setCustomColor(customColor);
        mutable.setIgnored(ignored != null && ignored);
        return mutable.toDescriptor();
    }
}
