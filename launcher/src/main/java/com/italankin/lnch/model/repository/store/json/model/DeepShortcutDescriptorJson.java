package com.italankin.lnch.model.repository.store.json.model;

import androidx.annotation.Keep;
import com.google.gson.annotations.SerializedName;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.DeepShortcutDescriptor;

public final class DeepShortcutDescriptorJson implements DescriptorJson {

    public static final String TYPE = "deep_shortcut";

    @Keep
    @SerializedName(PROPERTY_TYPE)
    public String type = TYPE;

    @SerializedName("id")
    public String shortcutId;

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

    @SerializedName("ignored")
    public Boolean ignored;

    @Keep
    public DeepShortcutDescriptorJson() {
    }

    public DeepShortcutDescriptorJson(DeepShortcutDescriptor descriptor) {
        shortcutId = descriptor.shortcutId;
        packageName = descriptor.packageName;
        originalLabel = descriptor.originalLabel;
        label = descriptor.label;
        color = descriptor.color;
        customLabel = descriptor.customLabel;
        customColor = descriptor.customColor;
        enabled = descriptor.enabled ? null : false;
        ignored = descriptor.ignored ? true : null;
    }

    @Override
    public Descriptor toDescriptor() {
        DeepShortcutDescriptor.Mutable mutable = new DeepShortcutDescriptor.Mutable(packageName, shortcutId);
        mutable.setOriginalLabel(originalLabel);
        mutable.setLabel(label);
        mutable.setColor(color);
        mutable.setCustomLabel(customLabel);
        mutable.setCustomColor(customColor);
        mutable.setEnabled(enabled == null || enabled);
        mutable.setIgnored(ignored != null && ignored);
        return mutable.toDescriptor();
    }
}
