package com.italankin.lnch.model.repository.apps.actions;

import com.italankin.lnch.bean.AppItem;
import com.italankin.lnch.model.repository.apps.AppsRepository;

import java.util.List;

public class SetVisibilityAction implements AppsRepository.Editor.Action {
    private final String packageName;
    private final boolean visible;

    public SetVisibilityAction(AppItem item, boolean visible) {
        this.packageName = item.packageName;
        this.visible = visible;
    }

    @Override
    public void apply(List<AppItem> items) {
        for (AppItem item : items) {
            if (item.packageName.equals(packageName)) {
                item.hidden = !visible;
                break;
            }
        }
    }
}
