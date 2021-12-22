package com.italankin.lnch.model.repository.notifications;

import android.service.notification.StatusBarNotification;

import com.italankin.lnch.model.descriptor.impl.AppDescriptor;

import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;

public class NotificationBag {

    final AppDescriptor descriptor;
    final List<StatusBarNotification> sbns;
    private final int hashCode;

    NotificationBag(AppDescriptor descriptor, StatusBarNotification sbn) {
        this(descriptor, Collections.singletonList(sbn));
    }

    NotificationBag(AppDescriptor descriptor, List<StatusBarNotification> sbns) {
        this.descriptor = descriptor;
        this.sbns = Collections.unmodifiableList(sbns);
        int h = descriptor.hashCode();
        for (StatusBarNotification sbn : sbns) {
            h = h * 31 + (sbn.getId() + 131);
        }
        this.hashCode = h;
    }

    public List<StatusBarNotification> getNotifications() {
        return sbns;
    }

    public int getCount() {
        return sbns.size();
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @NonNull
    @Override
    public String toString() {
        return "NotificationBag{count=" + getCount() + "}";
    }
}
