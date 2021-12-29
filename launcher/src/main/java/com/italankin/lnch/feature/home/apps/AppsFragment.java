package com.italankin.lnch.feature.home.apps;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.api.LauncherIntents;
import com.italankin.lnch.feature.base.AppFragment;
import com.italankin.lnch.feature.base.BackButtonHandler;
import com.italankin.lnch.feature.common.dialog.RenameDescriptorDialog;
import com.italankin.lnch.feature.common.dialog.SetColorDescriptorDialog;
import com.italankin.lnch.feature.home.adapter.AppDescriptorUiAdapter;
import com.italankin.lnch.feature.home.adapter.DeepShortcutDescriptorUiAdapter;
import com.italankin.lnch.feature.home.adapter.FolderDescriptorUiAdapter;
import com.italankin.lnch.feature.home.adapter.HomeAdapter;
import com.italankin.lnch.feature.home.adapter.IgnorableDescriptorUiAdapter;
import com.italankin.lnch.feature.home.adapter.IntentDescriptorUiAdapter;
import com.italankin.lnch.feature.home.adapter.PinnedShortcutDescriptorUiAdapter;
import com.italankin.lnch.feature.home.apps.delegate.AppClickDelegate;
import com.italankin.lnch.feature.home.apps.delegate.AppClickDelegateImpl;
import com.italankin.lnch.feature.home.apps.delegate.CustomizeDelegate;
import com.italankin.lnch.feature.home.apps.delegate.DeepShortcutClickDelegate;
import com.italankin.lnch.feature.home.apps.delegate.DeepShortcutClickDelegateImpl;
import com.italankin.lnch.feature.home.apps.delegate.ErrorDelegate;
import com.italankin.lnch.feature.home.apps.delegate.ErrorDelegateImpl;
import com.italankin.lnch.feature.home.apps.delegate.IntentClickDelegate;
import com.italankin.lnch.feature.home.apps.delegate.IntentClickDelegateImpl;
import com.italankin.lnch.feature.home.apps.delegate.ItemPopupDelegate;
import com.italankin.lnch.feature.home.apps.delegate.PinnedShortcutClickDelegate;
import com.italankin.lnch.feature.home.apps.delegate.PinnedShortcutClickDelegateImpl;
import com.italankin.lnch.feature.home.apps.delegate.SearchIntentStarterDelegate;
import com.italankin.lnch.feature.home.apps.delegate.SearchIntentStarterDelegateImpl;
import com.italankin.lnch.feature.home.apps.delegate.ShortcutStarterDelegate;
import com.italankin.lnch.feature.home.apps.delegate.ShortcutStarterDelegateImpl;
import com.italankin.lnch.feature.home.apps.folder.EditFolderFragment;
import com.italankin.lnch.feature.home.apps.folder.FolderFragment;
import com.italankin.lnch.feature.home.apps.popup.AppDescriptorPopupFragment;
import com.italankin.lnch.feature.home.apps.popup.CustomizeDescriptorPopupFragment;
import com.italankin.lnch.feature.home.apps.popup.DescriptorPopupFragment;
import com.italankin.lnch.feature.home.apps.popup.EditModePopupFragment;
import com.italankin.lnch.feature.home.apps.selectfolder.SelectFolderFragment;
import com.italankin.lnch.feature.home.behavior.SearchOverlayBehavior;
import com.italankin.lnch.feature.home.fragmentresult.FragmentResultManager;
import com.italankin.lnch.feature.home.model.Update;
import com.italankin.lnch.feature.home.model.UserPrefs;
import com.italankin.lnch.feature.home.repository.HomeDescriptorsState;
import com.italankin.lnch.feature.home.search.SearchOverlay;
import com.italankin.lnch.feature.home.util.IntentQueue;
import com.italankin.lnch.feature.home.util.MoveItemHelper;
import com.italankin.lnch.feature.home.widget.EditModePanel;
import com.italankin.lnch.feature.home.widget.HomeRecyclerView;
import com.italankin.lnch.feature.intentfactory.IntentFactoryActivity;
import com.italankin.lnch.feature.intentfactory.IntentFactoryResult;
import com.italankin.lnch.feature.settings.SettingsActivity;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.FolderDescriptor;
import com.italankin.lnch.model.descriptor.impl.IntentDescriptor;
import com.italankin.lnch.model.repository.descriptor.NameNormalizer;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.search.match.DescriptorMatch;
import com.italankin.lnch.model.repository.search.match.Match;
import com.italankin.lnch.model.repository.shortcuts.Shortcut;
import com.italankin.lnch.model.repository.shortcuts.ShortcutsRepository;
import com.italankin.lnch.model.repository.usage.UsageTracker;
import com.italankin.lnch.model.ui.CustomColorDescriptorUi;
import com.italankin.lnch.model.ui.CustomLabelDescriptorUi;
import com.italankin.lnch.model.ui.DescriptorUi;
import com.italankin.lnch.model.ui.InFolderDescriptorUi;
import com.italankin.lnch.model.ui.RemovableDescriptorUi;
import com.italankin.lnch.model.ui.impl.AppDescriptorUi;
import com.italankin.lnch.model.ui.impl.DeepShortcutDescriptorUi;
import com.italankin.lnch.model.ui.impl.FolderDescriptorUi;
import com.italankin.lnch.model.ui.impl.IntentDescriptorUi;
import com.italankin.lnch.model.ui.impl.PinnedShortcutDescriptorUi;
import com.italankin.lnch.util.DescriptorUtils;
import com.italankin.lnch.util.IntentUtils;
import com.italankin.lnch.util.PackageUtils;
import com.italankin.lnch.util.ResUtils;
import com.italankin.lnch.util.StatusBarUtils;
import com.italankin.lnch.util.ViewUtils;
import com.italankin.lnch.util.picasso.PackageIconHandler;
import com.italankin.lnch.util.widget.LceLayout;
import com.italankin.lnch.util.widget.popup.ActionPopupFragment;

