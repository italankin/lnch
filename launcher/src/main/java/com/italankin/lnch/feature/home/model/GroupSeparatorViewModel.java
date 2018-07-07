package com.italankin.lnch.feature.home.model;

import com.italankin.lnch.bean.AppItem;

public class GroupSeparatorViewModel extends AppViewModel {
    public boolean expanded = true;

    public GroupSeparatorViewModel(AppItem item) {
        super(item);
    }
}
