package com.italankin.lnch.ui.feature.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
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
import com.italankin.lnch.bean.AppItem;
import com.italankin.lnch.model.provider.Preferences;
import com.italankin.lnch.model.repository.search.SearchRepository;
import com.italankin.lnch.model.repository.search.match.IMatch;
import com.italankin.lnch.ui.base.AppActivity;
import com.italankin.lnch.ui.feature.settings.SettingsActivity;
import com.italankin.lnch.ui.util.SwapItemHelper;

import java.util.List;

public class HomeActivity extends AppActivity implements HomeView,
        SwapItemHelper.Callback,
        AppItemAdapter.Listener {

    private static final int REQUEST_CODE_SETTINGS = 1;

    @InjectPresenter
    HomePresenter presenter;

    private CoordinatorLayout root;
    private ViewGroup searchBar;
    private AutoCompleteTextView editSearch;
    private View btnSettings;
    private RecyclerView list;

    private InputMethodManager inputMethodManager;
    private BroadcastReceiver packageUpdatesReceiver;
    private PackageManager packageManager;

    private FrameLayout progressContainer;

    private boolean editMode = false;

    private TopBarBehavior searchBarBehavior;
    private ItemTouchHelper touchHelper;

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

        setContentView(R.layout.activity_launcher);
        root = findViewById(R.id.root);
        list = findViewById(R.id.list);
        searchBar = findViewById(R.id.search_bar);
        editSearch = findViewById(R.id.edit_search);
        btnSettings = findViewById(R.id.btn_settings);

        setupRoot();
        setupList();
        setupSearchBar();

        registerReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (packageUpdatesReceiver != null) {
            unregisterReceiver(packageUpdatesReceiver);
        }
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
    public void onAppsLoaded(List<AppItem> items, SearchRepository searchRepository, String layout) {
        hideProgress();
        AppItemAdapter adapter = (AppItemAdapter) list.getAdapter();
        if (adapter == null) {
            adapter = new AppItemAdapter(this, this);
        }
        adapter.setDataset(items);
        list.setLayoutManager(getLayoutManager(layout));
        list.setAdapter(adapter);
        list.setVisibility(View.VISIBLE);
        editSearch.setAdapter(new SearchAdapter(searchRepository));
    }

    @Override
    public void onStartEditMode() {
        setEditMode(true);
    }

    @Override
    public void onStopEditMode() {
        setEditMode(false);
    }

    @Override
    public void onItemsSwap(int from, int to) {
        list.getAdapter().notifyItemMoved(from, to);
    }

    @Override
    public void showError(Throwable e) {
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(int position, AppItem item) {
        if (editMode) {
            customizeApp(position, item);
        } else {
            startApp(item);
        }
    }

    @Override
    public void onItemLongClick(int position, AppItem item) {
        if (editMode) {
            View view = list.getLayoutManager().findViewByPosition(position);
            touchHelper.startDrag(list.getChildViewHolder(view));
        } else {
            startAppSettings(item);
        }
    }

    @Override
    public void onItemMove(int from, int to) {
        presenter.swapItems(from, to);
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

    void startApp(AppItem item) {
        Intent intent = packageManager.getLaunchIntentForPackage(item.packageName);
        if (intent != null && intent.resolveActivity(packageManager) != null) {
            startActivity(intent);
        }
    }

    void startAppSettings(AppItem item) {
        Uri uri = Uri.fromParts("package", item.packageName, null);
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent);
        }
    }

    private void registerReceiver() {
        packageUpdatesReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                presenter.notifyPackageChanged();
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");
        registerReceiver(packageUpdatesReceiver, filter);
    }

    private void onFireSearch(int pos) {
        if (editSearch.getText().length() > 0) {
            SearchAdapter adapter = (SearchAdapter) editSearch.getAdapter();
            if (adapter.getCount() > 0) {
                IMatch item = adapter.getItem(pos);
                Intent intent = item.getIntent();
                if (intent != null && intent.resolveActivity(getPackageManager()) != null) {
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
            int bottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48,
                    getResources().getDisplayMetrics());
            list.setPadding(0, 0, 0, bottom);
        } else {
            list.setPadding(0, 0, 0, 0);
        }
    }

    private RecyclerView.LayoutManager getLayoutManager(String layout) {
        if (layout == null) {
            layout = Preferences.LAYOUT_FLEX;
        }
        switch (layout) {
            case Preferences.LAYOUT_GRID:
                return getGridLayoutManager();
            case Preferences.LAYOUT_LINEAR:
                return getLinearLayoutManager();
            case Preferences.LAYOUT_FLEX:
            default:
                return getFlexboxLayoutManager();
        }
    }

    private RecyclerView.LayoutManager getGridLayoutManager() {
        return new GridLayoutManager(this, 2);
    }

    private RecyclerView.LayoutManager getLinearLayoutManager() {
        return new LinearLayoutManager(this);
    }

    private RecyclerView.LayoutManager getFlexboxLayoutManager() {
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setAlignItems(AlignItems.FLEX_START);
        return layoutManager;
    }

    private void customizeApp(int position, AppItem item) {
        CharSequence[] items = {
                "Rename",
                "Set color",
        };
        new AlertDialog.Builder(this)
                .setTitle(item.getLabel())
                .setItems(items, (d, w) -> {
                    switch (w) {
                        case 0:
                            renameApp(position, item);
                            break;
                        case 1:
                            setAppColor(position, item);
                            break;
                    }
                })
                .show();
    }

    private void renameApp(int position, AppItem item) {
        EditText editText = new AppCompatEditText(this);
        editText.setText(item.customLabel);
        editText.setSelectAllOnFocus(true);
        new AlertDialog.Builder(this)
                .setTitle(item.getLabel())
                .setView(editText)
                .setPositiveButton("Rename", (dialog1, which) -> {
                    String label = editText.getText().toString().trim();
                    if (!TextUtils.isEmpty(label)) {
                        item.customLabel = label;
                        list.getAdapter().notifyItemChanged(position);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setAppColor(int position, AppItem item) {
        EditText editText = new AppCompatEditText(this);
        editText.setText(String.format("%06x", item.getColor()).substring(2));
        editText.setSelectAllOnFocus(true);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

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
        new AlertDialog.Builder(this)
                .setTitle(item.getLabel())
                .setView(editText)
                .setPositiveButton("OK", (dialog1, which) -> {
                    String value = editText.getText().toString().trim();
                    if (!TextUtils.isEmpty(value)) {
                        try {
                            item.customColor = Integer.decode("0x" + value) + 0xff000000;
                        } catch (Exception e) {
                            showError(e);
                            return;
                        }
                    } else {
                        item.customColor = null;
                    }
                    list.getAdapter().notifyItemChanged(position);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void hideApp(AppItem item) {
        item.hidden = true;
    }
}

