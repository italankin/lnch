package com.italankin.lnch.feature.home.model;

import com.italankin.lnch.model.repository.descriptors.Descriptor;

public interface ItemViewModel {

    Descriptor getDescriptor();

    String getVisibleLabel();

    String getCustomLabel();

    void setCustomLabel(String label);

    int getVisibleColor();

    void setCustomColor(Integer color);

}
