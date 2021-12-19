package com.italankin.lnch.model.repository.prefs;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;

public class UserPreferences implements Preferences {

    private static final Map<String, Pref<?>> PREFS;

    static {
        PREFS = new HashMap<>(Preferences.ALL.size());
        for (Pref<?> pref : Preferences.ALL) {
            PREFS.put(pref.key(), pref);
        }
    }

    private final SharedPreferences prefs;
    private final Observable<String> updates;

    @Inject
    public UserPreferences(SharedPreferences sharedPreferences) {
        this.prefs = sharedPreferences;
        this.updates = Observable
                .<String>create(emitter -> {
                    OnSharedPreferenceChangeListener listener = (sp, key) -> {
                        if (!emitter.isDisposed()) {
                            emitter.onNext(key);
                        }
                    };
                    prefs.registerOnSharedPreferenceChangeListener(listener);
                    emitter.setCancellable(() -> prefs.unregisterOnSharedPreferenceChangeListener(listener));
                })
                .debounce(100, TimeUnit.MILLISECONDS)
                .share();
    }

    @Override
    public <T> T get(Pref<T> pref) {
        return pref.fetcher().fetch(prefs, pref.key());
    }

    @Override
    public <T> void set(Pref<T> pref, T newValue) {
        pref.updater().update(prefs, pref.key(), newValue);
    }

    @Override
    public void reset(Pref<?>... prefs) {
        SharedPreferences.Editor editor = this.prefs.edit();
        for (Pref<?> pref : prefs) {
            editor.remove(pref.key());
        }
        editor.apply();
    }

    @Override
    public Observable<Pref<?>> observe() {
        return updates.map(PREFS::get);
    }

    @Override
    public <T> Observable<T> observe(Pref<T> pref) {
        return observeValue(pref)
                .map(Value::get)
                .startWith(get(pref));
    }

    @Override
    public <T> Observable<Value<T>> observeValue(Pref<T> pref) {
        return updates
                .filter(pref.key()::equals)
                .map(key -> new Value<>(get(pref)));
    }
}
