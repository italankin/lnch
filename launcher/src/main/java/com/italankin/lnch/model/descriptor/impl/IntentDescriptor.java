package com.italankin.lnch.model.descriptor.impl;

import android.content.Intent;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.italankin.lnch.model.descriptor.CustomColorDescriptor;
import com.italankin.lnch.model.descriptor.CustomLabelDescriptor;
import com.italankin.lnch.model.descriptor.CustomLayoutDescriptor;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.DescriptorModels;
import com.italankin.lnch.model.descriptor.IgnorableDescriptor;
import com.italankin.lnch.model.descriptor.mutable.CustomColorMutableDescriptor;
import com.italankin.lnch.model.descriptor.mutable.CustomLabelMutableDescriptor;
import com.italankin.lnch.model.descriptor.mutable.CustomLayoutMutableDescriptor;
import com.italankin.lnch.model.descriptor.mutable.IgnorableMutableDescriptor;
import com.italankin.lnch.model.descriptor.mutable.MutableDescriptor;
import com.italankin.lnch.model.descriptor.props.ItemWidth;
import com.italankin.lnch.model.descriptor.props.TextAlign;
import com.italankin.lnch.model.repository.store.json.model.IntentDescriptorJson;
import com.italankin.lnch.model.ui.impl.IntentDescriptorUi;

import java.util.UUID;

/**
 * Custom intent descriptor (e.g. search intent)
 */
@DescriptorModels(
        json = IntentDescriptorJson.class,
        ui = IntentDescriptorUi.class,
        mutable = IntentDescriptor.Mutable.class
)
public final class IntentDescriptor implements Descriptor, CustomColorDescriptor, CustomLabelDescriptor,
        IgnorableDescriptor, CustomLayoutDescriptor {

    public static final String EXTRA_CUSTOM_INTENT = "com.italankin.lnch.extra.CUSTOM_INTENT";

    public final String id;
    public final String intentUri;
    public final String originalLabel;
    public final String label;
    public final String customLabel;
    public final int color;
    public final Integer customColor;
    @Nullable
    public final ItemWidth customWidth;
    @Nullable
    public final TextAlign customTextAlign;
    public final boolean ignored;

    public IntentDescriptor(Mutable mutable) {
        id = mutable.id;
        intentUri = mutable.intentUri;
        originalLabel = mutable.originalLabel;
        label = mutable.label;
        customLabel = mutable.customLabel;
        color = mutable.color;
        customColor = mutable.customColor;
        customWidth = mutable.customWidth;
        customTextAlign = mutable.customTextAlign;
        ignored = mutable.ignored;
    }

    @Nullable
    @Override
    public ItemWidth getCustomWidth() {
        return customWidth;
    }

    @Nullable
    @Override
    public TextAlign getCustomTextAlign() {
        return customTextAlign;
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
    public int getColor() {
        return color;
    }

    @Override
    public Integer getCustomColor() {
        return customColor;
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
    public boolean isIgnored() {
        return ignored;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj.getClass() != IntentDescriptor.class) {
            return false;
        }
        IntentDescriptor that = (IntentDescriptor) obj;
        return this.id.equals(that.id);
    }

    @Override
    public Mutable toMutable() {
        return new Mutable(this);
    }

    @NonNull
    @Override
    public String toString() {
        return "Intent{" + intentUri + '}';
    }

    public static class Mutable implements MutableDescriptor<IntentDescriptor>,
            CustomColorMutableDescriptor<IntentDescriptor>,
            CustomLabelMutableDescriptor<IntentDescriptor>,
            IgnorableMutableDescriptor<IntentDescriptor>,
            CustomLayoutMutableDescriptor<IntentDescriptor> {

        private final String id;
        private String intentUri;
        private String originalLabel;
        private String label;
        private String customLabel;
        private int color = Color.WHITE;
        private Integer customColor;
        private ItemWidth customWidth;
        private TextAlign customTextAlign;
        private boolean ignored;

        public Mutable(Intent intent, String originalLabel) {
            this("intent/" + UUID.randomUUID().toString(),
                    intent.toUri(Intent.URI_INTENT_SCHEME | Intent.URI_ALLOW_UNSAFE),
                    originalLabel);
        }

        public Mutable(String id, String intentUri, String originalLabel) {
            this.id = id;
            this.intentUri = intentUri;
            this.originalLabel = originalLabel;
        }

        public Mutable(IntentDescriptor descriptor) {
            id = descriptor.id;
            intentUri = descriptor.intentUri;
            originalLabel = descriptor.originalLabel;
            label = descriptor.label;
            customLabel = descriptor.customLabel;
            color = descriptor.color;
            customColor = descriptor.customColor;
            customWidth = descriptor.customWidth;
            customTextAlign = descriptor.customTextAlign;
            ignored = descriptor.ignored;
        }

        @Nullable
        @Override
        public ItemWidth getCustomWidth() {
            return customWidth;
        }

        @Override
        public void setCustomWidth(@Nullable ItemWidth width) {
            customWidth = width;
        }

        @Nullable
        @Override
        public TextAlign getCustomTextAlign() {
            return customTextAlign;
        }

        @Override
        public void setCustomTextAlign(@Nullable TextAlign textAlign) {
            customTextAlign = textAlign;
        }

        @Override
        public String getId() {
            return id;
        }

        public String getIntentUri() {
            return intentUri;
        }

        public void setIntentUri(String intentUri) {
            this.intentUri = intentUri;
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
        public String getOriginalLabel() {
            return originalLabel;
        }

        @Override
        public void setOriginalLabel(String originalLabel) {
            this.originalLabel = originalLabel != null ? originalLabel : "";
        }

        @Override
        public int getColor() {
            return color;
        }

        @Override
        public void setColor(int color) {
            this.color = color;
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
        public String getCustomLabel() {
            return customLabel;
        }

        @Override
        public void setCustomLabel(String customLabel) {
            this.customLabel = customLabel;
        }

        @Override
        public boolean isIgnored() {
            return ignored;
        }

        @Override
        public void setIgnored(boolean ignored) {
            this.ignored = ignored;
        }

        @Override
        public IntentDescriptor toDescriptor() {
            return new IntentDescriptor(this);
        }

        @NonNull
        @Override
        public String toString() {
            return "IntentDescriptor.Mutable{" + getId() + "}";
        }
    }
}
