package com.italankin.lnch.model.repository.notifications;

import android.service.notification.StatusBarNotification;

import androidx.annotation.NonNull;

import com.italankin.lnch.model.descriptor.impl.AppDescriptor;

import java.util.Collections;
import java.util.List;

public class NotificationBag {

    final AppDescriptor descriptor;
    final List<StatusBarNotification> sbns;
    private final int ongoingCount;
    private final int hashCode;

    NotificationBag(AppDescriptor descriptor, StatusBarNotification sbn) {
        this(descriptor, Collections.singletonList(sbn));
    }

    NotificationBag(AppDescriptor descriptor, List<StatusBarNotification> sbns) {
        this.descriptor = descriptor;
        this.sbns = Collections.unmodifiableList(sbns);
        int h = descriptor.hashCode();
        int ongoingCount = 0;
        for (StatusBarNotification sbn : sbns) {
            h = h * 31 + (sbn.getId() + 131);
            if (sbn.isOngoing()) {
                ongoingCount++;
            }
        }
        this.hashCode = h;
        this.ongoingCount = ongoingCount;
    }

    public List<StatusBarNotification> getNotifications() {
        return sbns;
    }

    public int getCount() {
        return sbns.size();
    }

    public int getOngoingCount() {
        return ongoingCount;
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
