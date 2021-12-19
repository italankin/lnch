package com.italankin.lnch.feature.home.apps.popup.notifications.item;

import android.app.Notification;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.service.notification.StatusBarNotification;

public class AppNotificationUi implements PopupNotificationItem {

    public final StatusBarNotification sbn;
    public final String title;
    public final String text;
    public final Drawable icon;
    public final int summaryCount;

    public AppNotificationUi(Context context, StatusBarNotification sbn) {
        this(context, sbn, 0);
    }

    public AppNotificationUi(Context context, StatusBarNotification sbn, int summaryCount) {
        this.sbn = sbn;
        Notification n = sbn.getNotification();
        CharSequence title = n.extras.getCharSequence(Notification.EXTRA_TITLE);
        this.title = title != null ? title.toString() : null;
        CharSequence text = n.extras.getCharSequence(Notification.EXTRA_TEXT);
        this.text = text != null ? text.toString() : null;
        Icon icon = n.getSmallIcon();
        this.icon = icon != null ? icon.loadDrawable(context) : null;
        this.summaryCount = summaryCount;
    }

    @Override
    public long compareKey() {
        return sbn.getPostTime();
    }
}
