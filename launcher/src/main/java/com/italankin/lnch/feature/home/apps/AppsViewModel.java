package com.italankin.lnch.feature.home.apps;

import android.content.Intent;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import com.italankin.lnch.feature.base.AppViewModel;
import com.italankin.lnch.feature.home.adapter.error.ErrorDescriptorUi;
import com.italankin.lnch.feature.home.apps.events.ShortcutPinEvent;
import com.italankin.lnch.feature.home.model.Update;
import com.italankin.lnch.feature.home.model.UserPrefs;
import com.italankin.lnch.feature.home.repository.EditModeState;
import com.italankin.lnch.feature.home.repository.HomeDescriptorsState;
import com.italankin.lnch.feature.home.repository.HomeEntry;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.descriptor.impl.FolderDescriptor;
import com.italankin.lnch.model.descriptor.impl.IntentDescriptor;
import com.italankin.lnch.model.descriptor.mutable.IgnorableMutableDescriptor;
import com.italankin.lnch.model.descriptor.mutable.MutableDescriptor;
import com.italankin.lnch.model.fonts.FontManager;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.descriptor.NameNormalizer;
import com.italankin.lnch.model.repository.descriptor.actions.*;
import com.italankin.lnch.model.repository.notifications.NotificationBag;
import com.italankin.lnch.model.repository.notifications.NotificationsRepository;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.shortcuts.Shortcut;
import com.italankin.lnch.model.repository.shortcuts.ShortcutsRepository;
import com.italankin.lnch.model.ui.*;
import com.italankin.lnch.model.ui.impl.AppDescriptorUi;
import com.italankin.lnch.model.ui.impl.FolderDescriptorUi;
import com.italankin.lnch.model.ui.impl.IntentDescriptorUi;
import com.italankin.lnch.model.ui.util.DescriptorUiDiffCallback;
import com.italankin.lnch.model.ui.util.DescriptorUiFactory;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import timber.log.Timber;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static androidx.recyclerview.widget.DiffUtil.calculateDiff;

public class AppsViewModel extends AppViewModel {

    private final HomeDescriptorsState homeDescriptorsState;
    private final DescriptorRepository descriptorRepository;
    private final ShortcutsRepository shortcutsRepository;
    private final NotificationsRepository notificationsRepository;
    private final EditModeState editModeState;
    private final Preferences preferences;
    private final NameNormalizer nameNormalizer;
    private final FontManager fontManager;

    private final BehaviorSubject<Update> updatesSubject = BehaviorSubject.create();
    private final PublishSubject<Throwable> errorsSubject = PublishSubject.create();
    private final PublishSubject<ShortcutPinEvent> shortcutPinSubject = PublishSubject.create();

    @Inject
    AppsViewModel(HomeDescriptorsState homeDescriptorsState,
            DescriptorRepository descriptorRepository,
            ShortcutsRepository shortcutsRepository,
            NotificationsRepository notificationsRepository,
            EditModeState editModeState,
            Preferences preferences,
            NameNormalizer nameNormalizer,
            FontManager fontManager) {
        this.homeDescriptorsState = homeDescriptorsState;
        this.descriptorRepository = descriptorRepository;
        this.shortcutsRepository = shortcutsRepository;
        this.notificationsRepository = notificationsRepository;
        this.editModeState = editModeState;
        this.preferences = preferences;
        this.nameNormalizer = nameNormalizer;
        this.fontManager = fontManager;

        reloadApps();
    }

    Observable<Update> updateEvents() {
        return updatesSubject.observeOn(AndroidSchedulers.mainThread());
    }

    Observable<Throwable> errorEvents() {
        return errorsSubject.observeOn(AndroidSchedulers.mainThread());
    }

    Observable<ShortcutPinEvent> shortcutPinEvents() {
        return shortcutPinSubject.observeOn(AndroidSchedulers.mainThread());
    }

    void reloadApps() {
        observe();
        update();
    }

    void moveItem(int from, int to) {
        editModeState.addAction(new MoveAction(from, to));
        homeDescriptorsState.moveItem(from, to);
    }

    void renameItem(CustomLabelDescriptorUi item, String customLabel) {
        String newLabel = customLabel == null || customLabel.isEmpty() ? null : customLabel;
        editModeState.addAction(new RenameAction(item.getDescriptor(), newLabel));
        item.setCustomLabel(newLabel);
        homeDescriptorsState.updateItem(item);
    }

