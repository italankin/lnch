package com.italankin.lnch.model.descriptor.impl;

import android.graphics.Color;
import androidx.annotation.NonNull;
import com.italankin.lnch.model.descriptor.*;
import com.italankin.lnch.model.descriptor.mutable.CustomColorMutableDescriptor;
import com.italankin.lnch.model.descriptor.mutable.CustomLabelMutableDescriptor;
import com.italankin.lnch.model.descriptor.mutable.IgnorableMutableDescriptor;
import com.italankin.lnch.model.descriptor.mutable.MutableDescriptor;
import com.italankin.lnch.model.repository.store.json.model.PinnedShortcutDescriptorJson;
import com.italankin.lnch.model.ui.impl.PinnedShortcutDescriptorUi;

import java.util.UUID;

/**
 * Pinned intent for {@code com.android.launcher.action.INSTALL_SHORTCUT}
 */
@DescriptorModels(
        json = PinnedShortcutDescriptorJson.class,
        ui = PinnedShortcutDescriptorUi.class,
        mutable = PinnedShortcutDescriptor.Mutable.class
)
public final class PinnedShortcutDescriptor implements Descriptor, CustomColorDescriptor, CustomLabelDescriptor,
        IgnorableDescriptor {

    public final String id;
    public final String uri;
    public final String originalLabel;
    public final String label;
    public final int color;
    public final String customLabel;
    public final Integer customColor;
    public final boolean ignored;

    public PinnedShortcutDescriptor(Mutable mutable) {
        id = mutable.id;
        uri = mutable.uri;
        originalLabel = mutable.originalLabel;
        label = mutable.label;
        color = mutable.color;
        customLabel = mutable.customLabel;
        customColor = mutable.customColor;
        ignored = mutable.ignored;
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
        return label != null ? label : getOriginalLabel();
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

    @NonNull
    @Override
    public String toString() {
        return "Shortcut{" + uri + "}";
    }

    @Override
    public Mutable toMutable() {
        return new Mutable(this);
    }

    public static class Mutable implements MutableDescriptor<PinnedShortcutDescriptor>,
            CustomColorMutableDescriptor<PinnedShortcutDescriptor>,
            CustomLabelMutableDescriptor<PinnedShortcutDescriptor>,
            IgnorableMutableDescriptor<PinnedShortcutDescriptor> {

        private final String id;
        private final String uri;
        private String originalLabel;
        private String label;
        private int color = Color.WHITE;
        private String customLabel;
        private Integer customColor;
        private boolean ignored;

        public Mutable(String uri, String originalLabel) {
            this("shortcut/" + UUID.randomUUID().toString(), uri, originalLabel);
        }

        public Mutable(String id, String uri, String originalLabel) {
            this.id = id;
            this.uri = uri;
            this.originalLabel = originalLabel;
        }

        public Mutable(PinnedShortcutDescriptor descriptor) {
            id = descriptor.id;
            uri = descriptor.uri;
            originalLabel = descriptor.originalLabel;
            label = descriptor.label;
            color = descriptor.color;
            customLabel = descriptor.customLabel;
            customColor = descriptor.customColor;
            ignored = descriptor.ignored;
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
        public void setOriginalLabel(String originalLabel) {
            this.originalLabel = originalLabel != null ? originalLabel : "";
        }

        public String getUri() {
            return uri;
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
        public int getColor() {
            return color;
        }

        @Override
        public void setColor(int color) {
            this.color = color;
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
        public PinnedShortcutDescriptor toDescriptor() {
            return new PinnedShortcutDescriptor(this);
        }
    }
}
