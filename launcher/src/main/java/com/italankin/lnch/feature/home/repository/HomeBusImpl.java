package com.italankin.lnch.feature.home.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
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
        Lifecycle lifecycle = lifecycleOwner.getLifecycle();
        if (lifecycle.getCurrentState() == Lifecycle.State.DESTROYED) {
            return;
        }
        subscribe(listener);
        lifecycle.addObserver(new DefaultLifecycleObserver() {
            @Override
            public void onDestroy(@NonNull LifecycleOwner owner) {
                owner.getLifecycle().removeObserver(this);
                unsubscribe(listener);
            }
        });
    }

    @Override
    public void unsubscribe(EventListener listener) {
        listeners.remove(listener);
    }
}
