package com.italankin.lnch.util.rxjava;

import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Stores {@link Disposable}s in a list of {@link WeakReference}, allowing GC to remove completed jobs.
 */
public class WeakDisposableList {

    private final ArrayList<WeakDisposable> disposables = new ArrayList<>(4);

    public synchronized void add(Disposable d) {
        trim();
        if (!d.isDisposed()) {
            disposables.add(new WeakDisposable(d));
        }
    }

    public synchronized void clear() {
        for (Disposable disposable : disposables) {
            disposable.dispose();
        }
        disposables.clear();
    }

    private void trim() {
        int oldSize = disposables.size();
        Iterator<WeakDisposable> iterator = disposables.iterator();
        while (iterator.hasNext()) {
            Disposable disposable = iterator.next();
            if (disposable.isDisposed()) {
                iterator.remove();
            }
        }
        Timber.d("trim: oldSize=%d, newSize=%d", oldSize, disposables.size());
    }

    private static class WeakDisposable implements Disposable {
        private final WeakReference<Disposable> ref;

        private WeakDisposable(Disposable disposable) {
            this.ref = new WeakReference<>(disposable);
        }

        @Override
        public void dispose() {
            Disposable d = ref.get();
            if (d != null && !d.isDisposed()) {
                d.dispose();
                ref.clear();
            }
        }

        @Override
        public boolean isDisposed() {
            Disposable d = ref.get();
            return d == null || d.isDisposed();
        }
    }
}
