package com.italankin.lnch.model.repository.descriptor.actions;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;

import java.util.List;

public class SetAliasesAction extends BaseAction {
    private final String id;
    private final List<String> aliases;

    public SetAliasesAction(AppDescriptor descriptor, List<String> aliases) {
        this.id = descriptor.getId();
        this.aliases = aliases;
    }

    @Override
    public void apply(List<Descriptor> items) {
        AppDescriptor descriptor = findById(items, id);
        descriptor.setAliases(aliases);
    }
}
