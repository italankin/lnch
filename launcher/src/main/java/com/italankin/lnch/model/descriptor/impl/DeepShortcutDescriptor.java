package com.italankin.lnch.model.descriptor.impl;

import com.italankin.lnch.model.descriptor.CustomColorDescriptor;
import com.italankin.lnch.model.descriptor.CustomLabelDescriptor;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.PackageDescriptor;

public final class DeepShortcutDescriptor implements Descriptor, PackageDescriptor,
        CustomColorDescriptor, CustomLabelDescriptor {

    public String id;
    public String packageName;
    public String label;
    public int color;
    public String customLabel;
    public Integer customColor;
    public boolean enabled = true;

    public DeepShortcutDescriptor() {
    }

    public DeepShortcutDescriptor(String packageName, String shortcutId) {
        this.packageName = packageName;
        this.id = shortcutId;
    }

    @Override
    public String getPackageName() {
        return packageName;
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

    @Override
    public DeepShortcutDescriptor copy() {
        DeepShortcutDescriptor copy = new DeepShortcutDescriptor(packageName, id);
        copy.label = label;
        copy.color = color;
        copy.customLabel = customLabel;
        copy.customColor = customColor;
        copy.enabled = enabled;
        return copy;
    }
}
