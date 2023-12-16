package com.italankin.lnch.model.descriptor.impl;

import android.content.ComponentName;
import android.graphics.Color;
import androidx.annotation.NonNull;
import com.italankin.lnch.model.descriptor.*;
import com.italankin.lnch.model.descriptor.mutable.*;
import com.italankin.lnch.model.repository.store.json.model.AppDescriptorJson;
import com.italankin.lnch.model.ui.impl.AppDescriptorUi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Application (each {@link ComponentName} will have its own {@link AppDescriptor})
 */
@DescriptorModels(
        json = AppDescriptorJson.class,
        ui = AppDescriptorUi.class,
        mutable = AppDescriptor.Mutable.class
)
public final class AppDescriptor implements Descriptor, PackageDescriptor, CustomColorDescriptor,
        CustomLabelDescriptor, IgnorableDescriptor, AliasDescriptor {

    public static final int FLAG_SEARCH_VISIBLE = 0x1;
    public static final int FLAG_SEARCH_SHORTCUTS_VISIBLE = 0x2;
    public static final int SEARCH_DEFAULT_FLAGS = FLAG_SEARCH_VISIBLE | FLAG_SEARCH_SHORTCUTS_VISIBLE;

    private static String makeId(String packageName, String componentName) {
        return componentName != null ? componentName : packageName;
    }

    private final String id;
    public final String packageName;
    public final long versionCode;
    public final String componentName;
    public final String originalLabel;
    public final String label;
    public final String customLabel;
    public final int color;
    public final Integer customColor;
    public final Integer customBadgeColor;
    public final boolean ignored;
    public final int searchFlags;
    public final boolean showShortcuts;
    public final List<String> aliases;
    private ComponentName componentNameValue;

    public AppDescriptor(Mutable mutable) {
        id = mutable.getId();
        packageName = mutable.packageName;
        versionCode = mutable.versionCode;
        componentName = mutable.componentName;
        originalLabel = mutable.originalLabel;
        label = mutable.label;
        customLabel = mutable.customLabel;
        color = mutable.color;
        customColor = mutable.customColor;
        customBadgeColor = mutable.customBadgeColor;
        ignored = mutable.ignored;
        searchFlags = mutable.searchFlags;
        showShortcuts = mutable.showShortcuts;
        aliases = Collections.unmodifiableList(mutable.aliases);
    }

    @Override
    public String getPackageName() {
        return packageName;
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
        return label != null ? label : getOriginalLabel();
    }

    @Override
    public String getCustomLabel() {
        return customLabel;
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
        if (componentName != null) {
            result = 31 * result + componentName.hashCode();
        }
        return result;
    }

    public ComponentName getComponentName() {
        if (componentNameValue == null && componentName != null) {
            componentNameValue = ComponentName.unflattenFromString(componentName);
        }
        return componentNameValue;
    }

    @Override
    public Mutable toMutable() {
        return new Mutable(this);
    }

    public static class Mutable implements MutableDescriptor<AppDescriptor>,
            CustomColorMutableDescriptor<AppDescriptor>,
            CustomLabelMutableDescriptor<AppDescriptor>,
            IgnorableMutableDescriptor<AppDescriptor>,
            AliasMutableDescriptor<AppDescriptor> {

        private final String packageName;
        private String componentName;
        private String originalLabel;
        private long versionCode;
        private String label;
        private String customLabel;
        private int color = Color.WHITE;
        private Integer customColor;
        private Integer customBadgeColor;
        private boolean ignored;
        private int searchFlags = SEARCH_DEFAULT_FLAGS;
        private boolean showShortcuts = true;
        private List<String> aliases = new ArrayList<>(0);

        public Mutable(String packageName, String componentName, long versionCode, String originalLabel) {
            this.packageName = packageName;
            this.componentName = componentName;
            this.versionCode = versionCode;
            this.originalLabel = originalLabel;
        }

        public Mutable(AppDescriptor descriptor) {
            packageName = descriptor.packageName;
            componentName = descriptor.componentName;
            originalLabel = descriptor.originalLabel;
            versionCode = descriptor.versionCode;
            label = descriptor.label;
            customLabel = descriptor.customLabel;
            color = descriptor.color;
            customColor = descriptor.customColor;
            customBadgeColor = descriptor.customBadgeColor;
            ignored = descriptor.ignored;
            searchFlags = descriptor.searchFlags;
            showShortcuts = descriptor.showShortcuts;
            aliases = new ArrayList<>(descriptor.aliases);
        }

        @Override
        public String getId() {
            return makeId(packageName, componentName);
        }

        public String getPackageName() {
            return packageName;
        }

        public long getVersionCode() {
            return versionCode;
        }

        public void setVersionCode(long versionCode) {
            this.versionCode = versionCode;
        }

        public String getComponentName() {
            return componentName;
        }

        public void setComponentName(String componentName) {
            this.componentName = componentName;
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
        public String getCustomLabel() {
            return customLabel;
        }

        @Override
        public void setCustomLabel(String customLabel) {
            this.customLabel = customLabel;
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
        public int getColor() {
            return color;
        }

        @Override
        public void setColor(int color) {
            this.color = color;
        }

        public void setCustomBadgeColor(Integer color) {
            this.customBadgeColor = color;
        }

        public void setShowShortcuts(boolean showShortcuts) {
            this.showShortcuts = showShortcuts;
        }

        @Override
        public List<String> getAliases() {
            return aliases;
        }

        @Override
        public void setAliases(List<String> aliases) {
            this.aliases = aliases != null ? aliases : new ArrayList<>(0);
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
        public boolean isIgnored() {
            return ignored;
        }

        @Override
        public void setIgnored(boolean ignored) {
            this.ignored = ignored;
        }

        public void setSearchFlags(int flags) {
            this.searchFlags = flags;
        }

        @Override
        public AppDescriptor toDescriptor() {
            return new AppDescriptor(this);
        }
    }
}
