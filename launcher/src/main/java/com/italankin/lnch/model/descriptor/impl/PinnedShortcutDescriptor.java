package com.italankin.lnch.model.descriptor.impl;

import android.support.annotation.Keep;

import com.google.gson.annotations.SerializedName;
import com.italankin.lnch.model.descriptor.CustomColorDescriptor;
import com.italankin.lnch.model.descriptor.CustomLabelDescriptor;
import com.italankin.lnch.model.descriptor.Descriptor;

import java.util.UUID;

public class PinnedShortcutDescriptor implements Descriptor, CustomColorDescriptor, CustomLabelDescriptor {

    private static final String UNKNOWN_NAME = "???";

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
    public PinnedShortcutDescriptor() {
    }

    public PinnedShortcutDescriptor(String uri, String label) {
        this.id = UUID.randomUUID().toString();
        this.uri = uri;
        this.label = label != null ? label.toUpperCase() : UNKNOWN_NAME;
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
        customColor = color;
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
        customLabel = label;
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
        if (obj.getClass() != PinnedShortcutDescriptor.class) {
            return false;
        }
        PinnedShortcutDescriptor that = (PinnedShortcutDescriptor) obj;
        return this.id.equals(that.id);
    }

    @Override
    public String toString() {
        return "Shortcut{" + uri + "}";
    }
}
