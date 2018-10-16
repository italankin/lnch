package com.italankin.lnch.model.repository.apps.actions;

import com.italankin.lnch.model.repository.apps.AppsRepository;
import com.italankin.lnch.model.repository.descriptors.CustomLabelDescriptor;
import com.italankin.lnch.model.repository.descriptors.Descriptor;

import java.util.List;

public class RenameAction implements AppsRepository.Editor.Action {
    private final CustomLabelDescriptor item;
    private final String customLabel;

    public RenameAction(CustomLabelDescriptor item, String customLabel) {
        this.item = item;
        this.customLabel = customLabel;
    }

    @Override
    public void apply(List<Descriptor> items) {
        for (Descriptor item : items) {
            if (this.item.equals(item)) {
                ((CustomLabelDescriptor) item).setCustomLabel(customLabel);
                break;
            }
        }
    }
}
