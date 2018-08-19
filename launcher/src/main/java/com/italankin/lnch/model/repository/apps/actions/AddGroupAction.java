package com.italankin.lnch.model.repository.apps.actions;

import com.italankin.lnch.model.repository.apps.AppsRepository;
import com.italankin.lnch.model.repository.descriptors.Descriptor;
import com.italankin.lnch.model.repository.descriptors.model.GroupDescriptor;

import java.util.List;

public class AddGroupAction implements AppsRepository.Editor.Action {
    private final int position;
    private final GroupDescriptor groupDescriptor;

    public AddGroupAction(int position, GroupDescriptor groupDescriptor) {
        this.position = position;
        this.groupDescriptor = groupDescriptor;
    }

    @Override
    public void apply(List<Descriptor> items) {
        items.add(position, groupDescriptor);
    }
}
