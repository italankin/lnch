package com.italankin.lnch.model.repository.descriptor;

import android.content.pm.LauncherApps;
import android.content.pm.ShortcutInfo;
import android.os.Process;
import android.os.UserHandle;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.NonNull;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

class LauncherAppsUpdates extends Observable<Object> {
    private static final Object NOTIFICATION = new Object();

    private final LauncherApps launcherApps;

    LauncherAppsUpdates(LauncherApps launcherApps) {
        this.launcherApps = launcherApps;
    }

    @Override
    protected void subscribeActual(Observer<? super Object> observer) {
        Callback callback = new Callback(observer);
        observer.onSubscribe(callback);
        launcherApps.registerCallback(callback);
    }

    private final class Callback extends LauncherApps.Callback implements Disposable {
        private final Observer<? super Object> observer;
        private AtomicBoolean disposed = new AtomicBoolean();

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

        @Override
        public void onShortcutsChanged(@NonNull String packageName, @NonNull List<ShortcutInfo> shortcuts, @NonNull UserHandle user) {
            sendNotification(user);
        }

        @Override
        public void dispose() {
            if (disposed.compareAndSet(false, true)) {
                launcherApps.unregisterCallback(this);
            }
        }

        @Override
        public boolean isDisposed() {
            return disposed.get();
        }

        private void sendNotification(UserHandle user) {
            if (Process.myUserHandle().equals(user)) {
                observer.onNext(NOTIFICATION);
            }
        }
    }
}
