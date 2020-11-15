package com.italankin.lnch.model.repository.prefs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import timber.log.Timber;

@SuppressLint("ApplySharedPref")
public class WidgetsStateImpl implements WidgetsState {

    private static final String PREFS_NAME = "widgets";
    private static final String KEY_IDS = "ids";

    private static final Type LIST_OF_INTEGERS = TypeToken.getParameterized(List.class, Integer.class).getType();

    private final SharedPreferences prefs;
    private final Gson gson;

    private volatile List<Integer> appWidgetIds = new ArrayList<>();

    public WidgetsStateImpl(Context context, Gson gson) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = gson;
    }

    @Override
    public Completable addWidgetId(int appWidgetId) {
        return Completable.fromRunnable(() -> {
            ArrayList<Integer> list = new ArrayList<>(appWidgetIds);
            list.add(appWidgetId);
            String s = gson.toJson(list, LIST_OF_INTEGERS);
            prefs.edit().putString(KEY_IDS, s).commit();
            Timber.d("wrote appWidgetIds=%s", list);
            appWidgetIds = list;
        });
    }

    @Override
    public Completable removeWidgetId(int appWidgetId) {
        return Completable.fromRunnable(() -> {
            ArrayList<Integer> list = new ArrayList<>(appWidgetIds);
            Iterator<Integer> i = list.iterator();
            while (i.hasNext()) {
                if (i.next() == appWidgetId) {
                    i.remove();
                    break;
                }
            }
            String s = gson.toJson(list, LIST_OF_INTEGERS);
            prefs.edit().putString(KEY_IDS, s).commit();
            Timber.d("wrote appWidgetIds=%s", list);
            appWidgetIds = list;
        });
    }

    @Override
    public Single<List<Integer>> loadWidgetIds() {
        return Single
                .<List<Integer>>fromCallable(() -> {
                    String s = prefs.getString(KEY_IDS, null);
                    if (s == null) {
                        return Collections.emptyList();
                    }
                    appWidgetIds = gson.fromJson(s, LIST_OF_INTEGERS);
                    Timber.d("loaded appWidgetIds=%s", appWidgetIds);
                    return appWidgetIds;
                })
                .onErrorReturnItem(Collections.emptyList());
    }
}
