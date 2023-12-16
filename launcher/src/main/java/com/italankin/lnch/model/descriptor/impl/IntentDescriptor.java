package com.italankin.lnch.model.descriptor.impl;

import android.content.Intent;
import android.graphics.Color;
import androidx.annotation.NonNull;
import com.italankin.lnch.model.descriptor.*;
import com.italankin.lnch.model.descriptor.mutable.CustomColorMutableDescriptor;
import com.italankin.lnch.model.descriptor.mutable.CustomLabelMutableDescriptor;
import com.italankin.lnch.model.descriptor.mutable.IgnorableMutableDescriptor;
import com.italankin.lnch.model.descriptor.mutable.MutableDescriptor;
import com.italankin.lnch.model.repository.store.json.model.IntentDescriptorJson;
import com.italankin.lnch.model.ui.impl.IntentDescriptorUi;

import java.util.UUID;

/**
 * Custom intent descriptor (e.g., search intent)
 */
@DescriptorModels(
        json = IntentDescriptorJson.class,
        ui = IntentDescriptorUi.class,
        mutable = IntentDescriptor.Mutable.class
)
public final class IntentDescriptor implements Descriptor, CustomColorDescriptor, CustomLabelDescriptor,
        IgnorableDescriptor {

    public static final String EXTRA_CUSTOM_INTENT = "com.italankin.lnch.extra.CUSTOM_INTENT";

    public final String id;
    public final String intentUri;
    public final String originalLabel;
    public final String label;
    public final String customLabel;
    public final int color;
    public final Integer customColor;
    public final boolean ignored;

    public IntentDescriptor(Mutable mutable) {
        id = mutable.id;
        intentUri = mutable.intentUri;
        originalLabel = mutable.originalLabel;
        label = mutable.label;
        customLabel = mutable.customLabel;
        color = mutable.color;
        customColor = mutable.customColor;
        ignored = mutable.ignored;
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
            IgnorableMutableDescriptor<IntentDescriptor> {

        private final String id;
        private String intentUri;
        private String originalLabel;
        private String label;
        private String customLabel;
        private int color = Color.WHITE;
        private Integer customColor;
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
            ignored = descriptor.ignored;
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
