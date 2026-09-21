package com.italankin.lnch.model.repository.store.json.model;

import android.graphics.Color;

import androidx.annotation.Keep;
import com.google.gson.annotations.SerializedName;
import com.italankin.lnch.model.descriptor.impl.DividerDescriptor;

public final class DividerDescriptorJson implements DescriptorJson {
    public static final String TYPE = "divider";

    @Keep
    @SerializedName(PROPERTY_TYPE)
    public String type = TYPE;

    @SerializedName("id")
    public String id;

    @SerializedName("original_label")
    public String originalLabel;

    @SerializedName("label")
    public String label;

    @SerializedName("custom_label")
    public String customLabel;

    @SerializedName("color")
    public int color = Color.WHITE;

    @SerializedName("custom_color")
    public Integer customColor;

    @Keep
    public DividerDescriptorJson() {
    }

    public DividerDescriptorJson(DividerDescriptor descriptor) {
        id = descriptor.id;
        originalLabel = descriptor.originalLabel;
        label = descriptor.label;
        customLabel = descriptor.customLabel;
        color = descriptor.color;
        customColor = descriptor.customColor;
    }

    @Override
    public DividerDescriptor toDescriptor() {
        DividerDescriptor.Mutable mutable = new DividerDescriptor.Mutable(id);
        mutable.setOriginalLabel(originalLabel);
        mutable.setLabel(label);
        mutable.setCustomLabel(customLabel);
        mutable.setColor(color);
        mutable.setCustomColor(customColor);
        return mutable.toDescriptor();
    }
}
