package com.italankin.lnch.model.repository.notifications;

import android.service.notification.StatusBarNotification;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class NotificationsRepositoryImpl implements NotificationsRepository {

    private final DescriptorRepository descriptorRepository;
    private final PublishSubject<Map<AppDescriptor, NotificationBadge>> updates = PublishSubject.create();

    private final ConcurrentMap<AppDescriptor, NotificationBadge> state = new ConcurrentHashMap<>();

    public NotificationsRepositoryImpl(DescriptorRepository descriptorRepository) {
        this.descriptorRepository = descriptorRepository;
    }

    @Override
    public void postNotification(StatusBarNotification sbn) {
        sendUpdate(sbn.getPackageName(), sbn.getId(), Type.POSTED);
    }

    @Override
    public void removeNotification(StatusBarNotification sbn) {
        sendUpdate(sbn.getPackageName(), sbn.getId(), Type.REMOVED);
    }

    @Override
    public void clearNotifications() {
        state.clear();
        updates.onNext(state);
    }

    @Override
    public Observable<Map<AppDescriptor, NotificationBadge>> observe() {
        return observeApps()
                .map(this::updateState)
                .startWith(state)
                .mergeWith(updates)
                .map(Collections::unmodifiableMap);
    }

    private Map<AppDescriptor, NotificationBadge> updateState(List<AppDescriptor> appDescriptors) {
        // remove any notifications, which belong to non-existent apps
        Set<AppDescriptor> apps = new HashSet<>(appDescriptors);
        for (Iterator<AppDescriptor> i = state.keySet().iterator(); i.hasNext(); ) {
            if (!apps.contains(i.next())) {
                i.remove();
            }
        }
        return state;
    }

    private Observable<List<AppDescriptor>> observeApps() {
        return descriptorRepository.observe()
                .map(descriptors -> {
                    List<AppDescriptor> result = new ArrayList<>(descriptors.size());
                    for (Descriptor descriptor : descriptors) {
                        if (descriptor instanceof AppDescriptor) {
                            result.add((AppDescriptor) descriptor);
                        }
                    }
                    return result;
                })
                .distinctUntilChanged();
    }

    private void sendUpdate(String packageName, int id, Type type) {
        List<AppDescriptor> appDescriptors = descriptorRepository.itemsOfType(AppDescriptor.class);
        AppDescriptor app = findAppDescriptor(appDescriptors, packageName);
        if (app != null) {
            NotificationBadge badge = state.get(app);
            if (badge != null) {
                switch (type) {
                    case POSTED:
                        if (!badge.ids.contains(id)) {
                            HashSet<Integer> s = new HashSet<>(badge.ids);
                            s.add(id);
                            state.put(app, new NotificationBadge(s));
                        }
                        break;
                    case REMOVED:
                        if (badge.ids.contains(id)) {
                            HashSet<Integer> s = new HashSet<>(badge.ids);
                            s.remove(id);
                            if (s.isEmpty()) {
                                state.remove(app);
                            } else {
                                state.put(app, new NotificationBadge(s));
                            }
                        }
                        break;
                }
            } else if (type == Type.POSTED) {
                state.put(app, new NotificationBadge(id));
            }
        }
        updates.onNext(state);
    }

    private AppDescriptor findAppDescriptor(List<AppDescriptor> descriptors, String packageName) {
        for (AppDescriptor appDescriptor : descriptors) {
            if (appDescriptor.packageName.equals(packageName)) {
                return appDescriptor;
            }
        }
        return null;
    }

    private enum Type {
        POSTED,
        REMOVED
    }
}
