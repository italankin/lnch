package com.italankin.lnch.feature.home.repository;

import android.annotation.SuppressLint;
import androidx.lifecycle.LifecycleOwner;
import com.italankin.lnch.model.descriptor.mutable.MutableDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.util.LifecycleUtils;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class EditModeStateImpl implements EditModeState {

    private final DescriptorRepository descriptorRepository;
    private final Preferences preferences;

    private final List<Callback> callbacks = new CopyOnWriteArrayList<>();

    private DescriptorRepository.Editor editor = EmptyEditor.INSTANCE;
    private final Map<Property<?>, Object> properties = new HashMap<>(4);

    public EditModeStateImpl(DescriptorRepository descriptorRepository, Preferences preferences) {
        this.descriptorRepository = descriptorRepository;
        this.preferences = preferences;
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
        properties.clear();
        for (Callback callback : callbacks) {
            callback.onEditModeActivate();
        }
    }

    @Override
    public void discard() {
        requireActive();
        editor.dispose();
        properties.clear();
        editor = EmptyEditor.INSTANCE;
        for (Callback callback : callbacks) {
            callback.onEditModeDiscard();
        }
    }

    @SuppressLint("CheckResult")
    @SuppressWarnings("unchecked")
    @Override
    public void commit() {
        requireActive();
        for (Map.Entry<Property<?>, Object> entry : properties.entrySet()) {
            Property<?> property = entry.getKey();
            if (property instanceof PreferenceProperty) {
                editor.enqueue(new PreferenceWriteAction<>(preferences, (PreferenceProperty<Object>) property, entry.getValue()));
            }
        }
        editor.commit()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onComplete() {
                        Timber.d("changes saved");
                        for (Callback callback : callbacks) {
                            callback.onEditModeCommitFinish();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "saving failed:");
                    }
                });
        editor = EmptyEditor.INSTANCE;
        properties.clear();
        for (Callback callback : callbacks) {
            callback.onEditModeCommitStart();
        }
    }

    @Override
    public boolean hasSomethingToCommit() {
        if (!editor.isEmpty()) {
            return true;
        }
        for (Property<?> property : properties.keySet()) {
            if (property instanceof PreferenceProperty) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void addAction(DescriptorRepository.Editor.Action action) {
        requireActive();
        editor.enqueue(action);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getProperty(Property<T> property) {
        requireActive();
        return (T) properties.get(property);
    }

    @Override
    public boolean isPropertySet(Property<?> property) {
        requireActive();
        return properties.containsKey(property);
    }

    @Override
    public <T> void setProperty(Property<T> property, T newValue) {
        requireActive();
        properties.put(property, newValue);
        Timber.d("setProperty: %s=%s", property, newValue);
        for (Callback callback : callbacks) {
            callback.onEditModePropertyChange(property, newValue);
        }
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

    private static class PreferenceWriteAction<T> implements DescriptorRepository.Editor.Action {
        private final Preferences preferences;
        private final PreferenceProperty<T> property;
        private final T newValue;

        private PreferenceWriteAction(Preferences preferences, PreferenceProperty<T> property, T newValue) {
            this.preferences = preferences;
            this.property = property;
            this.newValue = newValue;
        }

        @Override
        public void apply(List<MutableDescriptor<?>> items) {
            property.write(preferences, newValue);
        }
    }
}
