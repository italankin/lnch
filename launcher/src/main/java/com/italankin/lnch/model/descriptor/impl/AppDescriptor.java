package com.italankin.lnch.model.descriptor.impl;

import android.content.ComponentName;

import com.italankin.lnch.model.descriptor.AliasDescriptor;
import com.italankin.lnch.model.descriptor.CustomColorDescriptor;
import com.italankin.lnch.model.descriptor.CustomLabelDescriptor;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.IgnorableDescriptor;
import com.italankin.lnch.model.descriptor.PackageDescriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;

/**
 * Application (each {@link ComponentName} will have its own {@link AppDescriptor})
 */
public final class AppDescriptor implements Descriptor, PackageDescriptor, CustomColorDescriptor,
        CustomLabelDescriptor, IgnorableDescriptor, AliasDescriptor {

    public static final int FLAG_SEARCH_VISIBLE = 0x1;
    public static final int FLAG_SEARCH_SHORTCUTS_VISIBLE = 0x2;
    public static final int SEARCH_DEFAULT_FLAGS = FLAG_SEARCH_VISIBLE | FLAG_SEARCH_SHORTCUTS_VISIBLE;

    public String packageName;
    public long versionCode;
    public String componentName;
    public String label;
    public String customLabel;
    public int color;
    public Integer customColor;
    public boolean ignored;
    public int searchFlags = SEARCH_DEFAULT_FLAGS;
    public boolean showShortcuts = true;
    public List<String> aliases = new ArrayList<>();
    private ComponentName componentNameValue;

    public AppDescriptor() {
    }

    public AppDescriptor(String packageName) {
        this.packageName = packageName;
    }

    @Override
    public String getPackageName() {
        return packageName;
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
    public boolean isIgnored() {
        return ignored;
    }

    @Override
    public void setIgnored(boolean ignored) {
        this.ignored = ignored;
    }

    @Override
    public void setAliases(List<String> aliases) {
        this.aliases = aliases != null ? aliases : new ArrayList<>();
    }

    @Override
    public List<String> getAliases() {
        return aliases;
    }

    @NonNull
    @Override
    public String toString() {
        return "App{" + packageName + (ignored ? "*" : "") + "}";
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
        return Objects.equals(componentName, that.componentName);
    }

    @Override
    public int hashCode() {
        int result = packageName.hashCode();
        result = 31 * result + (componentName != null ? componentName.hashCode() : 0);
        return result;
    }

    public ComponentName getComponentName() {
        if (componentNameValue == null && componentName != null) {
            componentNameValue = ComponentName.unflattenFromString(componentName);
        }
        return componentNameValue;
    }

    @Override
    public AppDescriptor copy() {
        AppDescriptor copy = new AppDescriptor(packageName);
        copy.versionCode = versionCode;
        copy.componentName = componentName;
        copy.label = label;
        copy.customLabel = customLabel;
        copy.color = color;
        copy.customColor = customColor;
        copy.ignored = ignored;
        copy.searchFlags = searchFlags;
        copy.showShortcuts = showShortcuts;
        copy.aliases = new ArrayList<>(aliases);
        copy.componentNameValue = componentNameValue;
        return copy;
    }
}
