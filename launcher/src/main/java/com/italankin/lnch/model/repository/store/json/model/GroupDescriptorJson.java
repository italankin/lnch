package com.italankin.lnch.model.repository.store.json.model;

import com.google.gson.annotations.SerializedName;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.GroupDescriptor;

import androidx.annotation.Keep;

public final class GroupDescriptorJson implements DescriptorJson {

    public static final String TYPE = "group";

    @Keep
    @SerializedName(PROPERTY_TYPE)
    public String type = TYPE;

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
    public GroupDescriptorJson() {
    }

    public GroupDescriptorJson(GroupDescriptor descriptor) {
        this.id = descriptor.id;
        this.label = descriptor.label;
        this.customLabel = descriptor.customLabel;
        this.color = descriptor.color;
        this.customColor = descriptor.customColor;
    }

    @Override
    public Descriptor toDescriptor() {
        GroupDescriptor descriptor = new GroupDescriptor();
        descriptor.id = this.id;
        descriptor.label = this.label;
        descriptor.customLabel = this.customLabel;
        descriptor.color = this.color;
        descriptor.customColor = this.customColor;
        return descriptor;
    }
}
