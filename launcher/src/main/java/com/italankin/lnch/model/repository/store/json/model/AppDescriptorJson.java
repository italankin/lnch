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
    public Boolean ignored;

    @SerializedName("search_flags")
    public Integer searchFlags;

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
        this.ignored = descriptor.ignored ? true : null;
        this.searchFlags = descriptor.searchFlags != AppDescriptor.SEARCH_DEFAULT_FLAGS ? descriptor.searchFlags : null;
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
        descriptor.ignored = this.ignored == null ? false : this.ignored;
        descriptor.searchFlags = this.searchFlags != null ? this.searchFlags : AppDescriptor.SEARCH_DEFAULT_FLAGS;
        return descriptor;
    }
}
