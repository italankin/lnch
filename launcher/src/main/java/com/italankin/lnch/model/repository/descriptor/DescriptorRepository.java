package com.italankin.lnch.model.repository.descriptor;

import com.italankin.lnch.model.descriptor.Descriptor;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;

public interface DescriptorRepository {

    /**
     * Observe changes in descriptors data
     */
    Observable<List<Descriptor>> observe();

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
     * @return a unique state key of current items
     */
    int stateKey();

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

        interface Action {
            void apply(List<Descriptor> items);
        }
    }
}
