package com.italankin.lnch.model.repository.notifications;

import android.service.notification.StatusBarNotification;

import com.italankin.lnch.model.descriptor.impl.AppDescriptor;

import java.util.Map;

import io.reactivex.Observable;

public interface NotificationsRepository {

    void postNotification(StatusBarNotification sbn);

    void postNotifications(StatusBarNotification... sbns);

    void removeNotification(StatusBarNotification sbn);

    void clearNotifications();

    Observable<Map<AppDescriptor, AppNotifications>> observe();
}