    void changeItemCustomColor(CustomColorDescriptorUi item, Integer color) {
        editModeState.addAction(new SetColorAction(item.getDescriptor(), color));
        item.setCustomColor(color);
        homeDescriptorsState.updateItem(item);
    }

    void ignoreItem(String id) {
        HomeEntry<IgnorableDescriptorUi> entry = homeDescriptorsState.find(IgnorableDescriptorUi.class, id);
        if (entry == null) {
            return;
        }
        editModeState.addAction(new SetIgnoreAction(id, true));
        IgnorableDescriptorUi item = entry.item;
        item.setIgnored(true);
        homeDescriptorsState.updateItem(item);
    }

    void showItem(String id) {
        HomeEntry<IgnorableDescriptorUi> entry = homeDescriptorsState.find(IgnorableDescriptorUi.class, id);
        if (entry == null) {
            return;
        }
        editModeState.addAction(new SetIgnoreAction(id, false));
        IgnorableDescriptorUi item = entry.item;
        item.setIgnored(false);
        homeDescriptorsState.updateItem(item);
        moveItem(entry.position, homeDescriptorsState.items().size() - 1);
    }

    void addFolder(String label, @ColorInt int color, List<String> descriptorIds, boolean move) {
        FolderDescriptor.Mutable mutable = new FolderDescriptor.Mutable(label);
        mutable.setLabel(nameNormalizer.normalize(label));
        mutable.setColor(color);
        editModeState.addAction(new AddAction(mutable));
        FolderDescriptor folder = mutable.toDescriptor();
        FolderDescriptorUi folderUi = new FolderDescriptorUi(folder);
        homeDescriptorsState.insertItem(folderUi);
        addToFolder(folderUi, descriptorIds, move);
    }

    void addToFolder(String folderId, List<String> descriptorIds, boolean move) {
        HomeEntry<FolderDescriptorUi> entry = homeDescriptorsState.find(FolderDescriptorUi.class, folderId);
        if (entry != null) {
            addToFolder(entry.item, descriptorIds, move);
        }
    }

    void addIntent(Intent intent, String label) {
        IntentDescriptor.Mutable item = new IntentDescriptor.Mutable(intent, label);
        item.setLabel(nameNormalizer.normalize(label));
        editModeState.addAction(new AddAction(item));
        homeDescriptorsState.insertItem(new IntentDescriptorUi(item.toDescriptor()));
    }

    void editIntent(String id, Intent intent) {
        HomeEntry<IntentDescriptorUi> entry = homeDescriptorsState.find(IntentDescriptorUi.class, id);
        if (entry == null) {
            return;
        }
        editModeState.addAction(new EditIntentAction(id, intent));
        entry.item.intent = intent;
    }

    void removeItem(String id) {
        editModeState.addAction(new RemoveAction(id));
        homeDescriptorsState.removeById(id);
    }

    void editModeActivate() {
        if (descriptorRepository.items().isEmpty()) {
            descriptorRepository.update()
                    .delay(1, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new CompletableState() {
                        @Override
                        public void onComplete() {
                            editModeActivate();
                        }
                    });
            return;
        }
        editModeState.activate();
    }

    void editModeDiscard() {
        if (!editModeState.isActive()) {
            return;
        }
        editModeState.discard();
        update();
    }

    void editModeCommit() {
        if (!editModeState.isActive()) {
            return;
        }
        if (editModeState.hasSomethingToCommit()) {
            editModeState.commit();
        } else {
            editModeDiscard();
        }
    }

    void pinShortcut(String packageName, String shortcutId) {
        Shortcut shortcut = shortcutsRepository.getShortcut(packageName, shortcutId);
        if (shortcut != null) {
            pinShortcut(shortcut);
        }
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
        IntentDescriptor.Mutable descriptor = new IntentDescriptor.Mutable(intent, label.toString());
        descriptor.setColor(color);
        descriptor.setLabel(nameNormalizer.normalize(label));
        descriptorRepository.edit()
                .enqueue(new AddAction(descriptor))
                .commit()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableState() {
                    @Override
                    public void onError(Throwable e) {
                        errorsSubject.onNext(e);
                    }
                });
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
                        updateShortcuts();
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
                .filter(appItems -> {
                    return !editModeState.isActive() || !updatesSubject.hasValue();
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new State<>() {
                    @Override
                    public void onNext(Update update) {
                        Timber.d("Update: %s", update);
                        if (!update.isTransient()) {
                            homeDescriptorsState.setItems(update.items);
                        }
                        updatesSubject.onNext(update);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.d(e, "Update error:");
                        if (homeDescriptorsState.isInitialState()) {
                            Update update = new Update(Collections.singletonList(new ErrorDescriptorUi(e)), true)
                                    .with(new UserPrefs(preferences, fontManager));
                            updatesSubject.onNext(update);
                        } else {
                            errorsSubject.onNext(e);
                        }
                    }
                });
    }

