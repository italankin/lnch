package com.italankin.lnch.feature.home;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.base.AppActivity;
import com.italankin.lnch.feature.common.preferences.ScreenOrientationObservable;
import com.italankin.lnch.feature.common.preferences.SupportsOrientation;
import com.italankin.lnch.feature.common.preferences.ThemeObservable;
import com.italankin.lnch.feature.common.preferences.ThemedActivity;
import com.italankin.lnch.feature.home.adapter.AppViewModelAdapter;
import com.italankin.lnch.feature.home.adapter.DeepShortcutViewModelAdapter;
import com.italankin.lnch.feature.home.adapter.GroupViewModelAdapter;
import com.italankin.lnch.feature.home.adapter.HiddenAppViewModelAdapter;
import com.italankin.lnch.feature.home.adapter.HomeAdapter;
import com.italankin.lnch.feature.home.adapter.IntentViewModelAdapter;
import com.italankin.lnch.feature.home.adapter.PinnedShortcutViewModelAdapter;
import com.italankin.lnch.feature.home.adapter.SearchAdapter;
import com.italankin.lnch.feature.home.behavior.TopBarBehavior;
import com.italankin.lnch.feature.home.model.Update;
import com.italankin.lnch.feature.home.model.UserPrefs;
import com.italankin.lnch.feature.home.util.FakeStatusBarDrawable;
import com.italankin.lnch.feature.home.util.SwapItemHelper;
import com.italankin.lnch.feature.home.widget.EditModePanel;
import com.italankin.lnch.feature.home.widget.HomeRecyclerView;
import com.italankin.lnch.feature.receiver.StartShortcutReceiver;
import com.italankin.lnch.feature.settings.SettingsActivity;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.descriptor.impl.DeepShortcutDescriptor;
import com.italankin.lnch.model.descriptor.impl.IntentDescriptor;
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
import com.italankin.lnch.util.IntentUtils;
import com.italankin.lnch.util.PackageUtils;
import com.italankin.lnch.util.ResUtils;
import com.italankin.lnch.util.ViewUtils;
import com.italankin.lnch.util.picasso.PackageIconHandler;
import com.italankin.lnch.util.widget.ActionPopupWindow;
import com.italankin.lnch.util.widget.EditTextAlertDialog;
import com.italankin.lnch.util.widget.LceLayout;
import com.italankin.lnch.util.widget.colorpicker.ColorPickerDialog;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class HomeActivity extends AppActivity implements HomeView, SupportsOrientation, ThemedActivity,
        SwapItemHelper.Callback,
        AppViewModelAdapter.Listener,
        GroupViewModelAdapter.Listener,
        PinnedShortcutViewModelAdapter.Listener, DeepShortcutViewModelAdapter.Listener, IntentViewModelAdapter.Listener {

    public static final String ACTION_EDIT_MODE = "com.android.launcher.action.EDIT_MODE";

    private static final String KEY_SEARCH_SHOWN = "SEARCH_SHOWN";
    private static final int ANIM_LIST_APPEARANCE_DURATION = 400;

    @InjectPresenter
    HomePresenter presenter;

    private LceLayout root;
    private ViewGroup searchContainer;
    private AutoCompleteTextView searchEditText;
    private ImageView searchBtnGlobal;
    private View searchBtnSettings;
    private HomeRecyclerView list;

    private InputMethodManager inputMethodManager;
    private PackageManager packageManager;
    private Preferences preferences;
    private Picasso picasso;

    private boolean editMode = false;
    private boolean animateOnResume = false;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private TopBarBehavior searchBarBehavior;
    private ItemTouchHelper touchHelper;
    private Preferences.HomeLayout layout;
    private EditModePanel editModePanel;
    private PopupWindow popupWindow;
    private HomeAdapter adapter;

    @ProvidePresenter
    HomePresenter providePresenter() {
        return daggerService().presenters().home();
    }

    @Override
    protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state);

        inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        packageManager = getPackageManager();
        preferences = daggerService().main().getPreferences();
        picasso = daggerService().main().getPicassoFactory().create(this);

        setScreenOrientation();
        setTheme();

        setupWindow();

        setContentView(R.layout.activity_home);
        root = findViewById(R.id.root);
        list = findViewById(R.id.list);
        searchContainer = findViewById(R.id.search_container);
        searchEditText = findViewById(R.id.search_edit_text);
        searchBtnSettings = findViewById(R.id.search_settings);
        searchBtnGlobal = findViewById(R.id.search_global);

        setupRoot();
        setupList();
        setupSearchBar();

        if (!ACTION_EDIT_MODE.equals(getIntent().getAction())
                && state != null && state.getBoolean(KEY_SEARCH_SHOWN, false)) {
            searchBarBehavior.showNow();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        animateOnResume = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (animateOnResume) {
            animateOnResume = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                animateListAppearance();
            }
        }
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
                } else if (preferences.get(Preferences.SCROLL_TO_TOP)) {
                    list.smoothScrollToPosition(0);
                }
                break;
            }
            case ACTION_EDIT_MODE: {
                if (!editMode) {
                    animateOnResume = false;
                    presenter.startCustomize();
                }
                break;
            }
        }
    }

    @Override
    public void onOrientationChange(Preferences.ScreenOrientation screenOrientation, boolean changed) {
        setRequestedOrientation(screenOrientation.value());
    }

    @Override
    public void onThemeChanged(Preferences.ColorTheme colorTheme, boolean changed) {
        switch (colorTheme) {
            case DARK:
                setTheme(R.style.AppTheme_Dark_Launcher);
                break;
            case LIGHT:
                setTheme(R.style.AppTheme_Light_Launcher);
                break;
        }
        if (changed) {
            recreate();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    @Override
    public void showProgress() {
        root.showLoading();
    }

    @Override
    public void onReceiveUpdate(Update update) {
        if (adapter == null) {
            adapter = new HomeAdapter.Builder(this)
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
        adapter.setDataset(update.items);
        list.setVisibility(View.VISIBLE);
        if (update.items.isEmpty()) {
            root.empty()
                    .message(R.string.apps_list_empty)
                    .show();
        } else {
            root.showContent();
        }
        dismissPopup();

        applyUserPrefs(update.userPrefs);
        boolean needsFullUpdate = adapter.updateUserPrefs(update.userPrefs);
        if (needsFullUpdate) {
            adapter.notifyDataSetChanged();
        } else {
            update.dispatchTo(adapter);
        }

        if (ACTION_EDIT_MODE.equals(getIntent().getAction())) {
            // clear edit mode intent
            setIntent(new Intent());
            presenter.startCustomize();
        } else if (preferences.get(Preferences.FIRST_LAUNCH)) {
            preferences.set(Preferences.FIRST_LAUNCH, false);
            searchBarBehavior.show();
        }
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
        list.smoothScrollToPosition(startIndex + Math.min(1, count));
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
                popup.addShortcut(new ActionPopupWindow.ItemBuilder(this)
                        .setLabel(shortcut.getShortLabel())
                        .setIcon(shortcut.getIconUri())
                        .setEnabled(shortcut.isEnabled())
                        .setOnClickListener(v -> {
                            if (!shortcut.isEnabled()) {
                                onShortcutDisabled(shortcut.getDisabledMessage());
                            } else if (!shortcut.start(null, null)) {
                                showError(R.string.error);
                                presenter.updateShortcuts(item.getDescriptor());
                            }
                        })
                        .setOnPinClickListener(v -> presenter.pinShortcut(shortcut))
                );
            }
        }
        RecyclerView.ViewHolder viewHolder = list.findViewHolderForAdapterPosition(position);
        if (viewHolder != null) {
            showPopupWindow(popup, viewHolder.itemView);
        }
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
            switch (preferences.get(Preferences.APP_LONG_CLICK_ACTION)) {
                case INFO:
                    startAppSettings(item.packageName);
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

    ///////////////////////////////////////////////////////////////////////////
    // SwapItemHelper
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onItemMove(int from, int to) {
        presenter.swapApps(from, to);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Setup
    ///////////////////////////////////////////////////////////////////////////

    private void setupWindow() {
        Window window = getWindow();
        window.getDecorView().setOnApplyWindowInsetsListener((v, insets) -> {
            int stableInsetTop = insets.getStableInsetTop();
            root.setPadding(insets.getStableInsetLeft(), stableInsetTop,
                    insets.getStableInsetRight(), 0);
            list.setBottomInset(insets.getStableInsetBottom());
            root.setForeground(new FakeStatusBarDrawable(getColor(R.color.status_bar), stableInsetTop));
            return insets;
        });
        window.setFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER,
                WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
    }

    private void setupList() {
        touchHelper = new ItemTouchHelper(new SwapItemHelper(this));
        touchHelper.attachToRecyclerView(list);
    }

    private void setupRoot() {
        root.setBackgroundColor(preferences.get(Preferences.WALLPAPER_OVERLAY_COLOR));
        root.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    private void setupSearchBar() {
        searchBarBehavior = new TopBarBehavior(searchContainer, list, new TopBarBehavior.Listener() {
            @Override
            public void onShow() {
                if (preferences.get(Preferences.SEARCH_SHOW_SOFT_KEYBOARD)) {
                    searchEditText.requestFocus();
                }
                inputMethodManager.showSoftInput(searchEditText, 0);
            }

            @Override
            public void onHide() {
                searchEditText.setText("");
                inputMethodManager.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
                searchEditText.clearFocus();
            }
        });
        searchBarBehavior.setEnabled(!editMode);
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) searchContainer.getLayoutParams();
        lp.setBehavior(searchBarBehavior);
        searchContainer.setLayoutParams(lp);

        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                onFireSearch(0);
            }
            return true;
        });
        SearchAdapter.Listener listener = new SearchAdapter.Listener() {
            @Override
            public void onSearchItemClick(int position, Match match) {
                onFireSearch(position);
            }

            @Override
            public void onSearchItemPinClick(int position, Match match) {
                searchBarBehavior.hide();
                String label = match.getLabel(HomeActivity.this)
                        .toString()
                        .trim()
                        .toUpperCase(Locale.getDefault());
                IntentDescriptor intentDescriptor = new IntentDescriptor(match.getIntent(),
                        label, match.getColor(HomeActivity.this));
                presenter.pinIntent(intentDescriptor);
            }

            @Override
            public void onSearchItemInfoClick(int position, Match match) {
                if (!(match instanceof DescriptorMatch)) {
                    return;
                }
                Descriptor descriptor = ((DescriptorMatch) match).getDescriptor();
                String packageName = null;
                if (descriptor instanceof AppDescriptor) {
                    packageName = ((AppDescriptor) descriptor).packageName;
                } else if (descriptor instanceof DeepShortcutDescriptor) {
                    packageName = ((DeepShortcutDescriptor) descriptor).packageName;
                }
                if (packageName == null) {
                    return;
                }
                Intent intent = PackageUtils.getPackageSystemSettings(packageName);
                if (!IntentUtils.safeStartActivity(HomeActivity.this, intent)) {
                    showError(R.string.error);
                    return;
                }
                searchBarBehavior.hide();
            }
        };
        searchEditText.setAdapter(new SearchAdapter(picasso,
                daggerService().main().getSearchRepository(), listener));

        searchBtnSettings.setOnClickListener(v -> {
            searchBarBehavior.hide();
            Intent intent = SettingsActivity.getStartIntent(this);
            startActivity(intent);
        });
        searchBtnSettings.setOnLongClickListener(v -> {
            presenter.startCustomize();
            return true;
        });

        if (preferences.get(Preferences.SEARCH_SHOW_GLOBAL_SEARCH)) {
            setupGlobalSearchButton();
        }
    }

    private void setupGlobalSearchButton() {
        ComponentName searchActivity = PackageUtils.getGlobalSearchActivity(this);
        if (searchActivity == null) {
            return;
        }
        searchBtnGlobal.setVisibility(View.VISIBLE);
        searchBtnGlobal.setOnClickListener(v -> {
            Intent intent = new Intent().setComponent(searchActivity);
            if (IntentUtils.safeStartActivity(this, intent)) {
                searchBarBehavior.hide();
            } else {
                showError(R.string.error);
            }
        });
        searchBtnGlobal.setOnLongClickListener(v -> {
            startAppSettings(searchActivity.getPackageName());
            return true;
        });
        ViewUtils.setPaddingLeftDimen(searchEditText, R.dimen.searchbar_size);
        picasso.load(PackageIconHandler.uriFrom(searchActivity.getPackageName()))
                .error(R.drawable.ic_action_search)
                .into(searchBtnGlobal);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Start
    ///////////////////////////////////////////////////////////////////////////

    private void startApp(AppViewModel item) {
        searchBarBehavior.hide();
        Intent intent = packageManager.getLaunchIntentForPackage(item.packageName);
        if (intent != null) {
            if (item.componentName != null) {
                intent.setComponent(ComponentName.unflattenFromString(item.componentName));
            }
            ComponentName cn = intent.getComponent();
            if (cn != null && cn.getClassName().equals(HomeActivity.class.getCanonicalName())) {
                Intent settingsIntent = SettingsActivity.getStartIntent(this);
                startActivity(settingsIntent);
                return;
            }
            if (IntentUtils.safeStartActivity(this, intent)) {
                return;
            }
        }
        showError(R.string.error);
    }

    private void startAppSettings(String packageName) {
        Intent intent = PackageUtils.getPackageSystemSettings(packageName);
        if (!IntentUtils.safeStartActivity(this, intent)) {
            showError(R.string.error);
        }
    }

    private void startAppUninstall(AppViewModel item) {
        Intent intent = PackageUtils.getUninstallIntent(item.packageName);
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

    ///////////////////////////////////////////////////////////////////////////
    // Errors
    ///////////////////////////////////////////////////////////////////////////

    private void showErrorToast(CharSequence message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showError(@StringRes int message) {
        showErrorToast(getText(message));
    }

    ///////////////////////////////////////////////////////////////////////////
    // Other
    ///////////////////////////////////////////////////////////////////////////

    private void startDrag(int position) {
        if (preferences.get(Preferences.APPS_SORT_MODE) != Preferences.AppsSortMode.MANUAL) {
            Toast.makeText(this, R.string.error_manual_sorting_required, Toast.LENGTH_SHORT).show();
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

    private void applyUserPrefs(UserPrefs userPrefs) {
        setLayout(userPrefs.homeLayout);
        root.setBackgroundColor(userPrefs.overlayColor);
        list.setVerticalScrollBarEnabled(userPrefs.showScrollbar);
        if (userPrefs.globalSearch && searchBtnGlobal.getVisibility() != View.VISIBLE) {
            setupGlobalSearchButton();
        } else if (!userPrefs.globalSearch && searchBtnGlobal.getVisibility() == View.VISIBLE) {
            ViewUtils.setPaddingLeftDimen(searchEditText, R.dimen.search_padding_left_normal);
            searchBtnGlobal.setVisibility(View.GONE);
        }
    }

    private void setScreenOrientation() {
        Disposable disposable = new ScreenOrientationObservable(preferences).subscribe(this);
        compositeDisposable.add(disposable);
    }

    private void setTheme() {
        Disposable disposable = new ThemeObservable(preferences).subscribe(this);
        compositeDisposable.add(disposable);
    }

    private void setLayout(Preferences.HomeLayout layout) {
        if (layout != this.layout) {
            this.layout = layout;
            RecyclerView.LayoutManager layoutManager;
            switch (layout) {
                case COMPACT:
                default:
                    FlexboxLayoutManager lm = new FlexboxLayoutManager(this);
                    lm.setFlexDirection(FlexDirection.ROW);
                    lm.setAlignItems(AlignItems.FLEX_START);
                    layoutManager = lm;
            }
            list.setLayoutManager(layoutManager);
        }
    }

    private void onFireSearch(int pos) {
        if (searchEditText.getText().length() > 0) {
            if (!searchEditText.isPopupShowing()) {
                searchEditText.showDropDown();
                return;
            }
            SearchAdapter adapter = (SearchAdapter) searchEditText.getAdapter();
            if (adapter.getCount() > 0) {
                Match item = adapter.getItem(pos);
                handleSearchIntent(item.getIntent());
            }
            searchEditText.setText("");
        }
        searchBarBehavior.hide();
    }

    private void handleSearchIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        if (preferences.get(Preferences.SEARCH_USE_CUSTOM_TABS)
                && Intent.ACTION_VIEW.equals(intent.getAction())
                && intent.getData() != null) {
            CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
                    .setToolbarColor(ResUtils.resolveColor(this, R.attr.colorPrimary))
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
        ComponentName cn = intent.getComponent();
        if (cn != null && cn.getClassName().equals(HomeActivity.class.getCanonicalName())) {
            Intent settingsIntent = SettingsActivity.getStartIntent(this);
            startActivity(settingsIntent);
            return;
        }
        if (!IntentUtils.safeStartActivity(this, intent)) {
            showError(R.string.error);
        }
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

    private void setEditMode(boolean value) {
        if (editMode == value) {
            return;
        }
        editMode = value;
        searchBarBehavior.hide();
        searchBarBehavior.setEnabled(!value);
        if (value) {
            inputMethodManager.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
            editModePanel = new EditModePanel(this)
                    .setMessage(R.string.customize_hint)
                    .setOnSaveActionClickListener(v -> {
                        if (editModePanel != null && editModePanel.isShown()) {
                            presenter.stopCustomize();
                        }
                    })
                    .show(findViewById(R.id.coordinator));
        } else if (editModePanel != null) {
            editModePanel.dismiss();
            editModePanel = null;
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
                    .setIconDrawableTintAttr(R.attr.colorAccent)
                    .setOnClickListener(v -> setItemCustomLabel(position, (CustomLabelItem) item))
            );
        }
        if (item instanceof CustomColorItem) {
            popup.addShortcut(new ActionPopupWindow.ItemBuilder(this)
                    .setLabel(R.string.customize_item_color)
                    .setIcon(R.drawable.ic_action_color)
                    .setIconDrawableTintAttr(R.attr.colorAccent)
                    .setOnClickListener(v -> setItemColor(position, (CustomColorItem) item))
            );
        }
        if (item instanceof VisibleItem) {
            popup.addShortcut(new ActionPopupWindow.ItemBuilder(this)
                    .setLabel(R.string.customize_item_add_group)
                    .setIcon(R.drawable.ic_action_add_group)
                    .setIconDrawableTintAttr(R.attr.colorAccent)
                    .setOnClickListener(v -> {
                        presenter.addGroup(position, getString(R.string.new_group_label),
                                ResUtils.resolveColor(this, R.attr.colorGroupTitleDefault));
                    })
            );
        }
        if (item instanceof RemovableItem) {
            popup.addAction(new ActionPopupWindow.ItemBuilder(this)
                    .setIcon(R.drawable.ic_action_delete)
                    .setOnClickListener(v -> presenter.removeItem(position, item))
            );
        }
        RecyclerView.ViewHolder viewHolder = list.findViewHolderForAdapterPosition(position);
        if (viewHolder != null) {
            showPopupWindow(popup, viewHolder.itemView);
        }
    }

    private void setItemCustomLabel(int position, CustomLabelItem item) {
        String visibleLabel = item.getVisibleLabel();
        EditTextAlertDialog.builder(this)
                .setTitle(item.getVisibleLabel())
                .customizeEditText(editText -> {
                    editText.setText(visibleLabel);
                    editText.setSingleLine(true);
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
                .setCancellable(false)
                .show();
    }

    private void showItemPopup(int position, DescriptorItem item) {
        ActionPopupWindow popup = new ActionPopupWindow(this, picasso);
        if (item instanceof DeepShortcutViewModel) {
            popup.addShortcut(new ActionPopupWindow.ItemBuilder(this)
                    .setIcon(R.drawable.ic_app_info)
                    .setIconDrawableTintAttr(R.attr.colorAccent)
                    .setLabel(R.string.popup_app_info)
                    .setOnClickListener(v -> {
                        startAppSettings(((DeepShortcutViewModel) item).packageName);
                    })
            );
        }
        if (item instanceof RemovableItem) {
            popup.addShortcut(new ActionPopupWindow.ItemBuilder(this)
                    .setIcon(R.drawable.ic_action_delete)
                    .setIconDrawableTintAttr(R.attr.colorAccent)
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
        RecyclerView.ViewHolder viewHolder = list.findViewHolderForAdapterPosition(position);
        if (viewHolder != null) {
            showPopupWindow(popup, viewHolder.itemView);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Popup
    ///////////////////////////////////////////////////////////////////////////

    private void showPopupWindow(ActionPopupWindow popup, View anchor) {
        dismissPopup();
        list.setLayoutFrozen(true);
        popup.setOnDismissListener(() -> list.setLayoutFrozen(false));
        Rect bounds = new Rect();
        root.getWindowVisibleDisplayFrame(bounds);
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
}

