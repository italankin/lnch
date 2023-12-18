package com.italankin.lnch.feature.home.repository;

import android.annotation.SuppressLint;
import androidx.lifecycle.LifecycleOwner;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.util.LifecycleUtils;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EditModeStateImpl implements EditModeState {

    private final DescriptorRepository descriptorRepository;
    private final List<Callback> callbacks = new CopyOnWriteArrayList<>();
    private DescriptorRepository.Editor editor = EmptyEditor.INSTANCE;

    public EditModeStateImpl(DescriptorRepository descriptorRepository) {
        this.descriptorRepository = descriptorRepository;
    }

    @Override
    public boolean isActive() {
        return editor != EmptyEditor.INSTANCE && !editor.isDisposed();
    }

    @Override
    public void activate() {
        if (isActive()) {
            return;
        }
        editor = descriptorRepository.edit();
        for (Callback callback : callbacks) {
            callback.onEditModeActivate();
        }
    }

    @Override
    public void discard() {
        requireActive();
        editor.dispose();
        editor = EmptyEditor.INSTANCE;
        for (Callback callback : callbacks) {
            callback.onEditModeDiscard();
        }
    }

    @SuppressLint("CheckResult")
    @Override
    public void commit() {
        requireActive();
        editor.commit()
                .subscribeOn(Schedulers.io())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onComplete() {
                        Timber.d("changes saved");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "saving failed:");
                    }
                });
        editor = EmptyEditor.INSTANCE;
        for (Callback callback : callbacks) {
            callback.onEditModeCommit();
        }
    }

    @Override
    public boolean hasSomethingToCommit() {
        return !editor.isEmpty();
    }

    @Override
    public void addAction(DescriptorRepository.Editor.Action action) {
        requireActive();
        editor.enqueue(action);
    }

    @Override
    public void addCallback(Callback callback) {
        callbacks.add(callback);
    }

    @Override
    public void addCallback(LifecycleOwner lifecycleOwner, Callback callback) {
        addCallback(callback);
        LifecycleUtils.doOnDestroyOnce(lifecycleOwner, () -> {
            removeCallback(callback);
        });
    }

    @Override
    public void removeCallback(Callback callback) {
        callbacks.remove(callback);
    }

    private void requireActive() {
        if (!isActive()) {
            throw new IllegalStateException("edit mode is not active");
        }
    }

    private static class EmptyEditor implements DescriptorRepository.Editor {
        private static final DescriptorRepository.Editor INSTANCE = new EmptyEditor();

        @Override
        public DescriptorRepository.Editor enqueue(Action action) {
            return this;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public DescriptorRepository.Editor clear() {
            return this;
        }

        @Override
        public Completable commit() {
            return Completable.complete();
        }

        @Override
        public void dispose() {
        }

        @Override
        public boolean isDisposed() {
            return true;
        }
    }
}
