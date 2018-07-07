package com.italankin.lnch.model.repository.apps.actions;

import com.italankin.lnch.bean.AppItem;
import com.italankin.lnch.bean.GroupSeparator;
import com.italankin.lnch.model.repository.apps.AppsRepository;

import java.util.List;

public class AddSeparatorAction implements AppsRepository.Editor.Action {
    private final int position;
    private final GroupSeparator separator;

    public AddSeparatorAction(int position, GroupSeparator separator) {
        this.position = position;
        this.separator = separator;
    }

    @Override
    public void apply(List<AppItem> items) {
        items.add(position, separator);
    }
}
