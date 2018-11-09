package com.italankin.lnch.model.repository.apps;

import android.content.pm.LauncherApps;
import android.os.Process;
import android.os.UserHandle;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposables;

class LauncherAppsUpdates extends Observable<Object> {
    private static final Object NOTIFICATION = new Object();

    private final LauncherApps launcherApps;

    LauncherAppsUpdates(LauncherApps launcherApps) {
        this.launcherApps = launcherApps;
    }

    @Override
    protected void subscribeActual(Observer<? super Object> observer) {
        LauncherApps.Callback callback = new Callback(observer);
        observer.onSubscribe(Disposables.fromRunnable(() ->
                launcherApps.unregisterCallback(callback)));
    }

    private final class Callback extends LauncherApps.Callback {
        private final Observer<? super Object> observer;

        public Callback(Observer<? super Object> observer) {
            this.observer = observer;
        }

        @Override
        public void onPackageRemoved(String packageName, UserHandle user) {
            sendNotification(user);
        }

        @Override
        public void onPackageAdded(String packageName, UserHandle user) {
            sendNotification(user);
        }

        @Override
        public void onPackageChanged(String packageName, UserHandle user) {
            sendNotification(user);
        }

        @Override
        public void onPackagesAvailable(String[] packageNames, UserHandle user, boolean replacing) {
            sendNotification(user);
        }

        @Override
        public void onPackagesUnavailable(String[] packageNames, UserHandle user, boolean replacing) {
            sendNotification(user);
        }

        private void sendNotification(UserHandle user) {
            if (Process.myUserHandle().equals(user)) {
                observer.onNext(NOTIFICATION);
            }
        }
    }
}
