package com.italankin.lnch.model.repository.descriptor.apps;

import android.content.pm.LauncherApps;
import android.content.pm.ShortcutInfo;
import android.os.Bundle;
import android.os.Process;
import android.os.UserHandle;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

/**
 * Observe changes posted by {@link LauncherApps}
 */
class LauncherAppsObservable extends Observable<LauncherAppsObservable.Event> {
    private final LauncherApps launcherApps;

    LauncherAppsObservable(LauncherApps launcherApps) {
        this.launcherApps = launcherApps;
    }

    @Override
    protected void subscribeActual(Observer<? super Event> observer) {
        Callback callback = new Callback(observer);
        observer.onSubscribe(callback);
        launcherApps.registerCallback(callback);
    }

    private final class Callback extends LauncherApps.Callback implements Disposable {
        private final Observer<? super Event> observer;
        private final AtomicBoolean disposed = new AtomicBoolean();

        private Callback(Observer<? super Event> observer) {
            this.observer = observer;
        }

        @Override
        public void onPackageRemoved(String packageName, UserHandle user) {
            sendNotification(user, Event.REMOVED);
        }

        @Override
        public void onPackageAdded(String packageName, UserHandle user) {
            sendNotification(user, Event.ADDED);
        }

        @Override
        public void onPackageChanged(String packageName, UserHandle user) {
            sendNotification(user, Event.CHANGED);
        }

        @Override
        public void onPackagesAvailable(String[] packageNames, UserHandle user, boolean replacing) {
            sendNotification(user, Event.AVAILABLE);
        }

        @Override
        public void onPackagesUnavailable(String[] packageNames, UserHandle user, boolean replacing) {
            sendNotification(user, Event.UNAVAILABLE);
        }

        @Override
        public void onPackagesSuspended(String[] packageNames, UserHandle user) {
            sendNotification(user, Event.SUSPENDED);
        }

        @Override
        public void onPackagesSuspended(String[] packageNames, UserHandle user, @Nullable Bundle launcherExtras) {
            sendNotification(user, Event.SUSPENDED);
        }

        @Override
        public void onPackagesUnsuspended(String[] packageNames, UserHandle user) {
            sendNotification(user, Event.UNSUSPENDED);
        }

        @Override
        public void onShortcutsChanged(@NonNull String packageName, @NonNull List<ShortcutInfo> shortcuts, @NonNull UserHandle user) {
            sendNotification(user, Event.SHORTCUTS_CHANGED);
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

        private void sendNotification(UserHandle user, Event event) {
            if (isDisposed()) {
                return;
            }
            if (Process.myUserHandle().equals(user)) {
                Timber.d("event=%s", event);
                observer.onNext(event);
            }
        }
    }

    enum Event {
        ADDED,
        REMOVED,
        CHANGED,
        AVAILABLE,
        UNAVAILABLE,
        SUSPENDED,
        UNSUSPENDED,
        SHORTCUTS_CHANGED,
    }
}
