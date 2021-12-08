package com.italankin.lnch.model.repository.descriptor.actions;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;

import java.util.List;

public class ShortcutsVisibilityAction extends BaseAction {
    private final String id;
    private final boolean showShortcuts;

    public ShortcutsVisibilityAction(AppDescriptor descriptor, boolean showShortcuts) {
        this.id = descriptor.getId();
        this.showShortcuts = showShortcuts;
    }

    @Override
    public void apply(List<Descriptor> items) {
        AppDescriptor descriptor = findById(items, id);
        if (descriptor != null) {
            descriptor.showShortcuts = showShortcuts;
        }
    }
}
