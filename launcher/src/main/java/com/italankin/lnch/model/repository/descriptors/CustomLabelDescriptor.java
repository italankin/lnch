package com.italankin.lnch.model.repository.descriptors;

public interface CustomLabelDescriptor extends LabelDescriptor {

    void setCustomLabel(String label);

    String getCustomLabel();

    String getVisibleLabel();
}
