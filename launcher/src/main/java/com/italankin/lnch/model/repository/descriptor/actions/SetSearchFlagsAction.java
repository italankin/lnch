package com.italankin.lnch.model.repository.descriptor.actions;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;

import java.util.List;

public class SetSearchFlagsAction implements DescriptorRepository.Editor.Action {
    private final String id;
    private final int searchFlags;

    public SetSearchFlagsAction(String id, int searchFlags) {
        this.id = id;
        this.searchFlags = searchFlags;
    }

    @Override
    public void apply(List<Descriptor> items) {
        for (Descriptor item : items) {
            if (item.getId().equals(id)) {
                AppDescriptor app = (AppDescriptor) item;
                app.searchFlags = searchFlags;
                break;
            }
        }
    }
}
