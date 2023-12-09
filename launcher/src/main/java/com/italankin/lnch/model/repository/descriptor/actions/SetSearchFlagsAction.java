package com.italankin.lnch.model.repository.descriptor.actions;

import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.descriptor.mutable.MutableDescriptor;

import java.util.List;

public class SetSearchFlagsAction extends BaseAction {
    private final String id;
    private final int searchFlags;

    public SetSearchFlagsAction(AppDescriptor descriptor, int searchFlags) {
        this.id = descriptor.getId();
        this.searchFlags = searchFlags;
    }

    @Override
    public void apply(List<MutableDescriptor<?>> items) {
        AppDescriptor.Mutable descriptor = findById(items, id);
        if (descriptor != null) {
            descriptor.setSearchFlags(searchFlags);
        }
    }
}
