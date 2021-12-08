package com.italankin.lnch.feature.home.apps;

import android.content.Intent;

import com.arellomobile.mvp.InjectViewState;
import com.italankin.lnch.feature.base.AppPresenter;
import com.italankin.lnch.feature.home.model.Update;
import com.italankin.lnch.feature.home.model.UserPrefs;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.descriptor.impl.FolderDescriptor;
import com.italankin.lnch.model.descriptor.impl.IntentDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.descriptor.actions.AddAction;
import com.italankin.lnch.model.repository.descriptor.actions.RemoveAction;
import com.italankin.lnch.model.repository.descriptor.actions.RenameAction;
import com.italankin.lnch.model.repository.descriptor.actions.SetColorAction;
import com.italankin.lnch.model.repository.descriptor.actions.SetIgnoreAction;
import com.italankin.lnch.model.repository.descriptor.actions.SwapAction;
import com.italankin.lnch.model.repository.notifications.NotificationDot;
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
import com.italankin.lnch.util.ListUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
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

    void renameItem(int position, CustomLabelDescriptorUi item, String customLabel) {
        String s = customLabel.trim().isEmpty() ? null : customLabel;
        editor.enqueue(new RenameAction(item.getDescriptor(), s));
        item.setCustomLabel(s);
        getViewState().onItemChanged(position);
    }

    void changeItemCustomColor(int position, CustomColorDescriptorUi item, Integer color) {
        editor.enqueue(new SetColorAction(item.getDescriptor(), color));
        item.setCustomColor(color);
        getViewState().onItemChanged(position);
    }

    void ignoreItem(int position, IgnorableDescriptorUi item) {
        editor.enqueue(new SetIgnoreAction(item.getDescriptor(), true));
        item.setIgnored(true);
        getViewState().onItemChanged(position);
    }

    void addFolder(String label, @ColorInt int color) {
        FolderDescriptor item = new FolderDescriptor(label, color);
        editor.enqueue(new AddAction(item));
        items.add(new FolderDescriptorUi(item));
        getViewState().onItemInserted(items.size() - 1);
    }

    void addToFolder(String folderId, InFolderDescriptorUi item) {
        editor.enqueue(new AddToFolderAction(folderId, item.getDescriptor()));
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

    void removeItem(int position, DescriptorUi item) {
        Descriptor descriptor = item.getDescriptor();
        editor.enqueue(new RemoveAction(descriptor));
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

    void selectFolder(InFolderDescriptorUi item) {
        List<FolderDescriptorUi> folders = new ArrayList<>(4);
        for (DescriptorUi descriptor : items) {
            if (descriptor instanceof FolderDescriptorUi) {
                folders.add((FolderDescriptorUi) descriptor);
            }
        }
        getViewState().showSelectFolderDialog(item, folders);
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

    void removeItemImmediate(RemovableDescriptorUi item) {
        Descriptor descriptor = item.getDescriptor();
        DescriptorRepository.Editor editor = descriptorRepository.edit();
        editor.enqueue(new RemoveAction(descriptor));
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

    private Update calculateUpdates(Update previous, List<DescriptorUi> newItems) {
        DescriptorUiDiffCallback callback = new DescriptorUiDiffCallback(previous.items, newItems);
        DiffUtil.DiffResult diffResult = calculateDiff(callback, true);
        return new Update(newItems, diffResult);
    }

    private static class EditIntentAction implements DescriptorRepository.Editor.Action {

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
            for (Descriptor item : items) {
                if (item.getId().equals(id)) {
                    IntentDescriptor descriptor = (IntentDescriptor) item;
                    descriptor.intentUri = intent.toUri(Intent.URI_INTENT_SCHEME | Intent.URI_ALLOW_UNSAFE);
                    descriptor.setCustomLabel(label);
                }
            }
        }
    }

    private static class AddToFolderAction implements DescriptorRepository.Editor.Action {

        private final String folderId;
        private final Descriptor item;

        private AddToFolderAction(String folderId, Descriptor item) {
            this.folderId = folderId;
            this.item = item;
        }

        @Override
        public void apply(List<Descriptor> items) {
            for (Descriptor descriptor : items) {
                if (descriptor instanceof FolderDescriptor && descriptor.getId().equals(folderId)) {
                    ((FolderDescriptor) descriptor).items.add(item.getId());
                }
            }
        }
    }
}
