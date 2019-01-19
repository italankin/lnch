package com.italankin.lnch.feature.common.preferences;

import com.italankin.lnch.model.repository.prefs.Preferences;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

abstract class PreferenceObservable<V, L> {

    private final Preferences preferences;
    private final String key;
    private volatile V currentValue;

    PreferenceObservable(Preferences preferences, String key, V currentValue) {
        this.preferences = preferences;
        this.key = key;
        this.currentValue = currentValue;
    }

    public final Disposable subscribe(L listener) {
        onSubscribe(listener, currentValue);
        return preferences.observe()
                .filter(key::equals)
                .map(s -> getCurrentValue(preferences))
                .filter(value -> {
                    return currentValue == null
                            ? value == null
                            : !currentValue.equals(value);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(value -> {
                    onValueChanged(listener, currentValue, value);
                    currentValue = value;
                });
    }

    abstract void onSubscribe(L listener, V currentValue);

    abstract void onValueChanged(L listener, V oldValue, V newValue);

    abstract V getCurrentValue(Preferences preferences);
}
