package com.italankin.lnch.model.repository.store.json.model;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.DeepShortcutDescriptor;
import com.italankin.lnch.model.descriptor.props.ItemWidth;
import com.italankin.lnch.model.descriptor.props.TextAlign;

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

    @SerializedName("custom_width")
    public String customWidth;

    @SerializedName("custom_text_align")
    public String customTextAlign;

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
        customWidth = descriptor.customWidth != null ? descriptor.customWidth.key() : null;
        customTextAlign = descriptor.customTextAlign != null ? descriptor.customTextAlign.key() : null;
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
        mutable.setCustomWidth(ItemWidth.fromKey(customWidth));
        mutable.setCustomTextAlign(TextAlign.fromKey(customTextAlign));
        mutable.setEnabled(enabled == null || enabled);
        mutable.setIgnored(ignored != null && ignored);
        return mutable.toDescriptor();
    }
}
