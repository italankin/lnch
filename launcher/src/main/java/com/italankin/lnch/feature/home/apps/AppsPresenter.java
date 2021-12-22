package com.italankin.lnch.feature.home.apps;

import android.content.Intent;
import android.service.notification.StatusBarNotification;

import com.arellomobile.mvp.InjectViewState;
import com.italankin.lnch.feature.base.AppPresenter;
import com.italankin.lnch.feature.home.model.Update;
import com.italankin.lnch.feature.home.model.UserPrefs;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.DescriptorArg;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.descriptor.impl.FolderDescriptor;
import com.italankin.lnch.model.descriptor.impl.IntentDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.descriptor.actions.AddAction;
import com.italankin.lnch.model.repository.descriptor.actions.BaseAction;
import com.italankin.lnch.model.repository.descriptor.actions.RemoveAction;
import com.italankin.lnch.model.repository.descriptor.actions.RemoveFromFolderAction;
import com.italankin.lnch.model.repository.descriptor.actions.RenameAction;
import com.italankin.lnch.model.repository.descriptor.actions.SetColorAction;
import com.italankin.lnch.model.repository.descriptor.actions.SetIgnoreAction;
import com.italankin.lnch.model.repository.descriptor.actions.SwapAction;
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
import com.italankin.lnch.model.ui.impl.AppDescriptorUi;
import com.italankin.lnch.model.ui.impl.FolderDescriptorUi;
import com.italankin.lnch.model.ui.impl.IntentDescriptorUi;
import com.italankin.lnch.model.ui.util.DescriptorUiDiffCallback;
import com.italankin.lnch.model.ui.util.DescriptorUiFactory;
import com.italankin.lnch.util.ListUtils;

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

    private static final List<DescriptorUi> INITIAL = new ArrayList<>();

    private final DescriptorRepository descriptorRepository;
    private final ShortcutsRepository shortcutsRepository;
    private final NotificationsRepository notificationsRepository;
    private final Preferences preferences;
    /**
     * View commands will dispatch this instance on every state restore, so any changes
     * made to this list will be visible to new views.
     */
    private List<DescriptorUi> items = INITIAL;
    private DescriptorRepository.Editor editor;

    @Inject
    AppsPresenter(DescriptorRepository descriptorRepository, ShortcutsRepository shortcutsRepository,
            NotificationsRepository notificationsRepository, Preferences preferences) {
        this.descriptorRepository = descriptorRepository;
        this.shortcutsRepository = shortcutsRepository;
        this.notificationsRepository = notificationsRepository;
        this.preferences = preferences;
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

    void swapApps(int from, int to) {
        editor.enqueue(new SwapAction(from, to));
        ListUtils.swap(items, from, to);
        getViewState().onItemsSwap(from, to);
    }

    void renameItem(DescriptorArg arg) {
        int position = findDescriptorIndex(arg);
        if (position == -1) {
            return;
        }
        CustomLabelDescriptorUi item = (CustomLabelDescriptorUi) items.get(position);
        getViewState().showItemRenameDialog(position, item);
    }

    void renameItem(int position, CustomLabelDescriptorUi item, String customLabel) {
        String newLabel = customLabel == null || customLabel.isEmpty() ? null : customLabel;
        editor.enqueue(new RenameAction(item.getDescriptor(), newLabel));
        item.setCustomLabel(newLabel);
        getViewState().onItemChanged(position);
    }

    void changeItemCustomColor(DescriptorArg arg) {
        int position = findDescriptorIndex(arg);
        if (position == -1) {
            return;
        }
        CustomColorDescriptorUi item = (CustomColorDescriptorUi) items.get(position);
        getViewState().showItemSetColorDialog(position, item);
    }

    void changeItemCustomColor(int position, CustomColorDescriptorUi item, Integer color) {
        editor.enqueue(new SetColorAction(item.getDescriptor(), color));
        item.setCustomColor(color);
        getViewState().onItemChanged(position);
    }

    void ignoreItem(DescriptorArg arg) {
        int position = findDescriptorIndex(arg);
        if (position == -1) {
            return;
        }
        IgnorableDescriptorUi item = (IgnorableDescriptorUi) items.get(position);
        editor.enqueue(new SetIgnoreAction(arg.id, true));
        item.setIgnored(true);
        getViewState().onItemChanged(position);
    }

    void addFolder(String label, @ColorInt int color) {
        FolderDescriptor item = new FolderDescriptor(label, color);
        editor.enqueue(new AddAction(item));
        items.add(new FolderDescriptorUi(item));
        getViewState().onItemInserted(items.size() - 1);
    }

    void addToFolder(String folderId, String descriptorId) {
        FolderDescriptorUi folder = null;
        for (DescriptorUi item : items) {
            if (item.getDescriptor().getId().equals(folderId)) {
                folder = (FolderDescriptorUi) item;
                break;
            }
        }
        if (folder == null) {
            return;
        }
        if (folder.items.contains(descriptorId)) {
            getViewState().onFolderUpdated(folder, false);
            return;
        }
        editor.enqueue(new AddToFolderAction(folderId, descriptorId));
        folder.items.add(descriptorId);
        getViewState().onFolderUpdated(folder, true);
    }

    void addIntent(IntentDescriptor item) {
        editor.enqueue(new AddAction(item));
        items.add(new IntentDescriptorUi(item));
        getViewState().onItemInserted(items.size() - 1);
    }

    void editIntent(String id, Intent intent, String label) {
        editor.enqueue(new EditIntentAction(id, intent, label));
        for (int i = 0; i < items.size(); i++) {
            DescriptorUi item = items.get(i);
            if (item.getDescriptor().getId().equals(id)) {
                IntentDescriptorUi ui = (IntentDescriptorUi) item;
                ui.setCustomLabel(label);
                getViewState().onItemChanged(i);
                break;
            }
        }
    }

    void removeItem(DescriptorArg arg) {
        int position = findDescriptorIndex(arg);
        if (position == -1) {
            return;
        }
        editor.enqueue(new RemoveAction(arg.id));
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
        editor.dispose();
        editor = null;
        getViewState().onChangesDiscarded();
        update();
    }

    void selectFolder(DescriptorArg arg) {
        int position = findDescriptorIndex(arg);
        if (position == -1) {
            return;
        }
        InFolderDescriptorUi item = (InFolderDescriptorUi) items.get(position);
        List<FolderDescriptorUi> folders = new ArrayList<>(4);
        for (DescriptorUi descriptor : items) {
            if (descriptor instanceof FolderDescriptorUi) {
                folders.add((FolderDescriptorUi) descriptor);
            }
        }
        getViewState().showSelectFolderDialog(position, item, folders);
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

    void removeItemImmediate(String descriptorId) {
        DescriptorRepository.Editor editor = descriptorRepository.edit();
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

    private int findDescriptorIndex(DescriptorArg arg) {
        for (int i = 0, s = items.size(); i < s; i++) {
            DescriptorUi item = items.get(i);
            if (arg.is(item.getDescriptor())) {
                return i;
            }
        }
        return -1;
    }

    private static class EditIntentAction extends BaseAction {

        private final String id;
        private final Intent intent;
        private final String label;

        EditIntentAction(String id, Intent intent, String label) {
            this.id = id;
            this.intent = intent;
            this.label = label;
        }

        @Override
        public void apply(List<Descriptor> items) {
            IntentDescriptor descriptor = findById(items, id);
            if (descriptor != null) {
                descriptor.intentUri = intent.toUri(Intent.URI_INTENT_SCHEME | Intent.URI_ALLOW_UNSAFE);
                descriptor.setCustomLabel(label);
            }
        }
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
