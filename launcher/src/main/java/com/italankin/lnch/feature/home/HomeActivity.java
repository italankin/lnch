package com.italankin.lnch.feature.home;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
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
import com.italankin.lnch.feature.home.adapter.GroupViewModelAdapter;
import com.italankin.lnch.feature.home.adapter.HiddenAppViewModelAdapter;
import com.italankin.lnch.feature.home.adapter.SearchAdapter;
import com.italankin.lnch.feature.home.adapter.ShortcutViewModelAdapter;
import com.italankin.lnch.feature.home.descriptor.CustomColorItem;
import com.italankin.lnch.feature.home.descriptor.CustomLabelItem;
import com.italankin.lnch.feature.home.descriptor.DescriptorItem;
import com.italankin.lnch.feature.home.descriptor.GroupedItem;
import com.italankin.lnch.feature.home.descriptor.HiddenItem;
import com.italankin.lnch.feature.home.descriptor.LabelItem;
import com.italankin.lnch.feature.home.descriptor.RemovableItem;
import com.italankin.lnch.feature.home.descriptor.model.AppViewModel;
import com.italankin.lnch.feature.home.descriptor.model.GroupViewModel;
import com.italankin.lnch.feature.home.descriptor.model.ShortcutViewModel;
import com.italankin.lnch.feature.home.model.UserPrefs;
import com.italankin.lnch.feature.home.util.SwapItemHelper;
import com.italankin.lnch.feature.home.util.TopBarBehavior;
import com.italankin.lnch.feature.settings_root.SettingsActivity;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.search.match.Match;
import com.italankin.lnch.util.IntentUtils;
import com.italankin.lnch.util.adapterdelegate.CompositeAdapter;
import com.italankin.lnch.util.widget.EditTextAlertDialog;
import com.italankin.lnch.util.widget.LceLayout;
import com.italankin.lnch.util.widget.ListAlertDialog;
import com.italankin.lnch.util.widget.colorpicker.ColorPickerDialog;
import com.squareup.picasso.Picasso;

import java.net.URISyntaxException;
import java.util.List;

import timber.log.Timber;

