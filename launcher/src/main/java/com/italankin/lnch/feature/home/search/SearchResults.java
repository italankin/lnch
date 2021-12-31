package com.italankin.lnch.feature.home.search;

import com.italankin.lnch.model.repository.search.SearchRepository;
import com.italankin.lnch.model.repository.search.match.Match;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import timber.log.Timber;

class SearchResults {

    private final SearchRepository searchRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private final PublishSubject<CharSequence> events = PublishSubject.create();

    SearchResults(SearchRepository searchRepository) {
        this.searchRepository = searchRepository;
    }

    void query(CharSequence constraint) {
        events.onNext(constraint != null ? constraint : "");
    }

    @SuppressWarnings("unchecked")
    void subscribe(Callback callback) {
        events.debounce(200, TimeUnit.MILLISECONDS)
                .map(constraint -> {
                    if (constraint.length() == 0) {
                        return (List<Match>) searchRepository.recent();
                    }
                    return (List<Match>) searchRepository.search(constraint);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Match>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposables.add(d);
                    }

                    @Override
                    public void onNext(List<Match> matches) {
                        callback.onSearchResults(matches);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "subscribe:");
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    void unsubscribe() {
        disposables.clear();
    }

    interface Callback {
        void onSearchResults(List<Match> results);
    }
}
