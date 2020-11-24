package com.italankin.lnch.model.repository.store.json.model;

import com.google.gson.annotations.SerializedName;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.PinnedShortcutDescriptor;

import androidx.annotation.Keep;

public final class PinnedShortcutDescriptorJson implements DescriptorJson {

    public static final String TYPE = "shortcut";

    @Keep
    @SerializedName(PROPERTY_TYPE)
    public String type = TYPE;

    @SerializedName("id")
    public String id;

    @SerializedName("uri")
    public String uri;

    @SerializedName("label")
    public String label;

    @SerializedName("color")
    public int color;

    @SerializedName("custom_label")
    public String customLabel;

    @SerializedName("custom_color")
    public Integer customColor;

    @Keep
    public PinnedShortcutDescriptorJson() {
    }

    public PinnedShortcutDescriptorJson(PinnedShortcutDescriptor descriptor) {
        this.id = descriptor.id;
        this.uri = descriptor.uri;
        this.label = descriptor.label;
        this.color = descriptor.color;
        this.customLabel = descriptor.customLabel;
        this.customColor = descriptor.customColor;
    }

    @Override
    public Descriptor toDescriptor() {
        PinnedShortcutDescriptor descriptor = new PinnedShortcutDescriptor();
        descriptor.id = this.id;
        descriptor.uri = this.uri;
        descriptor.label = this.label;
        descriptor.color = this.color;
        descriptor.customLabel = this.customLabel;
        descriptor.customColor = this.customColor;
        return descriptor;
    }
}
