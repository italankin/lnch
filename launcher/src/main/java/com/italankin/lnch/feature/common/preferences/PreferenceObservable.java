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
        return preferences.observeValue(pref)
                .filter(value -> {
                    V newValue = value.get();
                    return currentValue == null
                            ? newValue == null
                            : !currentValue.equals(newValue);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(value -> {
                    V newValue = value.get();
                    onValueChanged(listener, currentValue, newValue);
                    currentValue = newValue;
                });
    }

    protected abstract void onSubscribe(L listener, V currentValue);

    protected abstract void onValueChanged(L listener, V oldValue, V newValue);
}