    private Observable<Update> observeApps() {
        Observable<List<DescriptorUi>> observeApps = descriptorRepository.observe()
                .map(DescriptorUiFactory::createItems);
        return Observable.combineLatest(observeApps, observeNotifications(), this::concatNotifications)
                .scan(Update.INITIAL, this::calculateUpdates);
    }

    private Observable<NotificationBagContainer> observeNotifications() {
        return Observable.combineLatest(
                notificationsRepository.observe(),
                preferences.observe(Preferences.NOTIFICATION_DOT, true),
                preferences.observe(Preferences.NOTIFICATION_DOT_FOLDERS, true),
                preferences.observe(Preferences.NOTIFICATION_DOT_ONGOING, true),
                (notifications, showNotificationDots, showFolderNotificationDots, showOngoing) -> {
                    if (showNotificationDots && !notifications.isEmpty()) {
                        return new NotificationBagContainer(notifications);
                    } else {
                        return NotificationBagContainer.EMPTY;
                    }
                });
    }

    @NonNull
    private List<DescriptorUi> concatNotifications(
            List<DescriptorUi> items, NotificationBagContainer notificationBagContainer) {
        if (notificationBagContainer.isEmpty()) {
            return items;
        }
        List<DescriptorUi> result = new ArrayList<>(items.size());
        boolean showOngoing = preferences.get(Preferences.NOTIFICATION_DOT_ONGOING);
        for (DescriptorUi item : items) {
            if (item instanceof AppDescriptorUi) {
                AppDescriptorUi app = (AppDescriptorUi) item;
                NotificationBag bag = notificationBagContainer.get(app.getDescriptor());
                result.add(concatAppNotifications(app, bag, showOngoing));
            } else if (item instanceof FolderDescriptorUi) {
                FolderDescriptorUi folder = (FolderDescriptorUi) item;
                result.add(concatFolderNotifications(notificationBagContainer, folder));
            } else {
                result.add(item);
            }
        }
        return result;
    }

    private AppDescriptorUi concatAppNotifications(
            AppDescriptorUi item, @Nullable NotificationBag bag, boolean showOngoing) {
        boolean badgeVisible = isBadgeVisible(bag, showOngoing);
        if (badgeVisible != item.isBadgeVisible()) {
            // create a copy of AppDescriptorUi to update state correctly
            AppDescriptorUi newApp = new AppDescriptorUi(item);
            newApp.setBadgeVisible(badgeVisible);
            return newApp;
        } else {
            return item;
        }
    }

    private FolderDescriptorUi concatFolderNotifications(
            NotificationBagContainer notificationBagContainer, FolderDescriptorUi item) {
        boolean showOnFolders = preferences.get(Preferences.NOTIFICATION_DOT_FOLDERS);
        if (!showOnFolders) {
            if (item.isBadgeVisible()) {
                FolderDescriptorUi newItem = new FolderDescriptorUi(item);
                newItem.setBadgeVisible(false);
                return newItem;
            } else {
                return item;
            }
        }
        boolean badgeVisible = false;
        AppDescriptor descriptor = null;
        boolean showOngoing = preferences.get(Preferences.NOTIFICATION_DOT_ONGOING);
        for (String descriptorId : item.items) {
            NotificationBag bag = notificationBagContainer.get(descriptorId);
            badgeVisible = isBadgeVisible(bag, showOngoing);
            if (badgeVisible) {
                descriptor = bag.getDescriptor();
                break;
            }
        }
        Integer badgeColor = badgeVisible && descriptor != null ? descriptor.customBadgeColor : null;
        if (badgeVisible != item.isBadgeVisible() ||
                badgeVisible && !Objects.equals(badgeColor, item.customBadgeColor)) {
            // create a copy of FolderDescriptorUi to update state correctly
            FolderDescriptorUi newItem = new FolderDescriptorUi(item);
            newItem.setBadgeVisible(badgeVisible);
            newItem.customBadgeColor = badgeColor;
            return newItem;
        } else {
            return item;
        }
    }

