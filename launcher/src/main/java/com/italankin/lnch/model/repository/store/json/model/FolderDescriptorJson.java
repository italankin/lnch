package com.italankin.lnch.model.repository.store.json.model;

import com.google.gson.annotations.SerializedName;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.FolderDescriptor;

import java.util.List;

import androidx.annotation.Keep;

public final class FolderDescriptorJson implements DescriptorJson {

    public static final String TYPE = "folder";
    public static final String OLD_TYPE = "group";

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

    @SerializedName("items")
    public List<String> items;

    @Keep
    public FolderDescriptorJson() {
    }

    public FolderDescriptorJson(FolderDescriptor descriptor) {
        this.id = descriptor.id;
        this.label = descriptor.label;
        this.customLabel = descriptor.customLabel;
        this.color = descriptor.color;
        this.customColor = descriptor.customColor;
        this.items = descriptor.items;
    }

    @Override
    public Descriptor toDescriptor() {
        FolderDescriptor descriptor = new FolderDescriptor();
        descriptor.id = this.id;
        descriptor.label = this.label;
        descriptor.customLabel = this.customLabel;
        descriptor.color = this.color;
        descriptor.customColor = this.customColor;
        if (items != null) {
            descriptor.items.addAll(items);
        }
        return descriptor;
    }
}
