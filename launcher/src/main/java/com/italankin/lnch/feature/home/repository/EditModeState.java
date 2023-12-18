package com.italankin.lnch.feature.home.repository;

import androidx.lifecycle.LifecycleOwner;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.prefs.Preferences;

public interface EditModeState {

    boolean isActive();

    void activate();

    void discard();

    void commit();

    boolean hasSomethingToCommit();

    void addAction(DescriptorRepository.Editor.Action action);

    <T> T getProperty(Property<T> property);

    boolean isPropertySet(Property<?> property);

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

    interface Property<T> {
    }

    interface PreferenceProperty<T> extends Property<T> {

        void write(Preferences preferences, T newValue);
    }
}
