package com.italankin.lnch.model.repository.descriptor.actions;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;

import java.util.List;

public class SetAliasesAction implements DescriptorRepository.Editor.Action {
    private final String id;
    private final List<String> aliases;

    public SetAliasesAction(AppDescriptor descriptor, List<String> aliases) {
        this.id = descriptor.getId();
        this.aliases = aliases;
    }

    @Override
    public void apply(List<Descriptor> items) {
        for (Descriptor item : items) {
            if (item.getId().equals(id)) {
                AppDescriptor app = (AppDescriptor) item;
                app.setAliases(aliases);
                break;
            }
        }
    }
}
