package com.italankin.lnch.model.repository.descriptor.actions;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;

import java.util.List;

public class SetSearchVisibilityAction implements DescriptorRepository.Editor.Action {

    private final AppDescriptor descriptor;
    private final boolean searchVisible;
    private final boolean shortcutsSearchVisible;

    public SetSearchVisibilityAction(AppDescriptor descriptor, boolean searchVisible, boolean shortcutsSearchVisible) {
        this.descriptor = descriptor;
        this.searchVisible = searchVisible;
        this.shortcutsSearchVisible = shortcutsSearchVisible;
    }

    @Override
    public void apply(List<Descriptor> items) {
        for (Descriptor item : items) {
            if (this.descriptor.equals(item)) {
                AppDescriptor app = (AppDescriptor) item;
                app.searchVisible = searchVisible;
                app.shortcutsSearchVisible = shortcutsSearchVisible;
                break;
            }
        }
    }
}
