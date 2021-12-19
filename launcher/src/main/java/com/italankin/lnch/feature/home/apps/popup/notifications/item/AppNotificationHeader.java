package com.italankin.lnch.feature.home.apps.popup.notifications.item;

public class AppNotificationHeader implements PopupNotificationItem {

    public final int count;

    public AppNotificationHeader(int count) {
        this.count = count;
    }

    @Override
    public long compareKey() {
        return -1;
    }
}
