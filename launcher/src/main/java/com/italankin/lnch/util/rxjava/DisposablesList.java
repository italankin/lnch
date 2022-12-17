package com.italankin.lnch.util.rxjava;

import java.util.ArrayList;
import java.util.Iterator;

import io.reactivex.disposables.Disposable;

public class DisposablesList {

    private final ArrayList<Disposable> disposables = new ArrayList<>();

    public synchronized void add(Disposable d) {
        removeDisposed();
        if (!d.isDisposed()) {
            disposables.add(d);
        }
    }

    public synchronized void clear() {
        for (Disposable disposable : disposables) {
            disposable.dispose();
        }
        disposables.clear();
    }

    private void removeDisposed() {
        Iterator<Disposable> iterator = disposables.iterator();
        while (iterator.hasNext()) {
            Disposable disposable = iterator.next();
            if (disposable.isDisposed()) {
                iterator.remove();
            }
        }
    }
}
