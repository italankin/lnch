package com.italankin.lnch.feature.home.apps;

import android.content.Intent;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
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
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import javax.inject.Inject;
import java.util.*;

import static androidx.recyclerview.widget.DiffUtil.calculateDiff;

@InjectViewState
public class AppsPresenter extends AppPresenter<AppsView> {

    private final HomeDescriptorsState homeDescriptorsState;
    private final DescriptorRepository descriptorRepository;
    private final ShortcutsRepository shortcutsRepository;
    private final NotificationsRepository notificationsRepository;
    private final Preferences preferences;
    private final NameNormalizer nameNormalizer;
    private final FontManager fontManager;

    private DescriptorRepository.Editor editor = EmptyEditor.INSTANCE;

    @Inject
    AppsPresenter(HomeDescriptorsState homeDescriptorsState,
            DescriptorRepository descriptorRepository,
            ShortcutsRepository shortcutsRepository,
            NotificationsRepository notificationsRepository,
            Preferences preferences, NameNormalizer nameNormalizer,
            FontManager fontManager) {
        this.homeDescriptorsState = homeDescriptorsState;
        this.descriptorRepository = descriptorRepository;
        this.shortcutsRepository = shortcutsRepository;
        this.notificationsRepository = notificationsRepository;
        this.preferences = preferences;
        this.nameNormalizer = nameNormalizer;
        this.fontManager = fontManager;
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
        if (editor == EmptyEditor.INSTANCE || editor.isDisposed()) {
            editor = descriptorRepository.edit();
        }
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
        item.label = nameNormalizer.normalize(label);
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
        editor = EmptyEditor.INSTANCE;
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
        editor = EmptyEditor.INSTANCE;
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
                .filter(appItems -> editor == EmptyEditor.INSTANCE)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new State<Update>() {
                    @Override
                    protected void onNext(AppsView viewState, Update update) {
                        Timber.d("Update: %s", update);
                        homeDescriptorsState.setItems(update.items);
                        viewState.onReceiveUpdate(update);
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

    private Observable<NotificationBagContainer> observeNotifications() {
        return Observable.combineLatest(
                notificationsRepository.observe(),
                preferences.observe(Preferences.NOTIFICATION_DOT),
                preferences.observe(Preferences.NOTIFICATION_DOT_FOLDERS),
                preferences.observe(Preferences.NOTIFICATION_DOT_ONGOING),
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
                        .filter(UserPrefs.PREFERENCES::contains)
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

    private static class EmptyEditor implements DescriptorRepository.Editor {
        private static final DescriptorRepository.Editor INSTANCE = new EmptyEditor();

        @Override
        public DescriptorRepository.Editor enqueue(Action action) {
            return this;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public DescriptorRepository.Editor clear() {
            return this;
        }

        @Override
        public Completable commit() {
            return Completable.complete();
        }

        @Override
        public void dispose() {
        }

        @Override
        public boolean isDisposed() {
            return true;
        }
    }
}
