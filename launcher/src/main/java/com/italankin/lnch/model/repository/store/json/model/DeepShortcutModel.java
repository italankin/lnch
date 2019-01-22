package com.italankin.lnch.model.repository.store.json.model;

import com.google.gson.annotations.SerializedName;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.DeepShortcutDescriptor;

import androidx.annotation.Keep;

public final class DeepShortcutModel implements JsonModel {

    public static final String TYPE = "deep_shortcut";

    @Keep
    @SerializedName(PROPERTY_TYPE)
    public String type = TYPE;

    @SerializedName("id")
    public String id;

    @SerializedName("packageName")
    public String packageName;

    @SerializedName("label")
    public String label;

    @SerializedName("color")
    public int color;

    @SerializedName("custom_label")
    public String customLabel;

    @SerializedName("custom_color")
    public Integer customColor;

    @SerializedName("enabled")
    public boolean enabled = true;

    @Keep
    public DeepShortcutModel() {
    }

    public DeepShortcutModel(DeepShortcutDescriptor descriptor) {
        this.id = descriptor.id;
        this.packageName = descriptor.packageName;
        this.label = descriptor.label;
        this.color = descriptor.color;
        this.customLabel = descriptor.customLabel;
        this.customColor = descriptor.customColor;
        this.enabled = descriptor.enabled;
    }

    @Override
    public Descriptor toDescriptor() {
        DeepShortcutDescriptor descriptor = new DeepShortcutDescriptor();
        descriptor.id = this.id;
        descriptor.packageName = this.packageName;
        descriptor.label = this.label;
        descriptor.color = this.color;
        descriptor.customLabel = this.customLabel;
        descriptor.customColor = this.customColor;
        descriptor.enabled = this.enabled;
        return descriptor;
    }
}
