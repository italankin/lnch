package com.italankin.lnch.model.repository.apps;

import com.italankin.lnch.model.repository.descriptors.Descriptor;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

public interface AppsRepository {

    Observable<List<Descriptor>> observe();

    Single<List<Descriptor>> fetch();

    Completable update();

    Editor edit();

    List<Descriptor> items();

    interface Editor {

        Editor enqueue(Action action);

        Completable commit();

        interface Action {
            void apply(List<Descriptor> items);
        }
    }
}
