package com.italankin.lnch.model.repository.apps.actions;

import com.italankin.lnch.bean.AppItem;
import com.italankin.lnch.model.repository.apps.AppsRepository;

import java.util.List;

public class SetCustomColorAction implements AppsRepository.Editor.Action {
    private final String packageName;
    private final Integer customColor;

    public SetCustomColorAction(AppItem item, Integer customColor) {
        this.packageName = item.id;
        this.customColor = customColor;
    }

    @Override
    public void apply(List<AppItem> items) {
        for (AppItem item : items) {
            if (item.id.equals(packageName)) {
                item.customColor = customColor;
                break;
            }
        }
    }
}
