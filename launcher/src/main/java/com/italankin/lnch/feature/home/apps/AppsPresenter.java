package com.italankin.lnch.feature.home.apps;

import android.content.Intent;
import android.service.notification.StatusBarNotification;

import com.arellomobile.mvp.InjectViewState;
import com.italankin.lnch.feature.base.AppPresenter;
import com.italankin.lnch.feature.home.model.Update;
import com.italankin.lnch.feature.home.model.UserPrefs;
import com.italankin.lnch.feature.home.repository.HomeDescriptorsState;
import com.italankin.lnch.feature.home.repository.HomeEntry;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.descriptor.impl.FolderDescriptor;
import com.italankin.lnch.model.descriptor.impl.IntentDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.descriptor.NameNormalizer;
import com.italankin.lnch.model.repository.descriptor.actions.AddAction;
import com.italankin.lnch.model.repository.descriptor.actions.BaseAction;
import com.italankin.lnch.model.repository.descriptor.actions.EditIntentAction;
import com.italankin.lnch.model.repository.descriptor.actions.MoveAction;
import com.italankin.lnch.model.repository.descriptor.actions.RemoveAction;
import com.italankin.lnch.model.repository.descriptor.actions.RemoveFromFolderAction;
import com.italankin.lnch.model.repository.descriptor.actions.RenameAction;
import com.italankin.lnch.model.repository.descriptor.actions.SetColorAction;
import com.italankin.lnch.model.repository.descriptor.actions.SetIgnoreAction;
import com.italankin.lnch.model.repository.notifications.NotificationBag;
import com.italankin.lnch.model.repository.notifications.NotificationsRepository;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.shortcuts.Shortcut;
import com.italankin.lnch.model.repository.shortcuts.ShortcutsRepository;
import com.italankin.lnch.model.ui.CustomColorDescriptorUi;
import com.italankin.lnch.model.ui.CustomLabelDescriptorUi;
import com.italankin.lnch.model.ui.DescriptorUi;
import com.italankin.lnch.model.ui.IgnorableDescriptorUi;
import com.italankin.lnch.model.ui.InFolderDescriptorUi;
import com.italankin.lnch.model.ui.RemovableDescriptorUi;
import com.italankin.lnch.model.ui.impl.AppDescriptorUi;
import com.italankin.lnch.model.ui.impl.FolderDescriptorUi;
import com.italankin.lnch.model.ui.impl.IntentDescriptorUi;
import com.italankin.lnch.model.ui.util.DescriptorUiDiffCallback;
import com.italankin.lnch.model.ui.util.DescriptorUiFactory;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static androidx.recyclerview.widget.DiffUtil.calculateDiff;

@InjectViewState
public class AppsPresenter extends AppPresenter<AppsView> {

    private final HomeDescriptorsState homeDescriptorsState;
    private final DescriptorRepository descriptorRepository;
    private final ShortcutsRepository shortcutsRepository;
    private final NotificationsRepository notificationsRepository;
    private final Preferences preferences;
    private final NameNormalizer nameNormalizer;

    private DescriptorRepository.Editor editor;

