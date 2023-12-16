package com.italankin.lnch.model.descriptor.impl;

import android.graphics.Color;
import androidx.annotation.NonNull;
import com.italankin.lnch.model.descriptor.CustomColorDescriptor;
import com.italankin.lnch.model.descriptor.CustomLabelDescriptor;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.DescriptorModels;
import com.italankin.lnch.model.descriptor.mutable.CustomColorMutableDescriptor;
import com.italankin.lnch.model.descriptor.mutable.CustomLabelMutableDescriptor;
import com.italankin.lnch.model.descriptor.mutable.MutableDescriptor;
import com.italankin.lnch.model.repository.store.json.model.FolderDescriptorJson;
import com.italankin.lnch.model.ui.impl.FolderDescriptorUi;
import com.italankin.lnch.util.ListUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A home screen folder
 */
@DescriptorModels(
        json = FolderDescriptorJson.class,
        ui = FolderDescriptorUi.class,
        mutable = FolderDescriptor.Mutable.class
)
public final class FolderDescriptor implements Descriptor, CustomColorDescriptor, CustomLabelDescriptor {

    public final String id;
    public final String originalLabel;
    public final String label;
    public final String customLabel;
    public final int color;
    public final Integer customColor;
    public final List<String> items;

    public FolderDescriptor(Mutable mutable) {
        id = mutable.id;
        originalLabel = mutable.originalLabel;
        label = mutable.label;
        customLabel = mutable.customLabel;
        color = mutable.color;
        customColor = mutable.customColor;
        items = mutable.items;
    }

    @Override
    public String getOriginalLabel() {
        return originalLabel;
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
    public Integer getCustomColor() {
        return customColor;
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
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj.getClass() != FolderDescriptor.class) {
            return false;
        }
        FolderDescriptor that = (FolderDescriptor) obj;
        return this.id.equals(that.id);
    }

    @NonNull
    @Override
    public String toString() {
        return "Folder{" + getVisibleLabel() + '}';
    }

    @Override
    public Mutable toMutable() {
        return new FolderDescriptor.Mutable(this);
    }

    public static class Mutable implements MutableDescriptor<FolderDescriptor>,
            CustomColorMutableDescriptor<FolderDescriptor>,
            CustomLabelMutableDescriptor<FolderDescriptor> {

        private final String id;
        private String originalLabel;
        private String label;
        private String customLabel;
        private int color = Color.WHITE;
        private Integer customColor;
        private List<String> items = new ArrayList<>(1);

        public Mutable(String originalLabel) {
            this("folder/" + UUID.randomUUID().toString(), originalLabel);
        }

        public Mutable(String id, String originalLabel) {
            this.id = id;
            this.originalLabel = originalLabel;
        }

        public Mutable(FolderDescriptor descriptor) {
            id = descriptor.id;
            originalLabel = descriptor.originalLabel;
            label = descriptor.label;
            customLabel = descriptor.customLabel;
            color = descriptor.color;
            customColor = descriptor.customColor;
            items.addAll(descriptor.items);
        }

        @Override
        public String getId() {
            return id;
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
        public String getOriginalLabel() {
            return originalLabel;
        }

        @Override
        public void setOriginalLabel(String originalLabel) {
            this.originalLabel = originalLabel != null ? originalLabel : "";
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

        public void addItem(String descriptorId) {
            items.add(descriptorId);
        }

        public void removeItem(String descriptorId) {
            items.remove(descriptorId);
        }

        public void move(int from, int to) {
            ListUtils.move(items, from, to);
        }

        public List<String> getItems() {
            return items;
        }

        public void setItems(List<String> items) {
            this.items = new ArrayList<>(items);
        }

        @Override
        public FolderDescriptor toDescriptor() {
            return new FolderDescriptor(this);
        }

        @NonNull
        @Override
        public String toString() {
            return "FolderDescriptor.Mutable{" + getId() + "}";
        }
    }
}
