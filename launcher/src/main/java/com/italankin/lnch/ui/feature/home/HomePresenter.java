package com.italankin.lnch.ui.feature.home;

import com.arellomobile.mvp.InjectViewState;
import com.italankin.lnch.bean.AppItem;
import com.italankin.lnch.bean.Unit;
import com.italankin.lnch.model.repository.apps.AppsRepository;
import com.italankin.lnch.model.repository.apps.actions.SwapAction;
import com.italankin.lnch.model.repository.search.SearchRepository;
import com.italankin.lnch.ui.base.AppPresenter;
import com.italankin.lnch.util.AppPrefs;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import timber.log.Timber;

import static com.italankin.lnch.bean.Unit.UNIT;

@InjectViewState
public class HomePresenter extends AppPresenter<HomeView> {

    private final AppsRepository appsRepository;
    private final SearchRepository searchRepository;
    private final AppPrefs appPrefs;
    private final Subject<Unit> reloadApps = PublishSubject.create();
    private List<AppItem> apps;
    private AppsRepository.Editor editor;

    @Inject
    HomePresenter(AppsRepository appsRepository, SearchRepository searchRepository, AppPrefs appPrefs) {
        this.appsRepository = appsRepository;
        this.searchRepository = searchRepository;
        this.appPrefs = appPrefs;
    }

    @Override
    protected void onFirstViewAttach() {
        observeApps();
        initialLoad();
        subscribeUpdates();
    }

    void notifyPackageChanged() {
        reloadApps.onNext(UNIT);
    }

    void reloadAppsImmediate() {
        update();
    }

    void startEditMode() {
        if (editor != null) {
            throw new IllegalStateException("Editor is not null!");
        }
        editor = appsRepository.edit();
        getViewState().onStartEditMode();
    }

    void swapItems(int from, int to) {
        if (editor == null) {
            throw new IllegalStateException();
        }
        editor.enqueue(new SwapAction(from, to));
        SwapAction.swap(apps, from, to);
        getViewState().onItemsSwap(from, to);
    }

    void stopEditMode() {
        if (editor == null) {
            throw new IllegalStateException("Editor is null!");
        }
        editor.commit()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableState() {
                    @Override
                    protected void onComplete(HomeView viewState) {
                        viewState.onStopEditMode();
                        update();
                    }

                    @Override
                    protected void onError(HomeView viewState, Throwable e) {
                        viewState.showError(e);
                    }
                });
        editor = null;
    }

    private void initialLoad() {
        getViewState().showProgress();
        update();
    }

    private void subscribeUpdates() {
        reloadApps
                .debounce(1, TimeUnit.SECONDS)
                .ignoreElements()
                .subscribe(new CompletableState() {
                    @Override
                    public void onComplete() {
                        update();
                    }
                });
    }

    private void update() {
        appsRepository.update()
                .subscribeOn(Schedulers.io())
                .subscribe(new CompletableState() {
                    @Override
                    public void onComplete() {
                        Timber.d("Apps updated");
                    }
                });
    }

    private void observeApps() {
        appsRepository.observeApps()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new State<List<AppItem>>() {
                    @Override
                    protected void onNext(HomeView viewState, List<AppItem> list) {
                        Timber.d("Receive update: %s", list);
                        apps = list;
                        viewState.onAppsLoaded(apps, searchRepository, appPrefs.homeLayout());
                    }
                });
    }
}
