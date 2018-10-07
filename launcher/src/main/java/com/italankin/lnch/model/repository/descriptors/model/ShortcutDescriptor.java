package com.italankin.lnch.model.repository.descriptors.model;

import android.support.annotation.Keep;

import com.google.gson.annotations.SerializedName;
import com.italankin.lnch.model.repository.descriptors.Descriptor;

public class ShortcutDescriptor implements Descriptor {

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

    @Override
    public int getVisibleColor() {
        return customColor != null ? customColor : color;
    }

    @Override
    public void setCustomColor(Integer color) {
        customColor = color;
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
    public boolean isHidden() {
        return false;
    }

    @Override
    public void setHidden(boolean hidden) {
        // empty
    }

    @Override
    public String toString() {
        return "Shortcut{" + uri + "}";
    }
}
