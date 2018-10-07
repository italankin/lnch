package com.italankin.lnch.feature.home.descriptor;

/**
 * Item with custom label which can be changed
 */
public interface CustomLabelItem extends LabelItem {
    void setCustomLabel(String label);

    String getCustomLabel();
}
