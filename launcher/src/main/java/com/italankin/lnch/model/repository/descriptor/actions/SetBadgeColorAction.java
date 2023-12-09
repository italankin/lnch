package com.italankin.lnch.model.repository.descriptor.actions;

import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.descriptor.mutable.MutableDescriptor;

import java.util.List;

public class SetBadgeColorAction extends BaseAction {
    private final String id;
    private final Integer newColor;

    public SetBadgeColorAction(AppDescriptor descriptor, Integer newColor) {
        this.id = descriptor.getId();
        this.newColor = newColor;
    }

    @Override
    public void apply(List<MutableDescriptor<?>> items) {
        AppDescriptor.Mutable descriptor = findById(items, id);
        if (descriptor != null) {
            descriptor.setCustomBadgeColor(newColor);
        }
    }
}
