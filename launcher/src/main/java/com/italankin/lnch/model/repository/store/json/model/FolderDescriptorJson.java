package com.italankin.lnch.model.repository.store.json.model;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.FolderDescriptor;
import com.italankin.lnch.model.descriptor.props.ItemWidth;
import com.italankin.lnch.model.descriptor.props.TextAlign;

import java.util.List;

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

    @SerializedName("original_label")
    public String originalLabel;

    @SerializedName("custom_label")
    public String customLabel;

    @SerializedName("color")
    public int color;

    @SerializedName("custom_color")
    public Integer customColor;

    @SerializedName("custom_width")
    public String customWidth;

    @SerializedName("custom_text_align")
    public String customTextAlign;

    @SerializedName("items")
    public List<String> items;

    @Keep
    public FolderDescriptorJson() {
    }

    public FolderDescriptorJson(FolderDescriptor descriptor) {
        this.id = descriptor.id;
        this.originalLabel = descriptor.originalLabel;
        this.label = descriptor.label;
        this.customLabel = descriptor.customLabel;
        this.color = descriptor.color;
        this.customColor = descriptor.customColor;
        this.customWidth = descriptor.customWidth != null ? descriptor.customWidth.key() : null;
        this.customTextAlign = descriptor.customTextAlign != null ? descriptor.customTextAlign.key() : null;
        this.items = descriptor.items;
    }

    @Override
    public Descriptor toDescriptor() {
        FolderDescriptor.Mutable mutable = new FolderDescriptor.Mutable(id, originalLabel);
        mutable.setLabel(label);
        mutable.setCustomLabel(customLabel);
        mutable.setColor(color);
        mutable.setCustomColor(customColor);
        mutable.setCustomWidth(ItemWidth.fromKey(customWidth));
        mutable.setCustomTextAlign(TextAlign.fromKey(customTextAlign));
        if (items != null) {
            mutable.setItems(items);
        }
        return mutable.toDescriptor();
    }
}
