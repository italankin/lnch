package com.italankin.lnch.model.repository.apps.actions;

import com.italankin.lnch.bean.AppItem;
import com.italankin.lnch.model.repository.apps.AppsRepository;

import java.util.List;

public class RemoveSeparatorAction implements AppsRepository.Editor.Action {
    private final int position;

    public RemoveSeparatorAction(int position) {
        this.position = position;
    }

    @Override
    public void apply(List<AppItem> items) {
        items.remove(position);
    }
}
