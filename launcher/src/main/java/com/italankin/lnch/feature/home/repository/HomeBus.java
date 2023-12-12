package com.italankin.lnch.feature.home.repository;

import androidx.lifecycle.LifecycleOwner;

public interface HomeBus {

    void post(Event event);

    void subscribe(EventListener listener);

    void subscribe(LifecycleOwner lifecycleOwner, EventListener listener);

    void unsubscribe(EventListener listener);

    interface EventListener {
        void onHomeEvent(HomeBus bus, Event event);
    }

    interface Event {
    }
}
