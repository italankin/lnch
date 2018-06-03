package com.italankin.lnch.feature.home;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.base.AppActivity;
import com.italankin.lnch.feature.home.adapter.AppViewModelAdapter;
import com.italankin.lnch.feature.home.adapter.HiddenAppViewModelAdapter;
import com.italankin.lnch.feature.home.adapter.SearchAdapter;
import com.italankin.lnch.feature.home.model.AppViewModel;
import com.italankin.lnch.feature.home.util.SwapItemHelper;
import com.italankin.lnch.feature.home.util.TopBarBehavior;
import com.italankin.lnch.feature.settings_root.SettingsActivity;
import com.italankin.lnch.model.provider.Preferences;
import com.italankin.lnch.model.repository.search.match.Match;
import com.italankin.lnch.util.TextWatcherAdapter;
import com.italankin.lnch.util.adapterdelegate.CompositeAdapter;
import com.italankin.lnch.util.widget.EditTextAlertDialog;
import com.italankin.lnch.util.widget.ListAlertDialog;

import java.util.List;

public class HomeActivity extends AppActivity implements HomeView,
        SwapItemHelper.Callback,
        AppViewModelAdapter.Listener {

    private static final int REQUEST_CODE_SETTINGS = 1;

    @InjectPresenter
    HomePresenter presenter;

    private CoordinatorLayout root;
    private ViewGroup searchBar;
    private AutoCompleteTextView editSearch;
    private View btnSettings;
    private RecyclerView list;

    private InputMethodManager inputMethodManager;
    private PackageManager packageManager;

    private FrameLayout progressContainer;

    private boolean editMode = false;

    private TopBarBehavior searchBarBehavior;
    private ItemTouchHelper touchHelper;
    private CompositeAdapter<AppViewModel> adapter;
    private String layout;

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
        adapter = new CompositeAdapter.Builder<AppViewModel>(this)
                .add(new AppViewModelAdapter(this))
                .add(new HiddenAppViewModelAdapter())
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
        if (progressContainer != null) {
            return;
        }
        list.setVisibility(View.INVISIBLE);
        progressContainer = new FrameLayout(this);
        ProgressBar progressBar = new ProgressBar(this);
        int size = getResources().getDimensionPixelSize(R.dimen.progress_indicator_size);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size, Gravity.CENTER);
        progressContainer.addView(progressBar, params);
        root.addView(progressContainer, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    public void onAppsLoaded(List<AppViewModel> items, String layout) {
        hideProgress();
        setLayout(layout);
        adapter.setDataset(items);
        list.setVisibility(View.VISIBLE);
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
    public void onItemsSwap(int from, int to) {
        list.getAdapter().notifyItemMoved(from, to);
    }

    @Override
    public void onItemChanged(int position) {
        list.getAdapter().notifyItemChanged(position);
    }

    @Override
    public void showError(Throwable e) {
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(int position, AppViewModel item) {
        if (editMode) {
            customizeApp(position, item);
        } else {
            startApp(item);
        }
    }

    @Override
    public void onItemLongClick(int position, AppViewModel item) {
        if (editMode) {
            View view = list.getLayoutManager().findViewByPosition(position);
            touchHelper.startDrag(list.getChildViewHolder(view));
        } else {
            startAppSettings(item);
        }
    }

    @Override
    public void onItemMove(int from, int to) {
        presenter.swapApps(from, to);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Private
    ///////////////////////////////////////////////////////////////////////////

    private void hideProgress() {
        if (progressContainer != null) {
            root.removeView(progressContainer);
            progressContainer = null;
        }
    }

    void startApp(AppViewModel item) {
        Intent intent = packageManager.getLaunchIntentForPackage(item.packageName);
        if (intent != null && intent.resolveActivity(packageManager) != null) {
            startActivity(intent);
        }
    }

    void startAppSettings(AppViewModel item) {
        Uri uri = Uri.fromParts("package", item.packageName, null);
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent);
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
            Snackbar snackbar = Snackbar.make(root, R.string.edit_mode_hint, Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction(R.string.edit_mode_exit, v -> {
                snackbar.dismiss();
                presenter.stopEditMode();
            });
            snackbar.show();
            list.setPadding(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.snackbar_size));
        } else {
            list.setPadding(0, 0, 0, 0);
        }
    }

    private void setLayout(String layout) {
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

    private void customizeApp(int position, AppViewModel item) {
        ListAlertDialog.builder(this)
                .setTitle(getAppTitle(item))
                .addItem(R.drawable.ic_action_name, R.string.edit_mode_action_name, () -> {
                    setAppName(position, item);
                })
                .addItem(R.drawable.ic_action_color, R.string.edit_mode_action_color, () -> {
                    setAppColor(position, item);
                })
                .addItem(R.drawable.ic_action_hide, R.string.edit_mode_action_hide, () -> {
                    hideApp(position, item);
                })
                .show();
    }

    private void setAppName(int position, AppViewModel item) {
        EditTextAlertDialog.builder(this)
                .setTitle(getAppTitle(item))
                .customizeEditText(editText -> {
                    editText.setText(item.customLabel);
                    editText.setSelectAllOnFocus(true);
                })
                .setPositiveButton(R.string.edit_mode_rename, (dialog, editText) -> {
                    String label = editText.getText().toString().trim();
                    presenter.renameApp(position, item, label);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void setAppColor(int position, AppViewModel item) {
        EditTextAlertDialog.builder(this)
                .setTitle(getAppTitle(item))
                .customizeEditText(editText -> {
                    editText.setText(String.format("%06x", item.getColor()).substring(2));
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
                    presenter.changeAppCustomColor(position, item, value);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private CharSequence getAppTitle(AppViewModel item) {
        SpannableStringBuilder ss = new SpannableStringBuilder(item.getLabel());
        ss.setSpan(new ForegroundColorSpan(item.getColor()), 0, ss.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        return ss;
    }

    private void hideApp(int position, AppViewModel item) {
        presenter.hideApp(position, item);
    }
}

