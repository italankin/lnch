package com.italankin.lnch.model.repository.apps;

import android.support.annotation.AnyThread;

import com.italankin.lnch.model.AppItem;

import java.util.List;

import rx.Observable;

public interface IAppsRepository {

    @AnyThread
    void reload();

    Observable<List<AppItem>> updates();

    List<AppItem> getApps();

}
