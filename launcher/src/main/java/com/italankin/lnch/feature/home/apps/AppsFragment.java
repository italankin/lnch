package com.italankin.lnch.feature.home.apps;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.api.LauncherIntents;
import com.italankin.lnch.api.LauncherShortcuts;
import com.italankin.lnch.feature.base.AppFragment;
import com.italankin.lnch.feature.base.BackButtonHandler;
import com.italankin.lnch.feature.home.adapter.AppViewModelAdapter;
import com.italankin.lnch.feature.home.adapter.DeepShortcutViewModelAdapter;
import com.italankin.lnch.feature.home.adapter.GroupViewModelAdapter;
import com.italankin.lnch.feature.home.adapter.HiddenAppViewModelAdapter;
import com.italankin.lnch.feature.home.adapter.HomeAdapter;
import com.italankin.lnch.feature.home.adapter.IntentViewModelAdapter;
import com.italankin.lnch.feature.home.adapter.PinnedShortcutViewModelAdapter;
import com.italankin.lnch.feature.home.behavior.SearchBarBehavior;
import com.italankin.lnch.feature.home.model.Update;
import com.italankin.lnch.feature.home.model.UserPrefs;
import com.italankin.lnch.feature.home.util.IntentQueue;
import com.italankin.lnch.feature.home.util.SwapItemHelper;
import com.italankin.lnch.feature.home.widget.EditModePanel;
import com.italankin.lnch.feature.home.widget.HomeRecyclerView;
import com.italankin.lnch.feature.home.widget.SearchBar;
import com.italankin.lnch.feature.settings.SettingsActivity;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.IntentDescriptor;
import com.italankin.lnch.model.repository.descriptor.NameNormalizer;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.search.match.DescriptorMatch;
import com.italankin.lnch.model.repository.search.match.Match;
import com.italankin.lnch.model.repository.shortcuts.Shortcut;
import com.italankin.lnch.model.viewmodel.CustomColorItem;
import com.italankin.lnch.model.viewmodel.CustomLabelItem;
import com.italankin.lnch.model.viewmodel.DescriptorItem;
import com.italankin.lnch.model.viewmodel.HiddenItem;
import com.italankin.lnch.model.viewmodel.RemovableItem;
import com.italankin.lnch.model.viewmodel.VisibleItem;
import com.italankin.lnch.model.viewmodel.impl.AppViewModel;
import com.italankin.lnch.model.viewmodel.impl.DeepShortcutViewModel;
import com.italankin.lnch.model.viewmodel.impl.GroupViewModel;
import com.italankin.lnch.model.viewmodel.impl.IntentViewModel;
import com.italankin.lnch.model.viewmodel.impl.PinnedShortcutViewModel;
import com.italankin.lnch.util.DescriptorUtils;
import com.italankin.lnch.util.IntentUtils;
import com.italankin.lnch.util.PackageUtils;
import com.italankin.lnch.util.ResUtils;
import com.italankin.lnch.util.StatusBarUtils;
import com.italankin.lnch.util.ViewUtils;
import com.italankin.lnch.util.picasso.PackageIconHandler;
import com.italankin.lnch.util.widget.ActionPopupWindow;
import com.italankin.lnch.util.widget.EditTextAlertDialog;
import com.italankin.lnch.util.widget.LceLayout;
import com.italankin.lnch.util.widget.colorpicker.ColorPickerDialog;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class AppsFragment extends AppFragment implements AppsView,
        BackButtonHandler,
        IntentQueue.OnIntentAction,
        DeepShortcutViewModelAdapter.Listener,
        IntentViewModelAdapter.Listener,
        AppViewModelAdapter.Listener,
        GroupViewModelAdapter.Listener,
        PinnedShortcutViewModelAdapter.Listener {

    private static final int ANIM_LIST_APPEARANCE_DURATION = 400;

    @InjectPresenter
    AppsPresenter presenter;

    private Preferences preferences;
    private Picasso picasso;
    private IntentQueue intentQueue;

    private LceLayout lce;
    private HomeRecyclerView list;
    private HomeAdapter adapter;
    private EditModePanel editModePanel;
    private SearchBar searchBar;
    private CoordinatorLayout coordinator;

    private PopupWindow popupWindow;
    private SearchBarBehavior searchBarBehavior;

    private ItemTouchHelper touchHelper;
    private Preferences.HomeLayout layout;

    private boolean animateOnResume;
    private boolean editMode;

    private Callbacks callbacks;

    @ProvidePresenter
    AppsPresenter providePresenter() {
        return LauncherApp.daggerService.presenters().apps();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        preferences = LauncherApp.daggerService.main().getPreferences();
        picasso = LauncherApp.daggerService.main().getPicassoFactory().create(requireContext());
        intentQueue = LauncherApp.daggerService.main().getIntentQueue();
        callbacks = (Callbacks) context;
    }

    @Override
    public void onStart() {
        super.onStart();
        animateOnResume = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (animateOnResume) {
            animateOnResume = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                animateListAppearance();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        searchBarBehavior.hide();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbacks = null;
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
        searchBar = view.findViewById(R.id.search_bar);
        list = view.findViewById(R.id.list);
        coordinator = view.findViewById(R.id.coordinator);

        setupSearchBar();

        touchHelper = new ItemTouchHelper(new SwapItemHelper(presenter::swapApps));
        touchHelper.attachToRecyclerView(list);

        registerWindowInsets(view);

        intentQueue.registerOnIntentAction(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        intentQueue.unregisterOnIntentAction(this);
    }

    @Override
    public boolean onIntent(Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            return false;
        }
        switch (action) {
            case Intent.ACTION_MAIN: {
                dismissPopup();
                if (searchBarBehavior.isShown()) {
                    searchBar.reset();
                    searchBarBehavior.hide();
                } else if (preferences.get(Preferences.SCROLL_TO_TOP)) {
                    list.smoothScrollToPosition(0);
                }
                return true;
            }
            case LauncherIntents.ACTION_EDIT_MODE: {
                if (!editMode) {
                    animateOnResume = false;
                    presenter.startCustomize();
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void onReceiveUpdate(Update update) {
        setItems(update.items);
        if (update.items.isEmpty()) {
            lce.empty()
                    .message(R.string.apps_list_empty)
                    .button(R.string.open_settings, this::startLnchSettings)
                    .show();
        } else {
            lce.showContent();
        }
        dismissPopup();

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
        if (dismissPopup()) {
            return true;
        }
        if (editMode) {
            presenter.confirmDiscardChanges();
            return true;
        }
        if (searchBarBehavior.isShown()) {
            searchBarBehavior.hide();
        } else {
            list.smoothScrollToPosition(0);
        }
        return true;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Start
    ///////////////////////////////////////////////////////////////////////////

    private void startApp(int position, AppViewModel item) {
        searchBarBehavior.hide();
        ComponentName componentName = DescriptorUtils.getComponentName(requireContext(), item.getDescriptor());
        if (componentName != null) {
            View view = list.findViewForAdapterPosition(position);
            if (startAppActivity(componentName, view)) {
                return;
            }
        }
        showError(R.string.error);
    }

    private boolean startAppActivity(ComponentName componentName, View view) {
        Rect bounds = ViewUtils.getViewBounds(view);
        Bundle opts = IntentUtils.getActivityLaunchOptions(view, bounds);
        return IntentUtils.safeStartMainActivity(requireContext(), componentName, bounds, opts);
    }

    private boolean startAppSettings(String packageName, @Nullable View view) {
        return IntentUtils.safeStartAppSettings(requireContext(), packageName, view);
    }

    private void startLnchSettings(View view) {
        startAppActivity(SettingsActivity.getComponentName(requireContext()), view);
    }

    private void startAppUninstall(AppViewModel item) {
        Intent intent = PackageUtils.getUninstallIntent(item.packageName);
        if (!IntentUtils.safeStartActivity(requireContext(), intent)) {
            showError(R.string.error);
        }
    }

    private void startShortcut(PinnedShortcutViewModel item) {
        Intent intent = IntentUtils.fromUri(item.uri);
        if (!IntentUtils.safeStartActivity(requireContext(), intent)) {
            showError(R.string.error);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Adapter callbacks
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onAppClick(int position, AppViewModel item) {
        if (editMode) {
            showCustomizePopup(position, item);
        } else {
            startApp(position, item);
        }
    }

    @Override
    public void onAppLongClick(int position, AppViewModel item) {
        if (editMode) {
            startDrag(position);
        } else {
            switch (preferences.get(Preferences.APP_LONG_CLICK_ACTION)) {
                case INFO:
                    View view = list.findViewForAdapterPosition(position);
                    startAppSettings(item.packageName, view);
                    break;
                case POPUP:
                default:
                    presenter.showAppPopup(position, item);
                    break;
            }
        }
    }

    @Override
    public void onGroupClick(int position, GroupViewModel item) {
        if (editMode) {
            showCustomizePopup(position, item);
        } else {
            presenter.toggleExpandableItemState(position, item);
        }
    }

    @Override
    public void onGroupLongClick(int position, GroupViewModel item) {
        if (editMode) {
            startDrag(position);
        } else {
            showItemPopup(position, item);
        }
    }

    @Override
    public void onPinnedShortcutClick(int position, PinnedShortcutViewModel item) {
        if (editMode) {
            showCustomizePopup(position, item);
        } else {
            startShortcut(item);
        }
    }

    @Override
    public void onPinnedShortcutLongClick(int position, PinnedShortcutViewModel item) {
        if (editMode) {
            startDrag(position);
        } else {
            showItemPopup(position, item);
        }
    }

    @Override
    public void onDeepShortcutClick(int position, DeepShortcutViewModel item) {
        if (editMode) {
            showCustomizePopup(position, item);
        } else {
            presenter.startShortcut(position, item);
        }
    }

    @Override
    public void onDeepShortcutLongClick(int position, DeepShortcutViewModel item) {
        if (editMode) {
            startDrag(position);
        } else {
            showItemPopup(position, item);
        }
    }

    @Override
    public void onIntentClick(int position, IntentViewModel item) {
        if (editMode) {
            showCustomizePopup(position, item);
        } else {
            handleSearchIntent(item.intent);
        }
    }

    @Override
    public void onIntentLongClick(int position, IntentViewModel item) {
        if (editMode) {
            startDrag(position);
        } else {
            showItemPopup(position, item);
        }
    }

    private void setEditMode(boolean value) {
        if (editMode == value) {
            return;
        }
        editMode = value;
        searchBarBehavior.hide();
        searchBarBehavior.setEnabled(!value);
        if (value) {
            searchBar.hideSoftKeyboard();
            editModePanel = new EditModePanel(requireContext())
                    .setMessage(R.string.customize_hint)
                    .setOnSaveActionClickListener(v -> {
                        if (editModePanel != null && editModePanel.isShown()) {
                            presenter.stopCustomize();
                        }
                    })
                    .show(coordinator);
        } else if (editModePanel != null) {
            editModePanel.dismiss();
            editModePanel = null;
        }
    }

    private void showCustomizePopup(int position, DescriptorItem item) {
        Context context = requireContext();
        ActionPopupWindow popup = new ActionPopupWindow(context, picasso);
        if (item instanceof HiddenItem) {
            popup.addAction(new ActionPopupWindow.ItemBuilder(context)
                    .setIcon(R.drawable.ic_action_hide)
                    .setOnClickListener(v -> presenter.hideItem(position, (HiddenItem) item))
            );
        }
        if (item instanceof CustomLabelItem) {
            popup.addShortcut(new ActionPopupWindow.ItemBuilder(context)
                    .setLabel(R.string.customize_item_rename)
                    .setIcon(R.drawable.ic_action_rename)
                    .setIconDrawableTintAttr(R.attr.colorAccent)
                    .setOnClickListener(v -> setItemCustomLabel(position, (CustomLabelItem) item))
            );
        }
        if (item instanceof CustomColorItem) {
            popup.addShortcut(new ActionPopupWindow.ItemBuilder(context)
                    .setLabel(R.string.customize_item_color)
                    .setEnabled(!preferences.get(Preferences.APPS_COLOR_OVERLAY_SHOW))
                    .setIcon(R.drawable.ic_action_color)
                    .setIconDrawableTintAttr(R.attr.colorAccent)
                    .setOnClickListener(v -> setItemColor(position, (CustomColorItem) item))
            );
        }
        if (item instanceof VisibleItem) {
            popup.addShortcut(new ActionPopupWindow.ItemBuilder(context)
                    .setLabel(R.string.customize_item_add_group)
                    .setIcon(R.drawable.ic_action_add_group)
                    .setIconDrawableTintAttr(R.attr.colorAccent)
                    .setOnClickListener(v -> {
                        presenter.addGroup(position, getString(R.string.new_group_label),
                                ResUtils.resolveColor(requireContext(), R.attr.colorGroupTitleDefault));
                    })
            );
        }
        if (item instanceof RemovableItem) {
            popup.addAction(new ActionPopupWindow.ItemBuilder(context)
                    .setIcon(R.drawable.ic_action_delete)
                    .setOnClickListener(v -> presenter.removeItem(position, item))
            );
        }
        View view = list.findViewForAdapterPosition(position);
        showPopupWindow(popup, view);
    }

    private void setItemCustomLabel(int position, CustomLabelItem item) {
        String visibleLabel = item.getVisibleLabel();
        EditTextAlertDialog.builder(requireContext())
                .setTitle(item.getVisibleLabel())
                .customizeEditText(editText -> {
                    editText.setText(visibleLabel);
                    editText.setSingleLine(true);
                    editText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
                    editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                    if (visibleLabel != null) {
                        editText.setSelection(visibleLabel.length());
                    }
                })
                .setPositiveButton(R.string.ok, (dialog, editText) -> {
                    String newLabel = editText.getText().toString().trim();
                    if (!newLabel.equals(visibleLabel)) {
                        presenter.renameItem(position, item, newLabel);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .setNeutralButton(R.string.customize_action_reset, (dialog, which) -> {
                    presenter.renameItem(position, item, "");
                })
                .setCancellable(false)
                .show();
    }

    private void setItemColor(int position, CustomColorItem item) {
        int visibleColor = item.getVisibleColor();
        ColorPickerDialog.builder(requireContext())
                .setHexVisible(false)
                .setSelectedColor(visibleColor)
                .setOnColorPickedListener(color -> {
                    if (color != visibleColor) {
                        presenter.changeItemCustomColor(position, item, color);
                    }
                })
                .setResetButton(getString(R.string.customize_action_reset), (dialog, which) -> {
                    presenter.changeItemCustomColor(position, item, null);
                })
                .setCancellable(false)
                .show();
    }

    private void showItemPopup(int position, DescriptorItem item) {
        Context context = requireContext();
        ActionPopupWindow popup = new ActionPopupWindow(context, picasso);
        if (item instanceof DeepShortcutViewModel) {
            popup.addShortcut(new ActionPopupWindow.ItemBuilder(context)
                    .setIcon(R.drawable.ic_app_info)
                    .setIconDrawableTintAttr(R.attr.colorAccent)
                    .setLabel(R.string.popup_app_info)
                    .setOnClickListener(v -> {
                        startAppSettings(((DeepShortcutViewModel) item).packageName, v);
                    })
            );
        }
        if (item instanceof RemovableItem) {
            popup.addShortcut(new ActionPopupWindow.ItemBuilder(context)
                    .setIcon(R.drawable.ic_action_delete)
                    .setIconDrawableTintAttr(R.attr.colorAccent)
                    .setLabel(R.string.customize_item_delete)
                    .setOnClickListener(v -> {
                        String visibleLabel = ((CustomLabelItem) item).getVisibleLabel();
                        String message = getString(R.string.popup_delete_message, visibleLabel);
                        new AlertDialog.Builder(requireContext())
                                .setTitle(R.string.popup_delete_title)
                                .setMessage(message)
                                .setNegativeButton(R.string.cancel, null)
                                .setPositiveButton(R.string.popup_delete_action, (dialog, which) -> {
                                    presenter.removeItemImmediate(position, item);
                                })
                                .show();
                    })
            );
        }
        View view = list.findViewForAdapterPosition(position);
        showPopupWindow(popup, view);
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
    public void onShortcutPinned(Shortcut shortcut) {
        Toast.makeText(requireContext(), getString(R.string.deep_shortcut_pinned, shortcut.getShortLabel()),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void startShortcut(int position, Shortcut shortcut) {
        if (handleCustomizeShortcut(shortcut.getPackageName(), shortcut.getId())) {
            return;
        }
        if (!shortcut.isEnabled()) {
            onShortcutDisabled(shortcut.getDisabledMessage());
            return;
        }
        View view = list.findViewForAdapterPosition(position);
        Rect bounds = ViewUtils.getViewBounds(view);
        Bundle opts = IntentUtils.getActivityLaunchOptions(view, bounds);
        if (!shortcut.start(bounds, opts)) {
            showError(R.string.error);
        }
    }

    @Override
    public void onItemsSwap(int from, int to) {
        adapter.notifyItemMoved(from, to);
    }

    @Override
    public void onItemChanged(int position) {
        adapter.notifyItemChanged(position);
    }

    @Override
    public void onItemInserted(int position) {
        adapter.notifyItemInserted(position);
    }

    @Override
    public void onItemsInserted(int startIndex, int count) {
        adapter.notifyItemRangeInserted(startIndex, count);
        list.scrollToPosition(startIndex + Math.min(1, count));
    }

    @Override
    public void onItemsRemoved(int startIndex, int count) {
        adapter.notifyItemRangeRemoved(startIndex, count);
    }

    @Override
    public void onShortcutNotFound() {
        showError(R.string.error_shortcut_not_found);
    }

    @Override
    public void onShortcutDisabled(CharSequence disabledMessage) {
        CharSequence message = TextUtils.isEmpty(disabledMessage)
                ? getText(R.string.error_shortcut_disabled)
                : disabledMessage;
        showErrorToast(message);
    }

    @Override
    public void onReceiveUpdateError(Throwable e) {
        lce.error()
                .button(v -> presenter.reloadApps())
                .message(e.getMessage())
                .show();
    }

    @Override
    public void showAppPopup(int position, AppViewModel item, List<Shortcut> shortcuts) {
        Context context = requireContext();
        boolean uninstallAvailable = !PackageUtils.isSystem(context.getPackageManager(), item.packageName);
        ActionPopupWindow.ItemBuilder infoItem = new ActionPopupWindow.ItemBuilder(context)
                .setLabel(R.string.popup_app_info)
                .setIcon(R.drawable.ic_app_info)
                .setOnClickListener(v -> startAppSettings(item.packageName, v));
        ActionPopupWindow.ItemBuilder uninstallItem = new ActionPopupWindow.ItemBuilder(context)
                .setLabel(R.string.popup_app_uninstall)
                .setIcon(R.drawable.ic_action_delete)
                .setOnClickListener(v -> startAppUninstall(item));

        ActionPopupWindow popup = new ActionPopupWindow(context, picasso);
        if (shortcuts.isEmpty()) {
            popup.addShortcut(infoItem.setIconDrawableTintAttr(R.attr.colorAccent));
            if (uninstallAvailable) {
                popup.addShortcut(uninstallItem.setIconDrawableTintAttr(R.attr.colorAccent));
            }
        } else {
            popup.addAction(infoItem);
            if (uninstallAvailable) {
                popup.addAction(uninstallItem);
            }
            for (Shortcut shortcut : shortcuts) {
                popup.addShortcut(new ActionPopupWindow.ItemBuilder(context)
                        .setLabel(shortcut.getShortLabel())
                        .setIcon(shortcut.getIconUri())
                        .setEnabled(shortcut.isEnabled())
                        .setOnClickListener(v -> {
                            if (handleCustomizeShortcut(shortcut.getPackageName(), shortcut.getId())) {
                                return;
                            }
                            if (!shortcut.isEnabled()) {
                                onShortcutDisabled(shortcut.getDisabledMessage());
                            } else {
                                Rect bounds = ViewUtils.getViewBounds(v);
                                Bundle opts = IntentUtils.getActivityLaunchOptions(v, bounds);
                                if (!shortcut.start(bounds, opts)) {
                                    showError(R.string.error);
                                    presenter.updateShortcuts(item.getDescriptor());
                                }
                            }
                        })
                        .setOnPinClickListener(v -> presenter.pinShortcut(shortcut))
                );
            }
        }
        View view = list.findViewForAdapterPosition(position);
        showPopupWindow(popup, view);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Handle intents
    ///////////////////////////////////////////////////////////////////////////

    private void handleSearchIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        Context context = requireContext();
        if (preferences.get(Preferences.SEARCH_USE_CUSTOM_TABS)
                && Intent.ACTION_VIEW.equals(intent.getAction())
                && intent.getData() != null) {
            CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
                    .setToolbarColor(ResUtils.resolveColor(context, R.attr.colorPrimary))
                    .addDefaultShareMenuItem()
                    .setShowTitle(true)
                    .build();
            customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (IntentUtils.canHandleIntent(context, customTabsIntent.intent)) {
                customTabsIntent.launchUrl(context, intent.getData());
            }
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 &&
                LauncherIntents.ACTION_START_SHORTCUT.equals(intent.getAction())) {
            String packageName = intent.getStringExtra(LauncherIntents.EXTRA_PACKAGE_NAME);
            String shortcutId = intent.getStringExtra(LauncherIntents.EXTRA_SHORTCUT_ID);
            if (!handleCustomizeShortcut(packageName, shortcutId)) {
                // start deep shortcut
                if (callbacks != null) {
                    callbacks.sendBroadcast(intent);
                }
            }
            return;
        }
        if (!IntentUtils.safeStartActivity(context, intent)) {
            showError(R.string.error);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Errors
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void showError(Throwable e) {
        showErrorToast(getString(R.string.error));
    }

    private void showErrorToast(CharSequence message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void showError(@StringRes int message) {
        showErrorToast(getText(message));
    }

    ///////////////////////////////////////////////////////////////////////////
    // Popup
    ///////////////////////////////////////////////////////////////////////////

    private void showPopupWindow(ActionPopupWindow popup, @Nullable View anchor) {
        if (anchor == null) {
            return;
        }
        dismissPopup();
        list.suppressLayout(true);
        popup.setOnDismissListener(() -> list.suppressLayout(false));
        Rect bounds = new Rect();
        lce.getWindowVisibleDisplayFrame(bounds);
        popup.showAtAnchor(anchor, bounds);
        popupWindow = popup;
    }

    private boolean dismissPopup() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
            popupWindow = null;
            return true;
        }
        return false;
    }

    ///////////////////////////////////////////////////////////////////////////
    // SearchBar
    ///////////////////////////////////////////////////////////////////////////

    private void setupSearchBar() {
        searchBarBehavior = new SearchBarBehavior(searchBar, list, new SearchBarBehavior.Listener() {
            @Override
            public void onShow() {
                if (preferences.get(Preferences.SEARCH_SHOW_SOFT_KEYBOARD)) {
                    searchBar.focusEditText();
                }
            }

            @Override
            public void onHide() {
                searchBar.reset();
            }

            @Override
            public void onShowExpand() {
                Boolean expandNotifications = preferences.get(Preferences.EXPAND_NOTIFICATIONS);
                if (expandNotifications) {
                    StatusBarUtils.expandStatusBar(requireContext());
                }
            }
        });
        searchBarBehavior.setEnabled(!editMode);
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) searchBar.getLayoutParams();
        lp.setBehavior(searchBarBehavior);
        searchBar.setLayoutParams(lp);

        SearchBar.Listener listener = new SearchBar.Listener() {
            @Override
            public void handleIntent(Intent intent) {
                handleSearchIntent(intent);
            }

            @Override
            public void onSearchFired() {
                searchBarBehavior.hide();
            }

            @Override
            public void onSearchItemPinClick(Match match) {
                searchBarBehavior.hide();
                NameNormalizer nameNormalizer = LauncherApp.daggerService.main().getNameNormalizer();
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
                if (packageName != null && startAppSettings(packageName, null)) {
                    searchBarBehavior.hide();
                }
            }
        };
        searchBar.setListener(listener);
        searchBar.setupSettings(v -> {
            searchBarBehavior.hide();
            startAppActivity(SettingsActivity.getComponentName(requireContext()), v);
        }, v -> {
            searchBarBehavior.hide(presenter::startCustomize);
            return true;
        });

        if (preferences.get(Preferences.SEARCH_SHOW_GLOBAL_SEARCH)) {
            setupGlobalSearchButton();
        }
    }

    private void setupGlobalSearchButton() {
        ComponentName searchActivity = PackageUtils.getGlobalSearchActivity(requireContext());
        if (searchActivity == null) {
            searchBar.hideGlobalSearch();
        } else {
            Uri icon = PackageIconHandler.uriFrom(searchActivity.getPackageName());
            searchBar.setupGlobalSearch(icon, v -> {
                Intent intent = new Intent().setComponent(searchActivity);
                if (IntentUtils.safeStartActivity(requireContext(), intent)) {
                    searchBarBehavior.hide();
                } else {
                    showError(R.string.error);
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
            showError(R.string.error_manual_sorting_required);
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

    private boolean handleCustomizeShortcut(String packageName, String shortcutId) {
        if (requireContext().getPackageName().equals(packageName)
                && LauncherShortcuts.ID_SHORTCUT_CUSTOMIZE.equals(shortcutId)) {
            presenter.startCustomize();
            return true;
        }
        return false;
    }

    private void animateListAppearance() {
        float endY = searchBarBehavior.isShown() ? list.getTranslationY() : 0;
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
        if (searchBarBehavior.isEnabled()) {
            animations.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                    searchBarBehavior.setEnabled(true);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    searchBarBehavior.setEnabled(true);
                }
            });
            searchBarBehavior.setEnabled(false);
        }
        animations.start();
    }

    private void applyUserPrefs(UserPrefs userPrefs) {
        if (userPrefs.globalSearch && !searchBar.isGlobalSearchVisible()) {
            setupGlobalSearchButton();
        } else if (!userPrefs.globalSearch && searchBar.isGlobalSearchVisible()) {
            searchBar.hideGlobalSearch();
        }
        searchBar.setSearchBarSizeDimen(userPrefs.largeSearchBar
                ? R.dimen.search_bar_size_large
                : R.dimen.search_bar_size);
        setLayout(userPrefs.homeLayout, userPrefs.homeAlignment);
        list.setVerticalScrollBarEnabled(userPrefs.showScrollbar);
    }

    private void setItems(List<DescriptorItem> items) {
        if (adapter == null) {
            adapter = new HomeAdapter.Builder(getContext())
                    .add(new AppViewModelAdapter(this))
                    .add(new HiddenAppViewModelAdapter())
                    .add(new GroupViewModelAdapter(this))
                    .add(new PinnedShortcutViewModelAdapter(this))
                    .add(new IntentViewModelAdapter(this))
                    .add(new DeepShortcutViewModelAdapter(this))
                    .recyclerView(list)
                    .setHasStableIds(true)
                    .create();
        }
        adapter.setDataset(items);
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

    public interface Callbacks {

        void sendBroadcast(Intent intent);
    }
}