    @Inject
    AppsPresenter(HomeDescriptorsState homeDescriptorsState,
            DescriptorRepository descriptorRepository,
            ShortcutsRepository shortcutsRepository,
            NotificationsRepository notificationsRepository,
            Preferences preferences, NameNormalizer nameNormalizer) {
        this.homeDescriptorsState = homeDescriptorsState;
        this.descriptorRepository = descriptorRepository;
        this.shortcutsRepository = shortcutsRepository;
        this.notificationsRepository = notificationsRepository;
        this.preferences = preferences;
        this.nameNormalizer = nameNormalizer;
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

    void startCustomize() {
        if (editor != null) {
            throw new IllegalStateException("Editor is not null!");
        }
        editor = descriptorRepository.edit();
        getViewState().onStartCustomize();
    }

    void moveItem(int from, int to) {
        editor.enqueue(new MoveAction(from, to));
        homeDescriptorsState.moveItem(from, to);
    }

    void renameItem(String id) {
        HomeEntry<CustomLabelDescriptorUi> entry = homeDescriptorsState.find(CustomLabelDescriptorUi.class, id);
        if (entry == null) {
            return;
        }
        getViewState().showItemRenameDialog(entry.position, entry.item);
    }

    void renameItem(CustomLabelDescriptorUi item, String customLabel) {
        String newLabel = customLabel == null || customLabel.isEmpty() ? null : customLabel;
        editor.enqueue(new RenameAction(item.getDescriptor(), newLabel));
        item.setCustomLabel(newLabel);
        homeDescriptorsState.updateItem(item);
    }

    void showSetItemColorDialog(String id) {
        HomeEntry<CustomColorDescriptorUi> entry = homeDescriptorsState.find(CustomColorDescriptorUi.class, id);
        if (entry == null) {
            return;
        }
        getViewState().showSetItemColorDialog(entry.position, entry.item);
    }

    void changeItemCustomColor(CustomColorDescriptorUi item, Integer color) {
        editor.enqueue(new SetColorAction(item.getDescriptor(), color));
        item.setCustomColor(color);
        homeDescriptorsState.updateItem(item);
    }

    void ignoreItem(String id) {
        HomeEntry<IgnorableDescriptorUi> entry = homeDescriptorsState.find(IgnorableDescriptorUi.class, id);
        if (entry == null) {
            return;
        }
        editor.enqueue(new SetIgnoreAction(id, true));
        IgnorableDescriptorUi item = entry.item;
        item.setIgnored(true);
        homeDescriptorsState.updateItem(item);
    }

    void addFolder(String label, @ColorInt int color) {
        FolderDescriptor item = new FolderDescriptor(label, color);
        editor.enqueue(new AddAction(item));
        homeDescriptorsState.insertItem(new FolderDescriptorUi(item));
    }

    void addToFolder(String descriptorId, String folderId) {
        HomeEntry<FolderDescriptorUi> entry = homeDescriptorsState.find(FolderDescriptorUi.class, folderId);
        if (entry == null) {
            return;
        }
        FolderDescriptorUi folder = entry.item;
        if (folder.items.contains(descriptorId)) {
            getViewState().onFolderUpdated(folder, false);
            return;
        }
        editor.enqueue(new AddToFolderAction(folderId, descriptorId));
        folder.items.add(descriptorId);
        getViewState().onFolderUpdated(folder, true);
    }

    void showFolder(String folderId) {
        HomeEntry<FolderDescriptorUi> entry = homeDescriptorsState.find(FolderDescriptorUi.class, folderId);
        if (entry == null) {
            return;
        }
        getViewState().showFolder(entry.position, entry.item.getDescriptor());
    }

    void addIntent(Intent intent, String label) {
        IntentDescriptor item = new IntentDescriptor(intent, label);
        item.label = nameNormalizer.normalize(label);
        editor.enqueue(new AddAction(item));
        homeDescriptorsState.insertItem(new IntentDescriptorUi(item));
    }

    void startEditIntent(String id) {
        HomeEntry<IntentDescriptorUi> entry = homeDescriptorsState.find(IntentDescriptorUi.class, id);
        if (entry == null) {
            return;
        }
        getViewState().showIntentEditor(entry.item);
    }

    void editIntent(String id, Intent intent) {
        HomeEntry<IntentDescriptorUi> entry = homeDescriptorsState.find(IntentDescriptorUi.class, id);
        if (entry == null) {
            return;
        }
        editor.enqueue(new EditIntentAction(id, intent));
        entry.item.intent = intent;
    }

    void removeItem(String id) {
        editor.enqueue(new RemoveAction(id));
        homeDescriptorsState.removeById(id);
    }

    void confirmDiscardChanges() {
        if (editor.isEmpty()) {
            discardChanges();
        } else {
            getViewState().onConfirmDiscardChanges();
        }
    }

    void discardChanges() {
        editor.dispose();
        editor = null;
        getViewState().onChangesDiscarded();
        update();
    }

    void selectFolder(String descriptorId) {
        HomeEntry<InFolderDescriptorUi> entry = homeDescriptorsState.find(InFolderDescriptorUi.class, descriptorId);
        if (entry == null) {
            return;
        }
        List<FolderDescriptorUi> folders = homeDescriptorsState.allByType(FolderDescriptorUi.class);
        getViewState().showSelectFolderDialog(entry.position, entry.item, folders);
    }

    void stopCustomize() {
        if (editor.isEmpty()) {
            discardChanges();
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

    void pinShortcut(String packageName, String shortcutId) {
        Shortcut shortcut = shortcutsRepository.getShortcut(packageName, shortcutId);
        if (shortcut != null) {
            pinShortcut(shortcut);
        }
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

    void removeFromFolder(String descriptorId, String folderId) {
        Single
                .fromCallable(() -> {
                    return descriptorRepository.findById(FolderDescriptor.class, folderId);
                })
                .flatMapCompletable(folder -> {
                    return descriptorRepository.edit()
                            .enqueue(new RemoveFromFolderAction(folder.id, descriptorId))
                            .commit();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableState() {
                    @Override
                    public void onError(@NonNull Throwable e) {
                        Timber.e(e, "removeFromFolder: %s", e.getMessage());
                    }
                });
    }

    void pinIntent(Intent intent, CharSequence label, @ColorInt int color) {
        IntentDescriptor descriptor = new IntentDescriptor(intent, label.toString(), color);
        descriptor.label = nameNormalizer.normalize(label);
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

    void requestRemoveItem(String descriptorId) {
        HomeEntry<RemovableDescriptorUi> entry = homeDescriptorsState.find(RemovableDescriptorUi.class, descriptorId);
        if (entry == null) {
            return;
        }
        getViewState().showDeleteDialog(entry.item);
    }

    void removeItemImmediate(RemovableDescriptorUi item) {
        DescriptorRepository.Editor editor = descriptorRepository.edit();
        String descriptorId = item.getDescriptor().getId();
        editor.enqueue(new RemoveAction(descriptorId));
        editor.commit()
                .subscribeOn(Schedulers.io())
                .subscribe(new CompletableState() {
                    @Override
                    public void onComplete() {
                        Timber.d("Item removed: descriptorId=%s", descriptorId);
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
                    public void onError(@NonNull Throwable e) {
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
                        homeDescriptorsState.setItems(update.items);
                        viewState.onReceiveUpdate(update);
                        updateShortcuts();
                    }

                    @Override
                    protected void onError(AppsView viewState, Throwable e) {
                        if (homeDescriptorsState.isInitialState()) {
                            viewState.onReceiveUpdateError(e);
                        } else {
                            viewState.showError(e);
                        }
                    }
                });
    }

    private Observable<Update> observeApps() {
        Observable<List<DescriptorUi>> observeApps = descriptorRepository.observe()
                .map(DescriptorUiFactory::createItems);
        return Observable.combineLatest(observeApps, observeNotifications(), this::concatNotifications)
                .scan(Update.EMPTY, this::calculateUpdates)
                .skip(1); // skip empty update
    }

    private Observable<Map<AppDescriptor, NotificationBag>> observeNotifications() {
        return Observable.combineLatest(
                notificationsRepository.observe(),
                preferences.observe(Preferences.NOTIFICATION_DOT),
                preferences.observe(Preferences.NOTIFICATION_DOT_ONGOING),
                (notifications, showNotificationDots, showOngoing) -> {
                    if (showNotificationDots) {
                        return notifications;
                    } else {
                        return Collections.emptyMap();
                    }
                });
    }

    @NotNull
    private List<DescriptorUi> concatNotifications(List<DescriptorUi> items,
            Map<AppDescriptor, NotificationBag> notifications) {
        if (notifications.isEmpty()) {
            return items;
        }
        List<DescriptorUi> result = new ArrayList<>(items.size());
        for (DescriptorUi item : items) {
            if (!(item instanceof AppDescriptorUi)) {
                result.add(item);
                continue;
            }
            AppDescriptorUi app = (AppDescriptorUi) item;
            NotificationBag notificationBag = notifications.get(app.getDescriptor());
            boolean badgeVisible = false;
            boolean showOngoing = preferences.get(Preferences.NOTIFICATION_DOT_ONGOING);
            if (notificationBag != null) {
                int count = notificationBag.getCount();
                if (!showOngoing) {
                    for (StatusBarNotification sbn : notificationBag.getNotifications()) {
                        if (sbn.isOngoing()) {
                            count--;
                        }
                    }
                }
                badgeVisible = count > 0;
            }
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

    private Update calculateUpdates(Update previous, List<DescriptorUi> newItems) {
        DescriptorUiDiffCallback callback = new DescriptorUiDiffCallback(previous.items, newItems);
        DiffUtil.DiffResult diffResult = calculateDiff(callback, true);
        return new Update(newItems, diffResult);
    }

    private static class AddToFolderAction extends BaseAction {

        private final String folderId;
        private final String descriptorId;

        private AddToFolderAction(String folderId, String descriptorId) {
            this.folderId = folderId;
            this.descriptorId = descriptorId;
        }

        @Override
        public void apply(List<Descriptor> items) {
            FolderDescriptor descriptor = findById(items, folderId);
            if (descriptor != null) {
                descriptor.items.add(descriptorId);
            }
        }
    }
}