    private void addToFolder(FolderDescriptorUi folder, List<String> descriptorIds, boolean move) {
        editModeState.addAction(new AddToFolderAction(folder.getDescriptor().getId(), descriptorIds, move));
        if (move) {
            for (String descriptorId : descriptorIds) {
                // ignore all descriptors, even if they already in folder
                HomeEntry<IgnorableDescriptorUi> entry = homeDescriptorsState.find(IgnorableDescriptorUi.class, descriptorId);
                if (entry != null) {
                    entry.item.setIgnored(true);
                    homeDescriptorsState.updateItem(entry.item);
                }
            }
        }
        for (String descriptorId : descriptorIds) {
            if (!folder.items.contains(descriptorId)) {
                folder.items.addAll(descriptorIds);
            }
        }
    }

    private void pinShortcut(Shortcut shortcut) {
        shortcutsRepository.pinShortcut(shortcut)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleState<>() {
                    @Override
                    public void onSuccess(Boolean pinned) {
                        shortcutPinSubject.onNext(new ShortcutPinEvent(shortcut, pinned));
                    }

                    @Override
                    public void onError(Throwable e) {
                        errorsSubject.onNext(e);
                    }
                });
    }

    private boolean isBadgeVisible(@Nullable NotificationBag bag, boolean showOngoing) {
        if (bag != null) {
            // bag will never be empty here
            return showOngoing || bag.getClearableCount() > 0;
        }
        return false;
    }

    private Observable<UserPrefs> observeUserPrefs() {
        return fontManager.loadUserFonts()
                .andThen(preferences.observe()
                        .filter(prefs -> {
                            for (Preferences.Pref<?> pref : prefs) {
                                if (UserPrefs.PREFERENCES.contains(pref)) return true;
                            }
                            return false;
                        })
                        .map(s -> new UserPrefs(preferences, fontManager))
                        .startWith(Observable.fromCallable(() -> {
                            // make sure we load fonts first
                            return new UserPrefs(preferences, fontManager);
                        }))
                        .distinctUntilChanged());
    }

    private Update calculateUpdates(Update previous, List<DescriptorUi> newItems) {
        DescriptorUiDiffCallback callback = new DescriptorUiDiffCallback(previous.items, newItems);
        DiffUtil.DiffResult diffResult = calculateDiff(callback, true);
        return new Update(newItems, diffResult);
    }

    private static class AddToFolderAction extends BaseAction {

        private final String folderId;
        private final List<String> descriptorIds;
        private final boolean move;

        private AddToFolderAction(String folderId, List<String> descriptorIds, boolean move) {
            this.folderId = folderId;
            this.descriptorIds = descriptorIds;
            this.move = move;
        }

        @Override
        public void apply(List<MutableDescriptor<?>> items) {
            FolderDescriptor.Mutable descriptor = findById(items, folderId);
            if (descriptor == null) {
                return;
            }
            for (String descriptorId : descriptorIds) {
                if (!descriptor.getItems().contains(descriptorId)) {
                    descriptor.addItem(descriptorId);
                }
                if (move) {
                    IgnorableMutableDescriptor<?> item = findById(items, descriptorId);
                    if (item != null) {
                        item.setIgnored(true);
                    }
                }
            }
        }
    }

    private static class NotificationBagContainer {
        static final NotificationBagContainer EMPTY = new NotificationBagContainer();

        private final Map<AppDescriptor, NotificationBag> notifications;
        @Nullable
        private Map<String, NotificationBag> byDescriptorId;

        NotificationBagContainer(Map<AppDescriptor, NotificationBag> notifications) {
            this.notifications = notifications;
        }

        private NotificationBagContainer() {
            notifications = Collections.emptyMap();
            byDescriptorId = Collections.emptyMap();
        }

        boolean isEmpty() {
            return notifications.isEmpty();
        }

        NotificationBag get(AppDescriptor descriptor) {
            return notifications.get(descriptor);
        }

        NotificationBag get(String descriptorId) {
            return getByDescriptorId().get(descriptorId);
        }

        private Map<String, NotificationBag> getByDescriptorId() {
            if (byDescriptorId == null) {
                byDescriptorId = new HashMap<>(notifications.size());
                for (Map.Entry<AppDescriptor, NotificationBag> entry : notifications.entrySet()) {
                    byDescriptorId.put(entry.getKey().getId(), entry.getValue());
                }
            }
            return byDescriptorId;
        }
    }
}
