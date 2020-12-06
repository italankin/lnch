package com.italankin.lnch;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.italankin.lnch.model.repository.notifications.NotificationsRepository;
import com.italankin.lnch.model.repository.prefs.Preferences;

import java.util.concurrent.TimeUnit;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class NotificationListener extends NotificationListenerService {

    private final CompositeDisposable disposables = new CompositeDisposable();
    private NotificationsRepository notificationsRepository;

    @Override
    public void onListenerConnected() {
        Timber.d("onListenerConnected");
        notificationsRepository = LauncherApp.daggerService.main().notificationsRepository();
        Preferences preferences = LauncherApp.daggerService.main().preferences();
        Disposable dot = preferences.observe(Preferences.NOTIFICATION_DOT)
                .subscribeOn(Schedulers.io())
                .map(Preferences.Value::get)
                .startWith(preferences.get(Preferences.NOTIFICATION_DOT))
                .delay(500, TimeUnit.MILLISECONDS)
                .subscribe(enabled -> {
                    if (enabled) {
                        notificationsRepository.postNotifications(getActiveNotifications());
                    } else {
                        notificationsRepository.clearNotifications();
                    }
                });
        disposables.add(dot);

        Disposable ongoing = preferences.observe(Preferences.NOTIFICATION_DOT_ONGOING)
                .subscribeOn(Schedulers.io())
                .delay(500, TimeUnit.MILLISECONDS)
                .subscribe(ignored -> {
                    notificationsRepository.clearNotifications();
                    notificationsRepository.postNotifications(getActiveNotifications());
                });
        disposables.add(ongoing);
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
        notificationsRepository.clearNotifications();
        disposables.clear();
    }
}
