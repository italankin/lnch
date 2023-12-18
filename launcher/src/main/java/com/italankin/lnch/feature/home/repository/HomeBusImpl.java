package com.italankin.lnch.feature.home.repository;

import androidx.lifecycle.LifecycleOwner;
import com.italankin.lnch.util.LifecycleUtils;
import timber.log.Timber;

import java.util.concurrent.CopyOnWriteArrayList;

public class HomeBusImpl implements HomeBus {

    private final CopyOnWriteArrayList<EventListener> listeners = new CopyOnWriteArrayList<>();

    @Override
    public void post(Event event) {
        Timber.d("post: %s", event);
        for (EventListener listener : listeners) {
            listener.onHomeEvent(this, event);
        }
    }

    @Override
    public void subscribe(EventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void subscribe(LifecycleOwner lifecycleOwner, EventListener listener) {
        subscribe(listener);
        LifecycleUtils.doOnDestroyOnce(lifecycleOwner, () -> {
            unsubscribe(listener);
        });
    }

    @Override
    public void unsubscribe(EventListener listener) {
        listeners.remove(listener);
    }
}
