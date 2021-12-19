package com.italankin.lnch.feature.home.apps.popup.notifications.item;

public interface PopupNotificationItem extends Comparable<PopupNotificationItem> {

    long compareKey();

    @Override
    default int compareTo(PopupNotificationItem o) {
        return Long.compare(compareKey(), o.compareKey());
    }
}
