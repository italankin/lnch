package com.italankin.lnch.model.repository.apps.actions;

import com.italankin.lnch.model.repository.apps.AppsRepository;
import com.italankin.lnch.model.repository.descriptors.Descriptor;

import java.util.List;

public class RecolorAction implements AppsRepository.Editor.Action {
    private final Descriptor item;
    private final Integer customColor;

    public RecolorAction(Descriptor item, Integer customColor) {
        this.item = item;
        this.customColor = customColor;
    }

    @Override
    public void apply(List<Descriptor> items) {
        for (Descriptor item : items) {
            if (this.item.equals(item)) {
                item.setCustomColor(customColor);
                break;
            }
        }
    }
}
