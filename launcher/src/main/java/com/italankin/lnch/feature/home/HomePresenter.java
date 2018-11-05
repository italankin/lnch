package com.italankin.lnch.feature.home;

import android.support.annotation.ColorInt;
import android.support.v7.util.DiffUtil;

import com.arellomobile.mvp.InjectViewState;
import com.italankin.lnch.feature.base.AppPresenter;
import com.italankin.lnch.feature.home.model.Update;
import com.italankin.lnch.feature.home.model.UserPrefs;
import com.italankin.lnch.model.descriptor.CustomColorDescriptor;
import com.italankin.lnch.model.descriptor.CustomLabelDescriptor;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.HiddenDescriptor;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.descriptor.impl.DeepShortcutDescriptor;
import com.italankin.lnch.model.descriptor.impl.GroupDescriptor;
import com.italankin.lnch.model.repository.apps.AppsRepository;
import com.italankin.lnch.model.repository.apps.actions.AddAction;
import com.italankin.lnch.model.repository.apps.actions.RecolorAction;
import com.italankin.lnch.model.repository.apps.actions.RemoveAction;
import com.italankin.lnch.model.repository.apps.actions.RenameAction;
import com.italankin.lnch.model.repository.apps.actions.RunnableAction;
import com.italankin.lnch.model.repository.apps.actions.SetVisibilityAction;
import com.italankin.lnch.model.repository.apps.actions.SwapAction;
import com.italankin.lnch.model.repository.apps.actions.UnpinShortcutAction;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.prefs.SeparatorState;
import com.italankin.lnch.model.repository.shortcuts.Shortcut;
import com.italankin.lnch.model.repository.shortcuts.ShortcutsRepository;
import com.italankin.lnch.model.viewmodel.CustomColorItem;
import com.italankin.lnch.model.viewmodel.CustomLabelItem;
import com.italankin.lnch.model.viewmodel.DescriptorItem;
import com.italankin.lnch.model.viewmodel.ExpandableItem;
import com.italankin.lnch.model.viewmodel.HiddenItem;
import com.italankin.lnch.model.viewmodel.VisibleItem;
import com.italankin.lnch.model.viewmodel.impl.AppViewModel;
import com.italankin.lnch.model.viewmodel.impl.DeepShortcutViewModel;
import com.italankin.lnch.model.viewmodel.impl.GroupViewModel;
import com.italankin.lnch.model.viewmodel.util.DescriptorItemDiffCallback;
import com.italankin.lnch.model.viewmodel.util.ViewModelFactory;
import com.italankin.lnch.util.ListUtils;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static android.support.v7.util.DiffUtil.calculateDiff;

@InjectViewState
public class HomePresenter extends AppPresenter<HomeView> {

    private final AppsRepository appsRepository;
    private final ShortcutsRepository shortcutsRepository;
    private final Preferences preferences;
    private final SeparatorState separatorState;
    /**
     * View commands will dispatch this instance on every state restore, so any changes
     * made to this list will be visible to new views.
     */
    private List<DescriptorItem> items;
    private AppsRepository.Editor editor;

    @Inject
    HomePresenter(AppsRepository appsRepository, ShortcutsRepository shortcutsRepository,
            Preferences preferences, SeparatorState separatorState) {
        this.appsRepository = appsRepository;
        this.shortcutsRepository = shortcutsRepository;
        this.preferences = preferences;
        this.separatorState = separatorState;
    }

    @Override
    protected void onFirstViewAttach() {
        reloadApps();
    }

    void loadApps() {
        getViewState().showProgress();
        update();
    }

    void reloadApps() {
        observeApps();
        loadApps();
    }

    void reloadAppsImmediate() {
        update();
    }

    void toggleExpandableItemState(int position, ExpandableItem item) {
        setItemExpanded(items, position, !item.isExpanded(), true);
    }

    void startCustomize() {
        if (editor != null) {
            throw new IllegalStateException("Editor is not null!");
        }
        editor = appsRepository.edit();
        expandAll();
        getViewState().onStartCustomize();
    }

    void swapApps(int from, int to) {
        requireEditor();
        editor.enqueue(new SwapAction(from, to));
        ListUtils.swap(items, from, to);
        getViewState().onItemsSwap(from, to);
    }

