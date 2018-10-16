package com.italankin.lnch.model.repository.descriptors.model;

import android.support.annotation.Keep;

import com.google.gson.annotations.SerializedName;
import com.italankin.lnch.model.repository.descriptors.CustomColorDescriptor;
import com.italankin.lnch.model.repository.descriptors.CustomLabelDescriptor;
import com.italankin.lnch.model.repository.descriptors.Descriptor;

import java.util.UUID;

public class ShortcutDescriptor implements Descriptor, CustomColorDescriptor, CustomLabelDescriptor {

    private static final String UNKNOWN_NAME = "???";

    @SerializedName("id")
    public String id;

    @SerializedName("uri")
    public String uri;

    @SerializedName("label")
    public String label;

    @SerializedName("color")
    public int color;

    @SerializedName("custom_label")
    public String customLabel;

    @SerializedName("custom_color")
    public Integer customColor;

    @Keep
    public ShortcutDescriptor() {
    }

    public ShortcutDescriptor(String uri, String label) {
        this.id = UUID.randomUUID().toString();
        this.uri = uri;
        this.label = label != null ? label.toUpperCase() : UNKNOWN_NAME;
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
    public int getVisibleColor() {
        return customColor != null ? customColor : color;
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
    public String getVisibleLabel() {
        return customLabel != null ? customLabel : label;
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
    public boolean equals(Object obj) {
        return this == obj || obj.getClass() == ShortcutDescriptor.class;
    }

    @Override
    public String toString() {
        return "Shortcut{" + uri + "}";
    }
}
