package com.italankin.lnch.model.repository.store.json.model;

import androidx.annotation.Keep;
import com.google.gson.annotations.SerializedName;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.PinnedShortcutDescriptor;

public final class PinnedShortcutDescriptorJson implements DescriptorJson {

    public static final String TYPE = "shortcut";

    @Keep
    @SerializedName(PROPERTY_TYPE)
    public String type = TYPE;

    @SerializedName("id")
    public String id;

    @SerializedName("uri")
    public String uri;

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

    @SerializedName("ignored")
    public Boolean ignored;

    @Keep
    public PinnedShortcutDescriptorJson() {
    }

    public PinnedShortcutDescriptorJson(PinnedShortcutDescriptor descriptor) {
        this.id = descriptor.id;
        this.uri = descriptor.uri;
        this.originalLabel = descriptor.originalLabel;
        this.label = descriptor.label;
        this.color = descriptor.color;
        this.customLabel = descriptor.customLabel;
        this.customColor = descriptor.customColor;
        this.ignored = descriptor.ignored ? true : null;
    }

    @Override
    public Descriptor toDescriptor() {
        PinnedShortcutDescriptor.Mutable mutable = new PinnedShortcutDescriptor.Mutable(id, uri, originalLabel);
        mutable.setOriginalLabel(originalLabel);
        mutable.setLabel(label);
        mutable.setColor(color);
        mutable.setCustomLabel(customLabel);
        mutable.setCustomColor(customColor);
        mutable.setIgnored(ignored != null && ignored);
        return mutable.toDescriptor();
    }
}
