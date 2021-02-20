package com.italankin.lnch.model.repository.descriptor;

import com.italankin.lnch.model.descriptor.Descriptor;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;

public interface DescriptorRepository {

    Observable<List<Descriptor>> observe();

    Completable update();

    Editor edit();

    Completable clear();

    List<Descriptor> items();

    <T extends Descriptor> List<T> itemsOfType(Class<T> klass);

    <T extends Descriptor> T findById(Class<T> klass, String id);

    interface Editor {

        Editor enqueue(Action action);

        boolean isEmpty();

        Editor clear();

        Completable commit();

        interface Action {
            void apply(List<Descriptor> items);
        }
    }
}