public class HomeActivity extends AppActivity implements HomeView,
        SwapItemHelper.Callback,
        AppViewModelAdapter.Listener,
        GroupViewModelAdapter.Listener, ShortcutViewModelAdapter.Listener {

    private static final String KEY_SEARCH_SHOWN = "SEARCH_SHOWN";
    private static final int REQUEST_CODE_SETTINGS = 1;

    @InjectPresenter
    HomePresenter presenter;

    private LceLayout root;
    private ViewGroup searchBar;
    private AutoCompleteTextView editSearch;
    private View btnSettings;
    private RecyclerView list;

    private InputMethodManager inputMethodManager;
    private PackageManager packageManager;

    private boolean editMode = false;

    private TopBarBehavior searchBarBehavior;
    private ItemTouchHelper touchHelper;
    private CompositeAdapter<DescriptorItem> adapter;
    private Preferences.HomeLayout layout;
    private Snackbar editModeSnackbar;
    private Preferences preferences;

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
            searchBarBehavior.show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_SEARCH_SHOWN, searchBarBehavior.isShown());
    }

    @Override
    public void onBackPressed() {
        if (editMode) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.customize_discard_message)
                    .setPositiveButton(R.string.customize_discard, (dialog, which) -> presenter.discardChanges())
                    .setNegativeButton(R.string.cancel, null)
                    .show();
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
        adapter = new CompositeAdapter.Builder<DescriptorItem>(this)
                .add(new AppViewModelAdapter(this))
                .add(new HiddenAppViewModelAdapter())
                .add(new GroupViewModelAdapter(this))
                .add(new ShortcutViewModelAdapter(this))
                .recyclerView(list)
                .setHasStableIds(true)
                .create();
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
        editSearch.setOnItemClickListener((parent, view, position, id) -> {
            onFireSearch(position);
        });
        editSearch.setThreshold(1);
        MainComponent mainComponent = daggerService().main();
        Picasso picasso = mainComponent.getPicassoFactory().create(this);
        editSearch.setAdapter(new SearchAdapter(picasso, mainComponent.getSearchRepository()));

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
        applyUserPrefs(userPrefs);
        adapter.setDataset(items);
        list.setVisibility(View.VISIBLE);
        root.showContent();
    }

    @Override
    public void onStartCustomize() {
        setEditMode(true);
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
    }

    @Override
    public void onItemsRemoved(int startIndex, int count) {
        list.getAdapter().notifyItemRangeRemoved(startIndex, count);
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
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Adapter listeners
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onAppClick(int position, AppViewModel item) {
        if (editMode) {
            customize(position, item);
        } else {
            startApp(item);
        }
    }

    @Override
    public void onAppLongClick(int position, AppViewModel item) {
        if (editMode) {
            startDrag(position);
        } else {
            startAppSettings(item);
        }
    }

    @Override
    public void onGroupClick(int position, GroupViewModel item) {
        if (editMode) {
            customize(position, item);
        } else {
            presenter.hideGroup(position);
        }
    }

    @Override
    public void onGroupLongClick(int position, GroupViewModel item) {
        if (editMode) {
            startDrag(position);
        }
    }

    @Override
    public void onShortcutClick(int position, ShortcutViewModel item) {
        if (editMode) {
            customize(position, item);
        } else {
            startShortcut(item);
        }
    }

    @Override
    public void onShortcutLongClick(int position, ShortcutViewModel item) {
        if (editMode) {
            startDrag(position);
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

    private void startApp(AppViewModel item) {
        searchBarBehavior.hide();
        Intent intent = packageManager.getLaunchIntentForPackage(item.packageName);
        if (intent != null) {
            if (item.componentName != null) {
                intent.setComponent(ComponentName.unflattenFromString(item.componentName));
            }
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                showError(e);
            }
        } else {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    private void startAppSettings(AppViewModel item) {
        Intent intent = IntentUtils.getPackageSystemSettings(item.packageName);
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    private void startShortcut(ShortcutViewModel item) {
        try {
            Intent intent = Intent.parseUri(item.uri, 0);
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
            }
        } catch (URISyntaxException e) {
            Timber.e(e, "startShortcut:");
        }
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
            if (customTabsIntent.intent.resolveActivity(packageManager) != null) {
                customTabsIntent.launchUrl(this, intent.getData());
            }
            return;
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        }
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

    private void customize(int position, DescriptorItem item) {
        ListAlertDialog.Builder builder = ListAlertDialog.builder(this);
        if (item instanceof LabelItem) {
            builder.setTitle(((LabelItem) item).getVisibleLabel());
        }
        if (item instanceof CustomLabelItem) {
            builder.addItem(R.drawable.ic_action_rename, R.string.customize_item_rename, () -> {
                setItemCustomLabel(position, (CustomLabelItem) item);
            });
        }
        if (item instanceof CustomColorItem) {
            builder.addItem(R.drawable.ic_action_color, R.string.customize_item_color, () -> {
                setItemColor(position, (CustomColorItem) item);
            });
        }
        if (item instanceof HiddenItem) {
            builder.addItem(R.drawable.ic_action_hide, R.string.customize_item_hide, () -> {
                presenter.hideItem(position, (HiddenItem) item);
            });
        }
        if (item instanceof GroupedItem) {
            builder.addItem(R.drawable.ic_action_add_group, R.string.customize_item_add_group, () -> {
                presenter.addGroup(position, getString(R.string.new_group_label),
                        getColor(R.color.group_default));
            });
        }
        if (item instanceof RemovableItem) {
            builder.addItem(R.drawable.ic_action_delete, R.string.customize_item_delete, () -> {
                presenter.removeItem(position);
            });
        }
        builder.show();
    }

    private void setItemCustomLabel(int position, CustomLabelItem item) {
        EditTextAlertDialog.builder(this)
                .setTitle(item.getVisibleLabel())
                .customizeEditText(editText -> {
                    editText.setText(item.getCustomLabel());
                    editText.setSelectAllOnFocus(true);
                })
                .setPositiveButton(R.string.ok, (dialog, editText) -> {
                    String label = editText.getText().toString().trim();
                    if (!label.equals(item.getCustomLabel())) {
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
}

