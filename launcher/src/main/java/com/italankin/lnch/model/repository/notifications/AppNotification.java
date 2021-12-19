package com.italankin.lnch.model.repository.notifications;

import android.app.Notification;
import android.service.notification.StatusBarNotification;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.annotation.Nullable;

public class AppNotification {

    public final List<StatusBarNotification> sbns;
    final Map<Integer, StatusBarNotification> sbnsMap;

    public final boolean isGroup;
    public final StatusBarNotification groupSbn;
    public final String groupKey;

    AppNotification(StatusBarNotification sbn) {
        this(Collections.singletonList(sbn), null);
    }

    AppNotification(StatusBarNotification sbn, String groupKey) {
        this(Collections.singletonList(sbn), groupKey);
    }

    AppNotification(List<StatusBarNotification> sbns, String groupKey) {
        this.sbns = Collections.unmodifiableList(sbns);
        HashMap<Integer, StatusBarNotification> map = new HashMap<>(sbns.size());
        StatusBarNotification groupSbn = null;
        for (StatusBarNotification sbn : sbns) {
            map.put(sbn.getId(), sbn);
            if ((sbn.getNotification().flags & Notification.FLAG_GROUP_SUMMARY) == Notification.FLAG_GROUP_SUMMARY) {
                groupSbn = sbn;
            }
        }
        this.groupSbn = groupSbn;
        this.groupKey = groupKey;
        this.isGroup = groupKey != null;
        this.sbnsMap = map;
    }

    Set<Integer> ids() {
        return sbnsMap.keySet();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return this == obj;
    }

    @Override
    public int hashCode() {
        int h = 1;
        for (StatusBarNotification sbn : sbns) {
            h = h * 31 + sbn.getId();
        }
        return h;
    }
}
