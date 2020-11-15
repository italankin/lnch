package com.italankin.lnch.model.repository.prefs;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

public interface WidgetsState {

    Completable addWidgetId(int appWidgetId);

    Completable removeWidgetId(int appWidgetId);

    Single<List<Integer>> loadWidgetIds();
}
