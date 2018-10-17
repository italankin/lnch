package com.italankin.lnch.model.viewmodel;

/**
 * Item with custom label which can be changed
 */
public interface CustomLabelItem extends LabelItem {
    void setCustomLabel(String label);

    String getCustomLabel();
}
