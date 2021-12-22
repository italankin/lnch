package com.italankin.lnch.feature.home.apps.popup.notifications;

import android.app.Notification;
import android.content.Context;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;

import com.italankin.lnch.feature.home.apps.popup.notifications.item.AppNotificationUi;
import com.italankin.lnch.feature.home.apps.popup.notifications.item.PopupNotificationItem;
import com.italankin.lnch.model.repository.notifications.NotificationBag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;

public class AppNotificationFactory {

    private static final boolean AT_LEAST_N = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;

    private final Context context;

    public AppNotificationFactory(Context context) {
        this.context = context;
    }

    public List<PopupNotificationItem> createNotifications(@Nullable NotificationBag bag) {
        if (bag == null || bag.getCount() == 0) {
            return Collections.emptyList();
        }
        Map<String, List<StatusBarNotification>> grouped = new HashMap<>(bag.getCount());
        List<StatusBarNotification> ungrouped = new ArrayList<>(bag.getCount());
        for (StatusBarNotification sbn : bag.getNotifications()) {
            if (ignore(sbn)) {
                continue;
            }
            if (AT_LEAST_N && sbn.isGroup()) {
                String groupKey = sbn.getGroupKey();
                List<StatusBarNotification> sbns = grouped.get(groupKey);
                if (sbns == null) {
                    sbns = new ArrayList<>(1);
                    grouped.put(groupKey, sbns);
                }
                sbns.add(sbn);
            } else {
                ungrouped.add(sbn);
            }
        }
        List<PopupNotificationItem> result = new ArrayList<>(bag.getCount());
        for (StatusBarNotification sbn : ungrouped) {
            result.add(new AppNotificationUi(context, sbn));
        }
        for (List<StatusBarNotification> sbns : grouped.values()) {
            StatusBarNotification summary = findSummary(sbns);
            if (summary != null) {
                result.add(new AppNotificationUi(context, summary, sbns.size()));
            } else {
                for (StatusBarNotification sbn : sbns) {
                    result.add(new AppNotificationUi(context, sbn));
                }
            }
        }
        if (result.isEmpty()) {
            return Collections.emptyList();
        }
        Collections.sort(result);
        return result;
    }

    private static StatusBarNotification findSummary(List<StatusBarNotification> sbns) {
        for (StatusBarNotification sbn : sbns) {
            if ((sbn.getNotification().flags & Notification.FLAG_GROUP_SUMMARY) == Notification.FLAG_GROUP_SUMMARY) {
                return sbn;
            }
        }
        return null;
    }

    private static boolean ignore(StatusBarNotification sbn) {
        if (sbn.isOngoing()) {
            return true;
        }
        Notification n = sbn.getNotification();
        CharSequence text = n.extras.getCharSequence(Notification.EXTRA_TEXT);
        return TextUtils.isEmpty(text);
    }
}
