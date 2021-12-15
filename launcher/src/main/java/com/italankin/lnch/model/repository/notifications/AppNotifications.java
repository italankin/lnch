package com.italankin.lnch.model.repository.notifications;

import android.service.notification.StatusBarNotification;

import com.italankin.lnch.model.descriptor.impl.AppDescriptor;

import java.util.Collections;
import java.util.Map;

public class AppNotifications {

    final AppDescriptor descriptor;
    final Map<Integer, StatusBarNotification> notifications;
    private final int hashCode;

    AppNotifications(AppDescriptor descriptor, StatusBarNotification sbn) {
        this.descriptor = descriptor;
        this.notifications = Collections.singletonMap(sbn.getId(), sbn);
        this.hashCode = descriptor.hashCode() * 31 + sbn.getId();
    }

    AppNotifications(AppDescriptor descriptor, Map<Integer, StatusBarNotification> sbns) {
        this.descriptor = descriptor;
        this.notifications = Collections.unmodifiableMap(sbns);
        int h = descriptor.hashCode();
        for (StatusBarNotification value : sbns.values()) {
            h = h * 31 + value.getId();
        }
        this.hashCode = h;
    }

    public int getCount() {
        return notifications.size();
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return "NotificationDot{count=" + getCount() + "}";
    }
}
