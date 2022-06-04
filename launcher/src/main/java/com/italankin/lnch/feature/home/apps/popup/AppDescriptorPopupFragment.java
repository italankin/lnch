package com.italankin.lnch.feature.home.apps.popup;

import android.animation.LayoutTransition;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.home.apps.delegate.CustomizeDelegate;
import com.italankin.lnch.feature.home.apps.delegate.ErrorDelegate;
import com.italankin.lnch.feature.home.apps.delegate.ErrorDelegateImpl;
import com.italankin.lnch.feature.home.apps.delegate.ShortcutStarterDelegate;
import com.italankin.lnch.feature.home.apps.delegate.ShortcutStarterDelegateImpl;
import com.italankin.lnch.feature.home.apps.popup.notifications.AppNotificationFactory;
import com.italankin.lnch.feature.home.apps.popup.notifications.AppNotificationUi;
import com.italankin.lnch.feature.home.apps.popup.notifications.AppNotificationUiAdapter;
import com.italankin.lnch.feature.home.apps.popup.notifications.NotificationSwipeCallback;
import com.italankin.lnch.feature.home.fragmentresult.FragmentResultContract;
import com.italankin.lnch.feature.home.fragmentresult.SignalFragmentResultContract;
import com.italankin.lnch.feature.home.repository.HomeEntry;
import com.italankin.lnch.model.descriptor.PackageDescriptor;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.repository.notifications.NotificationBag;
import com.italankin.lnch.model.repository.notifications.NotificationsRepository;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.shortcuts.Shortcut;
import com.italankin.lnch.model.repository.shortcuts.ShortcutsRepository;
import com.italankin.lnch.model.repository.usage.UsageTracker;
import com.italankin.lnch.model.ui.InFolderDescriptorUi;
import com.italankin.lnch.model.ui.impl.AppDescriptorUi;
import com.italankin.lnch.util.IntentUtils;
import com.italankin.lnch.util.ListUtils;
import com.italankin.lnch.util.NumberUtils;
import com.italankin.lnch.util.PackageUtils;
import com.italankin.lnch.util.adapterdelegate.CompositeAdapter;
import com.italankin.lnch.util.widget.popup.ActionPopupFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class AppDescriptorPopupFragment extends ActionPopupFragment implements
        AppNotificationUiAdapter.Listener,
        NotificationSwipeCallback.Listener {

    public static AppDescriptorPopupFragment newInstance(
            AppDescriptorUi descriptorUi,
            String requestKey,
            @Nullable Rect anchor) {
        AppDescriptorPopupFragment fragment = new AppDescriptorPopupFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_ANCHOR, anchor);
        args.putString(ARG_DESCRIPTOR_ID, descriptorUi.getDescriptor().getId());
        args.putString(ARG_REQUEST_KEY, requestKey);
        fragment.setArguments(args);
        return fragment;
    }

    private static final String ARG_DESCRIPTOR_ID = "descriptor_id";
    private static final String ARG_FOLDER_ID = "folder_id";

    private static final String BACKSTACK_NAME = "app_descriptor_popup";
    private static final String TAG = "app_descriptor_popup";

    private Preferences preferences;
    private NotificationsRepository notificationsRepository;
    private ShortcutsRepository shortcutsRepository;

    private boolean showNotifications;

    private RecyclerView notificationsList;
    private View notificationsListContainer;
    private CompositeAdapter<AppNotificationUi> notificationsAdapter;

    private ErrorDelegate errorDelegate;
    private ShortcutStarterDelegate shortcutStarterDelegate;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    public AppDescriptorPopupFragment setFolderId(String folderId) {
        requireArguments().putString(ARG_FOLDER_ID, folderId);
        return this;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        preferences = LauncherApp.daggerService.main().preferences();
        notificationsRepository = LauncherApp.daggerService.main().notificationsRepository();
        shortcutsRepository = LauncherApp.daggerService.main().shortcutsRepository();
        showNotifications = preferences.get(Preferences.NOTIFICATION_POPUP);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_app_descriptor_popup, container, false);
        root = view.findViewById(R.id.popup_root);
        root.setKeepLocationOnShrink(true);
        containerRoot = view.findViewById(R.id.popup_container_root);
        itemsContainer = view.findViewById(R.id.popup_item_container);
        notificationsList = view.findViewById(R.id.notifications_list);
        notificationsListContainer = view.findViewById(R.id.notifications_list_container);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (showNotifications) {
            setupNotifications();
        }

        Context context = requireContext();
        errorDelegate = new ErrorDelegateImpl(context);
        CustomizeDelegate customizeDelegate = () -> {
            Bundle result = new CustomizeContract().result();
            sendResult(result);
            dismiss();
        };
        UsageTracker usageTracker = LauncherApp.daggerService.main().usageTracker();
        shortcutStarterDelegate = new ShortcutStarterDelegateImpl(context, errorDelegate, customizeDelegate,
                usageTracker);

        load();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        compositeDisposable.clear();
    }

    @Override
    protected String getPopupBackstackName() {
        return BACKSTACK_NAME;
    }

    @Override
    protected String getPopupTag() {
        return TAG;
    }

    private void load() {
        String descriptorId = requireArguments().getString(ARG_DESCRIPTOR_ID);
        HomeEntry<AppDescriptorUi> entry = LauncherApp.daggerService.main()
                .homeDescriptorState()
                .find(AppDescriptorUi.class, descriptorId);
        if (entry == null) {
            throw new NullPointerException("No apps found by descriptorId=" + descriptorId);
        }
        AppDescriptorUi item = entry.item;
        buildItemPopup(item);
        createItemViews();
        showPopup();
    }

    private void buildItemPopup(AppDescriptorUi item) {
        Context context = requireContext();
        boolean uninstallAvailable = !PackageUtils.isSystem(context.getPackageManager(), item.packageName);
        ItemBuilder infoItem = new ItemBuilder()
                .setLabel(R.string.popup_app_info)
                .setIcon(R.drawable.ic_app_info)
                .setOnClickListener(v -> {
                    startAppSettings(item.getDescriptor(), v);
                });
        ItemBuilder uninstallItem = new ItemBuilder()
                .setLabel(R.string.popup_app_uninstall)
                .setIcon(R.drawable.ic_action_delete)
                .setOnClickListener(v -> {
                    startUninstall(item);
                });
        String folderId = requireArguments().getString(ARG_FOLDER_ID);

        List<Shortcut> shortcuts = getShortcuts(item);
        if (folderId != null && preferences.get(Preferences.DESTRUCTIVE_NON_EDIT)) {
            ItemBuilder removeFromFolderItem = new ItemBuilder()
                    .setIcon(R.drawable.ic_action_remove_from_folder)
                    .setLabel(R.string.customize_item_remove_from_folder)
                    .setOnClickListener(v -> {
                        removeFromFolder(item, folderId);
                    });
            if (shortcuts.isEmpty()) {
                addShortcut(removeFromFolderItem.setIconDrawableTintAttr(R.attr.colorAccent));
            } else {
                addAction(removeFromFolderItem);
            }
        }
        addAction(infoItem);
        if (uninstallAvailable) {
            addAction(uninstallItem);
        }
        for (Shortcut shortcut : shortcuts) {
            addShortcut(new ItemBuilder()
                    .setLabel(shortcut.getShortLabel())
                    .setIcon(shortcut.getIconUri())
                    .setEnabled(shortcut.isEnabled())
                    .setOnClickListener(v -> {
                        startShortcut(shortcut, v);
                    })
                    .setOnPinClickListener(v -> {
                        pinShortcut(shortcut);
                    })
            );
        }
        if (showNotifications) {
            subscribeForNotifications(item.getDescriptor());
        }
    }

    private List<Shortcut> getShortcuts(AppDescriptorUi item) {
        if (!preferences.get(Preferences.SHOW_SHORTCUTS) || !item.getDescriptor().showShortcuts) {
            return Collections.emptyList();
        }
        List<Shortcut> shortcuts = shortcutsRepository.getShortcuts(item.getDescriptor());
        if (shortcuts.isEmpty()) {
            return Collections.emptyList();
        }
        if (preferences.get(Preferences.SHORTCUTS_SORT_MODE) == Preferences.ShortcutsSortMode.REVERSED) {
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

    ///////////////////////////////////////////////////////////////////////////
    // Actions
    ///////////////////////////////////////////////////////////////////////////

    private void startShortcut(Shortcut shortcut, View v) {
        shortcutStarterDelegate.startShortcut(shortcut, v);
        dismissWithResult();
    }

    private void startUninstall(AppDescriptorUi item) {
        Intent intent = PackageUtils.getUninstallIntent(item.packageName);
        if (IntentUtils.safeStartActivity(requireContext(), intent)) {
            dismissWithResult();
        } else {
            errorDelegate.showError(R.string.error);
        }
    }

    private void startAppSettings(PackageDescriptor item, View bounds) {
        IntentUtils.safeStartAppSettings(requireContext(), item.getPackageName(), bounds);
        dismissWithResult();
    }

    private void removeFromFolder(InFolderDescriptorUi item, String folderId) {
        Bundle result = RemoveFromFolderContract.result(item.getDescriptor().getId(), folderId);
        sendResult(result);
        dismiss();
    }

    private void pinShortcut(Shortcut shortcut) {
        Bundle result = PinShortcutContract.result(shortcut.getPackageName(), shortcut.getId());
        sendResult(result);
        dismiss();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Notifications
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onNotificationSwiped(AppNotificationUi item) {
        PendingIntent deleteIntent = item.sbn.getNotification().deleteIntent;
        if (deleteIntent != null) {
            try {
                deleteIntent.send();
            } catch (PendingIntent.CanceledException e) {
                Timber.e(e, "deleteIntent.send:");
                dismissWithResult();
            }
        }
        cancelNotification(item);
    }

    @Override
    public void onNotificationClick(AppNotificationUi item) {
        Notification notification = item.sbn.getNotification();
        try {
            notification.contentIntent.send();
            if (((notification.flags & Notification.FLAG_AUTO_CANCEL)) == Notification.FLAG_AUTO_CANCEL) {
                cancelNotification(item);
            }
        } catch (PendingIntent.CanceledException e) {
            Timber.e(e, "contentIntent.send:");
        }
        dismissWithResult();
    }

    private void setupNotifications() {
        this.notificationsAdapter = new CompositeAdapter.Builder<AppNotificationUi>(requireContext())
                .add(new AppNotificationUiAdapter(this))
                .setHasStableIds(true)
                .recyclerView(notificationsList)
                .create();
        notificationsListContainer.setClipToOutline(true);
        new ItemTouchHelper(new NotificationSwipeCallback(this))
                .attachToRecyclerView(notificationsList);

        LayoutTransition transition = new LayoutTransition();
        transition.disableTransitionType(LayoutTransition.APPEARING);
        transition.disableTransitionType(LayoutTransition.CHANGE_APPEARING);
        transition.disableTransitionType(LayoutTransition.CHANGE_DISAPPEARING);
        transition.enableTransitionType(LayoutTransition.CHANGING);
        transition.enableTransitionType(LayoutTransition.DISAPPEARING);
        root.setLayoutTransition(transition);
    }

    private void subscribeForNotifications(AppDescriptor descriptor) {
        NotificationBag current = notificationsRepository.getByApp(descriptor);
        if (current == null || current.getCount() == 0) {
            return;
        }

        ViewGroup.LayoutParams lp = containerRoot.getLayoutParams();
        lp.width = getResources().getDimensionPixelSize(R.dimen.popup_notifications_width);
        containerRoot.setLayoutParams(lp);

        AppNotificationFactory appNotificationFactory = new AppNotificationFactory(requireContext());
        setNotifications(appNotificationFactory.createNotifications(current));

        Disposable d = notificationsRepository.observe()
                .skip(1)
                .subscribeOn(Schedulers.computation())
                .map(state -> {
                    NotificationBag notifications = state.get(descriptor);
                    return appNotificationFactory.createNotifications(notifications);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setNotifications,
                        e -> {
                            Timber.e(e, "subscribeForNotifications:");
                            dismissWithResult();
                        });
        compositeDisposable.add(d);
    }

    private void setNotifications(List<AppNotificationUi> items) {
        if (items.isEmpty()) {
            notificationsListContainer.setVisibility(View.GONE);
            compositeDisposable.clear();
        } else {
            notificationsAdapter.setDataset(items);
            notificationsAdapter.notifyDataSetChanged();
            TextView count = notificationsListContainer.findViewById(R.id.notifications_count);
            count.setText(String.valueOf(items.size()));
            notificationsListContainer.setVisibility(View.VISIBLE);
        }
    }

    private void cancelNotification(AppNotificationUi item) {
        NotificationsRepository.Callback callback = notificationsRepository.getCallback();
        if (callback != null) {
            callback.cancelNotification(item.sbn);
        }
    }

    public static class RemoveFromFolderContract implements FragmentResultContract<RemoveFromFolderContract.Result> {
        private static final String KEY = "app_remove_from_folder";
        private static final String DESCRIPTOR_ID = "descriptor_id";
        private static final String FOLDER_ID = "folder_id";

        static Bundle result(String descriptorId, String folderId) {
            Bundle result = new Bundle();
            result.putString(RESULT_KEY, KEY);
            result.putString(DESCRIPTOR_ID, descriptorId);
            result.putString(FOLDER_ID, folderId);
            return result;
        }

        @Override
        public String key() {
            return KEY;
        }

        @Override
        public Result parseResult(Bundle result) {
            return new Result(result.getString(DESCRIPTOR_ID), result.getString(FOLDER_ID));
        }

        public static class Result {
            public final String descriptorId;
            public final String folderId;

            Result(String descriptorId, String folderId) {
                this.descriptorId = descriptorId;
                this.folderId = folderId;
            }
        }
    }

    public static class PinShortcutContract implements FragmentResultContract<PinShortcutContract.Result> {
        private static final String KEY = "app_pin_shortcut";
        private static final String PACKAGE_NAME = "package_name";
        private static final String SHORTCUT_ID = "shortcut_id";

        static Bundle result(String packageName, String shortcutId) {
            Bundle result = new Bundle();
            result.putString(RESULT_KEY, KEY);
            result.putString(PACKAGE_NAME, packageName);
            result.putString(SHORTCUT_ID, shortcutId);
            return result;
        }

        @Override
        public String key() {
            return KEY;
        }

        @Override
        public Result parseResult(Bundle result) {
            return new Result(result.getString(PACKAGE_NAME), result.getString(SHORTCUT_ID));
        }

        public static class Result {
            public final String packageName;
            public final String shortcutId;

            Result(String packageName, String shortcutId) {
                this.packageName = packageName;
                this.shortcutId = shortcutId;
            }
        }
    }

    public static class CustomizeContract extends SignalFragmentResultContract {
        public CustomizeContract() {
            super("app_customize");
        }
    }
}
