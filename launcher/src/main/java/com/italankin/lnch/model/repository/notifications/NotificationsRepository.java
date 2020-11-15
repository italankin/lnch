package com.italankin.lnch.model.repository.notifications;

import com.italankin.lnch.model.descriptor.impl.AppDescriptor;

import java.util.Map;

import io.reactivex.Observable;

public interface NotificationsRepository {

    void postNotification(String packageName, int id);

    void removeNotification(String packageName, int id);

    void clearNotifications();

    Observable<Map<AppDescriptor, NotificationBadge>> observe();
}
