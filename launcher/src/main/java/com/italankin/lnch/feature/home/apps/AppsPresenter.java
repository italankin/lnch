package com.italankin.lnch.feature.home.apps;

import com.arellomobile.mvp.InjectViewState;
import com.italankin.lnch.feature.base.AppPresenter;
import com.italankin.lnch.feature.home.model.Update;
import com.italankin.lnch.feature.home.model.UserPrefs;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.descriptor.impl.GroupDescriptor;
import com.italankin.lnch.model.descriptor.impl.IntentDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.descriptor.actions.AddAction;
import com.italankin.lnch.model.repository.descriptor.actions.RecolorAction;
import com.italankin.lnch.model.repository.descriptor.actions.RemoveAction;
import com.italankin.lnch.model.repository.descriptor.actions.RenameAction;
import com.italankin.lnch.model.repository.descriptor.actions.RunnableAction;
import com.italankin.lnch.model.repository.descriptor.actions.SetIgnoreAction;
import com.italankin.lnch.model.repository.descriptor.actions.SwapAction;
import com.italankin.lnch.model.repository.notifications.NotificationDot;
import com.italankin.lnch.model.repository.notifications.NotificationsRepository;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.prefs.Preferences.ShortcutsSortMode;
import com.italankin.lnch.model.repository.prefs.SeparatorState;
import com.italankin.lnch.model.repository.shortcuts.Shortcut;
import com.italankin.lnch.model.repository.shortcuts.ShortcutsRepository;
import com.italankin.lnch.model.ui.CustomColorDescriptorUi;
import com.italankin.lnch.model.ui.CustomLabelDescriptorUi;
import com.italankin.lnch.model.ui.DescriptorUi;
import com.italankin.lnch.model.ui.ExpandableDescriptorUi;
import com.italankin.lnch.model.ui.IgnorableDescriptorUi;
import com.italankin.lnch.model.ui.VisibleDescriptorUi;
import com.italankin.lnch.model.ui.impl.AppDescriptorUi;
import com.italankin.lnch.model.ui.impl.DeepShortcutDescriptorUi;
import com.italankin.lnch.model.ui.impl.GroupDescriptorUi;
import com.italankin.lnch.model.ui.util.DescriptorUiDiffCallback;
import com.italankin.lnch.model.ui.util.DescriptorUiFactory;
import com.italankin.lnch.util.ListUtils;
import com.italankin.lnch.util.NumberUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import androidx.annotation.ColorInt;
import androidx.recyclerview.widget.DiffUtil;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static androidx.recyclerview.widget.DiffUtil.calculateDiff;

@InjectViewState
public class AppsPresenter extends AppPresenter<AppsView> {

    private static final List<DescriptorUi> INITIAL = new ArrayList<>();

    private final DescriptorRepository descriptorRepository;
    private final ShortcutsRepository shortcutsRepository;
    private final NotificationsRepository notificationsRepository;
    private final Preferences preferences;
    private final SeparatorState separatorState;
    /**
     * View commands will dispatch this instance on every state restore, so any changes
     * made to this list will be visible to new views.
     */
    private List<DescriptorUi> items = INITIAL;
    private DescriptorRepository.Editor editor;

    @Inject
    AppsPresenter(DescriptorRepository descriptorRepository, ShortcutsRepository shortcutsRepository,
            NotificationsRepository notificationsRepository, Preferences preferences, SeparatorState separatorState) {
        this.descriptorRepository = descriptorRepository;
        this.shortcutsRepository = shortcutsRepository;
        this.notificationsRepository = notificationsRepository;
        this.preferences = preferences;
        this.separatorState = separatorState;
    }

    @Override
    protected void onFirstViewAttach() {
        reloadApps();
    }

    void reloadApps() {
        observe();
        getViewState().showProgress();
        update();
    }

    void toggleExpandableItemState(int position, ExpandableDescriptorUi item) {
        setItemExpanded(items, position, !item.isExpanded(), true);
    }

    void startCustomize() {
        if (editor != null) {
            throw new IllegalStateException("Editor is not null!");
        }
        editor = descriptorRepository.edit();
        for (int i = 0, size = items.size(); i < size; i++) {
            DescriptorUi item = items.get(i);
            if (item instanceof ExpandableDescriptorUi) {
                setItemExpanded(items, i, true, true);
            }
        }
        getViewState().onStartCustomize();
    }

    void swapApps(int from, int to) {
        editor.enqueue(new SwapAction(from, to));
        ListUtils.swap(items, from, to);
        getViewState().onItemsSwap(from, to);
    }