import java.util.List;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class AppsFragment extends AppFragment implements AppsView,
        BackButtonHandler,
        IntentQueue.OnIntentAction,
        DeepShortcutDescriptorUiAdapter.Listener,
        IntentDescriptorUiAdapter.Listener,
        AppDescriptorUiAdapter.Listener,
        FolderDescriptorUiAdapter.Listener,
        PinnedShortcutDescriptorUiAdapter.Listener, HomeDescriptorsState.Callback {

    private static final int ANIM_LIST_APPEARANCE_DURATION = 400;

    private static final String REQUEST_KEY_APPS = "apps";

    @InjectPresenter
    AppsPresenter presenter;

    private Preferences preferences;
    private HomeDescriptorsState homeDescriptorsState;
    private IntentQueue intentQueue;

    private LceLayout lce;
    private HomeRecyclerView list;
    private HomeAdapter adapter;
    private EditModePanel editModePanel;
    private SearchOverlay searchOverlay;
    private CoordinatorLayout coordinator;

    private SearchOverlayBehavior searchOverlayBehavior;

    private ItemTouchHelper touchHelper;
    private Preferences.HomeLayout layout;

    private boolean animateOnResume;
    private boolean editMode;

    private AppClickDelegate appClickDelegate;
    private ErrorDelegate errorDelegate;
    private ItemPopupDelegate itemPopupDelegate;
    private PinnedShortcutClickDelegate pinnedShortcutClickDelegate;
    private DeepShortcutClickDelegate deepShortcutClickDelegate;
    private IntentClickDelegate intentClickDelegate;
    private SearchIntentStarterDelegate searchIntentStarterDelegate;

    private final ActivityResultLauncher<Void> createIntentLauncher = registerForActivityResult(
            new IntentFactoryActivity.CreateContract(),
            this::onNewIntentCreated);

    private final ActivityResultLauncher<IntentDescriptorUi> editIntentLauncher = registerForActivityResult(
            new IntentFactoryActivity.EditContract(),
            this::onIntentEdited);

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable hideSearchRunnable;

    @ProvidePresenter
    AppsPresenter providePresenter() {
        return LauncherApp.daggerService.presenters().apps();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        preferences = LauncherApp.daggerService.main().preferences();
        intentQueue = LauncherApp.daggerService.main().intentQueue();
        homeDescriptorsState = LauncherApp.daggerService.main().homeDescriptorState();

        registerFragmentResultListeners();
    }

    @Override
    public void onStart() {
        super.onStart();
        animateOnResume = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                preferences.get(Preferences.APPS_LIST_ANIMATE);
        if (hideSearchRunnable != null) {
            hideSearchRunnable = null;
            searchOverlayBehavior.hideNow();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (animateOnResume) {
            animateOnResume = false;
            animateListAppearance();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        searchOverlayBehavior.hide();
    }

    public void setAnimateOnResume(boolean animateOnResume) {
        this.animateOnResume = animateOnResume;
    }

    public void onRestart() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_apps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        lce = view.findViewById(R.id.lce_apps);
        searchOverlay = view.findViewById(R.id.search_bar);
        list = view.findViewById(R.id.list);
        coordinator = view.findViewById(R.id.coordinator);

        setupSearchOverlay();

        touchHelper = new ItemTouchHelper(new MoveItemHelper(presenter::moveItem));
        touchHelper.attachToRecyclerView(list);

        registerWindowInsets(view);

        initDelegates(requireContext());

        intentQueue.registerOnIntentAction(this);

        homeDescriptorsState.addCallback(this);
    }

    private void initDelegates(Context context) {
        ShortcutsRepository shortcutsRepository = LauncherApp.daggerService.main().shortcutsRepository();
        UsageTracker usageTracker = LauncherApp.daggerService.main().usageTracker();

        errorDelegate = new ErrorDelegateImpl(context);
        itemPopupDelegate = (item, anchor) -> {
            cancelListMotionEvents();
            Rect bounds = ViewUtils.getViewBoundsInsetPadding(anchor);
            if (item instanceof AppDescriptorUi) {
                AppDescriptorPopupFragment.newInstance((AppDescriptorUi) item, REQUEST_KEY_APPS, bounds)
                        .show(getParentFragmentManager());
            } else {
                DescriptorPopupFragment.newInstance(item, REQUEST_KEY_APPS, bounds)
                        .show(getParentFragmentManager());
            }
        };
        CustomizeDelegate customizeDelegate = presenter::startCustomize;
        ShortcutStarterDelegate shortcutStarterDelegate = new ShortcutStarterDelegateImpl(context, errorDelegate,
                customizeDelegate, usageTracker);
        pinnedShortcutClickDelegate = new PinnedShortcutClickDelegateImpl(context, errorDelegate, itemPopupDelegate,
                usageTracker);
        deepShortcutClickDelegate = new DeepShortcutClickDelegateImpl(shortcutStarterDelegate, itemPopupDelegate,
                shortcutsRepository);
        searchIntentStarterDelegate = new SearchIntentStarterDelegateImpl(context, preferences, errorDelegate,
                customizeDelegate, usageTracker);
        intentClickDelegate = new IntentClickDelegateImpl(searchIntentStarterDelegate, itemPopupDelegate);
        appClickDelegate = new AppClickDelegateImpl(context, preferences, errorDelegate, itemPopupDelegate,
                usageTracker);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        intentQueue.unregisterOnIntentAction(this);
        homeDescriptorsState.removeCallback(this);
    }

    @Override
    public boolean onIntent(Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            return false;
        }
        switch (action) {
            case Intent.ACTION_MAIN: {
                dismissPopups();
                if (searchOverlayBehavior.isShown()) {
                    searchOverlayBehavior.hide();
                } else if (preferences.get(Preferences.SCROLL_TO_TOP)) {
                    scrollToTop();
                }
                return true;
            }
            case LauncherIntents.ACTION_EDIT_MODE: {
                dismissPopups();
                if (!editMode) {
                    animateOnResume = false;
                    presenter.startCustomize();
                }
                return true;
            }
        }
        return false;
    }

    private void registerFragmentResultListeners() {
        new FragmentResultManager(getParentFragmentManager(), this, REQUEST_KEY_APPS)
                .register(new FolderFragment.CustomizeContract(), ignored -> {
                    presenter.startCustomize();
                })
                .register(new AppDescriptorPopupFragment.CustomizeContract(), ignored -> {
                    presenter.startCustomize();
                })
                .register(new AppDescriptorPopupFragment.PinShortcutContract(), result -> {
                    presenter.pinShortcut(result.packageName, result.shortcutId);
                })
                .register(new AppDescriptorPopupFragment.RemoveFromFolderContract(), result -> {
                    presenter.removeFromFolder(result.descriptorId, result.folderId);
                })
                .register(new DescriptorPopupFragment.RequestRemovalContract(), descriptorId -> {
                    presenter.requestRemoveItem(descriptorId);
                })
                .register(new DescriptorPopupFragment.RemoveFromFolderContract(), result -> {
                    presenter.removeFromFolder(result.descriptorId, result.folderId);
                })
                .register(new SelectFolderFragment.AddToFolderContract(), result -> {
                    presenter.addToFolder(result.descriptorId, result.folderId);
                })
                .register(new EditModePopupFragment.AddFolderContract(), ignored -> {
                    String label = getString(R.string.new_folder_default_label);
                    int color = ResUtils.resolveColor(requireContext(), R.attr.colorFolderTitleDefault);
                    presenter.addFolder(label, color);
                })
                .register(new EditModePopupFragment.CreateIntentContract(), ignored -> {
                    createIntentLauncher.launch(null);
                })
                .register(new CustomizeDescriptorPopupFragment.ShowSelectFolderContract(), descriptorId -> {
                    presenter.selectFolder(descriptorId);
                })
                .register(new CustomizeDescriptorPopupFragment.IgnoreContract(), descriptorId -> {
                    presenter.ignoreItem(descriptorId);
                })
                .register(new CustomizeDescriptorPopupFragment.RenameContract(), descriptorId -> {
                    presenter.renameItem(descriptorId);
                })
                .register(new CustomizeDescriptorPopupFragment.SetColorContract(), descriptorId -> {
                    presenter.showSetItemColorDialog(descriptorId);
                })
                .register(new CustomizeDescriptorPopupFragment.RemoveContract(), descriptorId -> {
                    presenter.removeItem(descriptorId);
                })
                .register(new CustomizeDescriptorPopupFragment.EditIntentContract(), descriptorId -> {
                    presenter.startEditIntent(descriptorId);
                })
                .register(new CustomizeDescriptorPopupFragment.EditFolderContract(), folderId -> {
                    presenter.showFolder(folderId);
                })
                .register(new ActionPopupFragment.ActionDoneContract(), ignored -> {
                    // empty
                })
                .attach();
    }

    @Override
    public void onReceiveUpdate(Update update) {
        setItems(update);
        if (update.items.isEmpty()) {
            lce.empty()
                    .message(R.string.apps_list_empty)
                    .button(R.string.open_settings, v -> {
                        startAppActivity(SettingsActivity.getComponentName(requireContext()), v);
                    })
                    .show();
        } else {
            lce.showContent();
        }

        applyUserPrefs(update.userPrefs);

        boolean needsFullUpdate = adapter.updateUserPrefs(update.userPrefs);
        if (needsFullUpdate) {
            adapter.notifyDataSetChanged();
        } else {
            update.dispatchTo(list.getAdapter());
        }
    }

    @Override
    public boolean onBackPressed() {
        if (editMode) {
            presenter.confirmDiscardChanges();
            return true;
        }
        if (searchOverlayBehavior.isShown()) {
            searchOverlayBehavior.hide();
            return true;
        }
        scrollToTop();
        return true;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Start
    ///////////////////////////////////////////////////////////////////////////

    private void startAppActivity(ComponentName componentName, View view) {
        Rect bounds = ViewUtils.getViewBounds(view);
        Bundle opts = IntentUtils.getActivityLaunchOptions(view, bounds);
        IntentUtils.safeStartMainActivity(requireContext(), componentName, bounds, opts);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Adapter callbacks
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onAppClick(int position, AppDescriptorUi item) {
        if (editMode) {
            showCustomizePopup(position, item);
        } else {
            View view = list.findViewForAdapterPosition(position);
            appClickDelegate.onAppClick(item, view);
        }
    }

    @Override
    public void onAppLongClick(int position, AppDescriptorUi item) {
        if (editMode) {
            startDrag(position);
        } else {
            View view = list.findViewForAdapterPosition(position);
            appClickDelegate.onAppLongClick(item, view);
        }
    }

    @Override
    public void onFolderClick(int position, FolderDescriptorUi item) {
        if (editMode) {
            showCustomizePopup(position, item);
        } else {
            showFolder(position, item.getDescriptor());
        }
    }

    @Override
    public void onFolderLongClick(int position, FolderDescriptorUi item) {
        if (editMode) {
            startDrag(position);
        } else {
            View anchor = list.findViewForAdapterPosition(position);
            itemPopupDelegate.showItemPopup(item, anchor);
        }
    }

    @Override
    public void onPinnedShortcutClick(int position, PinnedShortcutDescriptorUi item) {
        if (editMode) {
            showCustomizePopup(position, item);
        } else {
            pinnedShortcutClickDelegate.onPinnedShortcutClick(item);
        }
    }

    @Override
    public void onPinnedShortcutLongClick(int position, PinnedShortcutDescriptorUi item) {
        if (editMode) {
            startDrag(position);
        } else {
            View view = list.findViewForAdapterPosition(position);
            pinnedShortcutClickDelegate.onPinnedShortcutLongClick(item, view);
        }
    }

    @Override
    public void onDeepShortcutClick(int position, DeepShortcutDescriptorUi item) {
        if (editMode) {
            showCustomizePopup(position, item);
        } else {
            View view = list.findViewForAdapterPosition(position);
            deepShortcutClickDelegate.onDeepShortcutClick(item, view);
        }
    }

    @Override
    public void onDeepShortcutLongClick(int position, DeepShortcutDescriptorUi item) {
        if (editMode) {
            startDrag(position);
        } else {
            View view = list.findViewForAdapterPosition(position);
            deepShortcutClickDelegate.onDeepShortcutLongClick(item, view);
        }
    }

    @Override
    public void onIntentClick(int position, IntentDescriptorUi item) {
        if (editMode) {
            showCustomizePopup(position, item);
        } else {
            intentClickDelegate.onIntentClick(item);
        }
    }

    @Override
    public void onIntentLongClick(int position, IntentDescriptorUi item) {
        if (editMode) {
            startDrag(position);
        } else {
            View view = list.findViewForAdapterPosition(position);
            intentClickDelegate.onIntentLongClick(item, view);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // State callbacks
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onNewItems(List<DescriptorUi> items) {
        // empty
    }

    @Override
    public void onItemChanged(int position, DescriptorUi item) {
        adapter.notifyItemChanged(position);
    }

    @Override
    public void onItemRemoved(int position, DescriptorUi item) {
        adapter.notifyItemRemoved(position);
    }

    @Override
    public void onItemInserted(int position, DescriptorUi item) {
        adapter.notifyItemInserted(position);
        list.scrollToPosition(position);
    }

    @Override
    public void onItemMoved(int fromPosition, int toPosition) {
        adapter.notifyItemMoved(fromPosition, toPosition);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Other
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void showFolder(int position, FolderDescriptor descriptor) {
        Point point = null;
        View view = list.findViewForAdapterPosition(position);
        if (view != null) {
            int[] loc = new int[2];
            view.getLocationInWindow(loc);
            point = new Point(loc[0] + view.getWidth() / 2, loc[1]);
        }

        if (editMode) {
            EditFolderFragment.newInstance(descriptor, REQUEST_KEY_APPS, point)
                    .show(getParentFragmentManager(), android.R.id.content);
        } else {
            FolderFragment.newInstance(descriptor, REQUEST_KEY_APPS, point)
                    .show(getParentFragmentManager(), android.R.id.content);
        }
    }

    private void setEditMode(boolean value) {
        if (editMode == value) {
            return;
        }
        editMode = value;
        searchOverlayBehavior.hide();
        searchOverlayBehavior.setEnabled(!value);
        if (value) {
            dismissPopups();
            searchOverlay.hideSoftKeyboard();
            EditModePanel panel = new EditModePanel(requireContext())
                    .setMessage(R.string.customize_hint)
                    .setOnAddActionClickListener(this::showEditModeAddPopup)
                    .setOnSaveActionClickListener(v -> {
                        if (this.editModePanel != null && this.editModePanel.isShown()) {
                            presenter.stopCustomize();
                        }
                    });
            editModePanel = panel.show(coordinator);
        } else if (editModePanel != null) {
            editModePanel.dismiss();
            editModePanel = null;
        }
    }

    private void showCustomizePopup(int position, DescriptorUi item) {
        View view = list.findViewForAdapterPosition(position);
        Rect bounds = ViewUtils.getViewBoundsInsetPadding(view);
        CustomizeDescriptorPopupFragment.newInstance(item, REQUEST_KEY_APPS, bounds)
                .show(getParentFragmentManager());
    }

    private void showEditModeAddPopup(View anchor) {
        Rect bounds = ViewUtils.getViewBoundsInsetPadding(anchor);
        EditModePopupFragment.newInstance(REQUEST_KEY_APPS, bounds)
                .show(getParentFragmentManager());
    }

    private void onNewIntentCreated(@Nullable IntentFactoryResult result) {
        if (result == null) {
            return;
        }
        String label = getString(R.string.intent_factory_default_title);
        presenter.addIntent(result.intent, label);
    }

    private void onIntentEdited(@Nullable IntentFactoryResult result) {
        if (result == null || result.descriptorId == null) {
            return;
        }
        presenter.editIntent(result.descriptorId, result.intent);
    }

    @Override
    public void showProgress() {
        lce.showLoading();
    }

    @Override
    public void onStartCustomize() {
        setEditMode(true);
    }

    @Override
    public void onConfirmDiscardChanges() {
        new AlertDialog.Builder(requireContext())
                .setMessage(R.string.customize_discard_message)
                .setPositiveButton(R.string.customize_discard, (dialog, which) -> presenter.discardChanges())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onStopCustomize() {
        setEditMode(false);
        Toast.makeText(requireContext(), R.string.customize_saved, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onChangesDiscarded() {
        setEditMode(false);
    }

    @Override
    public void showDeleteDialog(RemovableDescriptorUi item) {
        String visibleLabel = ((CustomLabelDescriptorUi) item).getVisibleLabel();
        String message = getString(R.string.dialog_delete_message, visibleLabel);
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialog_delete_title)
                .setMessage(message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.dialog_delete_action, (dialog, which) -> {
                    presenter.removeItemImmediate((RemovableDescriptorUi) item);
                })
                .show();
    }

    @Override
    public void onShortcutPinned(Shortcut shortcut) {
        Toast.makeText(requireContext(), getString(R.string.deep_shortcut_pinned, shortcut.getShortLabel()),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onShortcutAlreadyPinnedError(Shortcut shortcut) {
        Toast.makeText(requireContext(), getString(R.string.deep_shortcut_already_pinned, shortcut.getShortLabel()),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReceiveUpdateError(Throwable e) {
        lce.error()
                .button(v -> presenter.reloadApps())
                .message(e.getMessage())
                .show();
    }

    @Override
    public void showSelectFolderDialog(int position, InFolderDescriptorUi item, List<FolderDescriptorUi> folders) {
        if (folders.isEmpty()) {
            errorDelegate.showError(R.string.folder_select_no_folders);
            return;
        }
        View view = list.findViewForAdapterPosition(position);
        Rect bounds = ViewUtils.getViewBoundsInsetPadding(view);
        SelectFolderFragment.newInstance(REQUEST_KEY_APPS, item, folders, bounds)
                .show(getParentFragmentManager());
    }

    @Override
    public void onFolderUpdated(FolderDescriptorUi item, boolean added) {
        String text;
        if (added) {
            text = getString(R.string.folder_select_selected, item.getVisibleLabel());
        } else {
            text = getString(R.string.folder_select_already_in_folder, item.getVisibleLabel());
        }
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showItemRenameDialog(int position, CustomLabelDescriptorUi item) {
        new RenameDescriptorDialog(requireContext(), item.getVisibleLabel(),
                (newLabel) -> presenter.renameItem(item, newLabel))
                .show();
    }

    @Override
    public void showSetItemColorDialog(int position, CustomColorDescriptorUi item) {
        new SetColorDescriptorDialog(requireContext(), item.getVisibleColor(),
                newColor -> presenter.changeItemCustomColor(item, newColor))
                .show();
    }

    @Override
    public void showIntentEditor(IntentDescriptorUi item) {
        editIntentLauncher.launch(item);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Errors
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void showError(Throwable e) {
        errorDelegate.showError(getString(R.string.error));
    }

    ///////////////////////////////////////////////////////////////////////////
    // Popup
    ///////////////////////////////////////////////////////////////////////////

    public void dismissPopups() {
        FragmentManager fragmentManager = getParentFragmentManager();
        if (!fragmentManager.isStateSaved()) {
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // SearchBar
    ///////////////////////////////////////////////////////////////////////////

    private void setupSearchOverlay() {
        searchOverlayBehavior = new SearchOverlayBehavior(searchOverlay, list, new SearchOverlayBehavior.Listener() {
            @Override
            public int calculateMaxOffset() {
                return searchOverlay.searchBarHeight();
            }

            @Override
            public void onShow() {
                searchOverlay.onSearchShown();
                if (preferences.get(Preferences.SEARCH_SHOW_SOFT_KEYBOARD)) {
                    searchOverlay.focusEditText();
                }
            }

            @Override
            public void onHide() {
                searchOverlay.onSearchHidden();
            }

            @Override
            public void onShowExpand() {
                Boolean expandNotifications = preferences.get(Preferences.EXPAND_NOTIFICATIONS);
                if (expandNotifications) {
                    StatusBarUtils.expandStatusBar(requireContext());
                }
            }
        });
        searchOverlayBehavior.setEnabled(!editMode);
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) searchOverlay.getLayoutParams();
        lp.setBehavior(searchOverlayBehavior);
        searchOverlay.setLayoutParams(lp);

        SearchOverlay.Listener listener = new SearchOverlay.Listener() {
            @Override
            public void handleIntent(Intent intent) {
                searchIntentStarterDelegate.handleSearchIntent(intent);
            }

            @Override
            public void handleDescriptorIntent(Intent intent, Descriptor descriptor) {
                searchIntentStarterDelegate.handleSearchIntent(intent, descriptor);
            }

            @Override
            public void onSearchFired() {
                hideSearchOverlayWithDelay();
            }

            @Override
            public void onSearchDismissed() {
                hideSearchOverlayWithDelay();
            }

            @Override
            public void onSearchItemPinClick(Match match) {
                searchOverlayBehavior.hide();
                NameNormalizer nameNormalizer = LauncherApp.daggerService.main().nameNormalizer();
                String label = nameNormalizer.normalize(match.getLabel(requireContext()));
                IntentDescriptor intentDescriptor = new IntentDescriptor(match.getIntent(),
                        label, match.getColor(requireContext()));
                presenter.pinIntent(intentDescriptor);
            }

            @Override
            public void onSearchItemInfoClick(Match match) {
                if (!(match instanceof DescriptorMatch)) {
                    return;
                }
                Descriptor descriptor = ((DescriptorMatch) match).getDescriptor();
                String packageName = DescriptorUtils.getPackageName(descriptor);
                if (packageName != null && IntentUtils.safeStartAppSettings(requireContext(), packageName, null)) {
                    hideSearchOverlayWithDelay();
                }
            }
        };
        searchOverlay.setListener(listener);
        searchOverlay.setupSettings(v -> {
            hideSearchOverlayWithDelay();
            startAppActivity(SettingsActivity.getComponentName(requireContext()), v);
        }, v -> {
            searchOverlayBehavior.hide(presenter::startCustomize);
            return true;
        });

        if (preferences.get(Preferences.SEARCH_SHOW_GLOBAL_SEARCH)) {
            setupGlobalSearchButton();
        }
    }

    private void setupGlobalSearchButton() {
        ComponentName searchActivity = PackageUtils.getGlobalSearchActivity(requireContext());
        if (searchActivity == null) {
            searchOverlay.hideGlobalSearch();
        } else {
            Uri icon = PackageIconHandler.uriFrom(searchActivity.getPackageName());
            searchOverlay.setupGlobalSearch(icon, v -> {
                Intent intent = new Intent().setComponent(searchActivity);
                if (IntentUtils.safeStartActivity(requireContext(), intent)) {
                    hideSearchOverlayWithDelay();
                } else {
                    errorDelegate.showError(R.string.error);
                }
            }, v -> {
                return IntentUtils.safeStartAppSettings(requireContext(), searchActivity.getPackageName(), v);
            });
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Other
    ///////////////////////////////////////////////////////////////////////////

    private void startDrag(int position) {
        if (preferences.get(Preferences.APPS_SORT_MODE) != Preferences.AppsSortMode.MANUAL) {
            errorDelegate.showError(R.string.error_manual_sorting_required);
            return;
        }
        RecyclerView.LayoutManager layoutManager = list.getLayoutManager();
        if (layoutManager == null) {
            return;
        }
        View view = layoutManager.findViewByPosition(position);
        if (view == null) {
            return;
        }
        touchHelper.startDrag(list.getChildViewHolder(view));
    }

    private void hideSearchOverlayWithDelay() {
        hideSearchRunnable = () -> {
            hideSearchRunnable = null;
            searchOverlayBehavior.hide();
        };
        handler.postDelayed(hideSearchRunnable, 1000);
    }

    private void cancelListMotionEvents() {
        MotionEvent e = MotionEvent.obtain(0, 0, MotionEvent.ACTION_CANCEL, 0, 0, 0);
        list.onTouchEvent(e);
        e.recycle();
    }

    private void animateListAppearance() {
        float endY = searchOverlayBehavior.isShown() ? list.getTranslationY() : 0;
        float startY = -endY - getResources().getDimension(R.dimen.list_appearance_translation_offset);
        list.setTranslationY(startY);
        list.setAlpha(0);
        ValueAnimator translationAnimator = ValueAnimator.ofFloat(startY, endY);
        translationAnimator.addUpdateListener(animation -> {
            list.setTranslationY((float) animation.getAnimatedValue());
        });
        ValueAnimator alphaAnimator = ValueAnimator.ofFloat(0, 1);
        alphaAnimator.addUpdateListener(animation -> {
            list.setAlpha(animation.getAnimatedFraction());
        });
        AnimatorSet animations = new AnimatorSet();
        animations.playTogether(translationAnimator, alphaAnimator);
        animations.setInterpolator(new DecelerateInterpolator(3));
        animations.setDuration(ANIM_LIST_APPEARANCE_DURATION);
        if (searchOverlayBehavior.isEnabled()) {
            animations.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                    searchOverlayBehavior.setEnabled(true);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    searchOverlayBehavior.setEnabled(true);
                }
            });
            searchOverlayBehavior.setEnabled(false);
        }
        animations.start();
    }

    private void applyUserPrefs(UserPrefs userPrefs) {
        if (userPrefs.globalSearch && !searchOverlay.isGlobalSearchVisible()) {
            setupGlobalSearchButton();
        } else if (!userPrefs.globalSearch && searchOverlay.isGlobalSearchVisible()) {
            searchOverlay.hideGlobalSearch();
        }
        searchOverlay.setSearchBarSizeDimen(userPrefs.largeSearchBar
                ? R.dimen.search_bar_size_large
                : R.dimen.search_bar_size);
        setLayout(userPrefs.homeLayout, userPrefs.homeAlignment);
        list.setVerticalScrollBarEnabled(userPrefs.showScrollbar);
    }

    private void setItems(Update update) {
        if (adapter == null) {
            adapter = new HomeAdapter.Builder(getContext())
                    .add(new AppDescriptorUiAdapter(this))
                    .add(new IgnorableDescriptorUiAdapter())
                    .add(new FolderDescriptorUiAdapter(this))
                    .add(new PinnedShortcutDescriptorUiAdapter(this))
                    .add(new IntentDescriptorUiAdapter(this))
                    .add(new DeepShortcutDescriptorUiAdapter(this))
                    .recyclerView(list)
                    .setHasStableIds(true)
                    .create();
            adapter.updateUserPrefs(update.userPrefs);
        }
        adapter.setDataset(update.items);
        list.setVisibility(View.VISIBLE);
    }

    private void setLayout(Preferences.HomeLayout layout, Preferences.HomeAlignment homeAlignment) {
        if (layout != this.layout) {
            this.layout = layout;
            list.setLayoutManager(new FlexboxLayoutManager(requireContext(), FlexDirection.ROW));
        }
        if (list.getLayoutManager() instanceof FlexboxLayoutManager) {
            FlexboxLayoutManager lm = (FlexboxLayoutManager) list.getLayoutManager();
            switch (homeAlignment) {
                case START:
                    lm.setJustifyContent(JustifyContent.FLEX_START);
                    break;
                case CENTER:
                    lm.setJustifyContent(JustifyContent.CENTER);
                    break;
                case END:
                    lm.setJustifyContent(JustifyContent.FLEX_END);
                    break;
            }
        }
    }

    private void scrollToTop() {
        if (preferences.get(Preferences.SMOOTH_SCROLL_TO_TOP)) {
            list.smoothScrollToPosition(0);
        } else {
            list.post(() -> list.scrollToPosition(0));
        }
    }

    private void registerWindowInsets(View view) {
        view.setOnApplyWindowInsetsListener((v, insets) -> {
            list.setBottomInset(insets.getStableInsetBottom());
            return insets;
        });

        WindowInsets insets = view.getRootWindowInsets();
        if (insets != null) {
            list.setBottomInset(insets.getStableInsetBottom());
        }
    }
}