    void renameItem(int position, CustomLabelItem item, String customLabel) {
        requireEditor();
        String s = customLabel.trim().isEmpty() ? null : customLabel;
        editor.enqueue(new RenameAction((CustomLabelDescriptor) item.getDescriptor(), s));
        item.setCustomLabel(s);
        getViewState().onItemChanged(position);
    }

    void changeItemCustomColor(int position, CustomColorItem item, Integer color) {
        requireEditor();
        editor.enqueue(new RecolorAction((CustomColorDescriptor) item.getDescriptor(), color));
        item.setCustomColor(color);
        getViewState().onItemChanged(position);
    }

    void hideItem(int position, HiddenItem item) {
        requireEditor();
        editor.enqueue(new SetVisibilityAction((HiddenDescriptor) item.getDescriptor(), false));
        item.setHidden(true);
        getViewState().onItemChanged(position);
    }

    void addGroup(int position, String label, @ColorInt int color) {
        requireEditor();
        GroupDescriptor item = new GroupDescriptor(label, color);
        editor.enqueue(new AddAction(position, item));
        items.add(position, new GroupViewModel(item));
        getViewState().onItemInserted(position);
    }

    void removeItem(int position, DescriptorItem item) {
        requireEditor();
        Descriptor descriptor = item.getDescriptor();
        if (descriptor instanceof DeepShortcutDescriptor) {
            editor.enqueue(new UnpinShortcutAction(shortcutsRepository, (DeepShortcutDescriptor) descriptor));
        } else {
            editor.enqueue(new RemoveAction(position));
            editor.enqueue(new RunnableAction(() -> separatorState.remove(descriptor.getId())));
        }
        items.remove(position);
        getViewState().onItemsRemoved(position, 1);
    }

    void confirmDiscardChanges() {
        requireEditor();
        if (editor.isEmpty()) {
            discardChanges();
        } else {
            getViewState().onConfirmDiscardChanges();
        }
    }

    void discardChanges() {
        requireEditor();
        editor = null;
        getViewState().onChangesDiscarded();
        update();
    }

