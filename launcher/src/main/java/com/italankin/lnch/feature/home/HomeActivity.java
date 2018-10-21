package com.italankin.lnch.feature.home;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.italankin.lnch.R;
import com.italankin.lnch.di.component.MainComponent;
import com.italankin.lnch.feature.base.AppActivity;
import com.italankin.lnch.feature.home.adapter.AppViewModelAdapter;
import com.italankin.lnch.feature.home.adapter.DeepShortcutViewModelAdapter;
import com.italankin.lnch.feature.home.adapter.GroupViewModelAdapter;
import com.italankin.lnch.feature.home.adapter.HiddenAppViewModelAdapter;
import com.italankin.lnch.feature.home.adapter.PinnedShortcutViewModelAdapter;
import com.italankin.lnch.feature.home.adapter.SearchAdapter;
import com.italankin.lnch.feature.home.model.UserPrefs;
import com.italankin.lnch.feature.home.util.SwapItemHelper;
import com.italankin.lnch.feature.home.util.TopBarBehavior;
import com.italankin.lnch.feature.receiver.StartShortcutReceiver;
import com.italankin.lnch.feature.settings_root.SettingsActivity;
import com.italankin.lnch.model.repository.prefs.Preferences;
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
import com.italankin.lnch.model.viewmodel.impl.PinnedShortcutViewModel;
import com.italankin.lnch.util.IntentUtils;
import com.italankin.lnch.util.PackageUtils;
import com.italankin.lnch.util.adapterdelegate.CompositeAdapter;
import com.italankin.lnch.util.widget.ActionPopupWindow;
import com.italankin.lnch.util.widget.EditTextAlertDialog;
import com.italankin.lnch.util.widget.LceLayout;
import com.italankin.lnch.util.widget.colorpicker.ColorPickerDialog;
import com.squareup.picasso.Picasso;

import java.util.List;

