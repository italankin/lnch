package com.italankin.lnch.model.descriptor;

public interface CustomLabelDescriptor extends LabelDescriptor {

    void setCustomLabel(String label);

    String getCustomLabel();

    String getVisibleLabel();
}
