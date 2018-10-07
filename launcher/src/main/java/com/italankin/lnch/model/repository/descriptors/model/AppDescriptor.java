package com.italankin.lnch.model.repository.descriptors.model;

import android.support.annotation.Keep;

import com.google.gson.annotations.SerializedName;
import com.italankin.lnch.model.repository.descriptors.Descriptor;

import java.util.Comparator;

public class AppDescriptor implements Descriptor {

    @SerializedName("package_name")
    public String packageName;

    @SerializedName("version_code")
    public long versionCode;

    @SerializedName("component_name")
    public String componentName;

    @SerializedName("label")
    public String label;

    @SerializedName("custom_label")
    public String customLabel;

    @SerializedName("color")
    public int color;

    @SerializedName("custom_color")
    public Integer customColor;

    @SerializedName("hidden")
    public boolean hidden;

    @Keep
    public AppDescriptor() {
    }

    public AppDescriptor(String packageName) {
        this.packageName = packageName;
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
    public void setCustomColor(Integer color) {
        customColor = color;
    }

    @Override
    public int getVisibleColor() {
        return customColor != null ? customColor : color;
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
        return "App{" + packageName + ", " + hidden + "}";
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

    static class NameComparator implements Comparator<AppDescriptor> {
        private final boolean asc;

        NameComparator(boolean asc) {
            this.asc = asc;
        }

        @Override
        public int compare(AppDescriptor lhs, AppDescriptor rhs) {
            int compare = String.CASE_INSENSITIVE_ORDER.compare(lhs.label, rhs.label);
            return asc ? compare : -compare;
        }
    }

}
