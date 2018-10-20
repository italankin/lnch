package com.italankin.lnch.model.descriptor.impl;

import android.support.annotation.Keep;

import com.google.gson.annotations.SerializedName;
import com.italankin.lnch.model.descriptor.CustomColorDescriptor;
import com.italankin.lnch.model.descriptor.CustomLabelDescriptor;
import com.italankin.lnch.model.descriptor.Descriptor;

public class DeepShortcutDescriptor implements Descriptor, CustomColorDescriptor, CustomLabelDescriptor {

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

    @Keep
    public DeepShortcutDescriptor() {
    }

    public DeepShortcutDescriptor(String packageName, String shortcutId) {
        this.packageName = packageName;
        this.id = shortcutId;
    }

    @Override
    public String getId() {
        return packageName + "/" + id;
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
        return getId().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj.getClass() != DeepShortcutDescriptor.class) {
            return false;
        }
        DeepShortcutDescriptor that = (DeepShortcutDescriptor) obj;
        return this.getId().equals(that.getId());
    }

    @Override
    public String toString() {
        return "DeepShortcut{" + getId() + "}";
    }
}
