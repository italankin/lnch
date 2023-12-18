package com.italankin.lnch.feature.home.repository;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;

public interface EditModeState {

    boolean isActive();

    void activate();

    void discard();

    void commit();

    boolean hasSomethingToCommit();

    void addAction(DescriptorRepository.Editor.Action action);

    <T> T getProperty(Property<T> property);

    <T> void setProperty(Property<T> property, T newValue);

    void addCallback(Callback callback);

    void addCallback(LifecycleOwner lifecycleOwner, Callback callback);

    void removeCallback(Callback callback);

    interface Callback {

        default void onEditModeActivate() {
        }

        default void onEditModeDiscard() {
        }

        default void onEditModeCommit() {
        }

        default <T> void onEditModePropertyChange(Property<T> property, T newValue) {
        }
    }

    class Property<T> {
        final String key;

        public Property(String key) {
            this.key = key;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj instanceof Property) {
                return key.equals(((Property<?>) obj).key);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }
    }
}
