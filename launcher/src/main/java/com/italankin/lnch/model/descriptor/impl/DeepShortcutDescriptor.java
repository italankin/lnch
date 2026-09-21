package com.italankin.lnch.model.descriptor.impl;

import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.italankin.lnch.model.descriptor.CustomColorDescriptor;
import com.italankin.lnch.model.descriptor.CustomLabelDescriptor;
import com.italankin.lnch.model.descriptor.CustomLayoutDescriptor;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.DescriptorModels;
import com.italankin.lnch.model.descriptor.IgnorableDescriptor;
import com.italankin.lnch.model.descriptor.PackageDescriptor;
import com.italankin.lnch.model.descriptor.mutable.CustomColorMutableDescriptor;
import com.italankin.lnch.model.descriptor.mutable.CustomLabelMutableDescriptor;
import com.italankin.lnch.model.descriptor.mutable.CustomLayoutMutableDescriptor;
import com.italankin.lnch.model.descriptor.mutable.IgnorableMutableDescriptor;
import com.italankin.lnch.model.descriptor.mutable.MutableDescriptor;
import com.italankin.lnch.model.descriptor.props.ItemWidth;
import com.italankin.lnch.model.descriptor.props.TextAlign;
import com.italankin.lnch.model.repository.store.json.model.DeepShortcutDescriptorJson;
import com.italankin.lnch.model.ui.impl.DeepShortcutDescriptorUi;

/**
 * A descriptor for pinned {@link com.italankin.lnch.model.repository.shortcuts.Shortcut}s
 */
@DescriptorModels(
        json = DeepShortcutDescriptorJson.class,
        ui = DeepShortcutDescriptorUi.class,
        mutable = DeepShortcutDescriptor.Mutable.class
)
public final class DeepShortcutDescriptor implements Descriptor, PackageDescriptor,
        CustomColorDescriptor, CustomLabelDescriptor, IgnorableDescriptor, CustomLayoutDescriptor {

    private final String id;
    public final String shortcutId;
    public final String packageName;
    public final String originalLabel;
    public final String label;
    public final int color;
    public final String customLabel;
    public final Integer customColor;
    @Nullable
    public final ItemWidth customWidth;
    @Nullable
    public final TextAlign customTextAlign;
    public final boolean enabled;
    public final boolean ignored;

    public DeepShortcutDescriptor(Mutable mutable) {
        id = mutable.id;
        shortcutId = mutable.shortcutId;
        packageName = mutable.packageName;
        originalLabel = mutable.originalLabel;
        label = mutable.label;
        color = mutable.color;
        customLabel = mutable.customLabel;
        customColor = mutable.customColor;
        customWidth = mutable.customWidth;
        customTextAlign = mutable.customTextAlign;
        enabled = mutable.enabled;
        ignored = mutable.ignored;
    }

    @Nullable
    @Override
    public ItemWidth getCustomWidth() {
        return customWidth;
    }

    @Nullable
    @Override
    public TextAlign getCustomTextAlign() {
        return customTextAlign;
    }

    @Override
    public String getPackageName() {
        return packageName;
    }

    @Override
    public String getId() {
        return id;
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
    public Integer getCustomColor() {
        return customColor;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getCustomLabel() {
        return customLabel;
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

    @Override
    public Mutable toMutable() {
        return new Mutable(this);
    }

    @NonNull
    @Override
    public String toString() {
        return "DeepShortcut{" + getId() + "}";
    }

    public static class Mutable implements MutableDescriptor<DeepShortcutDescriptor>,
            CustomColorMutableDescriptor<DeepShortcutDescriptor>,
            CustomLabelMutableDescriptor<DeepShortcutDescriptor>,
            IgnorableMutableDescriptor<DeepShortcutDescriptor>,
            CustomLayoutMutableDescriptor<DeepShortcutDescriptor> {

        private final String id;
        private final String shortcutId;
        private final String packageName;
        private String originalLabel;
        private String label;
        private int color = Color.WHITE;
        private String customLabel;
        private Integer customColor;
        private ItemWidth customWidth;
        private TextAlign customTextAlign;
        private boolean enabled = true;
        private boolean ignored;

        public Mutable(String packageName, String shortcutId) {
            this.id = packageName + "/" + shortcutId;
            this.packageName = packageName;
            this.shortcutId = shortcutId;
        }

        public Mutable(DeepShortcutDescriptor descriptor) {
            id = descriptor.id;
            packageName = descriptor.packageName;
            shortcutId = descriptor.shortcutId;
            originalLabel = descriptor.originalLabel;
            label = descriptor.label;
            color = descriptor.color;
            customLabel = descriptor.customLabel;
            customColor = descriptor.customColor;
            customWidth = descriptor.customWidth;
            customTextAlign = descriptor.customTextAlign;
            enabled = descriptor.enabled;
            ignored = descriptor.ignored;
        }

        @Nullable
        @Override
        public ItemWidth getCustomWidth() {
            return customWidth;
        }

        @Override
        public void setCustomWidth(@Nullable ItemWidth width) {
            customWidth = width;
        }

        @Nullable
        @Override
        public TextAlign getCustomTextAlign() {
            return customTextAlign;
        }

        @Override
        public void setCustomTextAlign(@Nullable TextAlign textAlign) {
            customTextAlign = textAlign;
        }

        @Override
        public String getId() {
            return id;
        }

        public String getPackageName() {
            return packageName;
        }

        public String getShortcutId() {
            return shortcutId;
        }

        @Override
        public String getOriginalLabel() {
            return originalLabel;
        }

        @Override
        public void setOriginalLabel(String originalLabel) {
            this.originalLabel = originalLabel != null ? originalLabel : "";
        }

        @Override
        public String getLabel() {
            return label;
        }

        @Override
        public void setLabel(String label) {
            this.label = label;
        }

        @Override
        public Integer getCustomColor() {
            return customColor;
        }

        @Override
        public void setCustomColor(Integer customColor) {
            this.customColor = customColor;
        }

        @Override
        public String getCustomLabel() {
            return customLabel;
        }

        @Override
        public void setCustomLabel(String customLabel) {
            this.customLabel = customLabel;
        }

        @Override
        public boolean isIgnored() {
            return ignored;
        }

        @Override
        public void setIgnored(boolean ignored) {
            this.ignored = ignored;
        }

        @Override
        public int getColor() {
            return color;
        }

        @Override
        public void setColor(int color) {
            this.color = color;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public DeepShortcutDescriptor toDescriptor() {
            return new DeepShortcutDescriptor(this);
        }

        @NonNull
        @Override
        public String toString() {
            return "DeepShortcutDescriptor.Mutable{" + getId() + "}";
        }
    }
}