public class HomeActivity extends AppActivity implements HomeView,
        SwapItemHelper.Callback,
        AppViewModelAdapter.Listener,
        GroupViewModelAdapter.Listener,
        PinnedShortcutViewModelAdapter.Listener, DeepShortcutViewModelAdapter.Listener {

    private static final String KEY_SEARCH_SHOWN = "SEARCH_SHOWN";
    private static final int REQUEST_CODE_SETTINGS = 1;

    private static final int ANIM_LIST_APPEARANCE_DURATION = 500;

    @InjectPresenter
    HomePresenter presenter;

    private LceLayout root;
    private ViewGroup searchBar;
    private AutoCompleteTextView editSearch;
    private View btnSettings;
    private RecyclerView list;

    private InputMethodManager inputMethodManager;
    private PackageManager packageManager;
    private Preferences preferences;
    private Picasso picasso;

    private boolean editMode = false;

    private TopBarBehavior searchBarBehavior;
    private ItemTouchHelper touchHelper;
    private Preferences.HomeLayout layout;
    private Snackbar editModeSnackbar;
    private PopupWindow popupWindow;

    @ProvidePresenter
    HomePresenter providePresenter() {
        return daggerService().presenters().home();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        packageManager = getPackageManager();
        preferences = daggerService().main().getPreferences();
        picasso = daggerService().main().getPicassoFactory().create(this);

        setupWindow();

        setContentView(R.layout.activity_home);
        root = findViewById(R.id.root);
        list = findViewById(R.id.list);
        searchBar = findViewById(R.id.search_bar);
        editSearch = findViewById(R.id.edit_search);
        btnSettings = findViewById(R.id.btn_settings);

        setupRoot();
        setupList();
        setupSearchBar();

        if (savedInstanceState != null && savedInstanceState.getBoolean(KEY_SEARCH_SHOWN, false)) {
            searchBarBehavior.showNow();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        animateListAppearance();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_SEARCH_SHOWN, searchBarBehavior.isShown());
    }

    @Override
    public void onBackPressed() {
        if (dismissPopup()) {
            return;
        }
        if (editMode) {
            presenter.confirmDiscardChanges();
            return;
        }
        if (searchBarBehavior.isShown()) {
            searchBarBehavior.hide();
        } else {
            list.smoothScrollToPosition(0);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String action = intent.getAction();
        if (action == null) {
            return;
        }
        switch (action) {
            case Intent.ACTION_MAIN: {
                dismissPopup();
                if (searchBarBehavior.isShown()) {
                    searchBarBehavior.hide();
                } else {
                    list.smoothScrollToPosition(0);
                }
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_SETTINGS) {
            switch (resultCode) {
                case SettingsActivity.RESULT_EDIT_MODE:
                    presenter.startCustomize();
                    return;
                default:
                    presenter.reloadAppsImmediate();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setupWindow() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER,
                WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
    }

    private void setupList() {
        touchHelper = new ItemTouchHelper(new SwapItemHelper(this));
        touchHelper.attachToRecyclerView(list);
    }

    private void setupRoot() {
        root.setBackgroundColor(preferences.overlayColor());
        root.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    private void setupSearchBar() {
        int maxOffset = getResources().getDimensionPixelSize(R.dimen.searchbar_size);
        searchBarBehavior = new TopBarBehavior(searchBar, list, maxOffset, new TopBarBehavior.Listener() {
            @Override
            public void onShow() {
                if (preferences.searchShowSoftKeyboard()) {
                    editSearch.requestFocus();
                }
                inputMethodManager.showSoftInput(editSearch, 0);
            }

            @Override
            public void onHide() {
                editSearch.setText("");
                inputMethodManager.hideSoftInputFromWindow(editSearch.getWindowToken(), 0);
                editSearch.clearFocus();
            }
        });
        searchBarBehavior.setEnabled(!editMode);
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) searchBar.getLayoutParams();
        lp.setBehavior(searchBarBehavior);
        searchBar.setLayoutParams(lp);

        editSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                onFireSearch(0);
            }
            return true;
        });
        editSearch.setThreshold(1);
        MainComponent mainComponent = daggerService().main();
        SearchAdapter.Listener listener = new SearchAdapter.Listener() {
            @Override
            public void onItemClick(int position, Match match) {
                onFireSearch(position);
            }

            @Override
            public void onItemLongClick(int position, Match match) {
                String packageName = match.getIntent().getPackage();
                if (packageName == null) {
                    return;
                }
                Intent intent = IntentUtils.getPackageSystemSettings(packageName);
                IntentUtils.safeStartActivity(HomeActivity.this, intent);
                if (!IntentUtils.safeStartActivity(HomeActivity.this, intent)) {
                    showError(R.string.error);
                    return;
                }
                searchBarBehavior.hide();
            }
        };
        editSearch.setAdapter(new SearchAdapter(picasso, mainComponent.getSearchRepository(), listener));

        btnSettings.setOnClickListener(v -> {
            searchBarBehavior.hide();
            Intent intent = SettingsActivity.getStartIntent(this);
            startActivityForResult(intent, REQUEST_CODE_SETTINGS);
        });
        btnSettings.setOnLongClickListener(v -> {
            presenter.startCustomize();
            return true;
        });
    }

    @Override
    public void showProgress() {
        root.showLoading();
    }

    @Override
    public void onAppsLoaded(List<DescriptorItem> items, UserPrefs userPrefs) {
        new CompositeAdapter.Builder<DescriptorItem>(this)
                .add(new AppViewModelAdapter(userPrefs, this))
                .add(new HiddenAppViewModelAdapter())
                .add(new GroupViewModelAdapter(userPrefs, this))
                .add(new PinnedShortcutViewModelAdapter(userPrefs, this))
                .add(new DeepShortcutViewModelAdapter(userPrefs, this))
                .dataset(items)
                .recyclerView(list)
                .setHasStableIds(true)
                .create();
        applyUserPrefs(userPrefs);
        list.setVisibility(View.VISIBLE);
        root.showContent();
        dismissPopup();
    }

    @Override
    public void onStartCustomize() {
        setEditMode(true);
    }

    @Override
    public void onConfirmDiscardChanges() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.customize_discard_message)
                .setPositiveButton(R.string.customize_discard, (dialog, which) -> presenter.discardChanges())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onStopCustomize() {
        setEditMode(false);
        Toast.makeText(this, R.string.customize_saved, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onChangesDiscarded() {
        setEditMode(false);
    }

    @Override
    public void onShortcutPinned(Shortcut shortcut) {
        Toast.makeText(this, getString(R.string.deep_shortcut_pinned, shortcut.getShortLabel()),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void startShortcut(Shortcut shortcut) {
        if (!shortcut.start(null, null)) {
            showError(R.string.error);
        }
    }

    @Override
    public void onItemsSwap(int from, int to) {
        list.getAdapter().notifyItemMoved(from, to);
    }

    @Override
    public void onItemChanged(int position) {
        list.getAdapter().notifyItemChanged(position);
    }

    @Override
    public void onItemInserted(int position) {
        list.getAdapter().notifyItemInserted(position);
    }

    @Override
    public void onItemsInserted(int startIndex, int count) {
        list.getAdapter().notifyItemRangeInserted(startIndex, count);
        list.smoothScrollToPosition(startIndex + Math.min(1, count));
    }

    @Override
    public void onItemsRemoved(int startIndex, int count) {
        list.getAdapter().notifyItemRangeRemoved(startIndex, count);
    }

    @Override
    public void onShortcutNotFound() {
        showError(R.string.error_shortcut_not_found);
    }

    @Override
    public void onAppsLoadError(Throwable e) {
        root.error()
                .button(v -> presenter.reloadApps())
                .message(e.getMessage())
                .show();
    }

    @Override
    public void showError(Throwable e) {
        showErrorToast(e.getMessage());
    }

    @Override
    public void showAppPopup(int position, AppViewModel item, List<Shortcut> shortcuts) {
        boolean uninstallAvailable = !PackageUtils.isSystem(packageManager, item.packageName);
        ActionPopupWindow.ItemBuilder infoItem = new ActionPopupWindow.ItemBuilder(this)
                .setLabel(R.string.popup_app_info)
                .setIcon(R.drawable.ic_app_info)
                .setOnClickListener(v -> startAppSettings(item.packageName));
        ActionPopupWindow.ItemBuilder uninstallItem = new ActionPopupWindow.ItemBuilder(this)
                .setLabel(R.string.popup_app_uninstall)
                .setIcon(R.drawable.ic_action_delete)
                .setOnClickListener(v -> startAppUninstall(item));

        ActionPopupWindow popup = new ActionPopupWindow(this, picasso);
        if (shortcuts.isEmpty()) {
            popup.addShortcut(infoItem);
            if (uninstallAvailable) {
                popup.addShortcut(uninstallItem);
            }
        } else {
            popup.addAction(infoItem);
            if (uninstallAvailable) {
                popup.addAction(uninstallItem);
            }
            for (Shortcut shortcut : shortcuts) {
                popup.addShortcut(new ActionPopupWindow.ItemBuilder(this)
                        .setLabel(shortcut.getShortLabel())
                        .setIcon(shortcut.getIconUri())
                        .setOnClickListener(v -> {
                            if (!shortcut.start(null, null)) {
                                showError(R.string.error);
                                presenter.updateShortcuts(item.getDescriptor());
                            }
                        })
                        .setOnPinClickListener(v -> presenter.pinShortcut(shortcut))
                );
            }
        }
        View itemView = list.findViewHolderForAdapterPosition(position).itemView;
        showPopupWindow(popup, itemView);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Adapter listeners
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onAppClick(int position, AppViewModel item) {
        if (editMode) {
            showCustomizePopup(position, item);
        } else {
            startApp(item);
        }
    }

    @Override
    public void onAppLongClick(int position, AppViewModel item) {
        if (editMode) {
            startDrag(position);
        } else {
            presenter.showAppPopup(position, item);
        }
    }

    @Override
    public void onGroupClick(int position, GroupViewModel item) {
        if (editMode) {
            showCustomizePopup(position, item);
        } else {
            presenter.toggleExpandableItemState(item);
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
            presenter.startShortcut(item);
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

    ///////////////////////////////////////////////////////////////////////////
    // SwapItemHelper
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onItemMove(int from, int to) {
        presenter.swapApps(from, to);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Private
    ///////////////////////////////////////////////////////////////////////////

    private boolean dismissPopup() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
            popupWindow = null;
            return true;
        }
        return false;
    }

    private void startApp(AppViewModel item) {
        searchBarBehavior.hide();
        Intent intent = packageManager.getLaunchIntentForPackage(item.packageName);
        if (intent != null) {
            if (item.componentName != null) {
                intent.setComponent(ComponentName.unflattenFromString(item.componentName));
            }
            if (IntentUtils.safeStartActivity(this, intent)) {
                return;
            }
        }
        showError(R.string.error);
    }

    private void startAppSettings(String packageName) {
        Intent intent = IntentUtils.getPackageSystemSettings(packageName);
        if (!IntentUtils.safeStartActivity(this, intent)) {
            showError(R.string.error);
        }
    }

    private void startAppUninstall(AppViewModel item) {
        Intent intent = IntentUtils.getUninstallIntent(item.packageName);
        if (!IntentUtils.safeStartActivity(this, intent)) {
            showError(R.string.error);
        }
    }

    private void startShortcut(PinnedShortcutViewModel item) {
        Intent intent = IntentUtils.fromUri(item.uri);
        if (!IntentUtils.safeStartActivity(this, intent)) {
            showError(R.string.error);
        }
    }

    private void showErrorToast(CharSequence message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showError(@StringRes int message) {
        showErrorToast(getText(message));
    }

    private void startDrag(int position) {
        View view = list.getLayoutManager().findViewByPosition(position);
        touchHelper.startDrag(list.getChildViewHolder(view));
    }

    private void applyUserPrefs(UserPrefs userPrefs) {
        setLayout(userPrefs.homeLayout);
        root.setBackgroundColor(userPrefs.overlayColor);
        list.setVerticalScrollBarEnabled(userPrefs.showScrollbar);
    }

    private void onFireSearch(int pos) {
        if (editSearch.getText().length() > 0) {
            SearchAdapter adapter = (SearchAdapter) editSearch.getAdapter();
            if (adapter.getCount() > 0) {
                Match item = adapter.getItem(pos);
                handleSearchIntent(item.getIntent());
            }
            editSearch.setText("");
        }
        searchBarBehavior.hide();
    }

    private void handleSearchIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        if (preferences.useCustomTabs() && Intent.ACTION_VIEW.equals(intent.getAction()) &&
                intent.getData() != null) {
            CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
                    .setToolbarColor(ContextCompat.getColor(this, R.color.primary))
                    .addDefaultShareMenuItem()
                    .setShowTitle(true)
                    .build();
            customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (IntentUtils.canHandleIntent(this, customTabsIntent.intent)) {
                customTabsIntent.launchUrl(this, intent.getData());
            }
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 &&
                StartShortcutReceiver.ACTION.equals(intent.getAction())) {
            sendBroadcast(intent);
            return;
        }
        if (!IntentUtils.safeStartActivity(this, intent)) {
            showError(R.string.error);
        }
    }

    private void animateListAppearance() {
        float endY = searchBarBehavior.isShown() ? list.getTranslationY() : 0;
        float startY = -endY - getResources().getDimension(R.dimen.list_start_translation_y);
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
        animations.start();
        searchBarBehavior.setEnabled(false);
    }

    private void setEditMode(boolean value) {
        if (editMode == value) {
            return;
        }
        editMode = value;
        searchBarBehavior.hide();
        searchBarBehavior.setEnabled(!editMode);
        if (editMode) {
            editModeSnackbar = Snackbar.make(findViewById(R.id.coordinator),
                    R.string.customize_snackbar_hint,
                    Snackbar.LENGTH_INDEFINITE);
            editModeSnackbar.setAction(R.string.customize_save, v -> {
                if (editModeSnackbar != null && editModeSnackbar.isShownOrQueued()) {
                    presenter.stopCustomize();
                }
            });
            editModeSnackbar.show();
            list.setPadding(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.snackbar_size));
        } else {
            list.setPadding(0, 0, 0, 0);
            if (editModeSnackbar != null) {
                editModeSnackbar.dismiss();
                editModeSnackbar = null;
            }
        }
    }

    private void setLayout(Preferences.HomeLayout layout) {
        if (layout != this.layout) {
            this.layout = layout;
            list.setLayoutManager(getLayoutManager(layout));
        }
    }

    private RecyclerView.LayoutManager getLayoutManager(Preferences.HomeLayout layout) {
        switch (layout) {
            case GRID:
                return new GridLayoutManager(this, 2);
            case LINEAR:
                return new LinearLayoutManager(this);
            case COMPACT:
            default:
                FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
                layoutManager.setFlexDirection(FlexDirection.ROW);
                layoutManager.setAlignItems(AlignItems.FLEX_START);
                return layoutManager;
        }
    }

    private void showCustomizePopup(int position, DescriptorItem item) {
        ActionPopupWindow popup = new ActionPopupWindow(this, picasso);
        if (item instanceof HiddenItem) {
            popup.addAction(new ActionPopupWindow.ItemBuilder(this)
                    .setIcon(R.drawable.ic_action_hide)
                    .setOnClickListener(v -> presenter.hideItem(position, (HiddenItem) item))
            );
        }
        if (item instanceof CustomLabelItem) {
            popup.addShortcut(new ActionPopupWindow.ItemBuilder(this)
                    .setLabel(R.string.customize_item_rename)
                    .setIcon(R.drawable.ic_action_rename)
                    .setOnClickListener(v -> setItemCustomLabel(position, (CustomLabelItem) item))
            );
        }
        if (item instanceof CustomColorItem) {
            popup.addShortcut(new ActionPopupWindow.ItemBuilder(this)
                    .setLabel(R.string.customize_item_color)
                    .setIcon(R.drawable.ic_action_color)
                    .setOnClickListener(v -> setItemColor(position, (CustomColorItem) item))
            );
        }
        if (item instanceof VisibleItem) {
            popup.addShortcut(new ActionPopupWindow.ItemBuilder(this)
                    .setLabel(R.string.customize_item_add_group)
                    .setIcon(R.drawable.ic_action_add_group)
                    .setOnClickListener(v -> {
                        presenter.addGroup(position, getString(R.string.new_group_label),
                                getColor(R.color.group_default));
                    })
            );
        }
        if (item instanceof RemovableItem) {
            popup.addAction(new ActionPopupWindow.ItemBuilder(this)
                    .setIcon(R.drawable.ic_action_delete)
                    .setOnClickListener(v -> presenter.removeItem(position, item))
            );
        }
        View itemView = list.findViewHolderForAdapterPosition(position).itemView;
        showPopupWindow(popup, itemView);
    }

    private void showItemPopup(int position, DescriptorItem item) {
        ActionPopupWindow popup = new ActionPopupWindow(this, picasso);
        if (item instanceof DeepShortcutViewModel) {
            popup.addShortcut(new ActionPopupWindow.ItemBuilder(this)
                    .setIcon(R.drawable.ic_app_info)
                    .setLabel(R.string.popup_app_info)
                    .setOnClickListener(v -> {
                        startAppSettings(((DeepShortcutViewModel) item).packageName);
                    })
            );
        }
        if (item instanceof RemovableItem) {
            popup.addShortcut(new ActionPopupWindow.ItemBuilder(this)
                    .setIcon(R.drawable.ic_action_delete)
                    .setLabel(R.string.customize_item_delete)
                    .setOnClickListener(v -> {
                        String visibleLabel = ((CustomLabelItem) item).getVisibleLabel();
                        String message = getString(R.string.popup_delete_message, visibleLabel);
                        new AlertDialog.Builder(this)
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
        View itemView = list.findViewHolderForAdapterPosition(position).itemView;
        showPopupWindow(popup, itemView);
    }

    private void setItemCustomLabel(int position, CustomLabelItem item) {
        String customLabel = item.getCustomLabel();
        EditTextAlertDialog.builder(this)
                .setTitle(item.getVisibleLabel())
                .customizeEditText(editText -> {
                    editText.setText(customLabel);
                    if (customLabel != null) {
                        editText.setSelection(customLabel.length());
                    }
                })
                .setPositiveButton(R.string.ok, (dialog, editText) -> {
                    String label = editText.getText().toString().trim();
                    if (!label.equals(customLabel)) {
                        presenter.renameItem(position, item, label);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .setNeutralButton(R.string.customize_action_reset, (dialog, which) -> {
                    presenter.renameItem(position, item, "");
                })
                .show();
    }

    private void setItemColor(int position, CustomColorItem item) {
        int visibleColor = item.getVisibleColor();
        ColorPickerDialog.builder(this)
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
                .show();
    }

    private void showPopupWindow(ActionPopupWindow popup, View anchor) {
        dismissPopup();
        list.setLayoutFrozen(true);
        popup.setOnDismissListener(() -> list.setLayoutFrozen(false));
        popup.showAtAnchor(anchor, computeScreenBounds());
        popupWindow = popup;
    }

    private Rect computeScreenBounds() {
        Resources res = getResources();
        int statusBarSize = res.getDimensionPixelSize(R.dimen.statusbar_size);
        int navBarSize = res.getDimensionPixelSize(R.dimen.navbar_size);
        DisplayMetrics dm = res.getDisplayMetrics();
        return new Rect(0, statusBarSize, dm.widthPixels, dm.heightPixels - navBarSize);
    }
}

