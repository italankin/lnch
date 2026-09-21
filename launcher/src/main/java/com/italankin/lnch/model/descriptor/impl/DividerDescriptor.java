package com.italankin.lnch.model.descriptor.impl;

import android.graphics.Color;

import com.italankin.lnch.model.descriptor.CustomColorDescriptor;
import com.italankin.lnch.model.descriptor.CustomLabelDescriptor;
import com.italankin.lnch.model.descriptor.DescriptorModels;
import com.italankin.lnch.model.descriptor.mutable.CustomColorMutableDescriptor;
import com.italankin.lnch.model.descriptor.mutable.CustomLabelMutableDescriptor;
import com.italankin.lnch.model.repository.store.json.model.DividerDescriptorJson;
import com.italankin.lnch.model.ui.impl.DividerDescriptorUi;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

@DescriptorModels(
        json = DividerDescriptorJson.class,
        ui = DividerDescriptorUi.class,
        mutable = DividerDescriptor.Mutable.class
)
public final class DividerDescriptor implements CustomLabelDescriptor, CustomColorDescriptor {
    public final String id;
    public final String originalLabel;
    public final String label;
    public final String customLabel;
    public final int color;
    public final Integer customColor;

    public DividerDescriptor(Mutable mutable) {
        id = mutable.id;
        originalLabel = mutable.originalLabel;
        label = mutable.label;
        customLabel = mutable.customLabel;
        color = mutable.color;
        customColor = mutable.customColor;
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
    public String getLabel() {
        return label != null ? label : originalLabel;
    }

    @Override
    public String getCustomLabel() {
        return customLabel;
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
    public Mutable toMutable() {
        return new Mutable(this);
    }

    // Dividers organize the home screen; they are not search results.
    @Override
    public Set<String> getSearchTokens() {
        return Collections.emptySet();
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof DividerDescriptor && id.equals(((DividerDescriptor) other).id);
    }

    public static class Mutable implements CustomLabelMutableDescriptor<DividerDescriptor>,
            CustomColorMutableDescriptor<DividerDescriptor> {
        private final String id;
        private String originalLabel = "";
        private String label;
        private String customLabel;
        private int color = Color.WHITE;
        private Integer customColor;

        public Mutable() {
            this("divider/" + UUID.randomUUID());
        }

        public Mutable(String id) {
            this.id = id;
        }

        public Mutable(DividerDescriptor descriptor) {
            id = descriptor.id;
            originalLabel = descriptor.originalLabel;
            label = descriptor.label;
            customLabel = descriptor.customLabel;
            color = descriptor.color;
            customColor = descriptor.customColor;
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
        public void setOriginalLabel(String label) {
            originalLabel = label != null ? label : "";
        }

        @Override
        public String getLabel() {
            return label != null ? label : originalLabel;
        }

        @Override
        public void setLabel(String label) {
            this.label = label;
        }

        @Override
        public String getCustomLabel() {
            return customLabel;
        }

        @Override
        public void setCustomLabel(String label) {
            customLabel = label;
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
        public DividerDescriptor toDescriptor() {
            return new DividerDescriptor(this);
        }
    }
}
