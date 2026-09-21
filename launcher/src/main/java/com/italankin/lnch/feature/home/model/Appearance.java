package com.italankin.lnch.feature.home.model;

import androidx.annotation.Nullable;

import com.italankin.lnch.model.descriptor.props.ItemWidth;
import com.italankin.lnch.model.descriptor.props.TextAlign;
import com.italankin.lnch.model.ui.CustomLayoutDescriptorUi;
import com.italankin.lnch.model.ui.DescriptorUi;

import java.util.Objects;

public final class Appearance {

    public static final Appearance DEFAULT = new Appearance(null, null);

    @Nullable
    public final ItemWidth width;
    @Nullable
    public final TextAlign textAlign;

    public Appearance(@Nullable ItemWidth width, @Nullable TextAlign textAlign) {
        this.width = width;
        this.textAlign = textAlign;
    }

    public static Appearance from(@Nullable DescriptorUi item) {
        if (!(item instanceof CustomLayoutDescriptorUi)) {
            return DEFAULT;
        }
        CustomLayoutDescriptorUi customLayout = (CustomLayoutDescriptorUi) item;
        ItemWidth width = customLayout.getCustomWidth();
        TextAlign textAlign = customLayout.getCustomTextAlign();
        return width == null && textAlign == null ? DEFAULT : new Appearance(width, textAlign);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Appearance)) {
            return false;
        }
        Appearance that = (Appearance) o;
        return width == that.width && textAlign == that.textAlign;
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, textAlign);
    }
}
