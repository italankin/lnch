package com.italankin.lnch.model.repository.apps;

import com.italankin.lnch.bean.AppItem;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

public interface AppsRepository {

    Observable<List<AppItem>> observeApps();

    Single<List<AppItem>> getAllApps();

    Completable update();

    Editor edit();

    List<AppItem> getApps();

    interface Editor {

        void enqueue(Action action);

        Completable commit();

        interface Action {
            void apply(List<AppItem> items);
        }
    }
}
