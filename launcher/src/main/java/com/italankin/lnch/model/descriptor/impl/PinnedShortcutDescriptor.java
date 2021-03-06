package com.italankin.lnch.model.descriptor.impl;

import com.italankin.lnch.model.descriptor.CustomColorDescriptor;
import com.italankin.lnch.model.descriptor.CustomLabelDescriptor;
import com.italankin.lnch.model.descriptor.Descriptor;

import java.util.UUID;

import androidx.annotation.ColorInt;

/**
 * Pinned intent for {@code com.android.launcher.action.INSTALL_SHORTCUT}
 */
public final class PinnedShortcutDescriptor implements Descriptor, CustomColorDescriptor, CustomLabelDescriptor {

    public String id;
    public String uri;
    public String label;
    public int color;
    public String customLabel;
    public Integer customColor;

    public PinnedShortcutDescriptor() {
    }

    public PinnedShortcutDescriptor(String uri, String label, @ColorInt int color) {
        this.id = "shortcut/" + UUID.randomUUID().toString();
        this.uri = uri;
        this.label = label;
        this.color = color;
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

    @Override
    public PinnedShortcutDescriptor copy() {
        PinnedShortcutDescriptor copy = new PinnedShortcutDescriptor();
        copy.id = id;
        copy.uri = uri;
        copy.label = label;
        copy.color = color;
        copy.customLabel = customLabel;
        copy.customColor = customColor;
        return copy;
    }
}
