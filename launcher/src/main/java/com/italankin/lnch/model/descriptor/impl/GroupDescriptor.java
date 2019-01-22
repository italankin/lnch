package com.italankin.lnch.model.descriptor.impl;

import com.italankin.lnch.model.descriptor.CustomColorDescriptor;
import com.italankin.lnch.model.descriptor.CustomLabelDescriptor;
import com.italankin.lnch.model.descriptor.Descriptor;

import java.util.UUID;

public final class GroupDescriptor implements Descriptor, CustomColorDescriptor, CustomLabelDescriptor {

    public String id;
    public String label;
    public String customLabel;
    public int color;
    public Integer customColor;

    public GroupDescriptor() {
    }

    public GroupDescriptor(String label, int color) {
        this.id = "group/" + UUID.randomUUID().toString();
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
        this.customColor = color;
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
        if (obj.getClass() != GroupDescriptor.class) {
            return false;
        }
        GroupDescriptor that = (GroupDescriptor) obj;
        return this.id.equals(that.id);
    }

    @Override
    public String toString() {
        return "Group{" + getVisibleLabel() + '}';
    }
}
