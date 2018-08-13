package com.italankin.lnch.feature.home;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.InputFilter;
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
import com.italankin.lnch.feature.base.AppActivity;
import com.italankin.lnch.feature.home.adapter.AppViewModelAdapter;
import com.italankin.lnch.feature.home.adapter.GroupSeparatorViewModelAdapter;
import com.italankin.lnch.feature.home.adapter.HiddenAppViewModelAdapter;
import com.italankin.lnch.feature.home.adapter.SearchAdapter;
import com.italankin.lnch.feature.home.model.AppViewModel;
import com.italankin.lnch.feature.home.model.GroupSeparatorViewModel;
import com.italankin.lnch.feature.home.model.ItemViewModel;
import com.italankin.lnch.feature.home.util.SwapItemHelper;
import com.italankin.lnch.feature.home.util.TopBarBehavior;
import com.italankin.lnch.feature.settings_root.SettingsActivity;
import com.italankin.lnch.model.provider.Preferences;
import com.italankin.lnch.model.repository.search.match.Match;
import com.italankin.lnch.util.TextWatcherAdapter;
import com.italankin.lnch.util.adapterdelegate.CompositeAdapter;
import com.italankin.lnch.util.widget.EditTextAlertDialog;
import com.italankin.lnch.util.widget.LceLayout;
import com.italankin.lnch.util.widget.ListAlertDialog;

import java.util.List;

