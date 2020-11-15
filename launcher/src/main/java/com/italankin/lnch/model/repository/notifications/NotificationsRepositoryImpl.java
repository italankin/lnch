package com.italankin.lnch.model.repository.notifications;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;

import org.jetbrains.annotations.NotNull;

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
    private final PublishSubject<Event> events = PublishSubject.create();
    private final PublishSubject<Map<AppDescriptor, NotificationBadge>> updates = PublishSubject.create();

    private final ConcurrentMap<AppDescriptor, NotificationBadge> state = new ConcurrentHashMap<>();

    public NotificationsRepositoryImpl(DescriptorRepository descriptorRepository) {
        this.descriptorRepository = descriptorRepository;
    }

    @Override
    public void postNotification(String packageName, int id) {
        events.onNext(new Event(packageName, Event.Type.POSTED, id));
    }

    @Override
    public void removeNotification(String packageName, int id) {
        events.onNext(new Event(packageName, Event.Type.REMOVED, id));
    }

    @Override
    public void clearNotifications() {
        state.clear();
        updates.onNext(state);
    }

    @Override
    public Observable<Map<AppDescriptor, NotificationBadge>> observe() {
        return Observable.combineLatest(observeApps(), events, this::updateState)
                .startWith(state)
                .mergeWith(updates)
                .map(Collections::unmodifiableMap);
    }

    @NotNull
    private Map<AppDescriptor, NotificationBadge> updateState(List<AppDescriptor> appDescriptors, Event event) {
        AppDescriptor app = findAppDescriptor(appDescriptors, event.packageName);
        if (app != null) {
            NotificationBadge badge = state.get(app);
            if (badge != null) {
                switch (event.type) {
                    case POSTED:
                        if (!badge.ids.contains(event.id)) {
                            Set<Integer> s = new HashSet<>(badge.ids);
                            s.add(event.id);
                            state.put(app, new NotificationBadge(s));
                        }
                        break;
                    case REMOVED:
                        if (badge.ids.contains(event.id)) {
                            Set<Integer> s = new HashSet<>(badge.ids);
                            s.remove(event.id);
                            if (s.isEmpty()) {
                                state.remove(app);
                            } else {
                                state.put(app, new NotificationBadge(s));
                            }
                        }
                        break;
                }
            } else if (event.type == Event.Type.POSTED) {
                state.put(app, new NotificationBadge(event.id));
            }
        }
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

    private AppDescriptor findAppDescriptor(List<AppDescriptor> descriptors, String packageName) {
        for (AppDescriptor appDescriptor : descriptors) {
            if (appDescriptor.packageName.equals(packageName)) {
                return appDescriptor;
            }
        }
        return null;
    }

    private static class Event {
        final String packageName;
        final Type type;
        final int id;

        Event(String packageName, Type type, int id) {
            this.packageName = packageName;
            this.type = type;
            this.id = id;
        }

        enum Type {
            POSTED,
            REMOVED
        }
    }
}
