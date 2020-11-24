package com.italankin.lnch.model.repository.store.json.model;

import com.google.gson.annotations.SerializedName;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;

import androidx.annotation.Keep;

public final class AppDescriptorJson implements DescriptorJson {

    public static final String TYPE = "app";

    @Keep
    @SerializedName(PROPERTY_TYPE)
    public String type = TYPE;

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

    @SerializedName(value = "ignored", alternate = "hidden")
    public boolean ignored;

    @SerializedName("search_visible")
    public Boolean searchVisible;

    @SerializedName("shortcuts_search_visible")
    public Boolean shortcutsSearchVisible;

    @Keep
    public AppDescriptorJson() {
    }

    public AppDescriptorJson(AppDescriptor descriptor) {
        this.packageName = descriptor.packageName;
        this.versionCode = descriptor.versionCode;
        this.componentName = descriptor.componentName;
        this.label = descriptor.label;
        this.customLabel = descriptor.customLabel;
        this.color = descriptor.color;
        this.customColor = descriptor.customColor;
        this.ignored = descriptor.ignored;
        this.searchVisible = descriptor.searchVisible ? null : false;
        this.shortcutsSearchVisible = descriptor.shortcutsSearchVisible ? null : false;
    }

    @Override
    public Descriptor toDescriptor() {
        AppDescriptor descriptor = new AppDescriptor();
        descriptor.packageName = this.packageName;
        descriptor.versionCode = this.versionCode;
        descriptor.componentName = this.componentName;
        descriptor.label = this.label;
        descriptor.customLabel = this.customLabel;
        descriptor.color = this.color;
        descriptor.customColor = this.customColor;
        descriptor.ignored = this.ignored;
        descriptor.searchVisible = this.searchVisible == null || this.searchVisible;
        descriptor.shortcutsSearchVisible = this.shortcutsSearchVisible == null || this.shortcutsSearchVisible;
        return descriptor;
    }
}
