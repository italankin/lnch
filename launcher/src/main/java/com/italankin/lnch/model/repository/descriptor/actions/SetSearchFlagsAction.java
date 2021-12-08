package com.italankin.lnch.model.repository.descriptor.actions;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;

import java.util.List;

public class SetSearchFlagsAction extends BaseAction {
    private final String id;
    private final int searchFlags;

    public SetSearchFlagsAction(AppDescriptor descriptor, int searchFlags) {
        this.id = descriptor.getId();
        this.searchFlags = searchFlags;
    }

    @Override
    public void apply(List<Descriptor> items) {
        AppDescriptor descriptor = findById(items, id);
        if (descriptor != null) {
            descriptor.searchFlags = searchFlags;
        }
    }
}
