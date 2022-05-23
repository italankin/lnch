package com.italankin.lnch.model.repository.notifications;

import android.service.notification.StatusBarNotification;

import androidx.annotation.NonNull;

import com.italankin.lnch.model.descriptor.impl.AppDescriptor;

import java.util.Collections;
import java.util.List;

public class NotificationBag {

    final AppDescriptor descriptor;
    final List<StatusBarNotification> sbns;
    private final int clearableCount;
    private final int hashCode;

    NotificationBag(AppDescriptor descriptor, StatusBarNotification sbn) {
        this(descriptor, Collections.singletonList(sbn));
    }

    NotificationBag(AppDescriptor descriptor, List<StatusBarNotification> sbns) {
        this.descriptor = descriptor;
        this.sbns = Collections.unmodifiableList(sbns);
        int h = descriptor.hashCode();
        int clearableCount = 0;
        for (StatusBarNotification sbn : sbns) {
            h = h * 31 + (sbn.getId() + 131);
            if (sbn.isClearable()) {
                clearableCount++;
            }
        }
        this.hashCode = h;
        this.clearableCount = clearableCount;
    }

    public List<StatusBarNotification> getNotifications() {
        return sbns;
    }

    public int getCount() {
        return sbns.size();
    }

    public int getClearableCount() {
        return clearableCount;
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
