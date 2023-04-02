package com.italankin.lnch.model.descriptor.impl;

import com.italankin.lnch.model.descriptor.CustomColorDescriptor;
import com.italankin.lnch.model.descriptor.CustomLabelDescriptor;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.DescriptorModels;
import com.italankin.lnch.model.repository.store.json.model.FolderDescriptorJson;
import com.italankin.lnch.model.ui.impl.FolderDescriptorUi;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;

/**
 * A home screen folder
 */
@DescriptorModels(json = FolderDescriptorJson.class, ui = FolderDescriptorUi.class)
public final class FolderDescriptor implements Descriptor, CustomColorDescriptor, CustomLabelDescriptor {

    public String id;
    public String originalLabel;
    public String label;
    public String customLabel;
    public int color;
    public Integer customColor;
    public final List<String> items = new ArrayList<>(4);

    public FolderDescriptor() {
    }

    public FolderDescriptor(String label, int color) {
        this.id = "folder/" + UUID.randomUUID().toString();
        this.originalLabel = this.label = label;
        this.color = color;
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
    public void setCustomColor(Integer color) {
        this.customColor = color;
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
    public void setCustomLabel(String label) {
        this.label = label;
        this.originalLabel = label;
        this.customLabel = label;
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
    public FolderDescriptor copy() {
        FolderDescriptor copy = new FolderDescriptor();
        copy.id = id;
        copy.originalLabel = copy.label = label;
        copy.customLabel = customLabel;
        copy.color = color;
        copy.customColor = customColor;
        copy.items.addAll(items);
        return copy;
    }
}
