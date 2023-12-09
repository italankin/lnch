package com.italankin.lnch.model.repository.descriptor;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.mutable.MutableDescriptor;
import io.reactivex.Completable;
import io.reactivex.Observable;

import java.util.List;

public interface DescriptorRepository {

    /**
     * Observe changes in descriptors data
     */
    Observable<List<Descriptor>> observe();

    /**
     * Observe changes in descriptors data
     *
     * @param updateIfEmpty when {@code true}, perform an update if state is empty
     */
    Observable<List<Descriptor>> observe(boolean updateIfEmpty);

    /**
     * Update descriptor data, reflecting (possible) external changes
     */
    Completable update();

    /**
     * @return an {@link Editor} object which can alter descriptors state
     */
    Editor edit();

    /**
     * Clear all descriptors data
     */
    Completable clear();

    /**
     * @return current state of descriptors
     */
    List<Descriptor> items();

    /**
     * @return descriptors list of a given {@code klass}
     */
    <T extends Descriptor> List<T> itemsOfType(Class<T> klass);

    /**
     * @return a descriptor of {@code klass} type and a given {@code id}
     */
    <T extends Descriptor> T findById(Class<T> klass, String id);

    /**
     * Editing interface for descriptors data
     */
    interface Editor {

        /**
         * Add an {@link Action} on descriptors data
         */
        Editor enqueue(Action action);

        /**
         * @return {@code true} if this editor has enqueued actions
         */
        boolean isEmpty();

        /**
         * Remove any enqueued actions in this editor instance
         */
        Editor clear();

        /**
         * Commit any changes made by this editor
         */
        Completable commit();

        /**
         * Dispose this editor, discarding changes
         */
        void dispose();

        /**
         * @return whenever this editor is disposed
         */
        boolean isDisposed();

        interface Action {
            void apply(List<MutableDescriptor<?>> items);
        }
    }
}