    void renameItem(int position, CustomLabelDescriptorUi item, String customLabel) {
        String s = customLabel.trim().isEmpty() ? null : customLabel;
        editor.enqueue(new RenameAction(item.getDescriptor().getId(), s));
        item.setCustomLabel(s);
        getViewState().onItemChanged(position);
    }

    void changeItemCustomColor(int position, CustomColorDescriptorUi item, Integer color) {
        editor.enqueue(new RecolorAction(item.getDescriptor().getId(), color));
        item.setCustomColor(color);
        getViewState().onItemChanged(position);
    }

    void ignoreItem(int position, IgnorableDescriptorUi item) {
        editor.enqueue(new SetIgnoreAction(item.getDescriptor().getId(), false));
        item.setIgnored(true);
        getViewState().onItemChanged(position);
    }

    void addGroup(int position, String label, @ColorInt int color) {
        GroupDescriptor item = new GroupDescriptor(label, color);
        editor.enqueue(new AddAction(position, item));
        items.add(position, new GroupDescriptorUi(item));
        getViewState().onItemInserted(position);
    }

    void removeItem(int position, DescriptorUi item) {
        Descriptor descriptor = item.getDescriptor();
        editor.enqueue(new RemoveAction(position));
        if (item instanceof ExpandableDescriptorUi) {
            editor.enqueue(new RunnableAction(() -> separatorState.remove(descriptor.getId())));
        }
        items.remove(position);
        getViewState().onItemsRemoved(position, 1);
    }

    void confirmDiscardChanges() {
        if (editor.isEmpty()) {
            discardChanges();
        } else {
            getViewState().onConfirmDiscardChanges();
        }
    }

    void discardChanges() {
        editor = null;
        getViewState().onChangesDiscarded();
        update();
    }

