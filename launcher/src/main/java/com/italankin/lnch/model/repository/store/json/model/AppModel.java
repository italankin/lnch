package com.italankin.lnch.model.repository.store.json.model;

import com.google.gson.annotations.SerializedName;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;

import androidx.annotation.Keep;

public final class AppModel implements JsonModel {

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

    @SerializedName("hidden")
    public boolean hidden;

    @Keep
    public AppModel() {
    }

    public AppModel(AppDescriptor descriptor) {
        this.packageName = descriptor.packageName;
        this.versionCode = descriptor.versionCode;
        this.componentName = descriptor.componentName;
        this.label = descriptor.label;
        this.customLabel = descriptor.customLabel;
        this.color = descriptor.color;
        this.customColor = descriptor.customColor;
        this.hidden = descriptor.hidden;
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
        descriptor.hidden = this.hidden;
        return descriptor;
    }
}
