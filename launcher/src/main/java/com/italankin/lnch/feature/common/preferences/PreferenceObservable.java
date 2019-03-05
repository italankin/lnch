package com.italankin.lnch.feature.common.preferences;

import com.italankin.lnch.model.repository.prefs.Preferences;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

abstract class PreferenceObservable<V, L> {

    private final Preferences preferences;
    private final Preferences.Pref<V> pref;
    private volatile V currentValue;

    PreferenceObservable(Preferences preferences, Preferences.Pref<V> pref) {
        this.preferences = preferences;
        this.pref = pref;
        this.currentValue = preferences.get(pref);
    }

    public final Disposable subscribe(L listener) {
        onSubscribe(listener, currentValue);
        return preferences.observe(pref)
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

    protected abstract void onSubscribe(L listener, V currentValue);

    protected abstract void onValueChanged(L listener, V oldValue, V newValue);
}
