package com.italankin.lnch.model.repository.store.json.model;

import com.google.gson.annotations.SerializedName;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.DeepShortcutDescriptor;

import androidx.annotation.Keep;

public final class DeepShortcutDescriptorJson implements DescriptorJson {

    public static final String TYPE = "deep_shortcut";

    @Keep
    @SerializedName(PROPERTY_TYPE)
    public String type = TYPE;

    @SerializedName("id")
    public String id;

    @SerializedName("packageName")
    public String packageName;

    @SerializedName("original_label")
    public String originalLabel;

    @SerializedName("label")
    public String label;

    @SerializedName("color")
    public int color;

    @SerializedName("custom_label")
    public String customLabel;

    @SerializedName("custom_color")
    public Integer customColor;

    @SerializedName("enabled")
    public Boolean enabled;

    @Keep
    public DeepShortcutDescriptorJson() {
    }

    public DeepShortcutDescriptorJson(DeepShortcutDescriptor descriptor) {
        this.id = descriptor.id;
        this.packageName = descriptor.packageName;
        this.originalLabel = descriptor.originalLabel;
        this.label = descriptor.label;
        this.color = descriptor.color;
        this.customLabel = descriptor.customLabel;
        this.customColor = descriptor.customColor;
        this.enabled = descriptor.enabled ? null : false;
    }

    @Override
    public Descriptor toDescriptor() {
        DeepShortcutDescriptor descriptor = new DeepShortcutDescriptor();
        descriptor.id = this.id;
        descriptor.packageName = this.packageName;
        descriptor.originalLabel = this.originalLabel;
        descriptor.label = this.label;
        descriptor.color = this.color;
        descriptor.customLabel = this.customLabel;
        descriptor.customColor = this.customColor;
        descriptor.enabled = this.enabled == null || this.enabled;
        return descriptor;
    }
}
