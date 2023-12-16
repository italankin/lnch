package com.italankin.lnch.feature.home.repository;

import androidx.lifecycle.LifecycleOwner;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;

public interface EditModeState {

    boolean isActive();

    void activate();

    void discard();

    void commit();

    boolean hasSomethingToCommit();

    void addAction(DescriptorRepository.Editor.Action action);

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
    }
}
