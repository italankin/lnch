package com.italankin.lnch.model.repository.store.json.model;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.IntentDescriptor;
import com.italankin.lnch.model.descriptor.props.ItemWidth;
import com.italankin.lnch.model.descriptor.props.TextAlign;

public final class IntentDescriptorJson implements DescriptorJson {

    public static final String TYPE = "intent";

    @Keep
    @SerializedName(PROPERTY_TYPE)
    public String type = TYPE;

    @SerializedName("id")
    public String id;

    @SerializedName("intent_uri")
    public String intentUri;

    @SerializedName("original_label")
    public String originalLabel;

    @SerializedName("label")
    public String label;

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

    @SerializedName("ignored")
    public Boolean ignored;

    @Keep
    public IntentDescriptorJson() {
    }

    public IntentDescriptorJson(IntentDescriptor descriptor) {
        this.id = descriptor.id;
        this.intentUri = descriptor.intentUri;
        this.originalLabel = descriptor.originalLabel;
        this.label = descriptor.label;
        this.customLabel = descriptor.customLabel;
        this.color = descriptor.color;
        this.customColor = descriptor.customColor;
        this.customWidth = descriptor.customWidth != null ? descriptor.customWidth.key() : null;
        this.customTextAlign = descriptor.customTextAlign != null ? descriptor.customTextAlign.key() : null;
        this.ignored = descriptor.ignored ? true : null;
    }

    @Override
    public Descriptor toDescriptor() {
        IntentDescriptor.Mutable mutable = new IntentDescriptor.Mutable(id, intentUri, originalLabel);
        mutable.setLabel(label);
        mutable.setCustomLabel(customLabel);
        mutable.setColor(color);
        mutable.setCustomColor(customColor);
        mutable.setCustomWidth(ItemWidth.fromKey(customWidth));
        mutable.setCustomTextAlign(TextAlign.fromKey(customTextAlign));
        mutable.setIgnored(ignored != null && ignored);
        return mutable.toDescriptor();
    }
}
