package com.italankin.lnch.model.repository.apps.actions;

import com.italankin.lnch.model.repository.apps.AppsRepository;
import com.italankin.lnch.model.repository.descriptors.Descriptor;

import java.util.List;

public class SetVisibilityAction implements AppsRepository.Editor.Action {
    private final Descriptor item;
    private final boolean visible;

    public SetVisibilityAction(Descriptor item, boolean visible) {
        this.item = item;
        this.visible = visible;
    }

    @Override
    public void apply(List<Descriptor> items) {
        for (Descriptor item : items) {
            if (this.item.equals(item)) {
                item.setHidden(!visible);
                break;
            }
        }
    }
}
