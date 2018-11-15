package com.italankin.lnch.model.repository.descriptor.actions;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.DeepShortcutDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.shortcuts.ShortcutsRepository;

import java.util.List;

public class UnpinShortcutAction implements DescriptorRepository.Editor.Action {

    private final ShortcutsRepository shortcutsRepository;
    private final DeepShortcutDescriptor descriptor;

    public UnpinShortcutAction(ShortcutsRepository shortcutsRepository, DeepShortcutDescriptor descriptor) {
        this.shortcutsRepository = shortcutsRepository;
        this.descriptor = descriptor;
    }

    @Override
    public void apply(List<Descriptor> items) {
        shortcutsRepository.unpinShortcut(descriptor.packageName, descriptor.id);
        items.remove(descriptor);
    }
}
