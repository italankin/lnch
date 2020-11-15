package com.italankin.lnch;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.italankin.lnch.model.repository.notifications.NotificationsRepository;
import com.italankin.lnch.model.repository.prefs.Preferences;

import java.util.concurrent.TimeUnit;

import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class NotificationListener extends NotificationListenerService {

    private Disposable prefDisposable = Disposables.disposed();

    private NotificationsRepository notificationsRepository;

    @Override
    public void onListenerConnected() {
        Timber.d("onListenerConnected");
        Preferences preferences = LauncherApp.daggerService.main().getPreferences();
        notificationsRepository = LauncherApp.daggerService.main().getNotificationsRepository();
        prefDisposable = preferences.observe(Preferences.NOTIFICATION_BADGE)
                .subscribeOn(Schedulers.io())
                .map(Preferences.Value::get)
                .startWith(preferences.get(Preferences.NOTIFICATION_BADGE))
                .delay(500, TimeUnit.MILLISECONDS)
                .subscribe(notificationBadgeEnabled -> {
                    if (notificationBadgeEnabled) {
                        for (StatusBarNotification sbn : getActiveNotifications()) {
                            notificationsRepository.postNotification(sbn.getPackageName(), sbn.getId());
                        }
                    } else {
                        notificationsRepository.clearNotifications();
                    }
                });
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Timber.d("onNotificationPosted: %s", sbn);
        notificationsRepository.postNotification(sbn.getPackageName(), sbn.getId());
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Timber.d("onNotificationRemoved: %s", sbn);
        notificationsRepository.removeNotification(sbn.getPackageName(), sbn.getId());
    }

    @Override
    public void onListenerDisconnected() {
        Timber.d("onListenerDisconnected");
        notificationsRepository.clearNotifications();
        prefDisposable.dispose();
    }
}
