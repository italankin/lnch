package com.italankin.lnch.model.descriptor.impl;

import com.italankin.lnch.model.descriptor.CustomColorDescriptor;
import com.italankin.lnch.model.descriptor.CustomLabelDescriptor;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.HiddenDescriptor;

public final class AppDescriptor implements Descriptor, CustomColorDescriptor, CustomLabelDescriptor,
        HiddenDescriptor {

    public String packageName;
    public long versionCode;
    public String componentName;
    public String label;
    public String customLabel;
    public int color;
    public Integer customColor;
    public boolean hidden;

    public AppDescriptor() {
    }

    public AppDescriptor(String packageName) {
        this.packageName = packageName;
    }

    @Override
    public String getId() {
        return componentName != null ? componentName : packageName;
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
    public void setCustomColor(Integer color) {
        customColor = color;
    }

    @Override
    public Integer getCustomColor() {
        return customColor;
    }

    @Override
    public int getColor() {
        return color;
    }

    @Override
    public boolean isHidden() {
        return hidden;
    }

    @Override
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    @Override
    public String toString() {
        return "App{" + packageName + (hidden ? "*" : "") + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AppDescriptor that = (AppDescriptor) o;
        if (!packageName.equals(that.packageName)) {
            return false;
        }
        return componentName != null
                ? componentName.equals(that.componentName)
                : that.componentName == null;
    }

    @Override
    public int hashCode() {
        int result = packageName.hashCode();
        result = 31 * result + (componentName != null ? componentName.hashCode() : 0);
        return result;
    }
}
