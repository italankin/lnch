package com.italankin.lnch.model.ui.impl;

import android.content.Intent;

import com.italankin.lnch.model.descriptor.impl.IntentDescriptor;
import com.italankin.lnch.model.ui.CustomColorDescriptorUi;
import com.italankin.lnch.model.ui.CustomLabelDescriptorUi;
import com.italankin.lnch.model.ui.DescriptorUi;
import com.italankin.lnch.model.ui.RemovableDescriptorUi;
import com.italankin.lnch.model.ui.VisibleDescriptorUi;
import com.italankin.lnch.util.IntentUtils;

import java.util.Objects;

public final class IntentDescriptorUi implements DescriptorUi,
        CustomLabelDescriptorUi,
        CustomColorDescriptorUi,
        RemovableDescriptorUi,
        VisibleDescriptorUi {

    public final Intent intent;
    private final IntentDescriptor descriptor;
    private final String label;
    private final int color;
    private String customLabel;
    private Integer customColor;
    private boolean visible = true;

    public IntentDescriptorUi(IntentDescriptor descriptor) {
        this.intent = IntentUtils.fromUri(descriptor.intentUri, Intent.URI_INTENT_SCHEME);
        this.descriptor = descriptor;
        this.label = descriptor.label;
        this.customLabel = descriptor.customLabel;
        this.color = descriptor.color;
        this.customColor = descriptor.customColor;
    }

    @Override
    public IntentDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public int getColor() {
        return color;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getCustomLabel() {
        return customLabel;
    }

    @Override
    public void setCustomLabel(String label) {
        this.customLabel = label;
    }

    @Override
    public void setCustomColor(Integer color) {
        this.customColor = color;
    }

    @Override
    public Integer getCustomColor() {
        return customColor;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public String toString() {
        return descriptor.toString();
    }

    @Override
    public boolean is(DescriptorUi another) {
        if (this == another) {
            return true;
        }
        if (this.getClass() != another.getClass()) {
            return false;
        }
        IntentDescriptorUi that = (IntentDescriptorUi) another;
        return this.descriptor.equals(that.descriptor)
                && this.visible == that.visible;
    }

    @Override
    public boolean deepEquals(DescriptorUi another) {
        if (this == another) {
            return true;
        }
        if (this.getClass() != another.getClass()) {
            return false;
        }
        IntentDescriptorUi that = (IntentDescriptorUi) another;
        return this.descriptor.equals(that.descriptor)
                && Objects.equals(this.customLabel, that.customLabel)
                && Objects.equals(this.customColor, that.customColor)
                && this.visible == that.visible;
    }
}
