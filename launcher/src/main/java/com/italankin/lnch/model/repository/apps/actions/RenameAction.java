package com.italankin.lnch.model.repository.apps.actions;

import com.italankin.lnch.bean.AppItem;
import com.italankin.lnch.model.repository.apps.AppsRepository;

import java.util.List;

public class RenameAction implements AppsRepository.Editor.Action {
    private final String packageName;
    private final String customLabel;

    public RenameAction(AppItem item, String customLabel) {
        this.packageName = item.packageName;
        this.customLabel = customLabel;
    }

    @Override
    public void apply(List<AppItem> items) {
        for (AppItem item : items) {
            if (item.packageName.equals(packageName)) {
                item.customLabel = customLabel;
                break;
            }
        }
    }
}
