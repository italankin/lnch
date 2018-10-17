package com.italankin.lnch.model.repository.apps.actions;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.repository.apps.AppsRepository;

import java.util.List;

public class RemoveAction implements AppsRepository.Editor.Action {
    private final int position;

    public RemoveAction(int position) {
        this.position = position;
    }

    @Override
    public void apply(List<Descriptor> items) {
        items.remove(position);
    }
}
