package com.italankin.lnch.model.repository.notifications;

import android.service.notification.StatusBarNotification;

import com.italankin.lnch.model.descriptor.impl.AppDescriptor;

import java.util.Map;

import androidx.annotation.Nullable;
import io.reactivex.Observable;

public interface NotificationsRepository {

    void setCallback(@Nullable Callback callback);

    @Nullable
    Callback getCallback();

    void postNotification(StatusBarNotification sbn);

    void postNotifications(StatusBarNotification... sbns);

    void removeNotification(StatusBarNotification sbn);

    void clearNotifications();

    @Nullable
    NotificationBag getByApp(AppDescriptor descriptor);

    Observable<Map<AppDescriptor, NotificationBag>> observe();

    interface Callback {
        void cancelNotification(StatusBarNotification sbn);
    }
}
