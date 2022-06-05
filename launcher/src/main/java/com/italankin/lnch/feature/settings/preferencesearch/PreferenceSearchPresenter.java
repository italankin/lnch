package com.italankin.lnch.feature.settings.preferencesearch;

import com.arellomobile.mvp.InjectViewState;
import com.italankin.lnch.feature.base.AppPresenter;
import com.italankin.lnch.feature.settings.searchstore.SettingsEntry;
import com.italankin.lnch.feature.settings.searchstore.SettingsStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

@InjectViewState
public class PreferenceSearchPresenter extends AppPresenter<PreferenceSearchView> {

    private final SettingsStore settingsStore;

    private final PublishSubject<String> queries = PublishSubject.create();

    @Inject
    public PreferenceSearchPresenter(SettingsStore settingsStore) {
        this.settingsStore = settingsStore;
    }

    @Override
    protected void onFirstViewAttach() {
        queries
                .observeOn(Schedulers.computation())
                .debounce(300, TimeUnit.MILLISECONDS)
                .map(query -> query.trim().toLowerCase(Locale.getDefault()))
                .distinctUntilChanged()
                .switchMapSingle(this::searchPreferences)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new State<List<PreferenceSearchItem>>() {
                    @Override
                    protected void onNext(PreferenceSearchView viewState, List<PreferenceSearchItem> results) {
                        viewState.onSearchResults(results);
                    }
                });
    }

    void search(String query) {
        queries.onNext(query);
    }

    private Single<List<PreferenceSearchItem>> searchPreferences(String query) {
        return Single.fromCallable(() -> {
            if (query.length() < 2) {
                return Collections.emptyList();
            }
            List<SettingsEntry> entries = settingsStore.search(query);
            LinkedHashMap<Integer, List<PreferenceSearchItem>> byCategories = new LinkedHashMap<>(entries.size() + 5);
            for (SettingsEntry entry : entries) {
                List<PreferenceSearchItem> categoryEntries = byCategories.get(entry.category());
                if (categoryEntries == null) {
                    categoryEntries = new ArrayList<>(4);
                    byCategories.put(entry.category(), categoryEntries);
                    categoryEntries.add(new PreferenceSearchCategory(entry.category()));
                }
                categoryEntries.add(new PreferenceSearch(entry));
            }
            List<PreferenceSearchItem> results = new ArrayList<>(entries.size() + 5);
            for (List<PreferenceSearchItem> value : byCategories.values()) {
                results.addAll(value);
            }
            return results;
        });
    }
}
