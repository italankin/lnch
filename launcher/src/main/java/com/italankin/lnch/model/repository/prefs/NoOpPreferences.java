package com.italankin.lnch.model.repository.prefs;

import io.reactivex.Observable;

public class NoOpPreferences implements Preferences {

    @Override
    public <T> T get(Pref<T> pref) {
        return null;
    }

    @Override
    public <T> Pref<T> find(String key) {
        return null;
    }

    @Override
    public <T> void set(Pref<T> pref, T newValue) {
    }

    @Override
    public Observable<Pref<?>> observe() {
        return Observable.empty();
    }

    @Override
    public <T> Observable<T> observe(Pref<T> pref) {
        return Observable.empty();
    }

    @Override
    public <T> Observable<Value<T>> observeValue(Pref<T> pref) {
        return Observable.empty();
    }

    @Override
    public void reset(Pref<?>... prefs) {
    }
}
