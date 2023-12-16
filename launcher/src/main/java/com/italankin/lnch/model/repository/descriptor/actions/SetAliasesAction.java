package com.italankin.lnch.model.repository.descriptor.actions;

import com.italankin.lnch.model.descriptor.AliasDescriptor;
import com.italankin.lnch.model.descriptor.mutable.AliasMutableDescriptor;
import com.italankin.lnch.model.descriptor.mutable.MutableDescriptor;

import java.util.List;

public class SetAliasesAction extends BaseAction {
    private final String id;
    private final List<String> aliases;

    public SetAliasesAction(AliasDescriptor descriptor, List<String> aliases) {
        this(descriptor.getId(), aliases);
    }

    public SetAliasesAction(String id, List<String> aliases) {
        this.id = id;
        this.aliases = aliases;
    }

    @Override
    public void apply(List<MutableDescriptor<?>> items) {
        AliasMutableDescriptor<?> descriptor = findById(items, id);
        if (descriptor != null) {
            descriptor.setAliases(aliases);
        }
    }
}
