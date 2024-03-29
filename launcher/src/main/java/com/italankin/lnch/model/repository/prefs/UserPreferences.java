package com.italankin.lnch.model.repository.prefs;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import timber.log.Timber;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class UserPreferences implements Preferences {

    private static final Map<String, Pref<?>> PREFS;

    static {
        PREFS = new HashMap<>(Preferences.ALL.size());
        for (Pref<?> pref : Preferences.ALL) {
            PREFS.put(pref.key(), pref);
        }
    }

    private final SharedPreferences prefs;
    private final PublishSubject<String> removedKeys = PublishSubject.create();
    private final Observable<List<String>> updates;

    @Inject
    public UserPreferences(SharedPreferences sharedPreferences) {
        this.prefs = sharedPreferences;
        this.updates = Observable
                .<String>create(emitter -> {
                    OnSharedPreferenceChangeListener listener = (sp, key) -> {
                        Timber.d("onPrefChanged: key=%s", key);
                        if (!emitter.isDisposed()) {
                            emitter.onNext(key);
                        }
                    };
                    prefs.registerOnSharedPreferenceChangeListener(listener);
                    emitter.setCancellable(() -> prefs.unregisterOnSharedPreferenceChangeListener(listener));
                })
                .mergeWith(removedKeys)
                .buffer(100,TimeUnit.MILLISECONDS)
                .share();
    }

    @Override
    public <T> T get(Pref<T> pref) {
        return pref.fetcher().fetch(prefs, pref.key());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Pref<T> find(String key) {
        return (Pref<T>) findByKey(key);
    }

    @Override
    public <T> void set(Pref<T> pref, T newValue) {
        pref.updater().update(prefs, pref.key(), newValue);
    }

    @Override
    public void reset(Pref<?>... prefs) {
        List<String> keys = new ArrayList<>(prefs.length);
        SharedPreferences.Editor editor = this.prefs.edit();
        for (Pref<?> pref : prefs) {
            String key = pref.key();
            editor.remove(key);
            keys.add(key);
        }
        editor.apply();
        for (String key : keys) {
            Timber.d("removed pref: key=%s", key);
            removedKeys.onNext(key);
        }
    }

    @Override
    public Observable<Set<Pref<?>>> observe() {
        return updates.map(keys -> {
            Set<Pref<?>> prefs = new HashSet<>(keys.size());
            for (String key : keys) {
                prefs.add(findByKey(key));
            }
            return prefs;
        });
    }

    @Override
    public <T> Observable<T> observe(Pref<T> pref, boolean startWithCurrent) {
        return observeValue(pref, startWithCurrent).map(Value::get);
    }

    @Override
    public <T> Observable<Value<T>> observeValue(Pref<T> pref, boolean startWithCurrent) {
        Observable<Value<T>> observable = updates
                .filter(keys -> keys.contains(pref.key()))
                .map(key -> new Value<>(get(pref)));
        if (startWithCurrent) {
            return observable.startWith(new Value<>(get(pref)));
        } else {
            return observable;
        }
    }

    private Pref<?> findByKey(String key) {
        Pref<?> pref = PREFS.get(key);
        if (pref == null) {
            throw new NullPointerException("No pref found for key=" + key + ", is it registered in Preferences.ALL?");
        }
        return pref;
    }
}