    void stopCustomize() {
        if (editor.isEmpty()) {
            update();
            return;
        }
        editor.commit()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableState() {
                    @Override
                    protected void onComplete(AppsView viewState) {
                        viewState.onStopCustomize();
                    }

                    @Override
                    protected void onError(AppsView viewState, Throwable e) {
                        viewState.showError(e);
                    }
                });
        editor = null;
    }

    void showAppPopup(int position, AppDescriptorUi item) {
        List<Shortcut> shortcuts = shortcutsRepository.getShortcuts(item.getDescriptor());
        getViewState().showAppPopup(position, item, processShortcuts(shortcuts));
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
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleState<Boolean>() {
                    @Override
                    protected void onSuccess(AppsView viewState, Boolean pinned) {
                        if (pinned) {
                            viewState.onShortcutPinned(shortcut);
                        } else {
                            viewState.onShortcutAlreadyPinnedError(shortcut);
                        }
                    }

                    @Override
                    protected void onError(AppsView viewState, Throwable e) {
                        viewState.showError(e);
                    }
                });
    }

    void pinIntent(IntentDescriptor descriptor) {
        descriptorRepository.edit()
                .enqueue(new AddAction(descriptor))
                .commit()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableState() {
                    @Override
                    protected void onError(AppsView viewState, Throwable e) {
                        viewState.showError(e);
                    }
                });
    }

    void startShortcut(int position, DeepShortcutDescriptorUi item) {
        Shortcut shortcut = shortcutsRepository.getShortcut(item.packageName, item.id);
        if (shortcut != null) {
            if (shortcut.isEnabled()) {
                getViewState().startShortcut(position, shortcut);
            } else {
                getViewState().onShortcutDisabled(shortcut.getDisabledMessage());
            }
        } else {
            getViewState().onShortcutNotFound();
        }
    }

    void removeItemImmediate(int position, DescriptorUi item) {
        Descriptor descriptor = item.getDescriptor();
        DescriptorRepository.Editor editor = descriptorRepository.edit();
        editor.enqueue(new RemoveAction(position));
        if (descriptor instanceof GroupDescriptor) {
            editor.enqueue(new RunnableAction(() -> separatorState.remove(descriptor.getId())));
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

    private void update() {
        descriptorRepository.update()
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

    private void observe() {
        Observable.combineLatest(observeApps(), observeUserPrefs(), Update::with)
                .filter(appItems -> editor == null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new State<Update>() {
                    @Override
                    protected void onNext(AppsView viewState, Update update) {
                        Timber.d("Update: %s", update);
                        items = update.items;
                        viewState.onReceiveUpdate(update);
                        updateShortcuts();
                    }

                    @Override
                    protected void onError(AppsView viewState, Throwable e) {
                        if (items == INITIAL) {
                            viewState.onReceiveUpdateError(e);
                        } else {
                            viewState.showError(e);
                        }
                    }
                });
    }

    private Observable<Update> observeApps() {
        Observable<List<DescriptorUi>> observeApps = descriptorRepository.observe()
                .map(DescriptorUiFactory::createItems)
                .doOnNext(this::restoreGroupsState);
        return Observable.combineLatest(observeApps, observeNotifications(), this::concatNotifications)
                .scan(Update.EMPTY, this::calculateUpdates)
                .skip(1); // skip empty update
    }

    private Observable<Map<AppDescriptor, NotificationDot>> observeNotifications() {
        return notificationsRepository.observe()
                .doOnNext(map -> {
                    Timber.d("notifications=%s", map);
                });
    }

    @NotNull
    private List<DescriptorUi> concatNotifications(List<DescriptorUi> items,
            Map<AppDescriptor, NotificationDot> notifications) {
        if (!preferences.get(Preferences.NOTIFICATION_DOT)) {
            return items;
        }
        List<DescriptorUi> result = new ArrayList<>(items.size());
        for (DescriptorUi item : items) {
            if (!(item instanceof AppDescriptorUi)) {
                result.add(item);
                continue;
            }
            AppDescriptorUi app = (AppDescriptorUi) item;
            NotificationDot notificationDot = notifications.get(app.getDescriptor());
            boolean badgeVisible = notificationDot != null && notificationDot.getCount() > 0;
            if (badgeVisible != app.isBadgeVisible()) {
                // create a copy of AppDescriptorUi to update state correctly
                AppDescriptorUi newApp = new AppDescriptorUi(app);
                newApp.setBadgeVisible(badgeVisible);
                result.add(newApp);
            } else {
                result.add(app);
            }
        }
        return result;
    }

    private Observable<UserPrefs> observeUserPrefs() {
        return preferences.observe()
                .filter(UserPrefs.PREFERENCES::contains)
                .map(s -> new UserPrefs(preferences))
                .startWith(new UserPrefs(preferences))
                .distinctUntilChanged();
    }

    private void restoreGroupsState(List<DescriptorUi> items) {
        for (int i = 0, size = items.size(); i < size; i++) {
            DescriptorUi item = items.get(i);
            if (item instanceof ExpandableDescriptorUi) {
                ExpandableDescriptorUi expandable = (ExpandableDescriptorUi) item;
                String id = expandable.getDescriptor().getId();
                setItemExpanded(items, i, separatorState.isExpanded(id), false);
            }
        }
    }

    private Update calculateUpdates(Update previous, List<DescriptorUi> newItems) {
        DescriptorUiDiffCallback callback = new DescriptorUiDiffCallback(previous.items, newItems);
        DiffUtil.DiffResult diffResult = calculateDiff(callback, true);
        return new Update(newItems, diffResult);
    }

    private List<Shortcut> processShortcuts(List<Shortcut> shortcuts) {
        if (preferences.get(Preferences.SHORTCUTS_SORT_MODE) == ShortcutsSortMode.REVERSED) {
            shortcuts = ListUtils.reversedCopy(shortcuts);
        }
        int max = NumberUtils.parseInt(preferences.get(Preferences.MAX_DYNAMIC_SHORTCUTS), -1);
        if (max < 0 || shortcuts.size() <= max) {
            return shortcuts;
        }
        List<Shortcut> result = new ArrayList<>(shortcuts.size());
        for (Shortcut shortcut : shortcuts) {
            if (!shortcut.isDynamic() || max-- > 0) {
                result.add(shortcut);
            }
        }
        return result;
    }

    private void setItemExpanded(List<DescriptorUi> items, int position, boolean expanded, boolean notify) {
        ExpandableDescriptorUi item = (ExpandableDescriptorUi) items.get(position);
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
        separatorState.setExpanded(item.getDescriptor().getId(), expanded);
        for (int i = startIndex; i < endIndex; i++) {
            VisibleDescriptorUi visibleItem = (VisibleDescriptorUi) items.get(i);
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

    private static int findNextExpandableItemIndex(List<DescriptorUi> items, int startPosition) {
        for (int i = startPosition; i < items.size(); i++) {
            if (items.get(i) instanceof ExpandableDescriptorUi) {
                return i;
            }
        }
        return -1;
    }
}
