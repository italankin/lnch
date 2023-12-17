package com.italankin.lnch.feature.settings.preferencesearch;

import com.italankin.lnch.feature.base.AppViewModel;
import com.italankin.lnch.feature.settings.searchstore.SettingsEntry;
import com.italankin.lnch.feature.settings.searchstore.SettingsStore;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class PreferenceSearchViewModel extends AppViewModel {

    private final SettingsStore settingsStore;

    private final PublishSubject<String> queries = PublishSubject.create();
    private final BehaviorSubject<List<PreferenceSearchItem>> searchResultsSubject = BehaviorSubject.create();

    @Inject
    public PreferenceSearchViewModel(SettingsStore settingsStore) {
        this.settingsStore = settingsStore;

        queries
                .observeOn(Schedulers.computation())
                .debounce(300, TimeUnit.MILLISECONDS)
                .map(query -> query.trim().toLowerCase(Locale.getDefault()))
                .filter(query -> !query.isBlank())
                .distinctUntilChanged()
                .switchMapSingle(this::searchPreferences)
                .subscribe(new State<>() {
                    @Override
                    public void onNext(List<PreferenceSearchItem> results) {
                        searchResultsSubject.onNext(results);
                    }
                });
    }

    Observable<List<PreferenceSearchItem>> searchResultsEvents() {
        return searchResultsSubject.observeOn(AndroidSchedulers.mainThread());
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
            LinkedHashMap<Integer, List<PreferenceSearchItem>> byCategories = new LinkedHashMap<>(5);
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
