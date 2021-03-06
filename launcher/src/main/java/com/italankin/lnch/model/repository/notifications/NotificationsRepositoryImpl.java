package com.italankin.lnch.model.repository.notifications;

import android.service.notification.StatusBarNotification;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.util.DescriptorUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import androidx.annotation.Nullable;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class NotificationsRepositoryImpl implements NotificationsRepository {

    private final DescriptorRepository descriptorRepository;
    private final Preferences preferences;
    private final PublishSubject<Map<AppDescriptor, NotificationDot>> updates = PublishSubject.create();

    private final ConcurrentMap<AppDescriptor, NotificationDot> state = new ConcurrentHashMap<>();

    public NotificationsRepositoryImpl(DescriptorRepository descriptorRepository, Preferences preferences) {
        this.descriptorRepository = descriptorRepository;
        this.preferences = preferences;
    }

    @Override
    public void postNotification(StatusBarNotification sbn) {
        if (modifyState(sbn, Type.POSTED)) {
            updates.onNext(state);
        }
    }

    @Override
    public void removeNotification(StatusBarNotification sbn) {
        if (modifyState(sbn, Type.REMOVED)) {
            updates.onNext(state);
        }
    }

    @Override
    public void postNotifications(StatusBarNotification... sbns) {
        boolean modified = false;
        for (StatusBarNotification sbn : sbns) {
            // always run modifyState function
            modified = modifyState(sbn, Type.POSTED) | modified;
        }
        if (modified) {
            updates.onNext(state);
        }
    }

    @Override
    public void clearNotifications() {
        state.clear();
        updates.onNext(state);
    }

    @Override
    public Observable<Map<AppDescriptor, NotificationDot>> observe() {
        return observeApps()
                .map(this::updateState)
                .startWith(state)
                .mergeWith(updates)
                .map(Collections::unmodifiableMap);
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

    private Map<AppDescriptor, NotificationDot> updateState(List<AppDescriptor> appDescriptors) {
        // remove any notifications, which belong to non-existent apps
        Set<AppDescriptor> apps = new HashSet<>(appDescriptors);
        for (Iterator<AppDescriptor> i = state.keySet().iterator(); i.hasNext(); ) {
            if (!apps.contains(i.next())) {
                i.remove();
            }
        }
        return state;
    }

    private boolean modifyState(StatusBarNotification sbn, Type type) {
        List<AppDescriptor> appDescriptors = descriptorRepository.itemsOfType(AppDescriptor.class);
        AppDescriptor app = DescriptorUtils.findAppByPackageName(appDescriptors, sbn.getPackageName());
        if (app != null) {
            NotificationDot dot = state.get(app);
            if (sbn.isOngoing() && !showOngoing()) {
                return modifyStateRemove(app, dot, sbn.getId());
            }
            switch (type) {
                case POSTED:
                    return modifyStatePost(app, dot, sbn.getId());
                case REMOVED:
                    return modifyStateRemove(app, dot, sbn.getId());
            }
        }
        return false;
    }

    private boolean modifyStatePost(AppDescriptor app, @Nullable NotificationDot dot, int id) {
        if (dot == null) {
            state.put(app, new NotificationDot(id));
            return true;
        }
        if (dot.ids.contains(id)) {
            return false;
        }
        HashSet<Integer> s = new HashSet<>(dot.ids);
        s.add(id);
        state.put(app, new NotificationDot(s));
        return true;
    }

    private boolean modifyStateRemove(AppDescriptor app, @Nullable NotificationDot dot, int id) {
        if (dot == null || !dot.ids.contains(id)) {
            return false;
        }
        HashSet<Integer> s = new HashSet<>(dot.ids);
        s.remove(id);
        if (s.isEmpty()) {
            state.remove(app);
        } else {
            state.replace(app, new NotificationDot(s));
        }
        return true;
    }

    private Boolean showOngoing() {
        return preferences.get(Preferences.NOTIFICATION_DOT_ONGOING);
    }

    private enum Type {
        POSTED,
        REMOVED
    }
}