public class HomeActivity extends AppActivity implements HomeView,
        SwapItemHelper.Callback,
        AppViewModelAdapter.Listener,
        GroupSeparatorViewModelAdapter.Listener {

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
    private CompositeAdapter<ItemViewModel> adapter;
    private String layout;
    private Snackbar editModeSnackbar;

    @ProvidePresenter
    HomePresenter providePresenter() {
        return daggerService().presenters().home();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        packageManager = getPackageManager();

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
    }

    @Override
    public void onBackPressed() {
        if (editMode) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.edit_mode_discard_message)
                    .setPositiveButton(R.string.edit_mode_discard, (dialog, which) -> presenter.discardChanges())
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
        if (searchBarBehavior.isShown()) {
            searchBarBehavior.hide();
        } else if (Intent.ACTION_MAIN.equals(intent.getAction())) {
            list.smoothScrollToPosition(0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_SETTINGS) {
            switch (resultCode) {
                case SettingsActivity.RESULT_CHANGED:
                    presenter.reloadAppsImmediate();
                    return;
                case SettingsActivity.RESULT_EDIT_MODE:
                    presenter.startEditMode();
                    return;
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
        adapter = new CompositeAdapter.Builder<ItemViewModel>(this)
                .add(new AppViewModelAdapter(this))
                .add(new HiddenAppViewModelAdapter())
                .add(new GroupSeparatorViewModelAdapter(this))
                .recyclerView(list)
                .setHasStableIds(true)
                .create();
    }

    private void setupRoot() {
        root.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    private void setupSearchBar() {
        searchBarBehavior = new TopBarBehavior(searchBar, list, new TopBarBehavior.Listener() {
            @Override
            public void onShow() {
                if (daggerService().main().getAppPrefs().searchShowSoftKeyboard()) {
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
        editSearch.setAdapter(new SearchAdapter(daggerService().main().getSearchRepository()));

        btnSettings.setOnClickListener(v -> {
            searchBarBehavior.hide();
            Intent intent = SettingsActivity.getStartIntent(this);
            startActivityForResult(intent, REQUEST_CODE_SETTINGS);
        });
        btnSettings.setOnLongClickListener(v -> {
            presenter.startEditMode();
            return true;
        });
    }

    @Override
    public void showProgress() {
        root.showLoading();
    }

    @Override
    public void onAppsLoaded(List<ItemViewModel> items, String layout) {
        setLayout(layout);
        adapter.setDataset(items);
        list.setVisibility(View.VISIBLE);
        root.showContent();
    }

    @Override
    public void onStartEditMode() {
        setEditMode(true);
    }

    @Override
    public void onStopEditMode() {
        setEditMode(false);
        Toast.makeText(this, R.string.edit_mode_saved, Toast.LENGTH_SHORT).show();
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
                .button(v -> presenter.loadApps())
                .message(e.getMessage())
                .show();
    }

    @Override
    public void showError(Throwable e) {
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAppClick(int position, AppViewModel item) {
        if (editMode) {
            customizeItem(position, item);
        } else {
            startApp(item);
        }
    }

    @Override
    public void onAppLongClick(int position, AppViewModel item) {
        if (editMode) {
            View view = list.getLayoutManager().findViewByPosition(position);
            touchHelper.startDrag(list.getChildViewHolder(view));
        } else {
            startAppSettings(item);
        }
    }

    @Override
    public void onSeparatorClick(int position, GroupSeparatorViewModel item) {
        if (editMode) {
            customizeSeparator(position, item);
        } else {
            presenter.hideGroup(position);
        }
    }

    @Override
    public void onSeparatorLongClick(int position, GroupSeparatorViewModel item) {
        if (editMode) {
            View view = list.getLayoutManager().findViewByPosition(position);
            touchHelper.startDrag(list.getChildViewHolder(view));
        }
    }

    @Override
    public void onItemMove(int from, int to) {
        presenter.swapApps(from, to);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Private
    ///////////////////////////////////////////////////////////////////////////

    void startApp(AppViewModel item) {
        searchBarBehavior.hide();
        Intent intent = packageManager.getLaunchIntentForPackage(item.packageName);
        if (intent != null && intent.resolveActivity(packageManager) != null) {
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

    void startAppSettings(AppViewModel item) {
        Uri uri = Uri.fromParts("package", item.packageName, null);
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    private void onFireSearch(int pos) {
        if (editSearch.getText().length() > 0) {
            SearchAdapter adapter = (SearchAdapter) editSearch.getAdapter();
            if (adapter.getCount() > 0) {
                Match item = adapter.getItem(pos);
                Intent intent = item.getIntent();
                if (intent != null && intent.resolveActivity(packageManager) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
                }
            }
            editSearch.setText("");
        }
        searchBarBehavior.hide();
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
                    R.string.edit_mode_hint,
                    Snackbar.LENGTH_INDEFINITE);
            editModeSnackbar.setAction(R.string.edit_mode_save, v -> {
                if (editModeSnackbar != null && editModeSnackbar.isShownOrQueued()) {
                    presenter.stopEditMode();
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

    private void setLayout(String layout) {
        if (layout == null) {
            layout = Preferences.LAYOUT_COMPACT;
        }
        if (!layout.equals(this.layout)) {
            this.layout = layout;
            list.setLayoutManager(getLayoutManager(layout));
        }
    }

    private RecyclerView.LayoutManager getLayoutManager(String layout) {
        if (layout == null) {
            layout = Preferences.LAYOUT_COMPACT;
        }
        switch (layout) {
            case Preferences.LAYOUT_GRID:
                return new GridLayoutManager(this, 2);
            case Preferences.LAYOUT_LINEAR:
                return new LinearLayoutManager(this);
            case Preferences.LAYOUT_COMPACT:
            default:
                FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
                layoutManager.setFlexDirection(FlexDirection.ROW);
                layoutManager.setAlignItems(AlignItems.FLEX_START);
                return layoutManager;
        }
    }

    private void customizeItem(int position, AppViewModel item) {
        ListAlertDialog.builder(this)
                .setTitle(item.getVisibleLabel())
                .addItem(R.drawable.ic_action_name, R.string.edit_mode_action_name, () -> {
                    setItemCustomLabel(position, item);
                })
                .addItem(R.drawable.ic_action_color, R.string.edit_mode_action_color, () -> {
                    setItemColor(position, item);
                })
                .addItem(R.drawable.ic_action_hide, R.string.edit_mode_action_hide, () -> {
                    presenter.hideApp(position, item);
                })
                .addItem(0, R.string.edit_mode_action_add_separator, () -> {
                    presenter.addSeparator(position);
                })
                .show();
    }

    private void setItemCustomLabel(int position, ItemViewModel item) {
        EditTextAlertDialog.builder(this)
                .setTitle(item.getCustomLabel())
                .customizeEditText(editText -> {
                    editText.setText(item.getCustomLabel());
                    editText.setSelectAllOnFocus(true);
                })
                .setPositiveButton(R.string.edit_mode_rename, (dialog, editText) -> {
                    String label = editText.getText().toString().trim();
                    presenter.renameItem(position, item, label);
                })
                .setNegativeButton(R.string.cancel, null)
                .setNeutralButton(R.string.edit_mode_reset, (dialog, which) -> {
                    presenter.renameItem(position, item, "");
                })
                .show();
    }

    private void setItemColor(int position, ItemViewModel item) {
        EditTextAlertDialog.builder(this)
                .setTitle(item.getVisibleLabel())
                .customizeEditText(editText -> {
                    editText.setText(String.format("%06x", item.getVisibleColor()).substring(2));
                    editText.setSelectAllOnFocus(true);
                    editText.addTextChangedListener(new TextWatcherAdapter() {
                        @Override
                        public void afterTextChanged(Editable s) {
                            if (s.length() == 6) {
                                try {
                                    int color = Integer.decode("0x" + s.toString()) + 0xff000000;
                                    editText.setTextColor(color);
                                } catch (NumberFormatException ignored) {
                                }
                            }
                        }
                    });
                    editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
                })
                .setPositiveButton(R.string.ok, (dialog, editText) -> {
                    String value = editText.getText().toString().trim();
                    presenter.changeItemCustomColor(position, item, value);
                })
                .setNegativeButton(R.string.cancel, null)
                .setNeutralButton(R.string.edit_mode_reset, (dialog, which) -> {
                    presenter.changeItemCustomColor(position, item, null);
                })
                .show();
    }

    private void customizeSeparator(int position, GroupSeparatorViewModel item) {
        ListAlertDialog.builder(this)
                .setTitle(item.getVisibleLabel())
                .addItem(R.drawable.ic_action_name, R.string.edit_mode_action_name, () -> {
                    setItemCustomLabel(position, item);
                })
                .addItem(R.drawable.ic_action_color, R.string.edit_mode_action_color, () -> {
                    setItemColor(position, item);
                })
                .addItem(0, R.string.edit_mode_action_remove_separator, () -> {
                    presenter.removeSeparator(position);
                })
                .show();
    }
}

