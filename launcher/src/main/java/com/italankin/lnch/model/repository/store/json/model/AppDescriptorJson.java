package com.italankin.lnch.model.repository.store.json.model;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.descriptor.props.ItemWidth;
import com.italankin.lnch.model.descriptor.props.TextAlign;

import java.util.List;

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

    @SerializedName("custom_badge_color")
    public Integer customBadgeColor;

    @SerializedName(value = "ignored", alternate = "hidden")
    public Boolean ignored;

    @SerializedName("search_flags")
    public Integer searchFlags;

    @SerializedName("show_shortcuts")
    public Boolean showShortcuts;

    @SerializedName("aliases")
    public List<String> aliases;

    @Keep
    public AppDescriptorJson() {
    }

    public AppDescriptorJson(AppDescriptor descriptor) {
        this.packageName = descriptor.packageName;
        this.versionCode = descriptor.versionCode;
        this.componentName = descriptor.componentName;
        this.originalLabel = descriptor.originalLabel;
        this.label = descriptor.label;
        this.customLabel = descriptor.customLabel;
        this.color = descriptor.color;
        this.customColor = descriptor.customColor;
        this.customWidth = descriptor.customWidth != null ? descriptor.customWidth.key() : null;
        this.customTextAlign = descriptor.customTextAlign != null ? descriptor.customTextAlign.key() : null;
        this.customBadgeColor = descriptor.customBadgeColor;
        this.ignored = descriptor.ignored ? true : null;
        this.searchFlags = descriptor.searchFlags != AppDescriptor.SEARCH_DEFAULT_FLAGS ? descriptor.searchFlags : null;
        this.showShortcuts = descriptor.showShortcuts ? null : false;
        this.aliases = descriptor.aliases.isEmpty() ? null : descriptor.aliases;
    }

    @Override
    public Descriptor toDescriptor() {
        AppDescriptor.Mutable mutable = new AppDescriptor.Mutable(packageName, componentName, versionCode, originalLabel);
        mutable.setColor(color);
        mutable.setCustomColor(customColor);
        mutable.setCustomWidth(ItemWidth.fromKey(customWidth));
        mutable.setCustomTextAlign(TextAlign.fromKey(customTextAlign));
        mutable.setLabel(label);
        mutable.setCustomLabel(customLabel);
        mutable.setCustomBadgeColor(customBadgeColor);
        mutable.setIgnored(ignored != null && ignored);
        mutable.setSearchFlags(searchFlags != null ? searchFlags : AppDescriptor.SEARCH_DEFAULT_FLAGS);
        mutable.setShowShortcuts(showShortcuts == null || showShortcuts);
        mutable.setAliases(aliases);
        return mutable.toDescriptor();
    }
}
