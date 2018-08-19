package com.italankin.lnch.feature.home;

import android.support.annotation.ColorInt;

import com.arellomobile.mvp.InjectViewState;
import com.italankin.lnch.feature.base.AppPresenter;
import com.italankin.lnch.feature.home.model.AppViewModel;
import com.italankin.lnch.feature.home.model.GroupViewModel;
import com.italankin.lnch.feature.home.model.ItemViewModel;
import com.italankin.lnch.model.repository.apps.AppsRepository;
import com.italankin.lnch.model.repository.apps.actions.AddGroupAction;
import com.italankin.lnch.model.repository.apps.actions.RemoveAction;
import com.italankin.lnch.model.repository.apps.actions.RenameAction;
import com.italankin.lnch.model.repository.apps.actions.SetCustomColorAction;
import com.italankin.lnch.model.repository.apps.actions.SetVisibilityAction;
import com.italankin.lnch.model.repository.apps.actions.SwapAction;
import com.italankin.lnch.model.repository.descriptors.Descriptor;
import com.italankin.lnch.model.repository.descriptors.model.AppDescriptor;
import com.italankin.lnch.model.repository.descriptors.model.GroupDescriptor;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.util.ListUtils;

import java.util.ArrayList;
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
    private List<ItemViewModel> items;
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
        GroupViewModel group = (GroupViewModel) items.get(position);
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
        ListUtils.swap(items, from, to);
        getViewState().onItemsSwap(from, to);
    }

    void renameItem(int position, ItemViewModel item, String customLabel) {
        requireEditMode();
        String s = customLabel.isEmpty() ? null : customLabel;
        editor.enqueue(new RenameAction(item.getDescriptor(), s));
        item.setCustomLabel(s);
        getViewState().onItemChanged(position);
    }

    void changeItemCustomColor(int position, ItemViewModel item, String value) {
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
        editor.enqueue(new SetCustomColorAction(item.getDescriptor(), customColor));
        item.setCustomColor(customColor);
        getViewState().onItemChanged(position);
    }

    void hideApp(int position, AppViewModel item) {
        requireEditMode();
        editor.enqueue(new SetVisibilityAction(item.item, false));
        item.hidden = true;
        getViewState().onItemChanged(position);
    }

    void addGroup(int position, String label, @ColorInt int color) {
        requireEditMode();
        GroupDescriptor item = new GroupDescriptor(label, color);
        editor.enqueue(new AddGroupAction(position, item));
        items.add(position, new GroupViewModel(item));
        getViewState().onItemInserted(position);
    }

    void removeGroup(int position) {
        requireEditMode();
        editor.enqueue(new RemoveAction(position));
        items.remove(position);
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
        appsRepository.observe()
                .filter(appItems -> editor == null)
                .map(this::mapItems)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new State<List<ItemViewModel>>() {
                    @Override
                    protected void onNext(HomeView viewState, List<ItemViewModel> list) {
                        Timber.d("Receive update: %s", list);
                        items = list;
                        viewState.onAppsLoaded(items, preferences.homeLayout());
                    }

                    @Override
                    protected void onError(HomeView viewState, Throwable e) {
                        if (items != null) {
                            viewState.showError(e);
                        } else {
                            viewState.onAppsLoadError(e);
                        }
                    }
                });
    }

    private List<ItemViewModel> mapItems(List<Descriptor> descriptors) {
        List<ItemViewModel> result = new ArrayList<>(descriptors.size());
        for (Descriptor descriptor : descriptors) {
            if (descriptor instanceof AppDescriptor) {
                result.add(new AppViewModel((AppDescriptor) descriptor));
            } else if (descriptor instanceof GroupDescriptor) {
                result.add(new GroupViewModel((GroupDescriptor) descriptor));
            }
        }
        return result;
    }

    private void setGroupExpanded(int position, boolean expanded) {
        GroupViewModel group = (GroupViewModel) items.get(position);
        int startIndex = position + 1;
        int endIndex = position;
        for (int i = startIndex, size = items.size(); i < size; i++) {
            if (items.get(i) instanceof GroupViewModel) {
                break;
            }
            endIndex = i;
        }
        if (startIndex == endIndex) {
            return;
        }
        group.expanded = expanded;
        for (int i = startIndex; i <= endIndex; i++) {
            ((AppViewModel) items.get(i)).visible = group.expanded;
        }
        if (group.expanded) {
            getViewState().onItemsInserted(startIndex, endIndex - startIndex);
        } else {
            getViewState().onItemsRemoved(startIndex, endIndex - startIndex);
        }
    }

    private void expandGroups() {
        for (int i = 0, size = items.size(); i < size; i++) {
            if (items.get(i) instanceof GroupViewModel) {
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