    void stopCustomize() {
        requireEditor();
        editor.commit()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableState() {
                    @Override
                    protected void onComplete(HomeView viewState) {
                        viewState.onStopCustomize();
                    }

                    @Override
                    protected void onError(HomeView viewState, Throwable e) {
                        viewState.showError(e);
                    }
                });
        editor = null;
    }

    void showAppPopup(int position, AppViewModel item) {
        List<Shortcut> shortcuts = shortcutsRepository.getShortcuts(item.getDescriptor());
        getViewState().showAppPopup(position, item, shortcuts);
    }

    void updateShortcuts(AppDescriptor descriptor) {
        shortcutsRepository.loadShortcuts(descriptor)
                .subscribeOn(Schedulers.io())
                .subscribe(new CompletableState() {
                    @Override
                    public void onComplete() {
                        Timber.d("Shortcuts updated for id=%s", descriptor.getId());
                    }
                });
    }

    void pinShortcut(Shortcut shortcut) {
        shortcutsRepository.pinShortcut(shortcut)
                .andThen(appsRepository.update())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableState() {
                    @Override
                    protected void onComplete(HomeView viewState) {
                        viewState.onShortcutPinned(shortcut);
                    }

                    @Override
                    protected void onError(HomeView viewState, Throwable e) {
                        viewState.showError(e);
                    }
                });
    }

    void startShortcut(DeepShortcutViewModel item) {
        Shortcut shortcut = shortcutsRepository.getShortcut(item.packageName, item.id);
        if (shortcut != null) {
            if (shortcut.isEnabled()) {
                getViewState().startShortcut(shortcut);
            } else {
                getViewState().onShortcutDisabled(shortcut.getDisabledMessage());
            }
        } else {
            getViewState().onShortcutNotFound();
        }
    }

    void removeItemImmediate(int position, DescriptorItem item) {
        Descriptor descriptor = item.getDescriptor();
        AppsRepository.Editor editor = appsRepository.edit();
        if (descriptor instanceof DeepShortcutDescriptor) {
            editor.enqueue(new UnpinShortcutAction(shortcutsRepository, (DeepShortcutDescriptor) descriptor));
        } else {
            editor.enqueue(new RemoveAction(position));
            if (descriptor instanceof GroupDescriptor) {
                editor.enqueue(new RunnableAction(() -> separatorState.remove(descriptor.getId())));
            }
        }
        editor.commit()
                .subscribeOn(Schedulers.io())
                .subscribe(new CompletableState() {
                    @Override
                    public void onComplete() {
                        Timber.d("Item removed: %s", descriptor);
                    }
                });
    }

    ///////////////////////////////////////////////////////////////////////////
    // Private
    ///////////////////////////////////////////////////////////////////////////

    private void requireEditor() {
        if (editor == null) {
            throw new IllegalStateException();
        }
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

    private void updateShortcuts() {
        shortcutsRepository.loadShortcuts()
                .subscribeOn(Schedulers.io())
                .subscribe(new CompletableState() {
                    @Override
                    public void onComplete() {
                        Timber.d("Shortcuts updated");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "updateShortcuts");
                    }
                });
    }

    private void observeApps() {
        appsRepository.observe()
                .filter(appItems -> editor == null)
                .map(ViewModelFactory::createItems)
                .doOnNext(this::restoreGroupsState)
                .scan(Update.EMPTY, this::calculateUpdates)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new State<Update>() {
                    @Override
                    protected void onNext(HomeView viewState, Update update) {
                        Timber.d("Receive update: %s", update.items);
                        items = update.items;
                        viewState.onAppsLoaded(update, getUserPrefs());
                        updateShortcuts();
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

    private UserPrefs getUserPrefs() {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.homeLayout = preferences.homeLayout();
        userPrefs.overlayColor = preferences.overlayColor();
        userPrefs.showScrollbar = preferences.showScrollbar();
        UserPrefs.ItemPrefs itemPrefs = new UserPrefs.ItemPrefs();
        itemPrefs.itemTextSize = preferences.itemTextSize();
        itemPrefs.itemPadding = preferences.itemPadding();
        itemPrefs.itemShadowRadius = preferences.itemShadowRadius();
        itemPrefs.itemFont = preferences.itemFont().typeface();
        itemPrefs.itemShadowColor = preferences.itemShadowColor();
        userPrefs.itemPrefs = itemPrefs;
        userPrefs.globalSearch = preferences.searchShowGlobal();
        return userPrefs;
    }

    private void restoreGroupsState(List<DescriptorItem> items) {
        for (int i = 0, size = items.size(); i < size; i++) {
            DescriptorItem item = items.get(i);
            if (item instanceof ExpandableItem) {
                ExpandableItem expandableItem = (ExpandableItem) item;
                String id = expandableItem.getDescriptor().getId();
                setItemExpanded(items, i, separatorState.isExpanded(id), false);
            }
        }
    }

    private Update calculateUpdates(Update previous, List<DescriptorItem> newItems) {
        DescriptorItemDiffCallback callback = new DescriptorItemDiffCallback(previous.items, newItems);
        DiffUtil.DiffResult diffResult = calculateDiff(callback, true);
        return new Update(newItems, diffResult);
    }

    private void setItemExpanded(List<DescriptorItem> items, int position, boolean expanded, boolean notify) {
        ExpandableItem item = (ExpandableItem) items.get(position);
        if (expanded == item.isExpanded()) {
            return;
        }
        int startIndex = position + 1;
        int endIndex = findNextExpandableItemIndex(items, startIndex);
        if (endIndex < 0) {
            endIndex = items.size();
        }
        int count = endIndex - startIndex;
        if (count <= 0) {
            return;
        }
        item.setExpanded(expanded);
        separatorState.setExanded(item.getDescriptor().getId(), expanded);
        for (int i = startIndex; i < endIndex; i++) {
            VisibleItem visibleItem = (VisibleItem) items.get(i);
            visibleItem.setVisible(expanded);
        }
        if (!notify) {
            return;
        }
        if (expanded) {
            getViewState().onItemsInserted(startIndex, count);
        } else {
            getViewState().onItemsRemoved(startIndex, count);
        }
    }

    private void expandAll() {
        for (int i = 0, size = items.size(); i < size; i++) {
            DescriptorItem item = items.get(i);
            if (item instanceof ExpandableItem) {
                setItemExpanded(items, i, true, true);
            }
        }
    }

    private static int findNextExpandableItemIndex(List<DescriptorItem> items, int startPosition) {
        for (int i = startPosition; i < items.size(); i++) {
            if (items.get(i) instanceof ExpandableItem) {
                return i;
            }
        }
        return -1;
    }
}
