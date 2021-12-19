package com.italankin.lnch.model.repository.notifications;

import android.service.notification.StatusBarNotification;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
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
    private final PublishSubject<Map<AppDescriptor, NotificationBag>> updates = PublishSubject.create();

    private final ConcurrentMap<AppDescriptor, NotificationBag> state = new ConcurrentHashMap<>();

    private volatile Callback callback;

    public NotificationsRepositoryImpl(DescriptorRepository descriptorRepository) {
        this.descriptorRepository = descriptorRepository;
    }

    @Override
    public void setCallback(@Nullable Callback callback) {
        this.callback = callback;
    }

    @Nullable
    @Override
    public Callback getCallback() {
        return callback;
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

    @Nullable
    @Override
    public NotificationBag getByApp(AppDescriptor descriptor) {
        return state.get(descriptor);
    }

    @Override
    public Observable<Map<AppDescriptor, NotificationBag>> observe() {
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

    private Map<AppDescriptor, NotificationBag> updateState(List<AppDescriptor> appDescriptors) {
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
        if (app == null) {
            return false;
        }
        NotificationBag dot = state.get(app);
        switch (type) {
            case POSTED:
                return modifyStatePost(app, dot, sbn);
            case REMOVED:
                return modifyStateRemove(app, dot, sbn);
            default:
                return false;
        }
    }

    private boolean modifyStatePost(AppDescriptor app, @Nullable NotificationBag bag, StatusBarNotification newSbn) {
        if (bag == null) {
            state.put(app, new NotificationBag(app, newSbn));
            return true;
        }
        if (containsNotification(bag, newSbn)) {
            return false;
        }
        ArrayList<StatusBarNotification> sbns = new ArrayList<>(bag.sbns);
        boolean replaced = false;
        for (int i = 0, s = sbns.size(); i < s; i++) {
            if (sbns.get(i).getId() == newSbn.getId()) {
                sbns.set(i, newSbn);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            sbns.add(newSbn);
        }
        state.put(app, new NotificationBag(app, sbns));
        return true;
    }

    private boolean modifyStateRemove(AppDescriptor app, @Nullable NotificationBag bag, StatusBarNotification sbn) {
        if (bag == null) {
            return false;
        }
        if (!containsNotification(bag, sbn)) {
            return false;
        }
        ArrayList<StatusBarNotification> sbns = new ArrayList<>(bag.sbns);
        for (int i = 0, s = sbns.size(); i < s; i++) {
            if (sbns.get(i).getId() == sbn.getId()) {
                sbns.remove(i);
                break;
            }
        }
        if (sbns.isEmpty()) {
            state.remove(app);
        } else {
            state.replace(app, new NotificationBag(app, sbns));
        }
        return true;
    }

    private static boolean containsNotification(NotificationBag bag, StatusBarNotification sbn) {
        for (StatusBarNotification s : bag.sbns) {
            if (s.getId() == sbn.getId()) {
                return true;
            }
        }
        return false;
    }

    private enum Type {
        POSTED,
        REMOVED
    }
}
