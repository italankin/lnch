package com.italankin.lnch.model.repository.apps.actions;

import com.italankin.lnch.bean.AppItem;
import com.italankin.lnch.model.repository.apps.AppsRepository;

import java.util.List;

public class VisibilityAction implements AppsRepository.Editor.Action {
    private final String packageName;
    private final boolean hide;

    public VisibilityAction(AppItem item, boolean hide) {
        this.packageName = item.packageName;
        this.hide = hide;
    }

    @Override
    public void apply(List<AppItem> items) {
        for (AppItem item : items) {
            if (item.packageName.equals(packageName)) {
                item.hidden = hide;
                break;
            }
        }
    }
}
