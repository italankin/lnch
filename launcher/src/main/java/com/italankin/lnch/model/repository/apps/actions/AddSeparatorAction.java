package com.italankin.lnch.model.repository.apps.actions;

import com.italankin.lnch.model.repository.apps.AppsRepository;
import com.italankin.lnch.model.repository.descriptors.Descriptor;
import com.italankin.lnch.model.repository.descriptors.model.GroupDescriptor;

import java.util.List;

public class AddSeparatorAction implements AppsRepository.Editor.Action {
    private final int position;
    private final GroupDescriptor separator;

    public AddSeparatorAction(int position, GroupDescriptor separator) {
        this.position = position;
        this.separator = separator;
    }

    @Override
    public void apply(List<Descriptor> items) {
        items.add(position, separator);
    }
}
