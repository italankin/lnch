package com.italankin.lnch.model.descriptor.impl;

import androidx.annotation.NonNull;

import com.italankin.lnch.model.descriptor.CustomColorDescriptor;
import com.italankin.lnch.model.descriptor.CustomLabelDescriptor;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.DescriptorModels;
import com.italankin.lnch.model.descriptor.IgnorableDescriptor;
import com.italankin.lnch.model.descriptor.PackageDescriptor;
import com.italankin.lnch.model.repository.store.json.model.DeepShortcutDescriptorJson;
import com.italankin.lnch.model.ui.impl.DeepShortcutDescriptorUi;

/**
 * A descriptor for pinned {@link com.italankin.lnch.model.repository.shortcuts.Shortcut}s
 */
@DescriptorModels(json = DeepShortcutDescriptorJson.class, ui = DeepShortcutDescriptorUi.class)
public final class DeepShortcutDescriptor implements Descriptor, PackageDescriptor,
        CustomColorDescriptor, CustomLabelDescriptor, IgnorableDescriptor {

    public String id;
    public String packageName;
    public String originalLabel;
    public String label;
    public int color;
    public String customLabel;
    public Integer customColor;
    public boolean enabled = true;
    public boolean ignored;

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
    public String getOriginalLabel() {
        return originalLabel;
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
    public void setIgnored(boolean ignored) {
        this.ignored = ignored;
    }

    @Override
    public boolean isIgnored() {
        return ignored;
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

    @NonNull
    @Override
    public String toString() {
        return "DeepShortcut{" + getId() + "}";
    }

    @Override
    public DeepShortcutDescriptor copy() {
        DeepShortcutDescriptor copy = new DeepShortcutDescriptor(packageName, id);
        copy.originalLabel = originalLabel;
        copy.label = label;
        copy.color = color;
        copy.customLabel = customLabel;
        copy.customColor = customColor;
        copy.enabled = enabled;
        copy.ignored = ignored;
        return copy;
    }
}
