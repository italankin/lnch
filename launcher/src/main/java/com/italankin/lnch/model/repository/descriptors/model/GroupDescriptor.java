package com.italankin.lnch.model.repository.descriptors.model;

import android.support.annotation.Keep;

import com.google.gson.annotations.SerializedName;
import com.italankin.lnch.model.repository.descriptors.Descriptor;

public class GroupDescriptor implements Descriptor {

    @SerializedName("id")
    public String id;

    @SerializedName("label")
    public String label;

    @SerializedName("custom_label")
    public String customLabel;

    @SerializedName("color")
    public int color;

    @SerializedName("custom_color")
    public Integer customColor;

    @Keep
    public GroupDescriptor() {
    }

    public GroupDescriptor(String label, int color) {
        this.id = Long.toHexString(System.currentTimeMillis());
        this.label = label;
        this.color = color;
    }

    @Override
    public int getVisibleColor() {
        return customColor != null ? customColor : color;
    }

    @Override
    public void setCustomColor(Integer color) {
        this.customColor = color;
    }

    @Override
    public String getVisibleLabel() {
        return customLabel != null ? customLabel : label;
    }

    @Override
    public void setCustomLabel(String label) {
        this.customLabel = label;
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public void setHidden(boolean hidden) {
        // empty
    }

    @Override
    public int hashCode() {
        return label.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj.getClass() == GroupDescriptor.class;
    }

    @Override
    public String toString() {
        return "Group{" + label + '}';
    }
}
