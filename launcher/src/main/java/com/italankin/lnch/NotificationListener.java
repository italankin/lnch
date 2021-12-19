package com.italankin.lnch;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.italankin.lnch.model.repository.notifications.NotificationsRepository;

import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

public class NotificationListener extends NotificationListenerService implements NotificationsRepository.Callback {

    private final CompositeDisposable disposables = new CompositeDisposable();
    private NotificationsRepository notificationsRepository;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationsRepository = LauncherApp.daggerService.main().notificationsRepository();
    }

    @Override
    public void onListenerConnected() {
        Timber.d("onListenerConnected");
        notificationsRepository.setCallback(this);

        Disposable d = Completable.timer(500, TimeUnit.MILLISECONDS)
                .subscribe(() -> {
                    notificationsRepository.postNotifications(getActiveNotifications());
                });
        disposables.add(d);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Timber.d("onNotificationPosted: %s", sbn);
        notificationsRepository.postNotification(sbn);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Timber.d("onNotificationRemoved: %s", sbn);
        notificationsRepository.removeNotification(sbn);
    }

    @Override
    public void onListenerDisconnected() {
        Timber.d("onListenerDisconnected");
        notificationsRepository.setCallback(null);
        notificationsRepository.clearNotifications();
        disposables.clear();
    }

    @Override
    public void cancelNotification(StatusBarNotification sbn) {
        cancelNotification(sbn.getKey());
    }
}
