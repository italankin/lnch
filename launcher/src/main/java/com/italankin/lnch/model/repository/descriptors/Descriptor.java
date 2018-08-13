package com.italankin.lnch.model.repository.descriptors;

public interface Descriptor {

    int getVisibleColor();

    void setCustomColor(Integer color);

    String getVisibleLabel();

    void setCustomLabel(String label);

    boolean isHidden();

    void setHidden(boolean hidden);

}
