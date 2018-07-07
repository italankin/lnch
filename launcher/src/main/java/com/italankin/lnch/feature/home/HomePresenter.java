package com.italankin.lnch.feature.home;

import com.arellomobile.mvp.InjectViewState;
import com.italankin.lnch.bean.GroupSeparator;
import com.italankin.lnch.feature.base.AppPresenter;
import com.italankin.lnch.feature.home.model.AppViewModel;
import com.italankin.lnch.feature.home.model.GroupSeparatorViewModel;
import com.italankin.lnch.model.repository.apps.AppsRepository;
import com.italankin.lnch.model.repository.apps.actions.AddSeparatorAction;
import com.italankin.lnch.model.repository.apps.actions.RemoveSeparatorAction;
import com.italankin.lnch.model.repository.apps.actions.RenameAction;
import com.italankin.lnch.model.repository.apps.actions.SetCustomColorAction;
import com.italankin.lnch.model.repository.apps.actions.SetVisibilityAction;
import com.italankin.lnch.model.repository.apps.actions.SwapAction;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.util.rx.ListMapper;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

@InjectViewState
public class HomePresenter extends AppPresenter<HomeView> {

    private final AppsRepository appsRepository;
    private final Preferences preferences;
    /**
     * View commands will dispatch this instance on every state restore, so any changes
     * made to this list will be visible to new views.
     */
    private List<AppViewModel> apps;
    private AppsRepository.Editor editor;

    @Inject
    HomePresenter(AppsRepository appsRepository, Preferences preferences) {
        this.appsRepository = appsRepository;
        this.preferences = preferences;
    }

    @Override
    protected void onFirstViewAttach() {
        observeApps();
        loadApps();
    }

    void loadApps() {
        getViewState().showProgress();
        update();
    }

    void reloadAppsImmediate() {
        update();
    }

    void hideGroup(int position) {
        GroupSeparatorViewModel group = (GroupSeparatorViewModel) apps.get(position);
        setGroupExpanded(position, !group.expanded);
    }

    void startEditMode() {
        if (editor != null) {
            throw new IllegalStateException("Editor is not null!");
        }
        editor = appsRepository.edit();
        expandGroups();
        getViewState().onStartEditMode();
    }

    void swapApps(int from, int to) {
        requireEditMode();
        editor.enqueue(new SwapAction(from, to));
        SwapAction.swap(apps, from, to);
        getViewState().onItemsSwap(from, to);
    }

    void renameApp(int position, AppViewModel item, String customLabel) {
        requireEditMode();
        String s = customLabel.isEmpty() ? null : customLabel;
        editor.enqueue(new RenameAction(item.item, s));
        item.customLabel = s;
        getViewState().onItemChanged(position);
    }

    void changeAppCustomColor(int position, AppViewModel item, String value) {
        requireEditMode();
        Integer customColor;
        if (value != null && !value.isEmpty()) {
            try {
                customColor = Integer.decode("0x" + value) + 0xff000000;
            } catch (Exception e) {
                getViewState().showError(e);
                return;
            }
        } else {
            customColor = null;
        }
        editor.enqueue(new SetCustomColorAction(item.item, customColor));
        item.customColor = customColor;
        getViewState().onItemChanged(position);
    }

    void hideApp(int position, AppViewModel item) {
        requireEditMode();
        editor.enqueue(new SetVisibilityAction(item.item, false));
        item.hidden = true;
        getViewState().onItemChanged(position);
    }

    void addSeparator(int position) {
        requireEditMode();
        GroupSeparator item = new GroupSeparator();
        editor.enqueue(new AddSeparatorAction(position, item));
        apps.add(position, new GroupSeparatorViewModel(item));
        getViewState().onItemInserted(position);
    }

    void removeSeparator(int position) {
        requireEditMode();
        editor.enqueue(new RemoveSeparatorAction(position));
        apps.remove(position);
        getViewState().onItemsRemoved(position, 1);
    }

    void discardChanges() {
        requireEditMode();
        editor = null;
        getViewState().onChangesDiscarded();
        update();
    }

    void stopEditMode() {
        requireEditMode();
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
                .filter(appItems -> editor == null)
                .map(ListMapper.create(item -> item.id.equals(GroupSeparator.ID) ?
                        new GroupSeparatorViewModel(item) : new AppViewModel(item)))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new State<List<AppViewModel>>() {
                    @Override
                    protected void onNext(HomeView viewState, List<AppViewModel> list) {
                        Timber.d("Receive update: %s", list);
                        apps = list;
                        viewState.onAppsLoaded(apps, preferences.homeLayout());
                    }

                    @Override
                    protected void onError(HomeView viewState, Throwable e) {
                        if (apps != null) {
                            viewState.showError(e);
                        } else {
                            viewState.onAppsLoadError(e);
                        }
                    }
                });
    }

    private void setGroupExpanded(int position, boolean expanded) {
        GroupSeparatorViewModel group = (GroupSeparatorViewModel) apps.get(position);
        int startIndex = position + 1;
        int endIndex = position;
        for (int i = startIndex, size = apps.size(); i < size; i++) {
            if (apps.get(i) instanceof GroupSeparatorViewModel) {
                break;
            }
            endIndex = i;
        }
        if (startIndex == endIndex) {
            return;
        }
        group.expanded = expanded;
        for (int i = startIndex; i <= endIndex; i++) {
            apps.get(i).hidden = !group.expanded;
        }
        if (group.expanded) {
            getViewState().onItemsInserted(startIndex, endIndex - startIndex);
        } else {
            getViewState().onItemsRemoved(startIndex, endIndex - startIndex);
        }
    }

    private void expandGroups() {
        for (int i = 0, size = apps.size(); i < size; i++) {
            if (apps.get(i) instanceof GroupSeparatorViewModel) {
                setGroupExpanded(i, true);
            }
        }
    }

    private void requireEditMode() {
        if (editor == null) {
            throw new IllegalStateException();
        }
    }
}
